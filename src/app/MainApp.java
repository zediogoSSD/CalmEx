package src.app;

import javafx.application.Application;
import javafx.stage.Stage;
import src.app.gui.MainWindow;

import java.net.URL;

public class MainApp extends Application {

    private static final String CSS_PATH = "/src/app/gui/estilo.css";

    public static String getCss() {
        URL url = MainApp.class.getResource(CSS_PATH);
        if (url == null) {
            System.out.println("CRITICAL ERROR: CSS not found at " + CSS_PATH);
            return null;
        }
        return url.toExternalForm();
    }

    @Override
    public void start(Stage primaryStage) {
        new MainWindow(primaryStage).show();
    }

    public static void main(String[] args) {
        launch(args);
    }
}