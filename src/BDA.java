package src;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.Statement;

public class BDA {
    public static void ligarEConfirmarBDA() {
        String url = "jdbc:sqlite:meu_tempo.db";

        try {
            Class.forName("org.sqlite.JDBC");
            Connection conn = DriverManager.getConnection(url);
            System.out.println("Conexão OK!");
        } catch (Exception e) {
            System.out.println("Não criou BD" + e.getMessage());
            e.printStackTrace();
        }
    }
}
