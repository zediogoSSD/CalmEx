package src;
import java.time.LocalDateTime;

import com.sun.jna.Library;
import com.sun.jna.Native;
import com.sun.jna.Pointer;


public class MainCode {
    public static void main(String[] args) throws InterruptedException {
        //array de chars para guardar o texto
        char[] guardiaoTexto = new char[1024];

        Atividade atividade = null;
        String janelaAtual = "";

        while(true) {
            Pointer windowsPointer = kbmInputs.INSTANCE.GetForegroundWindow();
            //descobre o nome de onde está
            kbmInputs.INSTANCE.GetWindowTextW(windowsPointer, guardiaoTexto, 1024);

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
            if(timeParadoSegundos >= 5) {
                System.out.println("Estás AFK há: " + timeParadoSegundos + "segundos.");
            } else {
                System.out.println("A trabalhar em: " + Native.toString(guardiaoTexto));
            }

            String janelaAgora = Native.toString(guardiaoTexto);
            if(!janelaAgora.equals(janelaAtual)) {
                System.out.println("Mudança de Janela Detetada");

                if(atividade != null) {
                    atividade.horaFim = LocalDateTime.now();
                    System.out.println("Esteve na app/página: " + atividade.janelaName + ", durante " + atividade.duracaoSegundos() + "segundos.");
                }

                Atividade janelaAtualAtividade = new Atividade(janelaAgora);
                atividade = janelaAtualAtividade;

                janelaAtual = janelaAgora;
            }

            //serve só para ele esperar para mandar outro se não for recebido, para não matar o PC
            Thread.sleep(1000);
        }
    }

}
