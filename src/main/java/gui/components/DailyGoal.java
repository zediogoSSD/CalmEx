package gui.components;

import javafx.css.PseudoClass;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressBar;
import javafx.scene.control.TextField;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import utils.ConfigUtils;
import utils.TimeUtils;
import javafx.scene.layout.Priority;

import java.time.LocalDate;
import java.util.Map;
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
        Stage dialog = new Stage();
        dialog.initStyle(StageStyle.UNDECORATED);
        dialog.initModality(Modality.APPLICATION_MODAL);
        dialog.initOwner(this.getScene().getWindow());

        // Create dialog content
        VBox dialogContent = new VBox(15);
        dialogContent.getStyleClass().add("caixinhas");
        dialogContent.setPadding(new Insets(20));
        dialogContent.setAlignment(Pos.CENTER);

        Label titleLabel = new Label("Definir Meta Diária");
        titleLabel.setStyle("-fx-font-weight: bold; -fx-font-size: 16px;");

        Label instructionLabel = new Label("Insira as horas (1-23):");
        instructionLabel.setStyle("-fx-font-size: 14px;");

        TextField inputField = new TextField(String.valueOf(goalHours.get()));
        inputField.setPrefWidth(200);
        inputField.setStyle("-fx-font-size: 14px; -fx-padding: 8;");
        inputField.selectAll();

        // Button container
        HBox buttonBox = new HBox(10);
        buttonBox.setAlignment(Pos.CENTER);

        Button confirmBtn = new Button("Confirmar");
        confirmBtn.getStyleClass().add("botao-medio");
        confirmBtn.setDefaultButton(true);

        Button cancelBtn = new Button("Cancelar");
        cancelBtn.getStyleClass().add("botao-medio");
        cancelBtn.setCancelButton(true);

        buttonBox.getChildren().addAll(confirmBtn, cancelBtn);

        dialogContent.getChildren().addAll(titleLabel, instructionLabel, inputField, buttonBox);

        // Event handlers
        confirmBtn.setOnAction(e -> {
            try {
                int newVal = Integer.parseInt(inputField.getText().trim());
                if (newVal > 0 && newVal < 24) {
                    goalHours.set(newVal);
                    ConfigUtils.salvarObjetivo(newVal);

                    if (latestData != null) {
                        updateProgress(latestData);
                    }
                    dialog.close();
                } else {
                    inputField.setStyle("-fx-font-size: 14px; -fx-padding: 8; -fx-border-color: red; -fx-border-width: 2;");
                }
            } catch (NumberFormatException ex) {
                inputField.setStyle("-fx-font-size: 14px; -fx-padding: 8; -fx-border-color: red; -fx-border-width: 2;");
            }
        });

        cancelBtn.setOnAction(e -> dialog.close());

        // Allow Enter to confirm
        inputField.setOnAction(e -> confirmBtn.fire());

        // Create scene and apply the same stylesheet as the main application
        Scene dialogScene = new Scene(dialogContent);

        // Apply the current stylesheet (light or dark mode)
        if (this.getScene() != null && this.getScene().getStylesheets() != null) {
            dialogScene.getStylesheets().addAll(this.getScene().getStylesheets());
        }

        dialog.setScene(dialogScene);

        // Center on parent window
        dialog.setOnShown(e -> {
            dialog.setX(this.getScene().getWindow().getX() +
                    (this.getScene().getWindow().getWidth() - dialog.getWidth()) / 2);
            dialog.setY(this.getScene().getWindow().getY() +
                    (this.getScene().getWindow().getHeight() - dialog.getHeight()) / 2);
        });

        inputField.requestFocus();
        dialog.showAndWait();
    }
}