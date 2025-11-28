package src;
import java.time.LocalDateTime;
import java.time.Duration;

public class Atividade {
    public String janelaName;
    public LocalDateTime horaInicio;
    public LocalDateTime horaFim;

    public Atividade(String janela) {
        this.janelaName = janela;
        this.horaInicio = LocalDateTime.now();
    }

    public long duracaoSegundos() {
        if(horaFim == null) {
            return 0;
        }

        return Duration.between(horaInicio, horaFim).getSeconds();
    }
}
