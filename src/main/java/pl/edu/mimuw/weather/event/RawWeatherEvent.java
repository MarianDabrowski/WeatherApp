package pl.edu.mimuw.weather.event;

/**
 * Created by marian on 6/17/17.
 */
import java.time.LocalDateTime;

public class RawWeatherEvent extends WeatherEvent{
    private final double value;
    private final LocalDateTime timestamp;
    private final boolean isWindDeg;

    public RawWeatherEvent(final LocalDateTime timestamp, final double value, final boolean isWindDeg) {
        this.timestamp = timestamp;
        this.value = value;
        this.isWindDeg = isWindDeg;
    }

    public RawWeatherEvent() {
        this.timestamp = LocalDateTime.now();
        this.isWindDeg = false;
        this.value = 0;
    }

    public double getValue() {
        return this.value;
    }

    public boolean isWindDeg() {
        return isWindDeg;
    }

    public LocalDateTime getTimestamp() {
        return timestamp;
    }

    @Override
    public String toString() {
        return "RawWeatherEvent{" +
                "value=" + value +
                ", timestamp=" + timestamp +
                ", isWindDeg=" + isWindDeg +
                '}';
    }
}

