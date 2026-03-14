package com.lsn.client.auth;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.Map;

public class AuthClient {

    private static final String CONTENT_TYPE = "Content-Type";
    private static final String APPLICATION_JSON = "application/json";

    private final ObjectMapper objectMapper = new ObjectMapper();
    private final HttpClient httpClient = HttpClient.newHttpClient();

    public void register(String serverUrl, String username, String password)
            throws IOException, InterruptedException {

        String body = objectMapper.writeValueAsString(Map.of("username", username, "password", password));

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(serverUrl + "/auth/register"))
                .header(CONTENT_TYPE, APPLICATION_JSON)
                .POST(HttpRequest.BodyPublishers.ofString(body))
                .build();

        HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());

        if (response.statusCode() != 201) {
            throw new RuntimeException("Registration failed (" + response.statusCode() + "): " + response.body());
        }
    }

    public String login(String serverUrl, String username, String password)
            throws IOException, InterruptedException {

        String body = objectMapper.writeValueAsString(Map.of("username", username, "password", password));

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(serverUrl + "/auth/login"))
                .header(CONTENT_TYPE, APPLICATION_JSON)
                .POST(HttpRequest.BodyPublishers.ofString(body))
                .build();

        HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());

        if (response.statusCode() != 200) {
            throw new RuntimeException("Login failed (" + response.statusCode() + "): " + response.body());
        }

        JsonNode json = objectMapper.readTree(response.body());
        return json.get("token").asText();
    }
}
