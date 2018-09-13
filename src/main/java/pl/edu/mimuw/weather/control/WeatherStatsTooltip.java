package pl.edu.mimuw.weather.control;

/**
 * Created by marian on 6/20/17.
 */

import pl.edu.mimuw.weather.event.*;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.text.Text;
import org.kordamp.ikonli.fontawesome.FontAwesome;
import org.kordamp.ikonli.javafx.FontIcon;
import org.ocpsoft.prettytime.PrettyTime;
import org.ocpsoft.prettytime.impl.ResourcesTimeFormat;
import org.ocpsoft.prettytime.units.JustNow;
import rx.Observable;
import rx.schedulers.JavaFxScheduler;

import java.time.ZoneId;
import java.util.Date;
import java.util.concurrent.TimeUnit;

public class WeatherStatsTooltip extends StackPane {

    private Observable<RawWeatherEvent> source;

    private RawWeatherEvent lastEvent;
    private Date lastDate;

    private VBox chartBox;
    private VBox container;

    public WeatherStatsTooltip (Observable<RawWeatherEvent> source, String title) {
        this.source = source;

        getStyleClass().add("tooltip");

        FontIcon clockIcon = new FontIcon(FontAwesome.CLOCK_O);
        Text timestampText = createTimestampText(source);

        HBox timestampBox = new HBox();
        timestampBox.getChildren().addAll(clockIcon, timestampText);

        container = new VBox();
        container.getChildren().addAll(timestampBox);

        getChildren().add(container);

    }

    private Text createTimestampText(Observable<RawWeatherEvent> source) {
        Text timestampText = new Text("No data");

        PrettyTime pt = new PrettyTime();
        JustNow unit = pt.getUnit(JustNow.class);
        pt.removeUnit(JustNow.class);
        unit.setMaxQuantity(1 * 1000L);
        pt.registerUnit(unit, new ResourcesTimeFormat(unit));

        source.subscribe(e -> {
            lastEvent = e;
            lastDate = Date.from(lastEvent.getTimestamp().atZone(ZoneId.systemDefault()).toInstant());
        });

        Observable.interval(1, TimeUnit.SECONDS, JavaFxScheduler.getInstance()).subscribe(ignore -> {
            if (lastEvent != null) {
                timestampText.setText("Last update: " + pt.format(lastDate));
            }
        });
        return timestampText;
    }

}

