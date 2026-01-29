package gui.dialogs;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.stage.Modality;
import javafx.stage.Stage;
import backend.Relatorios;
import utils.IconUtils;
import java.util.Objects;

import java.time.LocalDate;
import java.util.List;

public class HistoryDialog {

    public static void show(Stage owner, LocalDate date) {
        Stage stage = new Stage();
        stage.initOwner(owner);
        stage.initModality(Modality.WINDOW_MODAL);
        stage.setTitle("Histórico de Atividade");

        VBox layout = new VBox(15);
        layout.setPadding(new Insets(20));
        layout.getStyleClass().add("caixinhas");

        Label header = new Label("Histórico: " + date.toString());
        header.setStyle("-fx-font-size: 18px; -fx-font-weight: bold; -fx-text-fill: black;");

        ListView<HBox> list = new ListView<>();
        list.setStyle("-fx-background-color: transparent; -fx-control-inner-background: transparent; -fx-padding: 0;");
        VBox.setVgrow(list, Priority.ALWAYS);

        List<Relatorios.LogItem> items = Relatorios.LogItem.getHistorico(date.toString());

        for (Relatorios.LogItem item : items) {
            Label timeLbl = new Label(item.hora);
            timeLbl.setPrefWidth(60);
            timeLbl.setAlignment(Pos.CENTER_RIGHT);
            timeLbl.setStyle("-fx-text-fill: #333333; -fx-font-size: 13px;");

            ImageView iconView = new ImageView();
            var icon = IconUtils.carregarIcone(item.caminho);
            if (icon != null) {
                iconView.setImage(icon);
                iconView.setFitWidth(18);
                iconView.setFitHeight(18);
            }
            HBox iconBox = new HBox(iconView);
            iconBox.setPrefWidth(30);
            iconBox.setAlignment(Pos.CENTER);

            Label nameLbl = new Label(item.nome);
            HBox.setHgrow(nameLbl, Priority.ALWAYS);
            nameLbl.setStyle("-fx-text-fill: #333333; -fx-font-size: 14px;");

            HBox row = new HBox(10, timeLbl, iconBox, nameLbl);
            row.setAlignment(Pos.CENTER_LEFT);
            row.setPadding(new Insets(8, 5, 8, 5));
            row.setStyle("-fx-border-color: #707070ff; -fx-border-width: 0 0 1 0;");

            list.getItems().add(row);
        }

        layout.getChildren().addAll(header, list);
        Scene scene = new Scene(layout, 700, 550);

        try {
            String css = Objects.requireNonNull(HistoryDialog.class.getResource("/estilo.css")).toExternalForm();
            scene.getStylesheets().add(css);
        } catch (Exception e) {
            System.out.println("Could not load CSS: " + e.getMessage());
        }

        stage.setScene(scene);
        stage.show();
    }
}