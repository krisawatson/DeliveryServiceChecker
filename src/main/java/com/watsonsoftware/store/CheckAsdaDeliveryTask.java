package com.watsonsoftware.store;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.watsonsoftware.Logger;
import com.watsonsoftware.config.AsdaConfig;
import com.watsonsoftware.config.SlackConfig;
import com.watsonsoftware.model.AsdaDeliveryInformation;
import com.watsonsoftware.model.SlotInfo;
import com.watsonsoftware.model.asdaorder.*;
import com.watsonsoftware.slack.SlackUtils;

import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.*;
import java.util.*;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;

import static java.net.http.HttpRequest.BodyPublishers;
import static java.net.http.HttpResponse.BodyHandlers;

public class CheckAsdaDeliveryTask extends TimerTask {

    private static final Set<String> NOTIFIED_SLOTS = new HashSet<>();

    private static final AtomicBoolean ALREADY_PLACED_ORDER = new AtomicBoolean(false);

    private final HttpClient client;
    private final ObjectMapper objectMapper;
    private final SlackConfig slackConfig;
    private final AsdaConfig asdaConfig;

    private CheckAsdaDeliveryTask(SlackConfig slackConfig, AsdaConfig asdaConfig) {
        this.client = HttpClient.newHttpClient();
        this.objectMapper = new ObjectMapper();
        this.slackConfig = slackConfig;
        this.asdaConfig = asdaConfig;
    }

    public static CheckAsdaDeliveryTask create(SlackConfig slackConfig, AsdaConfig asdaConfig) {
        return new CheckAsdaDeliveryTask(slackConfig, asdaConfig);
    }

    @Override
    public void run() {
        Logger.info("Running request to check for available Asda slots");
        try {
            makeHttpRequest(Optional.empty(), false);
        } catch (Exception e) {
            Logger.info("ERROR - Unexpected exception " + e.getLocalizedMessage());
        }
    }

    private void makeHttpRequest(Optional<String> availableSlot, boolean isBookSlot) throws IOException, InterruptedException {
        HttpRequest request = getHttpRequest(availableSlot);
        HttpResponse<String> response = client.send(request, BodyHandlers.ofString());
        int statusCode = response.statusCode();
        if (statusCode != 200) {
            Logger.error("Request failed with error code " + statusCode);
        } else if (!isBookSlot){
            handleResponse(response);
        } else {
            Logger.info("Successfully booked an available slot");
        }
    }

    private HttpRequest getHttpRequest(Optional<String> availableSlot) throws IOException {
        Order order = buildAsdaOrder(asdaConfig, availableSlot);

        return HttpRequest.newBuilder()
                          .uri(URI.create(asdaConfig.getUrl()))
                          .timeout(Duration.ofMinutes(1))
                          .header("Content-Type", "application/json")
                          .POST(BodyPublishers.ofString(objectMapper.writeValueAsString(order)))
                          .build();
    }

    private String mapSlotInfo(SlotInfo slotInfo) {
        return "Between: "
                + slotInfo.getStartTime()
                + " and "
                + slotInfo.getEndTime() + "\n";
    }

    private void handleResponse(HttpResponse<String> response) throws IOException, InterruptedException {
        AsdaDeliveryInformation asdaDeliveryInformation =
                objectMapper.readValue(response.body(), AsdaDeliveryInformation.class);
        if (null != asdaDeliveryInformation.getData()) {
            Set<String> availableSlots = new TreeSet<>();
            AtomicReference<String> bookableSlot = new AtomicReference<>();
            asdaDeliveryInformation.getData().getSlotDays().forEach(slotDays ->
                    slotDays.getSlots().forEach(slot -> {
                        SlotInfo slotInfo = slot.getSlotInfo();
                        if (!"UNAVAILABLE".equals(slotInfo.getStatus())) {
                            Logger.info("Slot status is " + slot.getSlotInfo().getStatus());
                            String slots = mapSlotInfo(slot.getSlotInfo());
                            Logger.info("Found available slots " + slots);
                            availableSlots.add(slots);
                            if (asdaConfig.isAutoPlaceOrder()) {
                                bookableSlot.set(slot.getSlotInfo().getSlotId());
                            }
                        }
                    })
            );
            if (!availableSlots.isEmpty()) {
                availableSlots.removeAll(NOTIFIED_SLOTS);
                if (asdaConfig.isAutoPlaceOrder() && !ALREADY_PLACED_ORDER.get()) {
                    makeHttpRequest(Optional.of(bookableSlot.get()), true);
                    ALREADY_PLACED_ORDER.set(true);
                } else {
                    SlackUtils.sendNotificationSlackMessage(slackConfig, "Asda", availableSlots);
                }
                NOTIFIED_SLOTS.addAll(availableSlots);
            }
        } else {
            Logger.error("Failed to map response to Delivery Information");
            Logger.error("Response was " + response.body());
        }
    }

    private Order buildAsdaOrder(AsdaConfig asdaConfig, Optional<String> availableSlot) throws IOException {
        InputStream orderInfoIs = CheckAsdaDeliveryTask.class.getResourceAsStream("/AsdaOrderInfo.json");
        OrderInfo orderInfo = objectMapper.readValue(orderInfoIs, OrderInfo.class);
        OrderData data = buildAsdaOrderData(asdaConfig, orderInfo, availableSlot);
        return Order.builder().requestOrigin("gi").data(data).build();
    }

    private OrderData buildAsdaOrderData(AsdaConfig asdaConfig, OrderInfo orderInfo, Optional<String> availableSlot) {
        ZonedDateTime startDate = LocalDate.now().atStartOfDay().atZone(ZoneId.of("+01:00"));
        if (!asdaConfig.getSlotDate().isEmpty()) {
            startDate = LocalDate.parse(asdaConfig.getSlotDate()).atStartOfDay().atZone(ZoneId.of("+01:00"));
        }
        ZonedDateTime endDate = startDate.plusWeeks(2);
        Logger.info(String.format("Making request between %s and %s", startDate, endDate));
        ServiceInfo serviceInfo = buildServiceInfo();
        ServiceAddress serviceAddress = buildServiceAddress(asdaConfig.getPostcode().replaceAll(" ", ""));
        CustomerInfo customerInfo = buildCustomerInfo(asdaConfig.getAccountId());

        return OrderData.builder()
                        .serviceInfo(serviceInfo)
                        .startDate(startDate.toString())
                        .endDate(endDate.toString())
                        .reservedSlotId(availableSlot.orElse(""))
                        .serviceAddress(serviceAddress)
                        .customerInfo(customerInfo)
                        .orderInfo(orderInfo)
                        .build();
    }

    private ServiceInfo buildServiceInfo() {
        return ServiceInfo.builder().fulfillmentType("DELIVERY").build();
    }

    private ServiceAddress buildServiceAddress(String postcode) {
        return ServiceAddress.builder().postcode(postcode).build();
    }

    private CustomerInfo buildCustomerInfo(String accountId) {
        return CustomerInfo.builder().accountId(accountId).build();
    }
}
