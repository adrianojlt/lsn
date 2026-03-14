package com.lsn.api.repository;

import com.lsn.api.domain.Post;

import java.util.List;

public interface SocialNetworkRepository {

    void save(Post post);
    void follow(String follower, String followee);

    List<Post> findWallByUsername(String username);
    List<Post> findTimelineByUsername(String username);
}
