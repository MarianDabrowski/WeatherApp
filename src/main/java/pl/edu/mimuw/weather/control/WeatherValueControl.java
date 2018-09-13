package pl.edu.mimuw.weather.control;

/**
 * Created by marian on 6/20/17.
 */

import pl.edu.mimuw.weather.event.*;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.text.Text;
import org.kordamp.ikonli.javafx.FontIcon;
import org.kordamp.ikonli.weathericons.WeatherIcons;
import rx.Observable;

import java.text.DecimalFormat;

public class WeatherValueControl extends Pane{

    private FontIcon noDataIcon = new FontIcon(WeatherIcons.NA);
    private HBox innerContainer;
    private Text suffixLabel;
    private Text textControl;

    private ObjectProperty<Observable<RawWeatherEvent>> sourceProperty = new SimpleObjectProperty<>();

    private String formatPattern = "0.000";
    private DecimalFormat format = new DecimalFormat(formatPattern);
    private StringProperty suffixProperty = new SimpleStringProperty("");
    private StringProperty titleProperty = new SimpleStringProperty("-");

    public Observable<RawWeatherEvent> getSource() {
        return sourceProperty.get();
    }

    public void setSource(Observable<RawWeatherEvent> source) {
        source.subscribe(e -> {
            if (innerContainer == null) {
                createContentControls();
            }
            if (e.getValue() < -100) textControl.setText("-");
            else if (e.isWindDeg() == true){
                textControl.setText(windDirection(e.getValue()));
            }
            else textControl.setText(format.format(e.getValue()));
        });

        sourceProperty.set(source);
    }

    private String windDirection(double value) {
        value = value % 360;
        String[] windDir = {"N", "NE", "E", "SE", "S", "SW", "W", "NW"};
        double temp = 360.0 - 22.5, change = 22.5;
        String output = "-";
        for (int i = 0; i < 16; ++i){
            if(value >= temp && value < temp + change) output = windDir[i/2];
            temp = (temp + change) % 360;
        }
        return output;
    }

    public String getFormat() {
        return formatPattern;
    }

    public void setFormat(String pattern) {
        formatPattern = pattern;
        format = new DecimalFormat(pattern);
    }

    public String getSuffix() {
        return suffixProperty.get();
    }

    public void setSuffix(String sufix) {
        suffixProperty.set(sufix);
    }

    public String getTitle() {
        return titleProperty.get();
    }

    public void setTitle(String title) {
        titleProperty.set(title);
    }

    public WeatherValueControl() {
        noDataIcon.getStyleClass().add("no-data");
        getChildren().add(noDataIcon);
    }

    private void createContentControls() {
        getChildren().remove(noDataIcon);

        textControl = new Text();
        textControl.getStyleClass().add("weather-value");

        suffixLabel = new Text();
        suffixLabel.textProperty().bind(suffixProperty);
        suffixLabel.getStyleClass().add("helper-label");

        innerContainer = new HBox();
        innerContainer.getStyleClass().add("value-container");
        innerContainer.getChildren().addAll(textControl, suffixLabel);

        getChildren().add(innerContainer);
    }

    @Override
    protected void layoutChildren() {
		/* Custom children positioning */
        super.layoutChildren();

        if (noDataIcon.isVisible()) {
            noDataIcon.relocate((getWidth() - noDataIcon.getLayoutBounds().getWidth()) / 2,
                    (getHeight() - noDataIcon.getLayoutBounds().getHeight()) / 2);
        }

        if (innerContainer != null) {
            innerContainer.relocate((getWidth() - innerContainer.getLayoutBounds().getWidth()) / 2,
                    (getHeight() - innerContainer.getLayoutBounds().getHeight()) / 2);
        }

    }
}
