package com.lsn.client.repository;

import com.lsn.api.domain.Post;
import com.lsn.api.repository.SocialNetworkRepository;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class InMemoryRepository implements SocialNetworkRepository {

    private final Map<String, List<Post>> posts   = new HashMap<>();
    private final Map<String, Set<String>> follows = new HashMap<>();

    @Override
    public void save(Post post) {
        posts.computeIfAbsent(post.username(), k -> new ArrayList<>()).add(post);
    }

    @Override
    public List<Post> findTimelineByUsername(String username) {
        return posts.getOrDefault(username, Collections.emptyList());
    }

    @Override
    public void follow(String follower, String followee) {
        follows.computeIfAbsent(follower, k -> new HashSet<>()).add(followee);
    }

    @Override
    public List<Post> findWallByUsername(String username) {

        List<Post> wall = new ArrayList<>(findTimelineByUsername(username));

        Set<String> followees = follows.getOrDefault(username, Collections.emptySet());

        for (String followee : followees) {
            wall.addAll(findTimelineByUsername(followee));
        }

        return wall;
    }
}
