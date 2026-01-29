// MainApp.java (Root folder: src/main/java/)
// NO PACKAGE DECLARATION HERE

import javafx.application.Application;
import javafx.stage.Stage;
import gui.MainWindow; // Import the window from the gui package
import java.util.Objects;

public class MainApp extends Application {

    @Override
    public void start(Stage primaryStage) {
        // This is the code that used to be in Launcher
        MainWindow mainWin = new MainWindow(primaryStage);
        mainWin.show();
    }

    // This is the method your other classes were trying to call
    public static String getCss() {
        try {
            // Loading from the root of the 'resources' folder
            return Objects.requireNonNull(MainApp.class.getResource("/estilo.css")).toExternalForm();
        } catch (Exception e) {
            System.out.println("CSS not found!");
            return null;
        }
    }

    public static void main(String[] args) {
        launch(args);
    }
}