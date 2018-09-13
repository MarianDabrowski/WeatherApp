package pl.edu.mimuw.weather.event;

/**
 * Created by marian on 6/10/17.
 */
import java.time.LocalDateTime;

public final class ErrorEvent extends WeatherEvent {
    private final LocalDateTime timestamp;
    private final Throwable cause;

    public ErrorEvent(Throwable cause) {
        this.timestamp = LocalDateTime.now();
        this.cause = cause;
    }

    public LocalDateTime getTimestamp() {
        return this.timestamp;
    }

    public Throwable getCause() {
        return this.cause;
    }

    @java.lang.Override
    public java.lang.String toString() {
        return "ErrorEvent(timestamp=" + this.getTimestamp() + ", cause=" + this.getCause() + ")";
    }
}
