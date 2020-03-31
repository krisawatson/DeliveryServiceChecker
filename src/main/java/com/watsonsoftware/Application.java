package com.watsonsoftware;

import lombok.extern.slf4j.Slf4j;

import java.util.Timer;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;

import static java.util.concurrent.TimeUnit.MINUTES;

@Slf4j
public class Application {

    private static final long TIMER_RATE = 2 * 60 * 1000;

    public static void main(String[] args) {
        log.info("Starting application");
        CheckAsdaDeliveryTask task = new CheckAsdaDeliveryTask();
        try {
            new Timer().scheduleAtFixedRate(task, 0, TIMER_RATE);
        } catch (Exception e) {
            log.error("Error while running scheduled task", e);
        }
    }
}
