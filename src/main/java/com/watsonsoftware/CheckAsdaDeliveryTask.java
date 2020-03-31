package com.watsonsoftware;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.watsonsoftware.model.AsdaDeliveryInformation;
import com.watsonsoftware.model.SlotInfo;
import com.watsonsoftware.slack.SlackMessage;
import com.watsonsoftware.slack.SlackUtils;
import lombok.extern.slf4j.Slf4j;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.Duration;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.TimerTask;
import java.util.stream.Collectors;

import static java.net.http.HttpRequest.BodyPublishers;
import static java.net.http.HttpResponse.BodyHandlers;

@Slf4j
public class CheckAsdaDeliveryTask extends TimerTask {

    private static final String ASDA_DELIVERY_URL = "https://groceries.asda.com/api/v3/slot/view";
    private final HttpClient client;
    private final ObjectMapper objectMapper;

    CheckAsdaDeliveryTask() {
        this.client = HttpClient.newHttpClient();
        this.objectMapper = new ObjectMapper();
    }

    @Override
    public void run() {
        log.info("Running request to check for available slots");
        try {
            HttpRequest request = getHttpRequest();
            HttpResponse<String> response = client.send(request, BodyHandlers.ofString());
            int statusCode = response.statusCode();
            if (statusCode != 200) {
                log.error("Request failed with error code {}", statusCode);
            } else {
                handleResponse(response);
            }
        } catch (Exception e) {
            System.out.println("ERROR - Unexpected exception " + e.getLocalizedMessage());
            log.error("ERROR - unexpected exception ", e);
        }
    }

    private HttpRequest getHttpRequest() {
        InputStream is = CheckAsdaDeliveryTask.class.getResourceAsStream("/AsdaOrder.json");
        String source = new BufferedReader(new InputStreamReader(is)).lines().collect(Collectors.joining("\n"));
        ZonedDateTime startDate = LocalDate.now().atStartOfDay().atZone(ZoneId.of("+01:00"));
        ZonedDateTime endDate = startDate.plusMonths(1);
        source = source.replaceAll("_START_DATE", startDate.toString())
                       .replaceAll("_END_DATE", endDate.toString());

        log.info("Making request between {} and {}", startDate, endDate);
        return HttpRequest.newBuilder()
                          .uri(URI.create(ASDA_DELIVERY_URL))
                          .timeout(Duration.ofMinutes(1))
                          .header("Content-Type", "application/json")
                          .POST(BodyPublishers.ofString(source))
                          .build();
    }

    private String mapSlotInfo(SlotInfo slotInfo) {
        return "Between: "
                + slotInfo.getStartTime()
                + " and "
                + slotInfo.getEndTime() + "\n";
    }

    private void sendSlackMessage(List<String> availableSlots) {
        log.info("Sending message with available slots {}", availableSlots);
        String slotDetails = String.join("\n", availableSlots);
        SlackMessage message = SlackMessage.builder()
                                           .channel("random")
                                           .username("Delivery Bot")
                                           .text("<@UDKQ8R6H1> There are available delivery slots for Asda:\n" + slotDetails)
                                           .icon_emoji(":twice:")
                                           .build();
        SlackUtils.sendMessage(message);
    }

    private void handleResponse(HttpResponse<String> response) throws IOException {
        AsdaDeliveryInformation asdaDeliveryInformation =
                objectMapper.readValue(response.body(), AsdaDeliveryInformation.class);
        if (null != asdaDeliveryInformation.getData()) {
            List<String> availableSlots = new ArrayList<>();
            asdaDeliveryInformation.getData().getSlotDays().forEach(slotDays ->
                    slotDays.getSlots().forEach(slot -> {
                        if (!"UNAVAILABLE".equals(slot.getSlotInfo().getStatus())) {
                            log.info("Slot status is {}", slot.getSlotInfo().getStatus());
                            String slots = mapSlotInfo(slot.getSlotInfo());
                            log.info("Found available slots {}", slots);
                            availableSlots.add(slots);
                        }
                    })
            );
            if (!availableSlots.isEmpty()) {
                sendSlackMessage(availableSlots);
            }
        } else {
            log.error("Failed to map response to Delivery Information");
            log.error("Response was {}", response.body());
        }
    }
}
