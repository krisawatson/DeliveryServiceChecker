package com.watsonsoftware;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import com.watsonsoftware.config.AsdaConfig;
import com.watsonsoftware.config.Configuration;
import com.watsonsoftware.config.IcelandConfig;
import com.watsonsoftware.store.CheckAsdaDeliveryTask;
import com.watsonsoftware.store.CheckIcelandDeliveryTask;

import java.io.IOException;
import java.io.InputStream;
import java.util.Timer;

public class Application {

    private static final long TIMER_MIN = 60 * 1000;
    private static final ObjectMapper objectMapper = new ObjectMapper(new YAMLFactory());

    public static void main(String[] args) {
        Logger.info("Starting application");

        InputStream is = CheckAsdaDeliveryTask.class.getResourceAsStream("/configuration.yaml");
        Configuration config = null;
        try {
            objectMapper.findAndRegisterModules();
            config = objectMapper.readValue(is, Configuration.class);
        } catch (IOException e) {
            System.out.println("Failed to load configuration");
            System.exit(-1);
        }
        try {
            AsdaConfig asdaConfig = config.getAsda();
            if (asdaConfig.isOn()) {
                CheckAsdaDeliveryTask asdaTask = CheckAsdaDeliveryTask.create(config.getSlack(), config.getAsda());
                new Timer().scheduleAtFixedRate(asdaTask, 0, asdaConfig.getRate() * TIMER_MIN);
            }

            IcelandConfig icelandConfig = config.getIceland();
            if (icelandConfig.isOn()) {
                CheckIcelandDeliveryTask icelandTask =
                        CheckIcelandDeliveryTask.create(config.getSlack(), config.getIceland());
                new Timer().scheduleAtFixedRate(icelandTask, 0, icelandConfig.getRate() * TIMER_MIN);
            }
        } catch (Exception e) {
            Logger.error("Error while running scheduled task " + e);
        }
    }
}
