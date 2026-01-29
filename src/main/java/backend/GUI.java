package backend;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.beans.value.ChangeListener;
import javafx.embed.swing.SwingFXUtils;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.chart.BarChart;
import javafx.scene.chart.CategoryAxis;
import javafx.scene.chart.NumberAxis;
import javafx.scene.chart.XYChart;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.*;
import javafx.scene.shape.Line;
import javafx.stage.Modality;
import javafx.stage.Stage;

import javax.swing.*;
import javax.swing.filechooser.FileSystemView;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.format.TextStyle;
import java.time.temporal.TemporalAdjusters;
import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;

public class GUI extends Application{

    private LocalDate semanaAtualVisualizada;
    private Map<String, Integer> dadosTempoGerais;
    private XYChart.Series<String, Number> serieDados;
    private NumberAxis eixoY;
    private Line linhaMedia;
    private Label labelMedia;
    private double valorMediaFinal;
    private BarChart<String, Number> graficoSemanal;

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

        RowConstraints linhaQueCresce = new RowConstraints();
        linhaQueCresce.setVgrow(Priority.ALWAYS); // A linha ocupa o espaço vertical todo
        linhaQueCresce.setFillHeight(true);

        //aplicar as larguras e alturas na grelha
        grelhaCima.getColumnConstraints().addAll(colunaGrafico, colunaListaApps);
        grelhaCima.getRowConstraints().add(linhaQueCresce);

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
        LocalDate hoje = LocalDate.now();
        this.semanaAtualVisualizada = hoje.with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY));
        this.dadosTempoGerais = Relatorios.getTempoPorDia();

        //criar o gráfico
        this.serieDados = new XYChart.Series<>();
        serieDados.setName("Horas Trabalhadas");

        //definir os eixos
        //eixo X são categorias, dias da semana -> texto
        CategoryAxis eixoX = new CategoryAxis();
        eixoX.setLabel("Dias");

        //eixo Y são números, horas -> números
        this.eixoY = new NumberAxis();
        eixoX.setLabel("Horas");
        //teto
        eixoY.setAutoRanging(false);
        eixoY.setTickUnit(0.5);

        //criar o gráfico
        this.graficoSemanal = new BarChart<>(eixoX, eixoY);
        graficoSemanal.setTitle("Relatório Semanal");
        graficoSemanal.getData().add(serieDados);
        //nao animação
        graficoSemanal.setAnimated(false);


        //-----criar a linha da média vermelha-----
        this.linhaMedia = new Line();
        //css bonito
        linhaMedia.getStyleClass().add("linha-media");
        linhaMedia.setManaged(false); 
        linhaMedia.setVisible(false);

        //caixinha no canto a dizer a média
        this.labelMedia = new Label("Média: 0h 00");
        labelMedia.getStyleClass().add("texto-media");

        // Ícone da Linha
        Line iconeLinha = new Line(0, 0, 15, 0);
        iconeLinha.getStyleClass().add("linha-media");

        // A Caixinha
        HBox boxMedia = new HBox(8, iconeLinha, labelMedia);
        boxMedia.setAlignment(Pos.CENTER);
        boxMedia.getStyleClass().add("caixa-media-flutuante");
        
        // Importante: Impede a caixa de esticar
        boxMedia.setMaxSize(Region.USE_PREF_SIZE, Region.USE_PREF_SIZE);


        //teste para ver se faz as barras
        atualizarGrafico();


        //CAMADAS
        StackPane graficoCamadas = new StackPane(graficoSemanal, linhaMedia, boxMedia);
        StackPane.setAlignment(boxMedia, Pos.TOP_RIGHT); // Canto Superior Direito
        StackPane.setMargin(boxMedia, new Insets(5, 15, 0, 0));

        //teste css
        linhaMedia.setStyle("-fx-stroke: red; -fx-stroke-width: 3px;");
        linhaMedia.setManaged(false); 
        linhaMedia.setVisible(false);

        //agr precisamos de um listener, ou seja, se o utilizador mudar o formato da página, queremos que a barra tbm mude com a janela
        ChangeListener<Number> ajustaLinha = (obs, velho, novo) -> {
            atualizarLinhaMedia();
        };

        graficoSemanal.heightProperty().addListener(ajustaLinha);
        graficoSemanal.widthProperty().addListener(ajustaLinha);
        
        // Forçar o ouvinte a correr uma vez agora (para a linha aparecer logo no início)
        Platform.runLater(() -> ajustaLinha.changed(null, 0, 0));


        //---Gráfico de Barras com css bonito
        VBox caixaGrafico = new VBox(graficoCamadas);
        caixaGrafico.getStyleClass().add("caixinhas");
        caixaGrafico.setFillWidth(true);

        //para o gráfico tbm crescer quando a janela cresce
        VBox.setVgrow(graficoCamadas, Priority.ALWAYS);

        //meter na grelha (este 0,0 é o primeiro bloco da grelha, que está dividida em 4 partes, 2x2)
        grelhaCima.add(caixaGrafico, 0, 0);
        



        //---Lista de apps mais usadas---

        Label tituloListaApps = new Label("Apps mais usadas");

        //lista das apps
        ListView<HBox> listaApps = new ListView<>();
        //dados
        List<Relatorios.DadosApp> topApps = Relatorios.DadosApp.getTopApps();

        if (topApps.isEmpty() || topApps == null) {
            listaApps.getItems().add(new HBox(new Label("Sem dados hoje...")));
        } else {
            for (Relatorios.DadosApp app : topApps) {

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

        //limitar o espaço para as apps
        listaApps.setMaxHeight(Double.MAX_VALUE);
        //lista ocupar o tamanho todo da lista
        VBox.setVgrow(listaApps, Priority.ALWAYS);

        listaApps.setStyle("-fx-background-insets: 0; -fx-padding: 0;");
        
        //lista na vertical
        VBox caixaVertical = new VBox(10);
        caixaVertical.getChildren().addAll(tituloListaApps, listaApps);
        caixaVertical.getStyleClass().add("caixinhas");

        //não ficar com espaço morto
        caixaVertical.setStyle("-fx-padding: 15 15 5 15;");

        //crescer a lista de apps mais usadas
        VBox.setVgrow(listaApps, Priority.ALWAYS);
        
        //meter na grelha
        grelhaCima.add(caixaVertical, 1, 0);




        //---Objetivo (progressBar)---

        //objetivo
        AtomicInteger horasObjetivo = new AtomicInteger(carregarObjetivo());

        //tempo feito hoje
        String chaveHoje = LocalDate.now().toString();
        int segundosFeitos = 0;
        if(dadosTempoGerais != null) {
            segundosFeitos = dadosTempoGerais.getOrDefault(chaveHoje, 0);
        }
        final int segundosFeitosFinal = segundosFeitos;

        //texto e botoes
        Label tituloObjetivo = new Label("Objetivo Diário");
        tituloObjetivo.setStyle("-fx-font-weight: bold; -fx-font-size: 14px;");

        Button editarObjetivo = new Button("Editar ✎");
        editarObjetivo.setStyle("-fx-font-size: 12px; -fx-padding: 2 5 2 5; -fx-background-radius: 20;");

        //meter o editar ao lado do titulo
        HBox cabecalhoObjetivo = new HBox(10, tituloObjetivo);
        cabecalhoObjetivo.setAlignment(Pos.CENTER);

        //barra de progresso
        ProgressBar barraObjetivo = new ProgressBar(0);
        barraObjetivo.setPrefWidth(Double.MAX_VALUE); // Ocupar a largura toda disponível
        barraObjetivo.setPrefHeight(20);              // Altura da barra (mais gordinha)

        Label labelDetalhe = new Label();

        //atualizar tempo da barra
        Runnable atualizarBarra = () -> {
            int horasAtual = horasObjetivo.get();
            int segundosMeta = horasAtual * 3600;

            double progresso = (double) segundosFeitosFinal / segundosMeta;
            if (progresso > 1.0) progresso = 1.0;

            barraObjetivo.setProgress(progresso);
            labelDetalhe.setText(formatarTempo(segundosFeitosFinal) + " / " + horasAtual + "h");
            
            // Muda a cor da barra se atingiu o objetivo
            if (progresso >= 1.0) {
                barraObjetivo.setStyle("-fx-accent: green;");
            } else {
                barraObjetivo.setStyle(null);
            }
        };

        HBox tempoObjetivoEditar = new HBox(10, labelDetalhe, editarObjetivo);
        tempoObjetivoEditar.setAlignment(Pos.CENTER);
        atualizarBarra.run();

        //página nova quando clica em editar
        editarObjetivo.setOnAction(e -> {
            TextInputDialog dialog = new TextInputDialog(String.valueOf(horasObjetivo.get()));
            dialog.setTitle("Alterar Objetivo");
            dialog.setHeaderText("Definir Meta Diária");
            dialog.setContentText("Insira as horas:");

            // O ícone da janela de diálogo (opcional, usa o stage principal)
            dialog.initOwner(palco);

            Optional<String> result = dialog.showAndWait();
            
            result.ifPresent(numero -> {
                try {
                    int novoValor = Integer.parseInt(numero);
                    if(novoValor > 0 && novoValor < 24) {
                        horasObjetivo.set(novoValor);
                        salvarObjetivo(novoValor);
                        atualizarBarra.run(); // Recalcula a barra e o texto
                    }
                } catch (NumberFormatException ex) {
                    System.out.println("O utilizador não inseriu um número válido.");
                }
            });
        });

        //css bonito caixinhas
        VBox caixaObjetivo = new VBox(10);
        caixaObjetivo.getChildren().addAll(cabecalhoObjetivo, barraObjetivo, tempoObjetivoEditar);
        caixaObjetivo.getStyleClass().add("caixinhas");
        //centrar
        caixaObjetivo.setAlignment(Pos.CENTER);

        //meter na grelha
        grelhaBaixo.add(caixaObjetivo, 1, 0);



        //---Botões---
        
        Button btnAnterior = new Button("< Semana Anterior");
        btnAnterior.getStyleClass().add("botao-grande");
        
        Button btnDetalhes = new Button("Detalhes");
        btnDetalhes.getStyleClass().add("botao-grande");

        Button btnSeguinte = new Button("Semana Seguinte >");
        btnSeguinte.getStyleClass().add("botao-grande");
        btnSeguinte.setVisible(false);

        btnAnterior.setOnAction(e -> {
            semanaAtualVisualizada = semanaAtualVisualizada.minusWeeks(1);
            atualizarGrafico();
            verBotoes(btnAnterior, btnSeguinte);
        });

        btnSeguinte.setOnAction(e -> {
            semanaAtualVisualizada = semanaAtualVisualizada.plusWeeks(1);
            atualizarGrafico();
            verBotoes(btnAnterior, btnSeguinte);
        });

        btnDetalhes.setOnAction(e -> {
            detalhesHistorico(palco, LocalDate.now());
        });
        
        //lado a lado
        HBox caixaBotoes = new HBox(15);
        caixaBotoes.setAlignment(Pos.CENTER);
        caixaBotoes.getChildren().addAll(btnAnterior, btnDetalhes ,btnSeguinte);

        //meter na grelha
        grelhaBaixo.add(caixaBotoes, 0, 0);

        
        //cenário
        layoutPrincipal.getChildren().clear();
        layoutPrincipal.getChildren().addAll(grelhaCima, grelhaBaixo);
        //cresce a grelha de cima o máximo, para quando aumentas o tamanho da janela, os botoes e a progress bar ficar lá em baixo
        VBox.setVgrow(grelhaCima, Priority.ALWAYS);

        Scene cenario = new Scene(layoutPrincipal, 1000, 650);

        //ligar css ao GUI
        String css = Objects.requireNonNull(getClass().getResource("estilo.css")).toExternalForm();
        cenario.getStylesheets().add(css);

        //Palco
        palco.setTitle("Time Tracker");
        palco.setScene(cenario);
        palco.show();
    }

    //-----------FUNÇÕES AUXILIARES-----------

    //converter segundos em minutos e horas
    private static String formatarTempo(int tempo) {
        int horas = tempo / 3600;
        int minutos = (tempo % 3600) / 60;
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
    private static Image carregarIcone(String caminho) {
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

    private static int carregarObjetivo() {
        Properties props = new Properties();
        try (FileInputStream in = new FileInputStream("config.properties")){
            props.load(in);
            // Tenta ler "meta_diaria", se não existir devolve "5"
            String valor = props.getProperty("meta_diaria", "5");
            return Integer.parseInt(valor);
        } catch (NumberFormatException | IOException e) {
            return 5;
        }
    }

    private static void salvarObjetivo(int novoValor) {
        Properties props = new Properties();
        try (FileInputStream in = new FileInputStream("config.properties")){
            props.load(in);
        } catch (IOException exception) {

        }
        props.setProperty("meta_diaria", String.valueOf(novoValor));
        try (FileOutputStream out = new FileOutputStream("config.properties")){
            props.store(out, "Configurações do Time Tracker");
        } catch (IOException e) {
            System.out.println("Erro ao guardar objetivo: " + e.getMessage());
        }
    }

    private void verBotoes(Button btnAnterior, Button btnSeguinte) {
        LocalDate segundaFeiraAtualReal = LocalDate.now().with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY));
        //limite de 4 semanas para trás
        LocalDate limitePassado = segundaFeiraAtualReal.minusWeeks(3);
        
        if (semanaAtualVisualizada.isAfter(limitePassado)) {
            btnAnterior.setVisible(true);
        } else {
            btnAnterior.setVisible(false);
        }

        if(semanaAtualVisualizada.isBefore(segundaFeiraAtualReal)) {
            btnSeguinte.setVisible(true);
        } else {
            btnSeguinte.setVisible(false);
    }
    }

    private void atualizarGrafico() {
        serieDados.getData().clear();

        double totalSegundosSemana = 0;
        double maxHorasEncontrado = 0; // Para descobrirmos a barra mais alta

        // Loop de 7 dias (Segunda a Domingo)
        for (int i = 0; i < 7; i++) {
            LocalDate diaLoop = semanaAtualVisualizada.plusDays(i);
            String chaveDia = diaLoop.toString();

            // Buscar dados (se não houver, é 0)
            int segundos = dadosTempoGerais.getOrDefault(chaveDia, 0);
            double horas = segundos / 3600.0;

            //Adicionar ao Gráfico (Eixo X e Y)
            String nomeDia = diaLoop.getDayOfWeek().getDisplayName(TextStyle.SHORT, new Locale("pt", "PT"));
            serieDados.getData().add(new XYChart.Data<>(nomeDia, horas));

            //Somar para a Média
            totalSegundosSemana += segundos;

            //Verificar se é o novo Máximo (para o teto do gráfico)
            if (horas > maxHorasEncontrado) {
                maxHorasEncontrado = horas;
            }
        }
        
        // Calcular a média final (sempre a dividir por 7 dias)
        // Usamos 'final' para o Listener poder ler esta variável sem problemas
        this.valorMediaFinal = (totalSegundosSemana / 7.0) / 3600.0;

        if(labelMedia != null) {
            String textoMedia = formatarTempo((int)(valorMediaFinal * 3600));
            labelMedia.setText("Média: " + textoMedia);
        }
        atualizarLinhaMedia();

        // Calcular o teto do gráfico: O Maior valor entre (Barra Mais Alta) e (Média)
        // Multiplicamos por 1.1 para dar 10% de margem no topo
        double tetoDoGrafico = Math.max(maxHorasEncontrado, valorMediaFinal) * 1.1;
        if(tetoDoGrafico == 0) {
            tetoDoGrafico = 1.0;
        }
        eixoY.setUpperBound(tetoDoGrafico);


        //texto quando passamos o rato por cima
        for (XYChart.Data<String, Number> dados : serieDados.getData()) {
            // 1. Calcular o tempo bonito (ex: "1h 30m")
            double horas = dados.getYValue().doubleValue();
            int segundos = (int) (horas * 3600); // Converter de volta para segundos
            String textoTooltip = formatarTempo(segundos);

            //Criar o balao com o tempo
            Tooltip tooltip = new Tooltip(textoTooltip);
            tooltip.setStyle("-fx-font-size: 14px;"); // Letra maiorzinha
            tooltip.setShowDelay(javafx.util.Duration.millis(100)); // Aparece quase instantaneamente

            // 3. Colar na barra (Node)
            // Nota: O 'Node' é a barra azul visual
            Tooltip.install(dados.getNode(), tooltip);
        }
    }

    private void atualizarLinhaMedia() {
        //só corre se tiver alguma coisa
        if (graficoSemanal == null || linhaMedia == null || eixoY == null) return;
        //garante que as barras já estão desenhadas antes da linha da média
        Platform.runLater(() -> {

            Node areaDoGrafico = graficoSemanal.lookup(".chart-plot-background");
            
            if (graficoSemanal == null || graficoSemanal.getHeight() > 0) {
                    
                //perguntar ao Eixo Y: "Em que pixel fica a média?"
                double pixelY = eixoY.getDisplayPosition(valorMediaFinal);

                //coordenadas da área do gráfico
                double topoDoGrafico = areaDoGrafico.getBoundsInParent().getMinY();
                double esquerdaDoGrafico = areaDoGrafico.getBoundsInParent().getMinX();
                double larguraDoGrafico = areaDoGrafico.getBoundsInParent().getWidth();

                //
                double yFinal = topoDoGrafico + pixelY;
                //mover a linha para o sítio certo
                // Y = Topo da área + o pixel que o eixo calculou
                linhaMedia.setStartY(yFinal);
                linhaMedia.setEndY(yFinal);

                //Da esquerda até à direita da área do gráfico
                linhaMedia.setStartX(esquerdaDoGrafico);
                linhaMedia.setEndX(esquerdaDoGrafico + larguraDoGrafico);

                //mostrar a linha
                linhaMedia.setVisible(true);
                //puxar a linha para a camada da frente
                linhaMedia.toFront();
            }
        });
    }

    private void detalhesHistorico(Stage palco, LocalDate dataParaVer) {
        final Stage janelaDetalhes = new Stage();
        janelaDetalhes.initOwner(palco);
        //dá block à janela atrás
        janelaDetalhes.initModality(Modality.WINDOW_MODAL);
        janelaDetalhes.setTitle("Histórico de Atividade");

        //caixinha
        VBox layoutDetalhes = new VBox(15);
        layoutDetalhes.setPadding(new Insets(20));
        layoutDetalhes.getStyleClass().add("caixinhas");

        //titulo
        Label tituloHeader = new Label("Histórico: " + dataParaVer.toString());
        tituloHeader.setStyle("-fx-font-size: 18px; -fx-font-weight: bold; -fx-text-fill: white;");

        // A Lista
        ListView<HBox> listaHistorico = new ListView<>();
        // Remove as bordas padrão do JavaFX para ficar clean
        listaHistorico.setStyle("-fx-background-color: transparent; -fx-control-inner-background: transparent; -fx-padding: 0;");
        VBox.setVgrow(listaHistorico, Priority.ALWAYS);

        // --- BUSCAR DADOS DO RELATORIOS ---
        List<Relatorios.LogItem> logItems = Relatorios.LogItem.getHistorico(dataParaVer.toString());

        for (Relatorios.LogItem item : logItems) {
                
            //fazer "categorias" por dia ??? (ns se faço ou mostro só o de hoje)

            //mostrar horas
            Label labelHora = new Label(item.hora);
            labelHora.setPrefWidth(60);
            labelHora.setStyle("-fx-text-fill: #000000ff; -fx-font-size: 13px;");
            labelHora.setAlignment(Pos.CENTER_RIGHT);

            // 2. Ícone
            ImageView iconView = new ImageView();
            Image icon = carregarIcone(item.caminho);
            if (icon != null) {
                iconView.setImage(icon);
                iconView.setFitWidth(18);
                iconView.setFitHeight(18);
            }
            HBox caixaIcone = new HBox(iconView);
            caixaIcone.setPrefWidth(30);
            caixaIcone.setAlignment(Pos.CENTER);

            // 3. Nome da App/Aba
            Label labelNome = new Label(item.nome);
            labelNome.setStyle("-fx-text-fill: #000000ff; -fx-font-size: 14px;");
            HBox.setHgrow(labelNome, Priority.ALWAYS); // Empurra o resto para a direita

                
            // Juntar a Linha
            HBox linha = new HBox(10, labelHora, caixaIcone, labelNome);
            linha.setAlignment(Pos.CENTER_LEFT);
            linha.setPadding(new Insets(8, 5, 8, 5));
            // Borda subtil por baixo de cada item
            linha.setStyle("-fx-border-color: #333333; -fx-border-width: 0 0 1 0;");
            listaHistorico.getItems().add(linha);
        }

        layoutDetalhes.getChildren().addAll(tituloHeader, listaHistorico);

        Scene cena = new Scene(layoutDetalhes, 700, 550);
        try {
            cena.getStylesheets().add(getClass().getResource("estilo.css").toExternalForm());
        } catch(Exception e) {
            System.out.println("Deu merda");
        }

        janelaDetalhes.setScene(cena);
        janelaDetalhes.show();
    }

    public static void main(String[] args) {
        launch(args);
    }


}
