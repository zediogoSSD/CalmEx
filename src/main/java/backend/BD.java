package backend;

import java.io.File;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.Statement;

public class BD {

    // 1. O caminho seguro para a pasta do utilizador (Ex: C:\Users\zedio\.timetracker\)
    public static final String PASTA_SISTEMA = System.getProperty("user.home") + "/.timetracker/";

    // 2. O link absoluto para a base de dados
    public static final String URL_BD = "jdbc:sqlite:" + PASTA_SISTEMA + "meu_tempo.db";

    public static void ligarEConfirmarBD() {
        // Garante que a pasta existe antes de tentar criar a base de dados
        new File(PASTA_SISTEMA).mkdirs();

        // 3. FALTAVA O 'CaminhoExecutavel' AQUI!
        String sql = "CREATE TABLE IF NOT EXISTS atividades (" +
                "id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                "NomeJanela TEXT, " +
                "CaminhoExecutavel TEXT, " +
                "DataInicio TEXT, " +
                "DataFim TEXT, " +
                "Duracao INTEGER);";

        try {
            Class.forName("org.sqlite.JDBC");
            Connection conn = DriverManager.getConnection(URL_BD);
            System.out.println("Conexão OK! A usar BD em: " + PASTA_SISTEMA);

            Statement stmt = conn.createStatement();
            stmt.execute(sql);

        } catch (Exception e) {
            System.out.println("Não criou BD: " + e.getMessage());
            e.printStackTrace();
        }
    }

    public static void salvarAtividade(Atividade ativ) {
        String sql = "INSERT INTO atividades(NomeJanela, DataInicio, DataFim, Duracao, CaminhoExecutavel) VALUES(?,?,?,?,?)";

        // Usar a URL_BD global
        try (Connection conn = DriverManager.getConnection(URL_BD);
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, ativ.janelaName);
            pstmt.setString(2, ativ.horaInicio.toString());
            pstmt.setString(3, ativ.horaFim.toString());
            pstmt.setLong(4, ativ.duracaoSegundos());
            pstmt.setString(5, ativ.caminhoExecutavel == null ? "" : ativ.caminhoExecutavel);

            pstmt.executeUpdate();
            System.out.println("Dados guardados na BD!");

        } catch (Exception e) {
            System.out.println("Erro ao salvar: " + e.getMessage());
        }
    }

    public static void limpezaMensal() {
        String sql = "DELETE FROM atividades WHERE date(DataInicio) < date('now', '-28 days')";

        // Usar a URL_BD global
        try (Connection conn = DriverManager.getConnection(URL_BD);
             Statement stmt = conn.createStatement()) {

            stmt.executeUpdate(sql);

        } catch (Exception e) {
            System.out.println("Erro na limpeza: " + e.getMessage());
        }
    }
}