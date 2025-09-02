package it.progetto.progetto18.core;

import java.time.LocalTime;
import java.time.format.DateTimeFormatter;

public class TimeUtils {

    private static final DateTimeFormatter HHMMSS = DateTimeFormatter.ofPattern("HH:mm:ss");

    public static String formatSecondsToTime(int totalSeconds) {
        int hours = (totalSeconds / 3600) % 24;
        int minutes = (totalSeconds % 3600) / 60;
        int seconds = totalSeconds % 60;
        return String.format("%02d:%02d:%02d", hours, minutes, seconds);
    }

    public static String getReadableArrivalTime(int arrivalSeconds, int nowSeconds) {
        int diff = arrivalSeconds - nowSeconds;

        if (diff < 0) {
            return "passato alle " + formatSecondsToTime(arrivalSeconds);
        }

        int minutes = diff / 60;
        int hours = minutes / 60;

        if (minutes < 1) {
            return "ora";
        } else if (minutes < 60) {
            return "tra " + minutes + " min";
        } else if (hours < 24) {
            return "tra " + hours + "h " + (minutes % 60) + "min";
        } else {
            int days = hours / 24;
            String time = formatSecondsToTime(arrivalSeconds);
            return (days == 1 ? "domani " : "+" + days + "g ") + time;
        }
    }
}
