package backend;

import com.sun.jna.Library;
import com.sun.jna.Native;
import com.sun.jna.Pointer;
import com.sun.jna.Structure;
import com.sun.jna.platform.win32.WinDef.HWND;
import com.sun.jna.ptr.IntByReference;

public interface kbmInputs extends Library{
    kbmInputs INSTANCE = Native.load("user32", kbmInputs.class);

    Pointer GetForegroundWindow(); 
    /*isto funciona como uma televisão, a função é da API do windows, 
    portanto não consegues traduzi-la para Java, logo, em vez de tentares entrar na TV,
    arranjas um "comando", um pointer, para apontar para o que queres usar
    */

    int GetWindowTextW(Pointer identficadorJanelaWindows, char[] text, int max);


    @Structure.FieldOrder({"cbsize", "dwTime"})
    public static class LASTINPUTINFO extends Structure {
        public int cbsize;
        public int dwTime;
    }

    boolean GetLastInputInfo(LASTINPUTINFO resultado);

    //Descobre o ID da janela para o kernel32 pedir o PID desta mesma janela
    int GetWindowThreadProcessId(HWND hWnd, IntByReference lpdwProcessId);
}
