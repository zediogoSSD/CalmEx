package gui;

import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.layout.*;
import javafx.stage.Stage;
import gui.components.*;
import gui.dialogs.*;
import backend.Relatorios;
import java.time.LocalDate;
import java.util.Map;
import java.util.Objects;


public class MainWindow {

    private final Stage stage;
    private WeeklyChart chartComponent;
    private AppList appListComponent;
    private DailyGoal goalComponent;
    private Navigation navComponent;
    private Header headerComponent;

    public MainWindow(Stage stage) {
        this.stage = stage;
    }

    public void show() {
        VBox mainLayout = new VBox(20);
        mainLayout.setPadding(new Insets(20));

        headerComponent = new Header(this::toggleTheme);

        // 1. Initialize Components
        chartComponent = new WeeklyChart();
        appListComponent = new AppList();
        goalComponent = new DailyGoal();

        // Navigation needs callbacks
        navComponent = new Navigation(
                this::refreshData, // Called when date changes
                () -> HistoryDialog.show(stage, LocalDate.now()) // Called when details clicked
        );

        // 2. Build Layout (Top Grid)
        GridPane topGrid = new GridPane();
        topGrid.setHgap(20);

        ColumnConstraints col1 = new ColumnConstraints(); col1.setPercentWidth(70);
        ColumnConstraints col2 = new ColumnConstraints(); col2.setPercentWidth(30);
        topGrid.getColumnConstraints().addAll(col1, col2);

        RowConstraints rowGrow = new RowConstraints();
        rowGrow.setVgrow(Priority.ALWAYS);
        rowGrow.setFillHeight(true);
        topGrid.getRowConstraints().add(rowGrow);

        topGrid.add(chartComponent, 0, 0);
        topGrid.add(appListComponent, 1, 0);

        // 3. Build Layout (Bottom Grid)
        GridPane bottomGrid = new GridPane();
        bottomGrid.setHgap(20);

        ColumnConstraints colBot1 = new ColumnConstraints(); colBot1.setPercentWidth(40);
        ColumnConstraints colBot2 = new ColumnConstraints(); colBot2.setPercentWidth(60);
        bottomGrid.getColumnConstraints().addAll(colBot1, colBot2);

        bottomGrid.add(navComponent, 0, 0);
        bottomGrid.add(goalComponent, 1, 0);

        // 4. Final Assembly
        mainLayout.getChildren().addAll(headerComponent, topGrid, bottomGrid);
        VBox.setVgrow(topGrid, Priority.ALWAYS);

        mainLayout.setPadding(new Insets(0, 20, 20, 20));

        Scene scene = new Scene(mainLayout, 1000, 680);

        stage.setTitle("Time Tracker");
        stage.setScene(scene);

        loadCSS(false);

        stage.show();

        // Initial Load
        refreshData();
    }

    private void toggleTheme() {
        boolean isDarkMode = headerComponent.isDarkMode();
        loadCSS(isDarkMode);
    }

    private void loadCSS(boolean isDarkMode) {
        Scene currentScene = stage.getScene();
        if (currentScene != null) {
            currentScene.getStylesheets().clear();
            try {
                String cssFile = isDarkMode ? "/estiloDark.css" : "/estilo.css";
                String css = Objects.requireNonNull(getClass().getResource(cssFile)).toExternalForm();
                currentScene.getStylesheets().add(css);
            } catch (Exception e) {
                System.out.println("Não foi possível carregar o CSS no MainWindow: " + e.getMessage());
            }
        }
    }

    private void refreshData() {
        LocalDate currentWeek = navComponent.getCurrentDate();
        Map<String, Integer> data = Relatorios.getTempoPorDia(currentWeek); // Global fetch

        // Propagate data to components
        chartComponent.updateData(currentWeek, data);
        appListComponent.refresh(currentWeek);
        goalComponent.updateProgress(data);
    }
}