package com.watsonsoftware.slack;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.watsonsoftware.Logger;
import com.watsonsoftware.config.SlackConfig;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.Base64;
import java.util.List;
import java.util.Set;

public class SlackUtils {

    public static void sendNotificationSlackMessage(SlackConfig slackConfig, String store, Set<String> availableSlots) {
        Logger.info("Sending message with available slots " + availableSlots);
        String slotDetails = String.join("\n", availableSlots);
        String notifyUsers = String.join(" ", slackConfig.getNotify());
        String messageText = String.format("%s There are available delivery slots for %s\n%s", notifyUsers, store, slotDetails);
        SlackMessage message = SlackMessage.builder()
                                           .channel(slackConfig.getChannel())
                                           .username(slackConfig.getUsername())
                                           .text(messageText)
                                           .build();
        SlackUtils.sendMessage(slackConfig.getWebhook(), message);
    }

    private static void sendMessage(String webhook, SlackMessage message) {
        HttpClient client = HttpClient.newHttpClient();
        try {
            HttpRequest request = getHttpRequest(webhook, message);
            client.send(request, HttpResponse.BodyHandlers.ofString());
        } catch (IOException | InterruptedException e) {
            Logger.warn("Failed to send slack message");
        }
    }

    private static HttpRequest getHttpRequest(String webhook, SlackMessage message) throws IOException {
        ObjectMapper objectMapper = new ObjectMapper();
        String json = objectMapper.writeValueAsString(message);
        return HttpRequest.newBuilder()
                .uri(URI.create(webhook))
                .timeout(Duration.ofMinutes(1))
                .header("Accept", "application/json")
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(json))
                .build();
    }
}
