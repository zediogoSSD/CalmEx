package gui.components;

import javafx.application.Platform;
import javafx.beans.value.ChangeListener;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.chart.BarChart;
import javafx.scene.chart.CategoryAxis;
import javafx.scene.chart.NumberAxis;
import javafx.scene.chart.XYChart;
import javafx.scene.control.Label;
import javafx.scene.control.Tooltip;
import javafx.scene.layout.*;
import javafx.scene.shape.Line;
import utils.TimeUtils;

import java.util.function.Consumer;
import java.time.LocalDate;
import java.time.format.TextStyle;
import java.util.Locale;
import java.util.Map;

public class WeeklyChart extends VBox {

    private BarChart<String, Number> barChart;
    private NumberAxis yAxis;
    private XYChart.Series<String, Number> series;
    private Line averageLine;
    private Label averageLabel;
    private double currentAverageValue;

    private Consumer<LocalDate> onDaySelectedListener;
    private LocalDate currentlySelectedDate = LocalDate.now();

    public WeeklyChart() {
        initUI();
    }

    public void setOnDaySelectedListener(Consumer<LocalDate> listener) {
        this.onDaySelectedListener = listener;
    }

    private void initUI() {
        CategoryAxis xAxis = new CategoryAxis();
        xAxis.setLabel("Dias");

        yAxis = new NumberAxis();
        yAxis.setLabel("Horas");
        yAxis.setAutoRanging(false);
        yAxis.setTickUnit(0.5);

        barChart = new BarChart<>(xAxis, yAxis);
        barChart.setTitle("Relatório Semanal");
        barChart.setAnimated(false);
        barChart.getStyleClass().add("weekly-bar-chart");

        series = new XYChart.Series<>();
        series.setName("Horas Trabalhadas");
        barChart.getData().add(series);

        // Average Line Setup
        setupAverageLine();

        StackPane layers = new StackPane(barChart, averageLine, createAverageBadge());
        StackPane.setAlignment(averageLine, Pos.CENTER_LEFT);
        VBox.setVgrow(layers, Priority.ALWAYS);

        this.getChildren().add(layers);
        this.getStyleClass().add("caixinhas");
        this.setFillWidth(true);

        // Listener for resizing
        ChangeListener<Number> resizeListener = (obs, old, nev) -> updateAverageLinePosition();
        barChart.heightProperty().addListener(resizeListener);
        barChart.widthProperty().addListener(resizeListener);
    }

    private void setupAverageLine() {
        averageLine = new Line();
        averageLine.getStyleClass().add("linha-media");
        averageLine.setManaged(false);
        averageLine.setVisible(false);
    }

    private HBox createAverageBadge() {
        averageLabel = new Label("Média: 0h 00");
        averageLabel.getStyleClass().add("texto-media");

        Line iconLine = new Line(0, 0, 15, 0);
        iconLine.getStyleClass().add("linha-media");

        HBox box = new HBox(8, iconLine, averageLabel);
        box.setAlignment(Pos.CENTER);
        box.getStyleClass().add("caixa-media-flutuante");
        box.setMaxSize(Region.USE_PREF_SIZE, Region.USE_PREF_SIZE);

        StackPane.setAlignment(box, Pos.TOP_RIGHT);
        StackPane.setMargin(box, new Insets(1, 15, 0, 0));
        return box;
    }

    public void setSelectedDate(LocalDate date) {
        this.currentlySelectedDate = date;
        highlightSelectedBar();
    }

    public void updateData(LocalDate currentWeek, Map<String, Integer> data) {
        series.getData().clear();
        double totalSeconds = 0;
        double maxHours = 0;

        for (int i = 0; i < 7; i++) {
            LocalDate day = currentWeek.plusDays(i);
            int seconds = data.getOrDefault(day.toString(), 0);
            double hours = seconds / 3600.0;

            String dayName = day.getDayOfWeek().getDisplayName(TextStyle.SHORT, new Locale("pt", "PT"));
            XYChart.Data<String, Number> dataPoint = new XYChart.Data<>(dayName, hours);

            // Store the LocalDate inside the data point so we can retrieve it on click
            dataPoint.setExtraValue(day);

            series.getData().add(dataPoint);

            totalSeconds += seconds;
            if (hours > maxHours) maxHours = hours;
        }

        this.currentAverageValue = (totalSeconds / 7.0) / 3600.0;
        averageLabel.setText("Média: " + TimeUtils.formatarTempo((int)(currentAverageValue * 3600)));

        double ceiling = Math.max(maxHours, currentAverageValue) * 1.1;
        yAxis.setUpperBound(ceiling == 0 ? 1.0 : ceiling);

        updateAverageLinePosition();

        for (XYChart.Data<String, Number> dados : series.getData()) {
            double horas = dados.getYValue().doubleValue();
            int segundos = (int) (horas * 3600);
            String textoTooltip = TimeUtils.formatarTempo(segundos);

            Tooltip tooltip = new Tooltip(textoTooltip);
            tooltip.setStyle("-fx-font-size: 14px;");
            tooltip.setShowDelay(javafx.util.Duration.millis(100));

            // Make sure the Node is generated before we attach events
            Node barNode = dados.getNode();
            if (barNode != null) {
                Tooltip.install(barNode, tooltip);

                // Add the Hand Cursor to indicate it's clickable
                barNode.setStyle("-fx-cursor: hand;");

                // Add the Click Event
                barNode.setOnMouseClicked(event -> {
                    if (onDaySelectedListener != null) {
                        LocalDate clickedDate = (LocalDate) dados.getExtraValue();
                        onDaySelectedListener.accept(clickedDate);
                    }
                });
            }
        }

        if (barChart == null || averageLine == null) return;

        Platform.runLater(() -> {
            Node plotArea = barChart.lookup(".chart-plot-background");
            if (plotArea != null && barChart.getHeight() > 0) {
                double pixelY = yAxis.getDisplayPosition(currentAverageValue);
                double plotTop = plotArea.getBoundsInParent().getMinY();
                double plotLeft = plotArea.getBoundsInParent().getMinX();
                double plotWidth = plotArea.getBoundsInParent().getWidth();

                double finalY = plotTop + pixelY;

                averageLine.setStartY(finalY);
                averageLine.setEndY(finalY);
                averageLine.setStartX(plotLeft);
                averageLine.setEndX(plotLeft + plotWidth);
                averageLine.setVisible(true);
                averageLine.toFront();
            }
        });
    }

    private void highlightSelectedBar() {
        if (series == null) return;

        for (XYChart.Data<String, Number> data : series.getData()) {
            Node barNode = data.getNode();
            if (barNode != null) {
                // Remove the class from all bars first
                barNode.getStyleClass().remove("chart-bar-selected");

                // Add the class back only if it matches our selected date
                LocalDate barDate = (LocalDate) data.getExtraValue();
                if (barDate != null && barDate.equals(currentlySelectedDate)) {
                    barNode.getStyleClass().add("chart-bar-selected");
                }
            }
        }
    }

    private void updateAverageLinePosition() {
        if (barChart == null || averageLine == null) return;

        Platform.runLater(() -> {
            Node plotArea = barChart.lookup(".chart-plot-background");
            if (plotArea != null && barChart.getHeight() > 0) {
                double pixelY = yAxis.getDisplayPosition(currentAverageValue);
                double plotTop = plotArea.getBoundsInParent().getMinY();
                double plotLeft = plotArea.getBoundsInParent().getMinX();
                double plotWidth = plotArea.getBoundsInParent().getWidth();

                double finalY = plotTop + pixelY;

                averageLine.setStartY(finalY);
                averageLine.setEndY(finalY);
                averageLine.setStartX(plotLeft);
                averageLine.setEndX(plotLeft + plotWidth);
                averageLine.setVisible(true);
                averageLine.toFront();
            }
        });
    }


}