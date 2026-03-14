package com.lsn.server.service;

import com.lsn.api.domain.Post;
import com.lsn.api.repository.SocialNetworkRepository;
import com.lsn.server.domain.UserDocument;
import com.lsn.server.event.EventPublisher;
import com.lsn.server.repository.UserMongoRepository;
import org.springframework.stereotype.Service;

import java.time.Clock;
import java.time.Instant;
import java.util.List;

@Service
public class SocialNetworkService {

    private final Clock clock;
    private final UserMongoRepository userRepo;
    private final EventPublisher eventPublisher;
    private final SocialNetworkRepository repository;

    public SocialNetworkService(
            SocialNetworkRepository repository,
            UserMongoRepository userRepo,
            EventPublisher eventPublisher,
            Clock clock) {

        this.clock = clock;
        this.userRepo = userRepo;
        this.repository = repository;
        this.eventPublisher = eventPublisher;
    }

    public void post(String username, String message) {

        Post post = new Post(username, message, Instant.now(clock));
        repository.save(post);

        List<String> followers = userRepo.findByFollowingContaining(username)
                .stream()
                .map(UserDocument::getUsername)
                .toList();

        eventPublisher.publishPost(post, followers);
    }

    public void follow(String follower, String followee) {
        repository.follow(follower, followee);
        eventPublisher.publishFollow(follower, followee);
    }
}
