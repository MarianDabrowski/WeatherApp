package pl.edu.mimuw.weather.network;

/**
 * Created by marian on 6/8/17.
 */

import io.reactivex.netty.RxNetty;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import pl.edu.mimuw.weather.event.CurrentWeatherStateEvent;
import pl.edu.mimuw.weather.event.CurrentWindEvent;
import rx.Observable;
import java.io.IOException;

public class MeteoDataSource extends StatsDataSource {

    private static final String URL = "http://meteo.waw.pl/";
    private String temperatureId = "PARAM_0_TA";
    private String pressureId = "PARAM_0_PR";
    private String humidityId = "PARAM_0_RH";
    private String windSpeedId = "PARAM_0_WV";
    private String windDirectionId = "PARAM_WD";

    private double valueAsDouble(Element span) {
        String tempString = span.toString().split(">")[1].split("<")[0].replace(',', '.');
        return Double.parseDouble(tempString);
    }

    public CurrentWeatherStateEvent parseMeteoDataSource() throws IOException {
        Document doc = Jsoup.connect(URL).get();
        Element temperatureSpan  = doc.getElementById(temperatureId);
        Element pressureSpan = doc.getElementById(pressureId);
        Element humiditySpan = doc.getElementById(humidityId);
        Element windStrengthSpan = doc.getElementById(windSpeedId);
        Element windDirectionSpan = doc.getElementById(windDirectionId);

        double temperature = this.valueAsDouble(temperatureSpan);
        double pressure = this.valueAsDouble(pressureSpan);
        double humidity = this.valueAsDouble(humiditySpan);
        double windSpeed = this.valueAsDouble(windStrengthSpan);
        double windDeg = this.valueAsDouble(windDirectionSpan);
        CurrentWindEvent wind = new CurrentWindEvent(windDeg, windSpeed);

        return new CurrentWeatherStateEvent(temperature, pressure, NAN, humidity, wind);
    }

    protected <T> Observable<CurrentWeatherStateEvent> makeRequest() {

		/*
		 * We distract weather stats from meteo.waw.pl
		 * In order to do this we try to find keys in sourcecode which are supposed to give us the data
		 * If any of keys is not found then the corresponding NAN value is returned
		 */
        return RxNetty.createHttpRequest(prepareHttpGETRequest(URL)).compose(this::unpackResponse).map(htmlSource -> {
            CurrentWeatherStateEvent weather;
            try {
                weather = parseMeteoDataSource();
            } catch (IOException e) {
                e.printStackTrace();
                weather = new CurrentWeatherStateEvent();
            }

            return weather;
        });
    }
}
