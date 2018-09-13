package pl.edu.mimuw.weather.network;

/**
 * Created by marian on 6/23/17.
 */

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import io.reactivex.netty.RxNetty;
import pl.edu.mimuw.weather.event.CurrentAirPollutionEvent;
import rx.Observable;

public class AirPollutionDataSource extends StatsDataSource {
    private static final String URL = "http://powietrze.gios.gov.pl/pjp/current/getAQIDetailsList?param=AQI";

    private static final String STATION_ID_JSON_KEY = "stationId";
    private static final String VALUES_JSON_KEY = "values";

    private static final String PM25_JSON_KEY = "PM2.5";
    private static final String PM10_JSON_KEY = "PM10";

    @Override
    protected Observable<CurrentAirPollutionEvent> makeRequest() {
		/*
	     * issues an HTTP query to powietrze.gios.gov.pl
	     * it's job is to find any info concerning PM2.5 and PM10
		 * shall it not find NAN is returned which is -101
		 */
        return RxNetty.createHttpRequest(JsonHelper.withJsonHeader(prepareHttpGETRequest(URL)))
                .compose(this::unpackResponse).map(JsonHelper::asJsonArray).map(jsonArray -> {
                    String[] obsStations = {"544", "530", "531"};
                    double PM2point5 = NAN, PM10 = NAN;
                    int i;
                    for (int j = 0; j < obsStations.length; ++j) {
                        i = findIndexOfKey(jsonArray, obsStations[j]);
                        if (i!=-1 && jsonArray.get(i).getAsJsonObject().has(VALUES_JSON_KEY)) {
                            JsonObject jsonObject = jsonArray.get(i).getAsJsonObject().get(VALUES_JSON_KEY).
                                    getAsJsonObject();
                            if(jsonObject.has(PM25_JSON_KEY)) PM2point5 = jsonObject.get(PM25_JSON_KEY).getAsDouble();
                            if(jsonObject.has(PM10_JSON_KEY)) PM10 = jsonArray.get(i).getAsJsonObject().
                                    get(VALUES_JSON_KEY).getAsJsonObject().get(PM10_JSON_KEY).getAsDouble();
                            if (PM2point5!=101 || PM10!=101) break;
                        }
                    }
                    return new CurrentAirPollutionEvent(PM2point5, PM10);

                });
    }
    private int findIndexOfKey(JsonArray jsonArray, String key){
        for (int i = 0; i < jsonArray.size(); ++i){
            if (jsonArray.get(i).getAsJsonObject().get(STATION_ID_JSON_KEY).getAsString().equals(key))return i;
        }
        return -1;
    }
}