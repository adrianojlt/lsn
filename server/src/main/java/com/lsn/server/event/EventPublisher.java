package com.lsn.server.event;

import com.lsn.api.domain.Post;

import java.util.List;

public interface EventPublisher {
    void publishPost(Post post, List<String> followers);
    void publishFollow(String follower, String followee);
}
