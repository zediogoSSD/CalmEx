package gui.components;

import javafx.css.PseudoClass;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressBar;
import javafx.scene.control.TextInputDialog;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import utils.ConfigUtils;
import utils.TimeUtils;
import javafx.scene.layout.Priority;

import java.time.LocalDate;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicInteger;

public class DailyGoal extends VBox {

    private AtomicInteger goalHours;
    private ProgressBar progressBar;
    private Label detailLabel;
    private Map<String, Integer> latestData;

    public DailyGoal() {
        this.goalHours = new AtomicInteger(ConfigUtils.carregarObjetivo());
        initUI();
    }

    private void initUI() {
        Label title = new Label("Objetivo Diário");
        title.setStyle("-fx-font-weight: bold; -fx-font-size: 14px;");

        Button editBtn = new Button("Editar ✎");
        editBtn.getStyleClass().add("edit-button");
        editBtn.setOnAction(e -> openEditDialog());

        HBox header = new HBox(10, title);
        header.setAlignment(Pos.CENTER);

        progressBar = new ProgressBar(0);
        progressBar.setPrefWidth(Double.MAX_VALUE);
        HBox.setHgrow(progressBar, Priority.ALWAYS);
        progressBar.setPrefHeight(20);

        detailLabel = new Label();
        HBox footer = new HBox(10, detailLabel, editBtn);
        footer.setAlignment(Pos.CENTER);

        this.setSpacing(10);
        this.getStyleClass().add("caixinhas");
        this.setAlignment(Pos.CENTER);
        this.getChildren().addAll(header, progressBar, footer);
    }

    public void updateProgress(Map<String, Integer> data) {
        this.latestData = data;
        if (data == null) return;

        String todayKey = LocalDate.now().toString();
        int secondsDone = data.getOrDefault(todayKey, 0);

        int targetSeconds = goalHours.get() * 3600;
        double progress = (targetSeconds > 0) ? (double) secondsDone / targetSeconds : 0;

        progressBar.setProgress(Math.min(progress, 1.0));
        detailLabel.setText(TimeUtils.formatarTempo(secondsDone) + " / " + goalHours.get() + "h");

        progressBar.pseudoClassStateChanged(PseudoClass.getPseudoClass("finished"), progress >= 1);
    }

    private void openEditDialog() {
        TextInputDialog dialog = new TextInputDialog(String.valueOf(goalHours.get()));
        dialog.setTitle("Alterar Objetivo");
        dialog.setHeaderText("Definir Meta Diária");
        dialog.setContentText("Insira as horas:");

        Optional<String> result = dialog.showAndWait();
        result.ifPresent(val -> {
            try {
                int newVal = Integer.parseInt(val);
                if (newVal > 0 && newVal < 24) {
                    goalHours.set(newVal);
                    ConfigUtils.salvarObjetivo(newVal);

                    if (latestData != null) {
                        updateProgress(latestData);
                    }
                }
            } catch (NumberFormatException ignored) {}
        });
    }
}