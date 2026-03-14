package com.lsn.server.repository;

import com.lsn.api.domain.Post;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.data.mongo.DataMongoTest;
import org.springframework.context.annotation.Import;

import java.time.Instant;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@DataMongoTest
@Import(MongoSocialNetworkRepository.class)
class MongoSocialNetworkRepositoryTest {

    @Autowired
    private MongoSocialNetworkRepository repository;

    @Autowired
    private PostMongoRepository postMongoRepository;

    @Autowired
    private UserMongoRepository userMongoRepository;

    @BeforeEach
    void setUp() {

        postMongoRepository.deleteAll();
        userMongoRepository.deleteAll();
    }

    @Test
    void save_and_findTimelineByUsername_returnsPostsNewestFirst() {

        Instant older = Instant.parse("2024-01-01T10:00:00Z");
        Instant newer = Instant.parse("2024-01-01T11:00:00Z");

        repository.save(new Post("alice", "first post", older));
        repository.save(new Post("alice", "second post", newer));

        List<Post> timeline = repository.findTimelineByUsername("alice");

        assertThat(timeline).hasSize(2);
        assertThat(timeline.get(0).content()).isEqualTo("second post");
        assertThat(timeline.get(1).content()).isEqualTo("first post");
    }

    @Test
    void findTimelineByUsername_returnsOnlyUsersPosts() {

        repository.save(new Post("alice", "alice post", Instant.now()));
        repository.save(new Post("bob", "bob post", Instant.now()));

        List<Post> timeline = repository.findTimelineByUsername("alice");

        assertThat(timeline).hasSize(1);
        assertThat(timeline.get(0).username()).isEqualTo("alice");
    }

    @Test
    void follow_persistsFollowRelationship() {

        repository.follow("alice", "bob");

        repository.save(new Post("bob", "bob post", Instant.now()));

        List<Post> wall = repository.findWallByUsername("alice");

        assertThat(wall).hasSize(1);
        assertThat(wall.get(0).username()).isEqualTo("bob");
    }

    @Test
    void findWallByUsername_mergesOwnAndFollowedPostsNewestFirst() {

        Instant t1 = Instant.parse("2024-01-01T10:00:00Z");
        Instant t2 = Instant.parse("2024-01-01T10:30:00Z");
        Instant t3 = Instant.parse("2024-01-01T11:00:00Z");

        repository.save(new Post("alice", "alice early", t1));
        repository.save(new Post("bob", "bob middle", t2));
        repository.save(new Post("alice", "alice late", t3));

        repository.follow("alice", "bob");

        List<Post> wall = repository.findWallByUsername("alice");

        assertThat(wall).hasSize(3);
        assertThat(wall.get(0).content()).isEqualTo("alice late");
        assertThat(wall.get(1).content()).isEqualTo("bob middle");
        assertThat(wall.get(2).content()).isEqualTo("alice early");
    }
}
