package com.watsonsoftware;

import lombok.extern.slf4j.Slf4j;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;

import static java.util.concurrent.TimeUnit.MINUTES;

@Slf4j
public class Application {

    public static void main(String[] args) {
        log.info("Starting application");
        ScheduledExecutorService scheduler = Executors.newSingleThreadScheduledExecutor();
        CheckAsdaDeliveryTask task = new CheckAsdaDeliveryTask();
        try {
            scheduler.scheduleAtFixedRate(task, 0, 2, MINUTES);
        } catch (Exception e) {
            log.error("Error while running scheduled task", e);
        }
    }
}
