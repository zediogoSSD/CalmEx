package src;
import com.sun.jna.Library;
import com.sun.jna.Native;

public interface kernel32 extends Library {
    //este load serve para ele ir buscar a biblioteca do windows para ver o tempo
    kernel32 INSTANCE = Native.load("kernel32", kernel32.class);

    int GetTickCount();
}