package com.lsn;

import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.util.Comparator;
import java.util.List;
import java.util.function.Function;

public class SocialNetworkService {

    private final SocialNetworkRepository repository;
    private final Clock clock;

    public SocialNetworkService(SocialNetworkRepository repository, Clock clock) {
        this.repository = repository;
        this.clock      = clock;
    }

    public void post(String username, String message) {
        repository.save(new Post(username, message, Instant.now(clock)));
    }

    public List<String> read(String username) {
        return sortAndFormat(
                repository.findTimelineByUsername(username),
                post -> post.content() + " (" + timeAgo(post) + ")"
        );
    }

    public void follow(String follower, String followee) {
        repository.follow(follower, followee);
    }

    public List<String> wall(String username) {
        return sortAndFormat(
                repository.findWallByUsername(username),
                post -> post.username() + " - " + post.content() + " (" + timeAgo(post) + ")"
        );
    }

    private List<String> sortAndFormat(List<Post> posts, Function<Post, String> formatter) {
        return posts.stream()
                .sorted(Comparator.comparing(Post::postedAt).reversed())
                .map(formatter)
                .toList();
    }

    private String timeAgo(Post post) {
        return TimeFormatter.format(Duration.between(post.postedAt(), Instant.now(clock)));
    }
}
