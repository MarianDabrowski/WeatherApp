package pl.edu.mimuw.weather;

/**
 * Created by marian on 6/22/17.
 */

import com.jfoenix.controls.JFXDecorator;
import com.jfoenix.controls.JFXDialog;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.ChoiceBox;
import javafx.scene.image.Image;
import javafx.scene.layout.Pane;
import javafx.scene.layout.StackPane;
import javafx.stage.Stage;
import javafx.util.Duration;
import rx.Observable;
import rx.Subscription;
import rx.observables.JavaFxObservable;
import java.io.IOException;
import java.util.LinkedList;
import java.util.List;
import pl.edu.mimuw.weather.event.*;
import pl.edu.mimuw.weather.network.*;
import pl.edu.mimuw.weather.control.*;
import static pl.edu.mimuw.weather.event.EventStream.eventStream;
import static pl.edu.mimuw.weather.event.EventStream.joinStream;

public class AppMain extends Application {
    private static final org.apache.log4j.Logger log = org.apache.log4j.Logger.getLogger(AppMain.class);

    private static final String FXML_MAIN_FORM_TEMPLATE = "/fxml/weather-main.fxml";
    private static final String FXML_CLOSE_DIALOG_TEMPLATE = "/fxml/close-dialog.fxml";
    private static final String FXML_SETTINGS_DIALOG_TEMPLATE = "/fxml/settings-dialog.fxml";
    private static final String FONT_CSS = "/css/jfoenix-fonts.css";
    private static final String MATERIAL_CSS = "/css/jfoenix-design.css";
    private static final String JFX_CSS = "/css/jfx.css";
    private static final String logo = "/icons/weatherIcon.jpg";

    private CloseDialogController closeDialogController;
    private DialogControllerBase settingsDialogController;
    private Stage mainStage;

    private List<Subscription> sourceStreams = new LinkedList<>();

    private class DialogControllerBase {
        @FXML
        JFXDialog dialog;

        @FXML
        Button acceptButton;

        @FXML
        Button cancelButton;

        void initialize() {
            JavaFxObservable.actionEventsOf(cancelButton).subscribe(ignore -> {
                dialog.close();
            });
        }

        void show(StackPane pane) {
            dialog.show(pane);
        }

    }

    private class CloseDialogController extends DialogControllerBase {
        @FXML
        void initialize() {
            JavaFxObservable.actionEventsOf(acceptButton).subscribe(ignore -> {
                log.info("Exitting");
                AppMain.this.mainStage.close(); // closes the app
                System.exit(0); // unless upside command works
            });

            JavaFxObservable.actionEventsOf(cancelButton).subscribe(ignore -> {
                dialog.close();
            });
        }

        void show(StackPane pane) {
            dialog.show(pane);
        }
    }

    private class SettingsDialogController extends DialogControllerBase {
        @FXML
        ChoiceBox<String> sourceBox = new ChoiceBox<>() ;

        @FXML
        void initialize() {
            super.initialize();

            sourceBox.getItems().addAll("OpenWeather", "Meteo");

            sourceBox.setValue("OpenWeather");

            JavaFxObservable.actionEventsOf(acceptButton).subscribe(ignore -> {
                try {
                    AppMain.this.sourceStreams.stream().forEach(Subscription::unsubscribe);
                    AppMain.this.sourceStreams.clear();
                    if (sourceBox.getValue().equals("OpenWeather")){
                        AppMain.this.setupDataSourcesFromOpenWeather();
                        log.info("Weather stats data source changed to OpenWeather");
                    }
                    else {
                        AppMain.this.setupDataSourcesFromMeteo();
                        log.info("Weather stats data source changed to Meteo");
                    }
                } finally {
                    dialog.close();
                }
            });

        }

    }

    @Override
    public void start(Stage primaryStage) throws Exception {
        log.info("Starting Weather in Warsaw application...");
        mainStage = primaryStage;
        mainStage.setTitle("Weather in Warsaw");
        setupTooltipDuration();

        setupDataSourcesFromOpenWeather();

        setupEventHandler();
        Parent pane = FXMLLoader.load(AppMain.class.getResource(FXML_MAIN_FORM_TEMPLATE));

        JFXDecorator decorator = new JFXDecorator(mainStage, pane, false, false, true);
        decorator.setAccessibleText("Weather in Warsaw");
        ObservableList<Node> buttonsList = ((Pane) decorator.getChildren().get(0)).getChildren();
        /* Style the close button differently */
        buttonsList.get(buttonsList.size() - 1).getStyleClass().add("close-button");

        decorator.setOnCloseButtonAction(this::onClose);

        Scene scene = new Scene(decorator);
        scene.setFill(null);
        scene.getStylesheets().addAll(AppMain.class.getResource(FONT_CSS).toExternalForm(),
                AppMain.class.getResource(MATERIAL_CSS).toExternalForm(),
                AppMain.class.getResource(JFX_CSS).toExternalForm());

        mainStage.setScene(scene);

        mainStage.setWidth(645);
        mainStage.setHeight(200);
        mainStage.setResizable(false);

        mainStage.show();

        log.info("Application's up and running!");
    }


    private void onClose() {
        log.info("onClose");

        if (closeDialogController == null) {
            closeDialogController = new CloseDialogController();
            FXMLLoader loader = new FXMLLoader(AppMain.class.getResource(FXML_CLOSE_DIALOG_TEMPLATE));
            loader.setController(closeDialogController);
            try {
                loader.load();
            } catch (IOException e) {
                log.error(e);
                throw new RuntimeException(e);
            }
        }

        closeDialogController.show(getMainPane());
    }

    private StackPane getMainPane() {
        return (StackPane) mainStage.getScene().getRoot().lookup("#main");
    }

    private void createDialog(Object dialogController, String fxmlPath) {
        FXMLLoader loader = new FXMLLoader(AppMain.class.getResource(fxmlPath));
        loader.setController(dialogController);
        try {
            loader.load();
        } catch (IOException e) {
            log.error(e);
            throw new RuntimeException(e);
        }
    }

    private void setupDataSourcesFromOpenWeather() {
        StatsDataSource[] sources = { new OpenWeatherMapDataSource(),
                new AirPollutionDataSource() };
        for (StatsDataSource source : sources) {
            sourceStreams.add(joinStream(source.dataSourceStream()));
        }
    }

    private void setupDataSourcesFromMeteo(){
        StatsDataSource[] sources = { new MeteoDataSource(),
                new AirPollutionDataSource() };
        for (StatsDataSource source : sources) {
            sourceStreams.add(joinStream(source.dataSourceStream()));
        }
    }
    private void onSettingsRequested(){
        log.info("onSettingsRequested");

        if (settingsDialogController == null) {
            settingsDialogController = new SettingsDialogController();
            createDialog(settingsDialogController, FXML_SETTINGS_DIALOG_TEMPLATE);
        }

        settingsDialogController.show(getMainPane());
    }
    private void setupEventHandler() {
        Observable<WeatherEvent> events = eventStream().events();

        events.ofType(CurrentWeatherStateEvent.class).subscribe(log::info);
        events.ofType(CurrentAirPollutionEvent.class).subscribe(log::info);

        events.ofType(SettingsRequestEvent.class).subscribe(e -> onSettingsRequested());
    }

    private static void setupExceptionHandler() {
        Thread.setDefaultUncaughtExceptionHandler(
                (t, e) -> log.error("Uncaught exception in thread \'" + t.getName() + "\'", e));
    }
    private static void setupTooltipDuration() {
        TooltipProlongHelper.setTooltipDuration(Duration.millis(500), Duration.minutes(10), Duration.millis(500));
    }

    private static void setupTextRendering() {
		/*
		 * Workaround for font rendering issue.
		 * Consult: @link{https://stackoverflow.com/questions/18382969/can-the-
		 * rendering-of-the-javafx-2-8-font-be-improved} and linked materials
		 */
        System.setProperty("prism.text", "t2k");
        System.setProperty("prism.lcdtext", "true");
    }

    public static void main(String[] args) {
        setupExceptionHandler();

        setupTextRendering();

        Platform.setImplicitExit(true); // This should exit the application when
        // the main window gets closed
        Application.launch(AppMain.class, args);
    }
}
