package pl.edu.mimuw.weather.event;

import java.time.LocalDateTime;

/**
 * Created by marian on 6/10/17.
 */
public class WeatherStateEvent extends WeatherEvent {
    private final LocalDateTime timestamp;

    public WeatherStateEvent() {
        timestamp = LocalDateTime.now();
    }

    public LocalDateTime getTimestamp() {
        return this.timestamp;
    }

    @java.lang.Override
    public java.lang.String toString() {
        return "WeatherStateEvent(timestamp=" + this.getTimestamp() + ")";
    }
}