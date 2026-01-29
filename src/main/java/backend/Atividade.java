package backend;

import java.time.Duration;
import java.time.LocalDateTime;

public class Atividade {
    public String janelaName;
    public LocalDateTime horaInicio;
    public LocalDateTime horaFim;
    public String caminhoExecutavel;

    public Atividade(String nome, String caminho) {
        this.janelaName = nome;
        this.horaInicio = LocalDateTime.now();
        this.caminhoExecutavel = caminho;
    }

    public long duracaoSegundos() {
        if(horaFim == null) {
            return 0;
        }

        return Duration.between(horaInicio, horaFim).getSeconds();
    }
}
