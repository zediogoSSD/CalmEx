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
import java.time.DayOfWeek;
import java.time.temporal.TemporalAdjusters;
import javafx.scene.image.Image;
import javax.imageio.ImageIO;

public class MainWindow {

    private final Stage stage;
    private WeeklyChart chartComponent;
    private AppList appListComponent;
    private DailyGoal goalComponent;
    private Navigation navComponent;
    private Header headerComponent;

    //We save the current week's data here so the click listener can use it
    private Map<String, Integer> currentData;

    public MainWindow(Stage stage) {
        this.stage = stage;
    }

    public void show() {
        VBox mainLayout = new VBox(20);
        mainLayout.setPadding(new Insets(20));

        headerComponent = new Header(this::toggleTheme);
        VBox.setMargin(headerComponent, new Insets(20, 0, 0, 0));

        chartComponent = new WeeklyChart();
        appListComponent = new AppList();
        goalComponent = new DailyGoal();

        //Tell the chart what to do when a bar is clicked
        chartComponent.setOnDaySelectedListener(clickedDate -> {
            if (currentData != null) {
                // Updates the Daily Goal to show the specific clicked day
                goalComponent.setDateAndUpdate(clickedDate, currentData);
            }
        });

        navComponent = new Navigation(
                this::refreshData,
                () -> HistoryDialog.show(stage, LocalDate.now(), headerComponent.isDarkMode())
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

        stage.setTitle("CalmEx");
        stage.setScene(scene);

        try {
            Image appIcon = new Image(Objects.requireNonNull(getClass().getResourceAsStream("/icon.png")));
            stage.getIcons().add(appIcon);

            java.net.URL iconURL = getClass().getResource("/icon.png");
            if (iconURL != null) {
                java.awt.Image awtIcon = java.awt.Toolkit.getDefaultToolkit().getImage(iconURL);
                if (java.awt.Taskbar.isTaskbarSupported() && java.awt.Taskbar.getTaskbar().isSupported(java.awt.Taskbar.Feature.ICON_IMAGE)) {
                    java.awt.Taskbar.getTaskbar().setIconImage(awtIcon);
                }
            }
        } catch (UnsupportedOperationException e) {
            System.out.println("O Windows bloqueou a mudança da Taskbar (comum no IntelliJ).");
        } catch (Exception e) {
            System.out.println("Não foi possível carregar o ícone da Taskbar: " + e.getMessage());
        }

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
        LocalDate currentMonday = navComponent.getCurrentDate();

        // Guarda os dados na variável da classe
        currentData = Relatorios.getTempoPorDia(currentMonday);

        // Atualiza os componentes base
        chartComponent.updateData(currentMonday, currentData);
        appListComponent.refresh(currentMonday);

        // --- LÓGICA DE SELEÇÃO INTELIGENTE DO DIA ---
        LocalDate today = LocalDate.now();
        LocalDate mondayOfThisWeek = today.with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY));

        LocalDate dayToSelect = currentMonday; // Por defeito, escolhe segunda-feira

        if (currentMonday.equals(mondayOfThisWeek)) {
            // 1. Se estivermos na semana atual, escolhe SEMPRE o dia de hoje
            dayToSelect = today;
        } else {
            // 2. Se for uma semana passada, procura o primeiro dia que tenha tempo registado
            for (int i = 0; i < 7; i++) {
                LocalDate checkDate = currentMonday.plusDays(i);
                int timeLogged = currentData.getOrDefault(checkDate.toString(), 0);

                if (timeLogged > 0) {
                    dayToSelect = checkDate;
                    break; // Encontrou o primeiro dia com dados, para a procura!
                }
            }
        }

        // Coloca a borda no gráfico para indicar claramente qual dia foi selecionado automaticamente
        chartComponent.setSelectedDate(null);

        // Atualiza o painel do Objetivo Diário para mostrar os dados desse dia
        goalComponent.setDateAndUpdate(dayToSelect, currentData);
    }
}