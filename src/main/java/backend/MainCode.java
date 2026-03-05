package backend;

import com.sun.jna.Native;
import com.sun.jna.Pointer;
import com.sun.jna.platform.win32.WinDef.HWND;
import com.sun.jna.ptr.IntByReference;

import java.awt.*;
import java.time.LocalDateTime;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import javafx.application.Platform;
import javafx.stage.Stage;
import javax.imageio.ImageIO;

public class MainCode {

    // Variáveis globais para a thread em background aceder
    private static char[] guardiaoTexto = new char[1024];
    private static Atividade atividade = null;
    private static String janelaAtual = "";
    private static boolean fxIniciado = false;
    private static ScheduledExecutorService scheduler;
    private static final int LIMITE_AFK_SEGUNDOS = 900; // 15 minutos
    private static boolean emEncerramento = false;

    public static void main(String[] args) {
        System.out.println("A iniciar o Time Tracker em background...");

        // 1. Ligar BD e fazer limpeza
        BD.ligarEConfirmarBD();
        BD.limpezaMensal();

        // 2. Setup Inicial da Janela
        Pointer ptrInicial = kbmInputs.INSTANCE.GetForegroundWindow();
        kbmInputs.INSTANCE.GetWindowTextW(ptrInicial, guardiaoTexto, 1024);
        janelaAtual = limparTitulo(Native.toString(guardiaoTexto));

        // 3. Criar o ícone na System Tray (Para fechar o programa)
        setupSystemTray();

        configurarArranqueAutomatico();

        configurarShutdownHook();

        // 4. Iniciar o Tracker em Background
        startBackgroundTracker();
    }

    private static void startBackgroundTracker() {
        // Cria uma thread a correr em background
        scheduler = Executors.newSingleThreadScheduledExecutor(runnable -> {
            Thread thread = new Thread(runnable);
            thread.setDaemon(true); // <-- This is the magic line
            return thread;
        });

        // Corre este código a cada 1 segundo (substitui o while(true))
        scheduler.scheduleAtFixedRate(() -> {
            try {
                Pointer windowsPointer = kbmInputs.INSTANCE.GetForegroundWindow();
                kbmInputs.INSTANCE.GetWindowTextW(windowsPointer, guardiaoTexto, 1024);
                HWND hwnd = new HWND(windowsPointer);
                String caminho = getCaminhoEXE(hwnd);

                // AFK finder
                kbmInputs.LASTINPUTINFO info = new kbmInputs.LASTINPUTINFO();
                info.cbsize = info.size();
                kbmInputs.INSTANCE.GetLastInputInfo(info);

                int atividadeLigado = kernel32.INSTANCE.GetTickCount();
                int timeParadoMili = atividadeLigado - info.dwTime;
                int timeParadoSegundos = timeParadoMili / 1000;

                // Lógica de Pausa AFK
                if (timeParadoSegundos >= LIMITE_AFK_SEGUNDOS) {
                    // Se a atividade ainda está a correr, fecha-a e guarda na BD
                    if (atividade != null) {
                        // Subtrai o tempo que esteve parado para não contar esses 15 mins fantasma!
                        atividade.horaFim = LocalDateTime.now().minusSeconds(timeParadoSegundos);

                        System.out.println("AFK Detetado. Atividade fechada: " + atividade.janelaName);
                        BD.salvarAtividade(atividade);

                        atividade = null; // Fica nulo para sabermos que estamos AFK
                        janelaAtual = "AFK";
                    }
                } else {
                    // O utilizador está ativo
                    String janelaAgora = limparTitulo(Native.toString(guardiaoTexto));

                    // Se voltaste do AFK (atividade é nula), começa uma nova atividade
                    if (atividade == null) {
                        System.out.println("Voltou do AFK. A trabalhar em: " + janelaAgora);
                        atividade = new Atividade(janelaAgora, caminho);
                        janelaAtual = janelaAgora;
                    }
                    // Se mudaste de janela normalmente
                    else if (!janelaAgora.equals(janelaAtual)) {
                        atividade.horaFim = LocalDateTime.now();
                        BD.salvarAtividade(atividade);

                        atividade = new Atividade(janelaAgora, caminho);
                        janelaAtual = janelaAgora;
                    }
                }
            } catch (Exception e) {
                // Num executável real, os erros são silenciosos sem consola.
                System.out.println("Erro na thread background: " + e.getMessage());
            }
        }, 0, 1, TimeUnit.SECONDS);
    }

    private static void setupSystemTray() {
        if (!SystemTray.isSupported()) {
            System.out.println("SystemTray não é suportado no teu sistema!");
            return;
        }

        SystemTray tray = SystemTray.getSystemTray();

        try {
            java.net.URL urlIcone = MainCode.class.getResource("/icon.png");
            Image image = ImageIO.read(urlIcone);

            PopupMenu popup = new PopupMenu();

            MenuItem abrirGuiItem = new MenuItem("Abrir Interface");
            abrirGuiItem.addActionListener(e -> abrirInterface());
            popup.add(abrirGuiItem);

            MenuItem exitItem = new MenuItem("Sair do Time Tracker");

            // Quando clicam em "Sair", corre a função de encerramento seguro
            exitItem.addActionListener(e -> encerramentoSeguro());
            popup.add(exitItem);

            TrayIcon trayIcon = new TrayIcon(image, "Time Tracker", popup);
            trayIcon.setImageAutoSize(true);
            tray.add(trayIcon);

        } catch (Exception e) {
            System.out.println("Erro ao carregar a imagem: " + e.getMessage());
        }
    }

    private static void encerramentoSeguro() {
        // Prevent double-firing if the shutdown hook is also running
        if (emEncerramento) return;
        emEncerramento = true;

        System.out.println("A encerrar o Time Tracker...");

        if (scheduler != null) scheduler.shutdown();

        if (atividade != null) {
            atividade.horaFim = java.time.LocalDateTime.now();
            BD.salvarAtividade(atividade);
        }

        Relatorios.fazerRelatorioDiario();
        Relatorios.fazerRelatorioSemanal();

        System.exit(0);
    }

    public static String getCaminhoEXE(HWND ID) {
        IntByReference PID = new IntByReference();
        kbmInputs.INSTANCE.GetWindowThreadProcessId(ID, PID);
        Pointer processo = kernel32.INSTANCE.OpenProcess(kernel32.PROCESS_QUERY_INFORMATION | kernel32.PROCESS_VM_READ, false, PID.getValue());

        if(processo == null) {
            return "";
        }

        byte[] buffer = new byte[1024];
        IntByReference tamanho = new IntByReference(buffer.length);
        if(kernel32.INSTANCE.QueryFullProcessImageNameA(processo, 0, buffer, tamanho)) {
            kernel32.INSTANCE.CloseHandle(processo);
            return Native.toString(buffer);
        }

        kernel32.INSTANCE.CloseHandle(processo);
        return "";
    }

    public static String limparTitulo(String tituloSujo) {
        if (tituloSujo == null || tituloSujo.isEmpty()) {
            return "";
        }
        String apenasTextoNormal = tituloSujo.replaceAll("[^\\x20-\\x7E]", "");
        return apenasTextoNormal.trim();
    }

    private static void abrirInterface() {
        if (!fxIniciado) {
            fxIniciado = true;

            // Impede que fechar a janela (o "X") mate o background tracker!
            Platform.setImplicitExit(false);

            // Inicia o motor do JavaFX pela primeira vez
            Platform.startup(() -> {
                try {
                    Stage palco = new Stage();
                    gui.MainWindow janela = new gui.MainWindow(palco);
                    janela.show();
                } catch (Exception ex) {
                    System.out.println("Erro ao iniciar GUI: " + ex.getMessage());
                }
            });
        } else {
            // Se o motor JavaFX já está a correr, apenas desenha uma nova janela
            Platform.runLater(() -> {
                try {
                    Stage palco = new Stage();
                    gui.MainWindow janela = new gui.MainWindow(palco);
                    janela.show();
                } catch (Exception ex) {
                    System.out.println("Erro ao reabrir GUI: " + ex.getMessage());
                }
            });
        }
    }

    private static void configurarArranqueAutomatico() {
        try {
            String pastaAtual = System.getProperty("user.dir");

            // O ESCUDO DO SYSTEM32:
            // Se a pasta atual contém "system32", fomos iniciados pelo arranque do Windows.
            // Ignoramos a configuração para não estragar o Registo!
            if (pastaAtual.toLowerCase().contains("system32")) {
                System.out.println("Iniciado pelo arranque do Windows. Registo protegido.");
                return;
            }

            // Se não estamos no System32, o utilizador abriu a app manualmente.
            // Vamos gravar o caminho correto no Registo do Windows.
            String caminhoExe = pastaAtual + "\\TimeTracker.exe";

            // Comando com aspas escapadas (\\\") para proteger contra espaços nos nomes das pastas
            String comando = "reg add \"HKCU\\Software\\Microsoft\\Windows\\CurrentVersion\\Run\" /v \"TimeTracker\" /t REG_SZ /d \"\\\"" + caminhoExe + "\\\"\" /f";

            Runtime.getRuntime().exec(comando);
            System.out.println("Arranque automático atualizado para: " + caminhoExe);

        } catch (Exception e) {
            System.out.println("Erro ao definir arranque automático: " + e.getMessage());
        }
    }

    private static void configurarShutdownHook() {
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            // If we already closed manually via the System Tray, do nothing
            if (emEncerramento) return;
            emEncerramento = true;

            System.out.println("Windows is shutting down. Saving Time Tracker data...");

            if (scheduler != null) {
                scheduler.shutdownNow();
            }

            // Save the last activity before the PC dies
            if (atividade != null) {
                atividade.horaFim = java.time.LocalDateTime.now();
                BD.salvarAtividade(atividade);
            }

            Relatorios.fazerRelatorioDiario();
            Relatorios.fazerRelatorioSemanal();
        }));
    }
}