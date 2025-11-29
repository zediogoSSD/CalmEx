package src;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

public class Relatorios {
    public static void fazerRelatorioDiario() {
        //sql que seleciona o nome da janela e soma toda a sua duração, só do próprio dia, e agrupa por nome e ordena por tempo
        String sql = "SELECT NomeJanela, SUM(duracao) as TempoTotal " + "FROM atividades " + "WHERE date(DataInicio) = date('now') " + "GROUP BY Nomejanela " + "ORDER BY TempoTotal DESC";

        try (Connection conn = DriverManager.getConnection("jdbc:sqlite:meu_tempo.db");
            PreparedStatement stmt = conn.prepareStatement(sql);
            ResultSet rs = stmt.executeQuery()){
            
            System.out.println("---RELATÓRIO DE HOJE---");
            
            while(rs.next()) {
                String nome = rs.getString("NomeJanela");
                int tempo = rs.getInt("TempoTotal");

                if(tempo < 60) {
                    System.out.println(nome + ": " + tempo + " segundos.");
                } else {
                    int minutos = tempo / 60;
                    int segundosResto = tempo % 60;
                    if(minutos < 60) {
                        System.out.println(nome + ": " + minutos + " minutos e " + segundosResto + " segundos.");
                    } else {
                        int horas = minutos / 60;
                        int minutosRestantes = minutos % 60;

                        System.out.println(nome + ": " + horas + " horas " + minutosRestantes + " minutos e " + segundosResto + " segundos.");
                    }
                }                
            }

        } catch (Exception e) {
            System.out.println("Erro no relatório: " + e.getMessage());
        }
    }

    public static void fazerRelatorioSemanal() {

    }
}
