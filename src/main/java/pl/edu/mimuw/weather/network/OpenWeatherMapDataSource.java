package pl.edu.mimuw.weather.network;

/**
 * Created by marian on 6/8/17.
 */

import io.netty.buffer.ByteBuf;
import io.reactivex.netty.RxNetty;
import com.google.gson.JsonObject;
import io.reactivex.netty.protocol.http.client.HttpClientRequest;
import pl.edu.mimuw.weather.event.CurrentWindEvent;
import rx.Observable;
import pl.edu.mimuw.weather.event.CurrentWeatherStateEvent;

public class OpenWeatherMapDataSource extends StatsDataSource {
    private static final String URL = "http://api.openweathermap.org/data/2.5/weather?q=Warsaw,pl&APPID=a8491b6d3e85ede74109cae92270b4d7";
    private static final String WEATHER_JSON_KEY = "main";
    private static final String WIND_JSON_KEY = "wind";
    private static final String CLOUDS_JSON_KEY ="clouds";

    private static final String CLOUDS_DATA_JSON_KEY="all";
    private static final String TEMPERATURE_JSON_KEY = "temp";
    private static final String PRESSURE_JSON_KEY = "pressure";
    private static final String HUMIDITY_JSON_KEY = "humidity";
    private static final String WIND_SPEED_JSON_KEY = "speed";
    private static final String WIND_DEG_JSON_KEY = "deg";


    protected HttpClientRequest<ByteBuf> prepareHttpGETRequest(String url) {
		/*
		 * As the name says, this creates an HTTP GET request (but does not send
		 * it, sending is done elsewhere).
		 */
        return HttpClientRequest.createGet(url);
    }

    protected Observable<CurrentWeatherStateEvent> makeRequest() {

        return RxNetty.createHttpRequest(JsonHelper.withJsonHeader(prepareHttpGETRequest(URL)))
                .compose(this::unpackResponse).map(JsonHelper::asJsonObject).map(jsonObject -> {
                    CurrentWindEvent wind = new CurrentWindEvent();
                    double temperature = -101, pressure = -101, humidity = -101, clouds = -101, windSpeed = -101, windDeg = -101;
                    if (jsonObject.has(WEATHER_JSON_KEY)){
                        JsonObject weatherData = jsonObject.get(WEATHER_JSON_KEY).getAsJsonObject();
                        if (weatherData.has(TEMPERATURE_JSON_KEY))
                            temperature = weatherData.get(TEMPERATURE_JSON_KEY).getAsDouble() - 273.15;
                        if (weatherData.has(PRESSURE_JSON_KEY))
                            pressure = weatherData.get(PRESSURE_JSON_KEY).getAsDouble();
                        if (weatherData.has(HUMIDITY_JSON_KEY))
                            humidity = weatherData.get(HUMIDITY_JSON_KEY).getAsDouble();
                    }
                    if (jsonObject.has(CLOUDS_JSON_KEY) && jsonObject.get(CLOUDS_JSON_KEY).getAsJsonObject().has(CLOUDS_DATA_JSON_KEY)){
                        clouds = jsonObject.get(CLOUDS_JSON_KEY).getAsJsonObject().get(CLOUDS_DATA_JSON_KEY).getAsFloat();
                    }
                    if (jsonObject.has(WIND_JSON_KEY)){
                        JsonObject windData = jsonObject.get(WIND_JSON_KEY).getAsJsonObject();
                        if (windData.has(WIND_SPEED_JSON_KEY))
                            windSpeed = windData.get(WIND_SPEED_JSON_KEY).getAsDouble();
                        if (windData.has(WIND_DEG_JSON_KEY))
                            windDeg = windData.get(WIND_DEG_JSON_KEY).getAsDouble();
                        wind = new CurrentWindEvent(windDeg, windSpeed);
                    }
                    return new CurrentWeatherStateEvent(temperature, pressure, clouds, humidity, wind);
                });
    }

}

