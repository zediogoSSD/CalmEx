package src;
import com.sun.jna.Library;
import com.sun.jna.Native;
import com.sun.jna.Pointer;
import com.sun.jna.ptr.IntByReference;

//gere memória, ficheiros e os "processos" (aplicações)
public interface kernel32 extends Library {
    //este load serve para ele ir buscar a biblioteca do windows para ver o tempo
    kernel32 INSTANCE = Native.load("kernel32", kernel32.class);

    int GetTickCount();

    //Cada "processo" é uma casa segura, tem proteções, e nós temos de pedir com jeitinho para saber onde estamos

    //Pedir permissão para fazer perguntas
    int PROCESS_QUERY_INFORMATION = 0x0400;
    //VM = Virtual Memory = permissão para ler a memória do processo (morada da aplicação, caminho.EXE)
    int PROCESS_VM_READ = 0x0010;

    //Nós damos o PID (número do "processo" / aplicação) como um "crachá" / autorização para perguntar
    Pointer OpenProcess(int dwDesiredAccess, boolean bInheritHandle, int dwProcessId);
    //Fazer perguntas
    boolean QueryFullProcessImageNameA(Pointer hProcess, int dwFlags, byte[] lpExeName, IntByReference lpdwSize);    
    //Devolve os PID's, para não ficar a armazenar "crachás" inútis, porque as aplicações mudam o seu PID muitas vezes num dia
    boolean CloseHandle(Pointer hObject);
}