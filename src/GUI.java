package src;

import java.util.Locale.Category;

import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.chart.BarChart;
import javafx.scene.chart.CategoryAxis;
import javafx.scene.chart.NumberAxis;
import javafx.scene.chart.PieChart;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

public class GUI extends Application{
    public void start(Stage palco) {

        //layout da página, é uma grelha 2x2 portanto usamos GridPane
        GridPane grelha = new GridPane();

        //espaços para não estarem colados
        grelha.setHgap(10);
        grelha.setVgap(10);

        //---Gráfico de Barras---
        //definir os eixos

        //eixo X são categorias, dias da semana -> texto
        CategoryAxis eixoX = new CategoryAxis();
        eixoX.setLabel("Dias");

        //eixo Y são números, horas -> números
        NumberAxis eixoY = new NumberAxis();
        eixoX.setLabel("Horas");

        //criar o gráfico
        BarChart<String, Number> graficoSemanal = new BarChart<>(eixoX, eixoY);
        graficoSemanal.setTitle("Relatório Semanal");

        //meter na grelha (este 0,0 é o primeiro bloco da grelha, que está dividida em 4 partes, 2x2)
        grelha.add(graficoSemanal, 0, 0);


        //---Lista de apps mais usadas---

        Label tituloListaApps = new Label("Apps mais usadas");

        //lista das apps
        ListView<String> listaApps = new ListView<>();
        //dados
        listaApps.getItems().add("VS Code - 5h");
        listaApps.getItems().add("Chrome - 3h");

        //lista na vertical
        VBox caixaVertical = new VBox(10); //10 é o espaço entre as apps
        caixaVertical.getChildren().addAll(tituloListaApps, listaApps);

        //meter na grelha
        grelha.add(caixaVertical, 1, 0);


        //---Objetivo (PieChart)---
        PieChart graficoObjetivo = new PieChart();
        graficoObjetivo.setTitle("Objetivo Diário");

        //criar fatias
        PieChart.Data fatiaFeita = new PieChart.Data("Trabalhado", 70);
        PieChart.Data fatiaFalta = new PieChart.Data("Trabalhado", 30);

        graficoObjetivo.getData().addAll(fatiaFeita, fatiaFalta);

        //meter na grelha
        grelha.add(graficoObjetivo, 1, 1);


        //---Botões---
        
        Button btnAnterior = new Button("< Semana Anterior");
        Button btnSeguinte = new Button("Semana Seguinte >");
        
        //lado a lado
        HBox caixaBotoes = new HBox(15); // 15 de espaço
        caixaBotoes.getChildren().addAll(btnAnterior, btnSeguinte);

        //meter na grelha
        grelha.add(caixaBotoes, 0, 1);

        
        //cenário
        Scene cenario = new Scene(grelha, 900, 600);

        //Palco
        palco.setTitle("PALCO");
        palco.setScene(cenario);
        palco.show();
    }

    public static void main(String[] args) {
        launch(args);
    }
}
