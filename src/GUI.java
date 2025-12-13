package src;

import java.util.HashMap;
import java.util.Map;

import java.util.Locale;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.format.TextStyle;
import java.time.temporal.TemporalAdjuster;
import java.time.temporal.TemporalAdjusters;
import java.util.Locale.Category;

import javafx.application.Application;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.chart.BarChart;
import javafx.scene.chart.CategoryAxis;
import javafx.scene.chart.NumberAxis;
import javafx.scene.chart.PieChart;
import javafx.scene.chart.XYChart;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.layout.ColumnConstraints;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

public class GUI extends Application{
    public void start(Stage palco) {

        //novo layout, vamos fazer duas grelhas, uma de cima, com o gráfico de barras e a lista e uma de baixo, com botoes e objetivo
        VBox layoutPrincipal = new VBox(20); //espaço entre andares
        layoutPrincipal.setPadding(new Insets(20));

        //grelha de cima
        GridPane grelhaCima = new GridPane();
        grelhaCima.setHgap(20);
        //gráfico de barras ficar maior que a lista de apps, 70-30
        ColumnConstraints colunaGrafico = new ColumnConstraints();
        colunaGrafico.setPercentWidth(70);

        ColumnConstraints colunaListaApps = new ColumnConstraints();
        colunaListaApps.setPercentWidth(30);

        //aplicar as larguras na grelha
        grelhaCima.getColumnConstraints().addAll(colunaGrafico, colunaListaApps);

        //grelha de baixo
        GridPane grelhaBaixo = new GridPane();
        grelhaBaixo.setHgap(20);
        //piechart fica maior que os botoes
        ColumnConstraints colunaBotoes = new ColumnConstraints();
        colunaBotoes.setPercentWidth(40);

        ColumnConstraints colunaPieChart = new ColumnConstraints();
        colunaPieChart.setPercentWidth(60);

        grelhaBaixo.getColumnConstraints().addAll(colunaBotoes, colunaPieChart);

        
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
        //---Gráfico de Barras com css bonito
        VBox caixaGrafico = new VBox(graficoSemanal);
        caixaGrafico.getStyleClass().add("caixinhas");
        caixaGrafico.setFillWidth(true);


        //----DADOS REAIS NO GRÁFICO DE BARRAS----
        
        //limpar dados
        graficoSemanal.getData().clear();
        //criar XY dados
        XYChart.Series serieDados = new XYChart.Series();
        serieDados.setName("Horas Trabalhadas");

        //buscar os dados reais à BD
        Map<String, Integer> dadosReais = Relatorios.getTempoPorDia();

        //buscar data de hoje
        LocalDate hoje = LocalDate.now();

        //descobrir a segunda nessa semana
        LocalDate segunda = hoje.with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY));

        //loop de 7 dias
        for(int i = 0; i < 7; i++) {
            //calcular dia atual
            LocalDate diaLoop = segunda.plusDays(i);

            //converter para String
            String dados = diaLoop.toString();

            //verifica se existe dados para o dia, senão, põe 0
            int segundos = dadosReais.getOrDefault(dados, 0);

            double horas = segundos / 3600.0;

            String nomeDia = diaLoop.getDayOfWeek().getDisplayName(TextStyle.SHORT, new Locale("pt", "PT"));

            serieDados.getData().add(new XYChart.Data(nomeDia, horas));
        }

        //adicionar dados
        graficoSemanal.getData().add(serieDados);

        //meter na grelha (este 0,0 é o primeiro bloco da grelha, que está dividida em 4 partes, 2x2)
        grelhaCima.add(caixaGrafico, 0, 0);
        



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
        caixaVertical.getStyleClass().add("caixinhas");

        //meter na grelha
        grelhaCima.add(caixaVertical, 1, 0);




        //---Objetivo (PieChart)---
        PieChart graficoObjetivo = new PieChart();
        graficoObjetivo.setTitle("Objetivo Diário");

        //criar fatias
        PieChart.Data fatiaFeita = new PieChart.Data("Trabalhado", 70);
        PieChart.Data fatiaFalta = new PieChart.Data("Falta", 30);

        graficoObjetivo.getData().addAll(fatiaFeita, fatiaFalta);

        //css bonito caixinhas
        VBox caixaPieChartObjetivo = new VBox(10, graficoObjetivo);
        caixaPieChartObjetivo.getStyleClass().add("caixinhas");
        //centrar
        caixaPieChartObjetivo.setAlignment(Pos.CENTER);

        //meter na grelha
        grelhaBaixo.add(caixaPieChartObjetivo, 1, 1);




        //---Botões---
        
        Button btnAnterior = new Button("< Semana Anterior");
        Button btnDetalhes = new Button("Detalhes");
        Button btnSeguinte = new Button("Semana Seguinte >");
        
        //lado a lado
        HBox caixaBotoes = new HBox(15); // 15 de espaço
        caixaBotoes.getChildren().addAll(btnAnterior, btnDetalhes ,btnSeguinte);

        //meter na grelha
        grelhaBaixo.add(caixaBotoes, 0, 1);

        
        //cenário
        layoutPrincipal.getChildren().addAll(grelhaCima, grelhaBaixo);
        Scene cenario = new Scene(layoutPrincipal, 1000, 650);

        //ligar css ao GUI
        String css = getClass().getResource("estilo.css").toExternalForm();
        cenario.getStylesheets().add(css);

        //Palco
        palco.setTitle("PALCO");
        palco.setScene(cenario);
        palco.show();
    }

    public static void main(String[] args) {
        launch(args);
    }
}
