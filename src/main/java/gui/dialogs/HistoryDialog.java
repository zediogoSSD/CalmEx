package gui.dialogs;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.stage.Modality;
import javafx.stage.Stage;
import backend.Relatorios;
import utils.IconUtils;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.*;

public class HistoryDialog {

    private static final Map<String, Image> iconCache = new HashMap<>();

    public static void show(Stage owner, LocalDate date, boolean isDarkMode) {
        Stage stage = new Stage();
        stage.initOwner(owner);
        stage.initModality(Modality.WINDOW_MODAL);
        stage.setTitle("Histórico de Atividade");

        VBox layout = new VBox(15);
        layout.setPadding(new Insets(20));
        layout.getStyleClass().add("caixinhas");

        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd-MM-yyyy");
        String formattedDate = date.format(formatter);

        Label header = new Label("Histórico: " + formattedDate);
        header.getStyleClass().add("header-title");

        // Updated ListView to use the Data Model (LogItem) instead of HBox
        ListView<Relatorios.LogItem> list = new ListView<>();
        list.getStyleClass().add("list-view");
        VBox.setVgrow(list, Priority.ALWAYS);

        // Set the Cell Factory for Lazy Loading
        list.setCellFactory(param -> new LogCell());

        // Load data and add directly to the list
        List<Relatorios.LogItem> items = Relatorios.LogItem.getHistorico(date.toString());
        list.getItems().addAll(items);

        layout.getChildren().addAll(header, list);
        Scene scene = new Scene(layout, 700, 550);

        try {
            String cssFile = isDarkMode ? "/estiloDark.css" : "/estilo.css";
            String css = Objects.requireNonNull(HistoryDialog.class.getResource(cssFile)).toExternalForm();
            scene.getStylesheets().add(css);
        } catch (Exception e) {
            System.out.println("Could not load CSS: " + e.getMessage());
        }

        stage.setScene(scene);
        stage.show();
    }

    /**
     * Custom Cell Class that reuses UI components
     */
    private static class LogCell extends ListCell<Relatorios.LogItem> {
        private final HBox row;
        private final Label timeLbl = new Label();
        private final ImageView iconView = new ImageView();
        private final Label nameLbl = new Label();

        public LogCell() {
            timeLbl.setPrefWidth(60);
            timeLbl.setAlignment(Pos.CENTER_RIGHT);
            timeLbl.getStyleClass().add("texto-media");

            iconView.setFitWidth(18);
            iconView.setFitHeight(18);

            HBox iconBox = new HBox(iconView);
            iconBox.setPrefWidth(30);
            iconBox.setAlignment(Pos.CENTER);

            HBox.setHgrow(nameLbl, Priority.ALWAYS);
            nameLbl.getStyleClass().add("label");

            row = new HBox(10, timeLbl, iconBox, nameLbl);
            row.setAlignment(Pos.CENTER_LEFT);
            row.setPadding(new Insets(8, 5, 8, 5));
            row.getStyleClass().add("list-cell");
        }

        @Override
        protected void updateItem(Relatorios.LogItem item, boolean empty) {
            super.updateItem(item, empty);

            if (empty || item == null) {
                setGraphic(null);
                setText(null);
            } else {
                timeLbl.setText(item.hora);
                nameLbl.setText(item.nome);

                // Use the cache for the icon
                Image icon = iconCache.computeIfAbsent(item.caminho, path -> IconUtils.carregarIcone(path));
                iconView.setImage(icon);

                setGraphic(row);
            }
        }
    }
}