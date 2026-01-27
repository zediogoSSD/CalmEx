package src.app.gui.components;

import src.Relatorios;
import src.app.utils.IconUtils;
import src.app.utils.TimeUtils;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import java.util.List;

public class AppList extends VBox {

    private ListView<HBox> listView;

    public AppList() {
        Label title = new Label("Apps mais usadas");
        listView = new ListView<>();
        listView.setStyle("-fx-background-insets: 0; -fx-padding: 0;");

        this.setSpacing(10);
        this.getStyleClass().add("caixinhas");
        this.setStyle("-fx-padding: 15 15 5 15;");
        this.getChildren().addAll(title, listView);

        VBox.setVgrow(listView, Priority.ALWAYS);
    }

    public void refresh() {
        listView.getItems().clear();
        List<Relatorios.DadosApp> topApps = Relatorios.DadosApp.getTopApps();

        if (topApps == null || topApps.isEmpty()) {
            listView.getItems().add(new HBox(new Label("Sem dados hoje...")));
            return;
        }

        for (Relatorios.DadosApp app : topApps) {
            ImageView iconView = new ImageView();
            var icon = IconUtils.carregarIcone(app.caminho);
            if (icon != null) {
                iconView.setImage(icon);
                iconView.setFitWidth(32);
                iconView.setFitHeight(32);
            }

            String timeText = TimeUtils.formatarTempo(app.tempo);
            Label text = new Label(app.nome + "\n" + timeText);
            text.setStyle("-fx-font-size: 13px;");

            HBox row = new HBox(15, iconView, text);
            row.setPadding(new Insets(10, 0, 10, 5));
            row.setAlignment(Pos.CENTER_LEFT);
            listView.getItems().add(row);
        }
    }
}