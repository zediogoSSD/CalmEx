package src;
import com.sun.jna.Library;
import com.sun.jna.Native;
import com.sun.jna.Pointer;


public class MainCode {
    public static void main(String[] args) throws InterruptedException {
        //array de chars para guardar o texto
        char[] guardiaoTexto = new char[1024];

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
            //tempo atual do sistema
            int tempoLigado = kernel32.INSTANCE.GetTickCount();
            //diferença do tempo parado
            int timeParadoMili = tempoLigado - info.dwTime;

            int timeParadoSegundos = timeParadoMili/1000;
            if(timeParadoSegundos >= 60) {
                System.out.println("Estás AFK há: " + timeParadoSegundos + "segundos.");
            } else {
                System.out.println("A trabalhar em: " + Native.toString(guardiaoTexto));
            }

            //serve só para ele esperar para mandar outro se não for recebido, para não matar o PC
            Thread.sleep(1000);
        }
    }

}
