package utils;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Properties;

public class ConfigUtils {
    private static final String CONFIG_FILE = "config.properties";

    public static int carregarObjetivo() {
        Properties props = new Properties();
        try (FileInputStream in = new FileInputStream(CONFIG_FILE)){
            props.load(in);
            return Integer.parseInt(props.getProperty("meta_diaria", "5"));
        } catch (Exception e) {
            return 5;
        }
    }

    public static void salvarObjetivo(int novoValor) {
        Properties props = new Properties();
        try (FileInputStream in = new FileInputStream(CONFIG_FILE)){
            props.load(in);
        } catch (IOException ignored) {}

        props.setProperty("meta_diaria", String.valueOf(novoValor));

        try (FileOutputStream out = new FileOutputStream(CONFIG_FILE)){
            props.store(out, "Configurações do Time Tracker");
        } catch (IOException e) {
            System.out.println("Erro ao guardar objetivo: " + e.getMessage());
        }
    }
}