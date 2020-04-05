package com.watsonsoftware;

import java.util.Timer;

public class Application {

    private static final long TIMER_RATE = 60 * 1000;

    public static void main(String[] args) {
        Logger.info("Starting application");
        CheckAsdaDeliveryTask task = new CheckAsdaDeliveryTask();
        CheckIcelandDeliveryTask icelandTask = new CheckIcelandDeliveryTask();
        try {
            new Timer().scheduleAtFixedRate(task, 0, TIMER_RATE);
            new Timer().scheduleAtFixedRate(icelandTask, 0, TIMER_RATE);
        } catch (Exception e) {
            Logger.error("Error while running scheduled task " + e);
        }
    }
}
