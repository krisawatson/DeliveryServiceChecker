package com.watsonsoftware.slack;

import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.Base64;

public class SlackUtils {
    private static final String WEBHOOK_URL = "aHR0cHM6Ly9ob29rcy5zbGFjay5jb20vc2VydmljZXMvVERLVDI0NkpXL0IwMTE1SEhLVVQwL1JuZzAzMWZvelBpNHVxV1pnZ0RqclhZUQo=";

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
                .uri(URI.create(new String(Base64.getDecoder().decode(WEBHOOK_URL)).trim()))
                .timeout(Duration.ofMinutes(1))
                .header("Accept", "application/json")
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(json))
                .build();
    }
}
