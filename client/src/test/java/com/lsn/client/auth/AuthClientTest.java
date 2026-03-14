package com.lsn.client.auth;

import com.github.tomakehurst.wiremock.WireMockServer;
import com.github.tomakehurst.wiremock.core.WireMockConfiguration;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.post;
import static com.github.tomakehurst.wiremock.client.WireMock.urlEqualTo;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatException;
import static org.assertj.core.api.Assertions.assertThatNoException;

class AuthClientTest {

    private WireMockServer wireMock;
    private AuthClient authClient;

    @BeforeEach
    void setUp() {
        wireMock = new WireMockServer(WireMockConfiguration.wireMockConfig().dynamicPort());
        wireMock.start();
        authClient = new AuthClient();
    }

    @AfterEach
    void tearDown() {
        wireMock.stop();
    }

    @Test
    void register_success_doesNotThrow() {
        wireMock.stubFor(post(urlEqualTo("/auth/register"))
                .willReturn(aResponse().withStatus(201)));

        assertThatNoException().isThrownBy(() ->
                authClient.register(serverUrl(), "alice", "password"));
    }

    @Test
    void register_conflict_throwsException() {
        wireMock.stubFor(post(urlEqualTo("/auth/register"))
                .willReturn(aResponse().withStatus(409).withBody("Username already taken")));

        assertThatException().isThrownBy(() ->
                authClient.register(serverUrl(), "alice", "password"));
    }

    @Test
    void login_success_returnsToken() throws Exception {
        wireMock.stubFor(post(urlEqualTo("/auth/login"))
                .willReturn(aResponse()
                        .withStatus(200)
                        .withHeader("Content-Type", "application/json")
                        .withBody("{\"token\":\"test-jwt-token\"}")));

        String result = authClient.login(serverUrl(), "alice", "password");

        assertThat(result).isEqualTo("test-jwt-token");
    }

    @Test
    void login_unauthorized_throwsException() {
        wireMock.stubFor(post(urlEqualTo("/auth/login"))
                .willReturn(aResponse().withStatus(401).withBody("Invalid credentials")));

        assertThatException().isThrownBy(() ->
                authClient.login(serverUrl(), "alice", "wrongpassword"));
    }

    private String serverUrl() {
        return "http://localhost:" + wireMock.port();
    }
}
