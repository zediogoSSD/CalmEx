package src.app.gui;

import src.Relatorios;
import src.app.MainApp;
import src.app.gui.components.*;
import src.app.gui.dialogs.HistoryDialog;

import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.layout.*;
import javafx.stage.Stage;
import java.time.LocalDate;
import java.util.Map;
import java.util.Objects;

public class MainWindow {

    private final Stage stage;
    private WeeklyChart chartComponent;
    private AppList appListComponent;
    private DailyGoal goalComponent;
    private Navigation navComponent;

    public MainWindow(Stage stage) {
        this.stage = stage;
    }

    public void show() {
        VBox mainLayout = new VBox(20);
        mainLayout.setPadding(new Insets(20));

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
        mainLayout.getChildren().addAll(topGrid, bottomGrid);
        VBox.setVgrow(topGrid, Priority.ALWAYS);

        Scene scene = new Scene(mainLayout, 1000, 650);
        String css = MainApp.getCss();
        if (css != null) {
            scene.getStylesheets().add(css);
        }

        stage.setTitle("Time Tracker");
        stage.setScene(scene);
        stage.show();

        // Initial Load
        refreshData();
    }

    private void refreshData() {
        LocalDate currentWeek = navComponent.getCurrentDate();
        Map<String, Integer> data = Relatorios.getTempoPorDia(); // Global fetch

        // Propagate data to components
        chartComponent.updateData(currentWeek, data);
        appListComponent.refresh();
        goalComponent.updateProgress(data);
    }
}