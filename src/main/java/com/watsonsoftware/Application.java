package com.watsonsoftware;

import lombok.extern.slf4j.Slf4j;

import java.util.Timer;

@Slf4j
public class Application {

    private static final int DEFAULT_RATE = 2;
    private static final long MINUTE = 60 * 1000;

    public static void main(String[] args) {
        log.info("Starting application");
        int rate = DEFAULT_RATE;
        if (args.length > 0) {
            log.info("Args are " + String.join(", ", args));
            rate = Integer.parseInt(args[0]);
        }
        log.info("Time is set to " + rate);
        CheckAsdaDeliveryTask task = new CheckAsdaDeliveryTask();
        try {
            new Timer().scheduleAtFixedRate(task, 0, rate * MINUTE);
        } catch (Exception e) {
            log.error("Error while running scheduled task", e);
        }
    }
}
