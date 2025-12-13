package src;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.HashMap;
import java.util.Map;

public class Relatorios {

    public static String limparNome(String tituloCompleto) {

        int posicaoTraco = tituloCompleto.lastIndexOf(" - ");

        if (posicaoTraco == -1 || tituloCompleto == null) {
            return tituloCompleto;
        }
        
        return tituloCompleto.substring(posicaoTraco + 3);
    }

    public static void fazerRelatorioDiario() {
        //sql que seleciona o nome da janela e soma toda a sua duração, só do próprio dia, e agrupa por nome e ordena por tempo
        String sql = "SELECT NomeJanela, SUM(duracao) as TempoTotal " + "FROM atividades " + "WHERE date(DataInicio) = date('now') " + "GROUP BY Nomejanela " + "ORDER BY TempoTotal DESC";

        try (Connection conn = DriverManager.getConnection("jdbc:sqlite:meu_tempo.db");
            PreparedStatement stmt = conn.prepareStatement(sql);
            ResultSet rs = stmt.executeQuery()){
            
            System.out.println("---RELATÓRIO HOJE---");

            Map<String, Integer> juntarTabsNumSó = new HashMap<>();
            
            while(rs.next()) {
                String nomeCompleto = rs.getString("NomeJanela");
                String nomeBonito = limparNome(nomeCompleto);
                int tempo = rs.getInt("TempoTotal");

                if(nomeBonito == null || nomeBonito.trim().isEmpty()) {
                    continue;
                }

                if(juntarTabsNumSó.containsKey(nomeBonito)) {
                    int tempoAntigo = juntarTabsNumSó.get(nomeBonito);
                    juntarTabsNumSó.put(nomeBonito, tempoAntigo + tempo);
                } else {
                    juntarTabsNumSó.put(nomeBonito, tempo);
                }             
            }

            for (String nomeApp : juntarTabsNumSó.keySet()) {
                int tempoFinal = juntarTabsNumSó.get(nomeApp);

                if (tempoFinal < 60) {
                    System.out.println(nomeApp + ": " + tempoFinal + " segundos.");
                } else {
                    int minutos = tempoFinal / 60;
                    int segundosResto = tempoFinal % 60;

                    if (minutos < 60) {
                        System.out.println(nomeApp + ": " + minutos + " minutos e " + segundosResto + " segundos.");
                    } else {
                        int horas = minutos / 60;
                        int minutosRestantes = minutos % 60;
                        
                        System.out.println(nomeApp + ": " + horas + " horas, " + minutosRestantes + " minutos e " + segundosResto + " segundos.");
                    }
                }
        }

        } catch (Exception e) {
            System.out.println("Erro no relatório: " + e.getMessage());
        }
    }

    public static void fazerRelatorioSemanal() {
        //sql que seleciona o nome da janela e soma toda a sua duração, só do próprio dia, e agrupa por nome e ordena por tempo
        String sql = "SELECT NomeJanela, SUM(duracao) as TempoTotal " + "FROM atividades " + "WHERE date(DataInicio) >= date('now', '-7 days') " + "GROUP BY Nomejanela " + "ORDER BY TempoTotal DESC";

        try (Connection conn = DriverManager.getConnection("jdbc:sqlite:meu_tempo.db");
            PreparedStatement stmt = conn.prepareStatement(sql);
            ResultSet rs = stmt.executeQuery()){
            
            System.out.println("---RELATÓRIO SEMANAL---");

            Map<String, Integer> juntarTabsNumSó = new HashMap<>();
            
            while(rs.next()) {
                String nomeCompleto = rs.getString("NomeJanela");
                String nomeBonito = limparNome(nomeCompleto);
                int tempo = rs.getInt("TempoTotal");

                if(nomeBonito == null || nomeBonito.trim().isEmpty()) {
                    continue;
                }

                if(juntarTabsNumSó.containsKey(nomeBonito)) {
                    int tempoAntigo = juntarTabsNumSó.get(nomeBonito);
                    juntarTabsNumSó.put(nomeBonito, tempoAntigo + tempo);
                } else {
                    juntarTabsNumSó.put(nomeBonito, tempo);
                }             
            }

            for (String nomeApp : juntarTabsNumSó.keySet()) {
                int tempoFinal = juntarTabsNumSó.get(nomeApp);

                if (tempoFinal < 60) {
                    System.out.println(nomeApp + ": " + tempoFinal + " segundos.");
                } else {
                    int minutos = tempoFinal / 60;
                    int segundosResto = tempoFinal % 60;

                    if (minutos < 60) {
                        System.out.println(nomeApp + ": " + minutos + " minutos e " + segundosResto + " segundos.");
                    } else {
                        int horas = minutos / 60;
                        int minutosRestantes = minutos % 60;
                        
                        System.out.println(nomeApp + ": " + horas + " horas, " + minutosRestantes + " minutos e " + segundosResto + " segundos.");
                    }
                }
        }

        } catch (Exception e) {
            System.out.println("Erro no relatório: " + e.getMessage());
        }
    }

    public static Map<String, Integer> getTempoPorDia() {
        int limiteAFK = 3600;
        String sql = "SELECT date(DataInicio) as Dia, SUM(Duracao) as TempoTotal " + "FROM atividades " + "WHERE date(DataInicio) >= date('now', '-7 days') " + "AND Duracao < " + limiteAFK + " " + "GROUP BY date(DataInicio) " + "ORDER BY Dia ASC";
        
        Map<String, Integer> mapaTempo = new HashMap<>();

        try (Connection conn = DriverManager.getConnection("jdbc:sqlite:meu_tempo.db");
         PreparedStatement stmt = conn.prepareStatement(sql);
         ResultSet rs = stmt.executeQuery()) {

        while (rs.next()) {
            String dia = rs.getString("Dia"); // Vem como "2025-12-10"
            int tempo = rs.getInt("TempoTotal");
            mapaTempo.put(dia, tempo);
        }

        } catch (Exception e) {
            System.out.println("Erro ao buscar dias: " + e.getMessage());
        }

        return mapaTempo;
    }
}
