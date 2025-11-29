package src;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.Statement;

public class BD {
    public static void ligarEConfirmarBD() {
        String url = "jdbc:sqlite:meu_tempo.db";
        String sql = "CREATE TABLE IF NOT EXISTS atividades (" + "id INTEGER PRIMARY KEY AUTOINCREMENT, " + "NomeJanela TEXT, DataInicio TEXT, DataFim TEXT, Duracao INTEGER" + ");";

        try {
            Class.forName("org.sqlite.JDBC");
            Connection conn = DriverManager.getConnection(url);
            System.out.println("Conexão OK!");

            Statement stmt = conn.createStatement();
            stmt.execute(sql);
            
        } catch (Exception e) {
            System.out.println("Não criou BD" + e.getMessage());
            e.printStackTrace();
        }
    }

    public static void salvarAtividade(Atividade ativ) {
    
        String sql = "INSERT INTO atividades(NomeJanela, DataInicio, DataFim, Duracao) VALUES(?,?,?,?)";

        try (Connection conn = DriverManager.getConnection("jdbc:sqlite:meu_tempo.db");
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
        
            // 1. Nome da Janela
            pstmt.setString(1, ativ.janelaName);

            // 2. Data Inicio
            pstmt.setString(2, ativ.horaInicio.toString());

            // 3. Data Fim
            pstmt.setString(3, ativ.horaFim.toString());

            // 4. Duração
            pstmt.setLong(4, ativ.duracaoSegundos());

            pstmt.executeUpdate();
            System.out.println("Dados guardados na BD!");

        } catch (Exception e) {
            System.out.println("Erro ao salvar: " + e.getMessage());
        }
    }
}
