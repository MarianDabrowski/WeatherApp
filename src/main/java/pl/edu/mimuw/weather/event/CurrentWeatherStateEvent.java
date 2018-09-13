package pl.edu.mimuw.weather.event;

/**
 * Created by marian on 6/10/17.
 */
public class CurrentWeatherStateEvent extends WeatherStateEvent {

    private double temperature;
    private double pressure;
    private double cloudy;
    private double humidity;
    private CurrentWindEvent wind = new CurrentWindEvent();

    public double getTemperature() {
        return temperature;
    }

    public double getPressure() {
        return pressure;
    }

    public double getCloudy() {
        return cloudy;
    }

    public double getHumidity() {
        return humidity;
    }



    public double getWindSpeed() {
        return wind.getWindSpeed();
    }

    public double getWindDeg() {
        return wind.getWindDeg();
    }

    public CurrentWeatherStateEvent() {}

    public CurrentWeatherStateEvent(double temperature, double pressure, double cloudy,
                        double humidity) {
        this.temperature = temperature;
        this.pressure = pressure;
        this.cloudy = cloudy;
        this.humidity = humidity;
    }

    public CurrentWeatherStateEvent(double temperature, double pressure, double cloudy,
                        double humidity, CurrentWindEvent wind) {
        this(temperature, pressure, cloudy, humidity);
        this.wind = wind;
    }

    private String correctCloudyToPrint() {
        if(cloudy == -1) return "-";
        return Double.toString(cloudy);
    }

    public void print() {
        String s = "Temperature in C: " + temperature + '\n' +
                "Pressure in HPA: " + pressure + '\n' +
                "Cloudy: " + this.correctCloudyToPrint() + '\n' +
                "Hummidity in %: " + humidity + '\n' +
                wind.toString();
        System.out.println(s);
    }

}