package com.lsn.client.repository;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.lsn.api.domain.Post;
import com.lsn.api.repository.SocialNetworkRepository;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Instant;
import java.util.List;
import java.util.Map;

public class HttpSocialNetworkRepository implements SocialNetworkRepository {

    private static final String CONTENT_TYPE  = "Content-Type";
    private static final String AUTHORIZATION = "Authorization";
    private static final String APPLICATION_JSON = "application/json";

    private final String token;
    private final String serverUrl;
    private final HttpClient httpClient;
    private final ObjectMapper objectMapper;

    public HttpSocialNetworkRepository(String serverUrl, String token) {
        this.token = token;
        this.serverUrl = serverUrl;
        this.httpClient = HttpClient.newHttpClient();
        this.objectMapper = new ObjectMapper().registerModule(new JavaTimeModule());
    }

    @Override
    public void save(Post post) {

        try {

            String body = objectMapper.writeValueAsString(Map.of("content", post.content()));

            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(serverUrl + "/posts"))
                    .header(CONTENT_TYPE, APPLICATION_JSON)
                    .header(AUTHORIZATION, "Bearer " + token)
                    .POST(HttpRequest.BodyPublishers.ofString(body))
                    .build();

            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());

            if (response.statusCode() != 201) {
                System.err.println("Post failed: " + response.body());
            }
        } catch (IOException | InterruptedException e) {
            Thread.currentThread().interrupt();
            System.err.println("Post error: " + e.getMessage());
        }
    }

    @Override
    public List<Post> findTimelineByUsername(String username) {

        try {

            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(serverUrl + "/users/" + username + "/posts"))
                    .header(AUTHORIZATION, "Bearer " + token)
                    .GET()
                    .build();

            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
            List<Map<String, Object>> raw = objectMapper.readValue(response.body(), new TypeReference<>() {});

            return raw.stream().map(this::toPost).toList();
        } catch (IOException | InterruptedException e) {
            Thread.currentThread().interrupt();
            System.err.println("Read error: " + e.getMessage());
            return List.of();
        }
    }

    @Override
    public void follow(String follower, String followee) {

        try {

            String body = objectMapper.writeValueAsString(Map.of("target", followee));

            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(serverUrl + "/following"))
                    .header(CONTENT_TYPE, APPLICATION_JSON)
                    .header(AUTHORIZATION, "Bearer " + token)
                    .POST(HttpRequest.BodyPublishers.ofString(body))
                    .build();

            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
            if (response.statusCode() != 201) {
                System.err.println("Follow failed: " + response.body());
            }
        } catch (IOException | InterruptedException e) {
            Thread.currentThread().interrupt();
            System.err.println("Follow error: " + e.getMessage());
        }
    }

    @Override
    public List<Post> findWallByUsername(String username) {

        try {

            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(serverUrl + "/users/" + username + "/wall"))
                    .header(AUTHORIZATION, "Bearer " + token)
                    .GET()
                    .build();

            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
            List<Map<String, Object>> raw = objectMapper.readValue(response.body(), new TypeReference<>() {});
            return raw.stream().map(this::toWallPost).toList();
        } catch (IOException | InterruptedException e) {
            Thread.currentThread().interrupt();
            System.err.println("Wall error: " + e.getMessage());
            return List.of();
        }
    }

    private Post toPost(Map<String, Object> map) {
        String content  = (String) map.get("content");
        Instant postedAt = Instant.parse((String) map.get("postedAt"));
        return new Post("", content, postedAt);
    }

    private Post toWallPost(Map<String, Object> map) {
        String username = (String) map.get("username");
        String content  = (String) map.get("content");
        Instant postedAt = Instant.parse((String) map.get("postedAt"));
        return new Post(username, content, postedAt);
    }
}
