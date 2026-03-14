package com.lsn.client;

import com.lsn.api.domain.Post;
import com.lsn.client.repository.InMemoryRepository;
import com.lsn.client.service.SocialNetworkService;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;
import java.time.temporal.ChronoUnit;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class SocialNetworkServiceTest {

    private static final Instant FIXED_NOW = Instant.parse("2026-03-11T12:00:00Z");
    private static final Clock  FIXED_CLOCK = Clock.fixed(FIXED_NOW, ZoneOffset.UTC);

    private SocialNetworkService service;
    private InMemoryRepository repository;

    @BeforeEach
    void setUp() {
        repository = new InMemoryRepository();
        service    = new SocialNetworkService(repository, FIXED_CLOCK);
    }

    @Test
    void postThenReadReturnsSingleFormattedMessage() {
        repository.save(new Post("alice", "hello", FIXED_NOW.minus(1, ChronoUnit.MINUTES)));

        List<String> timeline = service.read("alice");

        assertThat(timeline).containsExactly("hello (1 minute ago)");
    }

    @Test
    void readUnknownUserReturnsEmptyList() {
        List<String> timeline = service.read("unknown");

        assertThat(timeline).isEmpty();
    }

    @Test
    void readReturnsMultiplePostsNewestFirst() {
        repository.save(new Post("alice", "hello", FIXED_NOW.minus(2, ChronoUnit.MINUTES)));
        repository.save(new Post("alice", "world", FIXED_NOW.minus(1, ChronoUnit.MINUTES)));

        List<String> timeline = service.read("alice");

        assertThat(timeline).containsExactly(
                "world (1 minute ago)",
                "hello (2 minutes ago)"
        );
    }

    @Test
    void wallWithNoFollowsReturnsOwnPosts() {
        repository.save(new Post("alice", "hello", FIXED_NOW.minus(1, ChronoUnit.MINUTES)));

        List<String> wall = service.wall("alice");

        assertThat(wall).containsExactly("alice - hello (1 minute ago)");
    }

    @Test
    void wallAfterFollowingReturnsOwnAndFolloweePosts() {
        repository.save(new Post("alice", "hello", FIXED_NOW.minus(2, ChronoUnit.MINUTES)));
        repository.save(new Post("bob",   "world", FIXED_NOW.minus(1, ChronoUnit.MINUTES)));
        service.follow("alice", "bob");

        List<String> wall = service.wall("alice");

        assertThat(wall).containsExactly(
                "bob - world (1 minute ago)",
                "alice - hello (2 minutes ago)"
        );
    }

    @Test
    void wallAfterFollowingMultipleUsersReturnsAllPostsNewestFirst() {

        repository.save(new Post("alice",   "alice post",   FIXED_NOW.minus(3, ChronoUnit.MINUTES)));
        repository.save(new Post("bob",     "bob post",     FIXED_NOW.minus(2, ChronoUnit.MINUTES)));
        repository.save(new Post("charlie", "charlie post", FIXED_NOW.minus(1, ChronoUnit.MINUTES)));

        service.follow("alice", "bob");
        service.follow("alice", "charlie");

        List<String> wall = service.wall("alice");

        assertThat(wall).containsExactly(
                "charlie - charlie post (1 minute ago)",
                "bob - bob post (2 minutes ago)",
                "alice - alice post (3 minutes ago)"
        );
    }

    @Test
    void readReturnsEmptyListForUserWithNoPosts() {

        service.follow("alice", "bob");

        repository.save(new Post("bob", "bob post", FIXED_NOW.minus(1, ChronoUnit.MINUTES)));

        List<String> timeline = service.read("alice");

        assertThat(timeline).isEmpty();
    }

    @Test
    void wallReturnsEmptyListForUserWithNoPostsAndNoFollows() {
        List<String> wall = service.wall("alice");
        assertThat(wall).isEmpty();
    }
}
