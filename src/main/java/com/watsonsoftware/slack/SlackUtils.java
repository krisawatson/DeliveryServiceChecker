package com.watsonsoftware.slack;

import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;

public class SlackUtils {
    private static final String WEBHOOK_URL = "https://hooks.slack.com/services/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";

    public static void sendMessage(SlackMessage message) {
        HttpClient client = HttpClient.newHttpClient();
        try {
            HttpRequest request = getHttpRequest(message);
            client.send(request, HttpResponse.BodyHandlers.ofString());
        } catch (IOException | InterruptedException e) {
            System.err.println("Failed to send slack message");
        }
    }

    private static HttpRequest getHttpRequest(SlackMessage message) throws IOException {
        ObjectMapper objectMapper = new ObjectMapper();
        String json = objectMapper.writeValueAsString(message);

        return HttpRequest.newBuilder()
                .uri(URI.create(WEBHOOK_URL))
                .timeout(Duration.ofMinutes(1))
                .header("Accept", "application/json")
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(json))
                .build();
    }
}
