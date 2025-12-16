package src;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import java.util.Locale;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.format.TextStyle;
import java.time.temporal.TemporalAdjusters;

import javafx.application.Application;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.geometry.VPos;
import javafx.scene.Scene;
import javafx.scene.chart.BarChart;
import javafx.scene.chart.CategoryAxis;
import javafx.scene.chart.NumberAxis;
import javafx.scene.chart.XYChart;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.layout.ColumnConstraints;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

import java.io.File;
import javax.swing.filechooser.FileSystemView; // Para pedir o ícone ao Windows
import java.awt.image.BufferedImage;           // Formato de imagem antigo
import javafx.embed.swing.SwingFXUtils;        // O Tradutor (Swing -> FX)
import javafx.scene.image.Image;               // A imagem final que queremos
import javafx.scene.image.ImageView;
import javax.swing.Icon;

//progress bar
import javafx.scene.control.ProgressBar;


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
        //progress bar fica maior que os botoes
        ColumnConstraints colunaBotoes = new ColumnConstraints();
        colunaBotoes.setPercentWidth(40);

        ColumnConstraints colunaPieChart = new ColumnConstraints();
        colunaPieChart.setPercentWidth(60);

        grelhaBaixo.getColumnConstraints().addAll(colunaBotoes, colunaPieChart);

        //VARIAVEL GERAL
        Map<String, Integer> dadosTempoGerais = Relatorios.getTempoPorDia();
        
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
        Map<String, Integer> dadosReais = dadosTempoGerais;

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

            serieDados.getData().add(new XYChart.Data<>(nomeDia, horas));
        }

        //adicionar dados
        graficoSemanal.getData().add(serieDados);

        //meter na grelha (este 0,0 é o primeiro bloco da grelha, que está dividida em 4 partes, 2x2)
        grelhaCima.add(caixaGrafico, 0, 0);
        



        //---Lista de apps mais usadas---

        Label tituloListaApps = new Label("Apps mais usadas");

        //lista das apps
        ListView<HBox> listaApps = new ListView<>();
        //dados
        List<Relatorios.DadosApp> topApps = Relatorios.DadosApp.getTopApps();

        if (topApps.isEmpty()) {
            listaApps.getItems().add(new HBox(new Label("Sem dados hoje...")));
        } else {
            for (Relatorios.DadosApp app : topApps) {

                System.out.println("APP: " + app.nome + " | CAMINHO: " + app.caminho);

                //icones
                ImageView visualIcone = new ImageView();
                Image imagem = carregarIcone(app.caminho); 
                
                if (imagem != null) {
                    visualIcone.setImage(imagem);
                    visualIcone.setFitWidth(32);
                    visualIcone.setFitHeight(32);
                }

                //texto bonito
                String tempoTexto = formatarTempo(app.tempo);
                Label texto = new Label(app.nome + "\n" + tempoTexto);
                texto.setStyle("-fx-font-size: 13px;");

                //juntar icones com o texto
                HBox linha = new HBox(15, visualIcone, texto);
                linha.setPadding(new Insets(10, 0, 10, 5));
                linha.setAlignment(Pos.CENTER_LEFT);

                listaApps.getItems().add(linha);
            }
        }

        //lista ocupar o tamanho todo da lista
        VBox.setVgrow(listaApps, Priority.ALWAYS);
        
        //lista na vertical
        VBox caixaVertical = new VBox(10);
        caixaVertical.getChildren().addAll(tituloListaApps, listaApps);
        caixaVertical.getStyleClass().add("caixinhas");
        caixaVertical.setFillWidth(true);

        //meter na grelha
        grelhaCima.add(caixaVertical, 1, 0);




        //---Objetivo (progressBar)---

        //objetivo
        int horasObjetivo = 5;
        int segundosObjetivo = horasObjetivo * 3600;

        // Buscar o tempo feito hoje
        String chaveHoje = LocalDate.now().toString();
        int segundosFeitos = 0;
        if(dadosTempoGerais != null) {
            segundosFeitos = dadosTempoGerais.getOrDefault(chaveHoje, 0);
        }

        // 2. Calcular a Percentagem (0.0 a 1.0)
        double progresso = (double) segundosFeitos / segundosObjetivo;
        
        // Impedir que a barra "rebente" se trabalhares mais que o objetivo
        if (progresso > 1.0) {
            progresso = 1.0;
        }

        // 3. Criar a Barra Visual
        ProgressBar barraObjetivo = new ProgressBar(progresso);
        barraObjetivo.setPrefWidth(Double.MAX_VALUE); // Ocupar a largura toda disponível
        barraObjetivo.setPrefHeight(20);              // Altura da barra (mais gordinha)

        // 4. Criar os Textos
        Label tituloObjetivo = new Label("Objetivo Diário");
        tituloObjetivo.setStyle("-fx-font-weight: bold; -fx-font-size: 14px;");

        String textoProgresso = formatarTempo(segundosFeitos) + " / " + horasObjetivo + "h";
        Label labelDetalhe = new Label(textoProgresso);

        //css bonito caixinhas
        VBox caixaObjetivo = new VBox(10);
        caixaObjetivo.getChildren().addAll(tituloObjetivo, barraObjetivo, labelDetalhe);
        caixaObjetivo.getStyleClass().add("caixinhas");
        //centrar
        caixaObjetivo.setAlignment(Pos.CENTER);

        //meter na grelha
        grelhaBaixo.add(caixaObjetivo, 1, 0);




        //---Botões---
        
        Button btnAnterior = new Button("< Semana Anterior");
        Button btnDetalhes = new Button("Detalhes");
        Button btnSeguinte = new Button("Semana Seguinte >");
        
        //lado a lado
        HBox caixaBotoes = new HBox(15);
        caixaBotoes.setAlignment(Pos.CENTER);
        caixaBotoes.getChildren().addAll(btnAnterior, btnDetalhes ,btnSeguinte);

        //meter na grelha
        grelhaBaixo.add(caixaBotoes, 0, 0);

        
        //cenário
        layoutPrincipal.getChildren().clear();
        layoutPrincipal.getChildren().addAll(grelhaCima, grelhaBaixo);
        Scene cenario = new Scene(layoutPrincipal, 1000, 650);

        //ligar css ao GUI
        String css = getClass().getResource("estilo.css").toExternalForm();
        cenario.getStylesheets().add(css);

        //Palco
        palco.setTitle("Time Tracker");
        palco.setScene(cenario);
        palco.show();


       
    }

    //converter segundos em minutos e horas
    private String formatarTempo(int tempo) {
        int horas = tempo / 3600;
        int minutos = (tempo / 3600) % 60;
        int segundos = tempo % 60;

        if(horas > 0) {
            return horas + "h " + minutos + "m";
        } else if(minutos > 0) {
            return minutos + "m " + segundos + "s";
        } else {
            return segundos + "s";
        }
    }

    //ir buscar icon da app
    private Image carregarIcone(String caminho) {
        try {
            if (caminho == null || caminho.isEmpty()) return null;

            File ficheiro = new File(caminho);
            if (!ficheiro.exists()) return null;

            // 1. Pedir o ícone ao sistema operativo
            Icon icon = FileSystemView.getFileSystemView().getSystemIcon(ficheiro);
            
            // 2. Preparar uma tela vazia (BufferedImage)
            BufferedImage bImg = new BufferedImage(
                icon.getIconWidth(), 
                icon.getIconHeight(), 
                BufferedImage.TYPE_INT_ARGB
            );
            
            // 3. Pintar o ícone nessa tela
            icon.paintIcon(null, bImg.getGraphics(), 0, 0);

            // 4. Converter para formato JavaFX
            return SwingFXUtils.toFXImage(bImg, null);

        } catch (Exception e) {
            System.out.println("Erro ao carregar ícone: " + e.getMessage());
            return null; // Se falhar, devolvemos nulo (depois pomos um ícone genérico)
        }
    }

    public static void main(String[] args) {
        launch(args);
    }
}
