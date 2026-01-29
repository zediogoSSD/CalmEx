package utils;

public class TimeUtils {
    public static String formatarTempo(int tempo) {
        int horas = tempo / 3600;
        int minutos = (tempo % 3600) / 60;
        int segundos = tempo % 60;

        if (horas > 0) {
            return horas + "h " + minutos + "m";
        } else if (minutos > 0) {
            return minutos + "m " + segundos + "s";
        } else {
            return segundos + "s";
        }
    }
}