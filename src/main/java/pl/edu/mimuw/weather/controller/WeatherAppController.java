package pl.edu.mimuw.weather.controller;

/**
 * Created by marian on 6/20/17.
 */

import pl.edu.mimuw.weather.event.*;
import pl.edu.mimuw.weather.control.*;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.Tooltip;
import rx.Observable;
import rx.functions.Func1;
import rx.observables.JavaFxObservable;
import rx.schedulers.JavaFxScheduler;
import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.text.MessageFormat;
import java.time.format.DateTimeFormatter;
import java.util.concurrent.TimeUnit;

import pl.edu.mimuw.weather.event.RawWeatherEvent;

import static pl.edu.mimuw.weather.event.EventStream.*;

public class WeatherAppController {
    private static final int ERROR_MSG_MAX_LENGTH = 400;
    private static final int ERROR_MSG_DURATION = 30; // Show error icon for 30
    // seconds

    @FXML
    private WeatherValueControl temperatureControl;

    @FXML
    private WeatherValueControl pressureControl;

    @FXML
    private WeatherValueControl humidityControl;

    @FXML
    private WeatherValueControl windSpeedControl;

    @FXML
    private WeatherValueControl windDegControl;

    @FXML
    private WeatherValueControl PM25Control;

    @FXML
    private WeatherValueControl PM10Control;

    @FXML
    private WeatherValueControl cloudinessControl;

    @FXML
    private Node errorIcon;

    @FXML
    private Node workingIcon;

    @FXML
    private Button refreshButton;

    @FXML
    private Button settingsButton;

    @FXML
    private void initialize() {
        initializeStatus();
        initalizeRefreshHandler();
        initializeSettingsHandler();
        initializeTooltips();
    }

    public Observable<RawWeatherEvent> getTemperature() {
        return getWeatherStatsStream(CurrentWeatherStateEvent::getTemperature, false);
    }
    public Observable<RawWeatherEvent> getPressure() {
        return getWeatherStatsStream(CurrentWeatherStateEvent::getPressure, false);
    }
    public Observable<RawWeatherEvent> getHumidity() {
        return getWeatherStatsStream(CurrentWeatherStateEvent::getHumidity, false);
    }
    public Observable<RawWeatherEvent> getWindSpeed() {
        return getWeatherStatsStream(CurrentWeatherStateEvent::getWindSpeed, false);
    }
    public Observable<RawWeatherEvent> getWindDeg() {
        return getWeatherStatsStream(CurrentWeatherStateEvent::getWindDeg, true);
    }
    public Observable<RawWeatherEvent> getCloudiness() {
        return getWeatherStatsStream(CurrentWeatherStateEvent::getCloudy, false);
    }
    public Observable<RawWeatherEvent> getPM25() {
        return getPMStatsStream(CurrentAirPollutionEvent::getPM2point5Pollution);
    }
    public Observable<RawWeatherEvent> getPM10() {
        return getPMStatsStream(CurrentAirPollutionEvent::getPM10Pollution);
    }

    private void initalizeRefreshHandler() {
        joinStream(JavaFxObservable.actionEventsOf(refreshButton).map(e -> new RefreshRequestEvent()));
    }
    private void initializeSettingsHandler() {
        joinStream(JavaFxObservable.actionEventsOf(settingsButton).map(e -> new SettingsRequestEvent()));
    }

    private void initializeStatus() {
        Observable<WeatherEvent> events = eventStream().eventsInFx();
        /*
         * Basically, we keep track of the difference between issued requests
         * and completed requests
         * If this difference is > 0 we display the spinning icon...
         */
        workingIcon.visibleProperty()
                .bind(binding(events.ofType(NetworkRequestIssuedEvent.class).map(e -> 1)
                        /* Every issued request contributes +1 */
                        .mergeWith(events.ofType(NetworkRequestFinishedEvent.class).map(e -> -1)
                                /* Every issued request contributes -1 */
                                .delay(2, TimeUnit.SECONDS, JavaFxScheduler.getInstance()))
                        /*
                         * We delay completion events for 2 seconds so that the spinning icon is
                         * always displayed for at least 2 seconds and it does not blink
                         */
                        .scan(0, (x, y) -> x + y).map(v -> v > 0))
                );

		/*
		 * This should show the error icon when an error event arrives and hides
		 * the icon after 30 seconds unless another error arrives
		 */
        Observable<ErrorEvent> errors = events.ofType(ErrorEvent.class);
        errorIcon.visibleProperty()
                .bind(onEvent(errors, true).andOn(
                        errors.throttleWithTimeout(ERROR_MSG_DURATION, TimeUnit.SECONDS, JavaFxScheduler.getInstance()),
                        false).toBinding());
    }

    private void initializeTooltips() {
        Tooltip.install(workingIcon, new Tooltip("Fetching data..."));

        Tooltip errorTooltip = new Tooltip();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");
        eventStream().eventsInFx().ofType(ErrorEvent.class).subscribe(e -> {
            ByteArrayOutputStream ostream = new ByteArrayOutputStream();
            e.getCause().printStackTrace(new PrintStream(ostream));
            String details = new String(ostream.toByteArray());
            if (details.length() > ERROR_MSG_MAX_LENGTH) {
                details = details.substring(0, ERROR_MSG_MAX_LENGTH) + "\u2026";
                /* Add ellipsis (...) at the end */
            }
            errorTooltip.setText(MessageFormat.format("An error has occurred ({0}):\n{1}",
                    e.getTimestamp().format(formatter), details));
        });
        Tooltip.install(errorIcon, errorTooltip);

        WeatherValueControl[] weatherValueControls = { temperatureControl, pressureControl,  humidityControl, windSpeedControl,
        windDegControl, PM25Control, PM10Control, cloudinessControl};
        for (WeatherValueControl control : weatherValueControls) {
            Tooltip tooltipPopup = new Tooltip();
            WeatherStatsTooltip tooltipContent = new WeatherStatsTooltip(control.getSource(), control.getTitle());

            tooltipPopup.setGraphic(tooltipContent);

            Tooltip.install(control, tooltipPopup);
        }
    }

    private Observable<RawWeatherEvent> getWeatherStatsStream(Func1<CurrentWeatherStateEvent, Double> extractor, boolean isWindDeg) {
        return eventStream().eventsInFx().ofType(CurrentWeatherStateEvent.class)
                .map(e -> new RawWeatherEvent(e.getTimestamp(), extractor.call(e),isWindDeg));
    }

    private Observable<RawWeatherEvent> getPMStatsStream(Func1<CurrentAirPollutionEvent, Double> extractor) {
        return eventStream().eventsInFx().ofType(CurrentAirPollutionEvent.class)
                .map(e -> new RawWeatherEvent(e.getTimestamp(), extractor.call(e), false));
    }
}
