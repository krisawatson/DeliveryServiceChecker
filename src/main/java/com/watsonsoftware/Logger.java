package com.watsonsoftware;

import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;

import static java.lang.Thread.currentThread;

public class Logger {

    private static final DateTimeFormatter DATE_TIME_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")
                                                                                  .withZone(ZoneId.systemDefault());
    private static final String LOG_FORMAT = "%s [%s] %s - %s";

    public static void info(String message) {
        String timestamp = DATE_TIME_FORMATTER.format(Instant.now());
        System.out.println(String.format(LOG_FORMAT, timestamp, currentThread().getName(), "INFO", message));
    }

    public static void error(String message) {
        String timestamp = DATE_TIME_FORMATTER.format(Instant.now());
        System.err.println(String.format(LOG_FORMAT, timestamp, currentThread().getName(), "ERROR", message));
    }

    public static void warn(String message) {
        String timestamp = DATE_TIME_FORMATTER.format(Instant.now());
        System.out.println(String.format(LOG_FORMAT, timestamp, currentThread().getName(), "WARN", message));
    }
}
