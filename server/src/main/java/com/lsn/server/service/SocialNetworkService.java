package com.lsn.server.service;

import com.lsn.api.domain.Post;
import com.lsn.api.repository.SocialNetworkRepository;
import org.springframework.stereotype.Service;

import java.time.Clock;
import java.time.Instant;

@Service
public class SocialNetworkService {

    private final Clock clock;
    private final SocialNetworkRepository repository;

    public SocialNetworkService(SocialNetworkRepository repository, Clock clock) {

        this.clock = clock;
        this.repository = repository;
    }

    public void post(String username, String message) {
        repository.save(new Post(username, message, Instant.now(clock)));
    }

    public void follow(String follower, String followee) {
        repository.follow(follower, followee);
    }
}
