package com.lsn.client.repository;

import com.github.tomakehurst.wiremock.WireMockServer;
import com.github.tomakehurst.wiremock.core.WireMockConfiguration;
import com.lsn.api.domain.Post;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.List;

import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.get;
import static com.github.tomakehurst.wiremock.client.WireMock.post;
import static com.github.tomakehurst.wiremock.client.WireMock.urlEqualTo;
import static org.assertj.core.api.Assertions.assertThat;

class HttpSocialNetworkRepositoryTest {

    private WireMockServer wireMock;
    private HttpSocialNetworkRepository repository;

    @BeforeEach
    void setUp() {
        wireMock = new WireMockServer(WireMockConfiguration.wireMockConfig().dynamicPort());
        wireMock.start();
        repository = new HttpSocialNetworkRepository("http://localhost:" + wireMock.port(), "test-token");
    }

    @AfterEach
    void tearDown() {
        wireMock.stop();
    }

    @Test
    void save_sendsPostRequest() {
        wireMock.stubFor(post(urlEqualTo("/posts"))
                .willReturn(aResponse().withStatus(201)));

        repository.save(new Post("alice", "hello", Instant.now()));

        wireMock.verify(1, com.github.tomakehurst.wiremock.client.WireMock.postRequestedFor(
                urlEqualTo("/posts")));
    }

    @Test
    void findTimelineByUsername_returnsParsedPosts() {
        wireMock.stubFor(get(urlEqualTo("/users/alice/posts"))
                .willReturn(aResponse()
                        .withStatus(200)
                        .withHeader("Content-Type", "application/json")
                        .withBody("[{\"content\":\"hello\",\"postedAt\":\"2024-01-01T10:00:00Z\"}]")));

        List<Post> posts = repository.findTimelineByUsername("alice");

        assertThat(posts).hasSize(1);
        assertThat(posts.get(0).content()).isEqualTo("hello");
        assertThat(posts.get(0).postedAt()).isEqualTo(Instant.parse("2024-01-01T10:00:00Z"));
    }

    @Test
    void follow_sendsPostRequest() {
        wireMock.stubFor(post(urlEqualTo("/following"))
                .willReturn(aResponse().withStatus(201)));

        repository.follow("alice", "bob");

        wireMock.verify(1, com.github.tomakehurst.wiremock.client.WireMock.postRequestedFor(
                urlEqualTo("/following")));
    }

    @Test
    void findWallByUsername_returnsParsedPosts() {
        wireMock.stubFor(get(urlEqualTo("/users/alice/wall"))
                .willReturn(aResponse()
                        .withStatus(200)
                        .withHeader("Content-Type", "application/json")
                        .withBody("[{\"username\":\"bob\",\"content\":\"hi\",\"postedAt\":\"2024-01-01T10:00:00Z\"}]")));

        List<Post> posts = repository.findWallByUsername("alice");

        assertThat(posts).hasSize(1);
        assertThat(posts.get(0).username()).isEqualTo("bob");
        assertThat(posts.get(0).content()).isEqualTo("hi");
    }
}
