package backend;

import com.sun.jna.Native;
import com.sun.jna.Pointer;
import com.sun.jna.platform.win32.WinDef.HWND;
import com.sun.jna.ptr.IntByReference;

import java.time.LocalDateTime;


public class MainCode {
    public static void main(String[] args) throws InterruptedException {

        //array de chars para guardar o texto
        char[] guardiaoTexto = new char[1024];
        Atividade atividade = null;
        //aponta para a janela que está aberta, preciso disto aqui para não haver logo no inicio uma mudança de janela

        Pointer ptrInicial = kbmInputs.INSTANCE.GetForegroundWindow();
        kbmInputs.INSTANCE.GetWindowTextW(ptrInicial, guardiaoTexto, 1024);
    
        // Define a memória inicial JÁ LIMPA
        String janelaAtual = limparTitulo(Native.toString(guardiaoTexto));
    
        System.out.println("Programa Iniciado.");
        System.out.println("Janela atual: " + janelaAtual);

        //confirma e liga (se existir) uma BD
        BD.ligarEConfirmarBD();
        //fazer a limpezaMensal
        BD.limpezaMensal();
        
        //relatório diário quando o programa é desligado
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            System.out.println("A encerrar o Time Tracker...");
            Relatorios.fazerRelatorioDiario();
            Relatorios.fazerRelatorioSemanal();
        }));

        while(true) {
            Pointer windowsPointer = kbmInputs.INSTANCE.GetForegroundWindow();
            //descobre o nome de onde está
            kbmInputs.INSTANCE.GetWindowTextW(windowsPointer, guardiaoTexto, 1024);
            
            HWND hwnd = new HWND(windowsPointer);
            //apanhar o caminho do executável
            String caminho = getCaminhoEXE(hwnd);
            System.out.println("Janela: " + Native.toString(guardiaoTexto) + "| Caminho: " + caminho);

            //AFK finder
            kbmInputs.LASTINPUTINFO info = new kbmInputs.LASTINPUTINFO();
            info.cbsize = info.size();
            kbmInputs.INSTANCE.GetLastInputInfo(info);
            //output é info.dwtime, dá-nos a última vez que mexemos

            //passar de tick counter para segundos/minutos (a partir do "detetor" do windows)
            //atividade atual do sistema
            int atividadeLigado = kernel32.INSTANCE.GetTickCount();
            //diferença do atividade parado
            int timeParadoMili = atividadeLigado - info.dwTime;

            int timeParadoSegundos = timeParadoMili/1000;
            if(timeParadoSegundos >= 900) {
                System.out.println("Estás AFK há: " + timeParadoSegundos + " segundos.");
            } else {
                System.out.println("A trabalhar em: " + Native.toString(guardiaoTexto));
            }

            //Feedback de que janela/app estamos no momento e quando há mudança de janela
            String janelaAgora = limparTitulo(Native.toString(guardiaoTexto));
            if(!janelaAgora.equals(janelaAtual)) {
                System.out.println("---Mudança de Janela Detetada---");

                if(atividade != null) {
                    //fecha a atividade, ou seja, fecha a janela
                    atividade.horaFim = LocalDateTime.now();
                    System.out.println("Esteve na app/página: " + atividade.janelaName + ", durante " + atividade.duracaoSegundos() + " segundos.");
                    //salva a atividade na BD
                    BD.salvarAtividade(atividade);
                }

                //Muda e começa a contar o tempo na nova janela (aka Atividade)
                Atividade janelaAtualAtividade = new Atividade(janelaAgora, caminho);
                atividade = janelaAtualAtividade;

                janelaAtual = janelaAgora;
            }

            //serve só para ele esperar para mandar outro se não for recebido, para não matar o PC
            Thread.sleep(1000);
        }
    }

    public static String getCaminhoEXE(HWND ID) {
        //sitio onde vamos guardar o PID da app
        IntByReference PID = new IntByReference();

        //ir buscar o ID que vai ser buscado pelo User32 (o nosso kbmInputs (sim, devia mudar o nome))
        kbmInputs.INSTANCE.GetWindowThreadProcessId(ID, PID);

        //já tenho o PID guardado, agr preciso de pedir permissões para tirar as informações
        Pointer processo = kernel32.INSTANCE.OpenProcess(kernel32.PROCESS_QUERY_INFORMATION | kernel32.PROCESS_VM_READ, false, PID.getValue());

        if(processo == null) {
            return "";
        }
        
        byte[] buffer = new byte[1024];
        IntByReference tamanho = new IntByReference(buffer.length);
        //perguntar o caminho, true se correr vem
        if(kernel32.INSTANCE.QueryFullProcessImageNameA(processo, 0, buffer, tamanho)) {
            kernel32.INSTANCE.CloseHandle(processo);
            //String linda do caminho
            return Native.toString(buffer);
        }

        kernel32.INSTANCE.CloseHandle(processo);
        return "";
    }

    //função para limpar o título (para não criar 2 tabelas iguais onde o nome muda uma virgula)
    public static String limparTitulo(String tituloSujo) {
        
        if (tituloSujo == null || tituloSujo.isEmpty()) {
            return "";
        }

        String apenasTextoNormal = tituloSujo.replaceAll("[^\\x20-\\x7E]", "");

        return apenasTextoNormal.trim();
    }
}
