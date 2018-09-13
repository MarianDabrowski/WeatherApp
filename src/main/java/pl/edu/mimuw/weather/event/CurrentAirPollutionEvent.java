package pl.edu.mimuw.weather.event;

/**
 * Created by marian on 6/14/17.
 */

public class CurrentAirPollutionEvent extends RawWeatherEvent{
    private double PM2point5Pollution = -1;
    private double PM10Pollution = -1;

    private String correctStingFromPM2point5() {
        if(PM2point5Pollution < 0) return "-";
        return Double.toString(PM2point5Pollution);
    }

    private String correctStringFromPm10() {
        if(PM10Pollution < 0) return "-";
        return Double.toString(PM10Pollution);
    }

    @Override
    public String toString() {
        String s = "PM2.5 : " + correctStingFromPM2point5() + '\n' +
                "PM10 : " + this.correctStringFromPm10() + '\n';
        return s;
    }

    public CurrentAirPollutionEvent(double pm2point5, double pm10) {
        this.PM2point5Pollution = pm2point5;
        this.PM10Pollution = pm10;
    }

    public double getPM2point5Pollution() {
        return PM2point5Pollution;
    }

    public double getPM10Pollution() {
        return PM10Pollution;
    }
}
