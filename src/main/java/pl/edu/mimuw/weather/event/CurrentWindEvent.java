package pl.edu.mimuw.weather.event;

/**
 * Created by marian on 6/14/17.
 */
public class CurrentWindEvent extends WeatherEvent {
    private double windSpeed = -1;
    private double windDeg = -1;

    public double getWindSpeed() {
        return windSpeed;
    }

    public double getWindDeg() {
        return windDeg;
    }

    public CurrentWindEvent(double deg, double speed) {
        this.windDeg = deg;
        this.windSpeed = speed;
    }

    public CurrentWindEvent() {}

    @Override
    public String toString() {
        String s = "Strength : -\n Direction : -\n";
        if(windSpeed != -1) {
            s = "Strength : " + windSpeed + '\n' +
                    "Deg : " + windDeg + '\n';
        }
        return s;
    }
}
