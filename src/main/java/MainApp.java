package src;

import javafx.application.Application;
import javafx.stage.Stage;
import src.app.gui.MainWindow;

import java.net.URL;

public class MainApp extends Application {

    String css = this.getClass().getResource("/style.css").toExternalForm();

    public static String getCss() {
        URL url = MainApp.class.getResource(getCss());
        if (url == null) {
            System.out.println("CRITICAL ERROR: CSS not found at " + getCss());
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