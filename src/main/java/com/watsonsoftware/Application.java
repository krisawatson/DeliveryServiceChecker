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
        scheduler.scheduleAtFixedRate(new CheckAsdaDeliveryTask(), 0, 2, MINUTES);
    }
}
