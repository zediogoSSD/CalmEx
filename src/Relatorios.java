package src;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.LinkedHashMap;
import java.util.List;

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
                String dia = rs.getString("Dia");
                int tempo = rs.getInt("TempoTotal");
                mapaTempo.put(dia, tempo);
            }

        } catch (Exception e) {
            System.out.println("Erro ao buscar dias: " + e.getMessage());
        }

        return mapaTempo;
    }

    public static Map<String, Integer> getAppsMaisUsadasPorDia() {
        int limiteAFK = 3600; // 1 hora de limite para não contar AFK
    
        String sql = "SELECT NomeJanela, SUM(Duracao) as TempoTotal " + "FROM atividades " + "WHERE date(DataInicio) = date('now') " + "AND Duracao < " + limiteAFK + " " + "GROUP BY NomeJanela " + "ORDER BY TempoTotal DESC " + "LIMIT 15";

        //Usamos LinkedHashMap para ter ordem
        Map<String, Integer> topApps = new LinkedHashMap<>();

        try (Connection conn = DriverManager.getConnection("jdbc:sqlite:meu_tempo.db");
            PreparedStatement stmt = conn.prepareStatement(sql);
            ResultSet rs = stmt.executeQuery()) {

            while (rs.next()) {
                String nome = rs.getString("NomeJanela");
                int tempo = rs.getInt("TempoTotal");
                topApps.put(nome, tempo);
            }

        } catch (Exception e) {
            System.out.println("Erro ao buscar Top Apps: " + e.getMessage());
        }
    
        return topApps;
    }

    public static class DadosApp {
        public String nome; 
        public int tempo;
        public String caminho;

        public DadosApp(String nome, int tempo, String caminho) {
            this.nome = nome;
            this.tempo = tempo;
            this.caminho = caminho;
        }

        public static List<DadosApp> getTopApps() {

            String sql = "SELECT NomeJanela, SUM(Duracao) as TempoTotal, MAX(CaminhoExecutavel) as Caminho " + "FROM atividades " + "WHERE date(DataInicio) = date('now') " + "GROUP BY NomeJanela";
            Map<String, DadosApp> mapaAgrupado = new HashMap<>();

            try (Connection conn = DriverManager.getConnection("jdbc:sqlite:meu_tempo.db");
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {
                
                while (rs.next()) {
                    String nomeCru = rs.getString("NomeJanela");
                    int tempoLido = rs.getInt("TempoTotal");
                    String caminhoLido = rs.getString("Caminho");

                    String nomeLimpo = limparNome(nomeCru);
                    if(nomeLimpo == null || nomeLimpo.trim().isEmpty()) {
                        continue;
                    }

                    //Acontece quando a app já está na BD, e precisa de atualizar o tempo dessa app
                    if(mapaAgrupado.containsKey(nomeLimpo)) {
                        //dado registado no mapa
                        DadosApp appExistente = mapaAgrupado.get(nomeLimpo);

                        //Somar o tempo novo ao que ele já tinha
                        appExistente.tempo += tempoLido;

                        //Verifica se o icone precisa de ser atualizado, pôr icon nas aplicações que estavam guardadas sem icon
                        if(appExistente.caminho == null || appExistente.caminho.isEmpty()) {
                            appExistente.caminho = caminhoLido;
                        }

                    //else = situação em que é a primeira vez que usamos uma app, criamos um novo DadosApp e metemos no map
                    } else {
                        DadosApp appNova = new DadosApp(nomeLimpo, tempoLido, caminhoLido);
                        mapaAgrupado.put(nomeLimpo, appNova);
                    }
                }

            } catch (Exception e) {
                System.out.println("Erro no top 5: " + e.getMessage());
            }


            //Ordenar e cortar a lista
            List<DadosApp> listaFinal = new ArrayList<>(mapaAgrupado.values());
            
            // IMPORTANTE: Isto ordena do MAIOR para o MENOR
            listaFinal.sort((app1, app2) -> Integer.compare(app2.tempo, app1.tempo));

            // Retorna apenas as top 5
            if(listaFinal.size() > 5) {
                return listaFinal.subList(0, 5);
            }
            return listaFinal;
        }
    }
}
