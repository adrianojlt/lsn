package com.lsn.server.websocket;

import com.lsn.api.domain.Post;
import com.lsn.server.dto.FeedEvent;
import com.lsn.server.event.EventPublisher;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Component;

import java.time.Clock;
import java.time.Instant;
import java.util.List;

@Component
public class WebSocketEventPublisher implements EventPublisher {

    private static final String FEED_DESTINATION = "/queue/feed";

    private final Clock clock;
    private final SessionRegistry sessionRegistry;
    private final SimpMessagingTemplate messaging;

    public WebSocketEventPublisher(
            SimpMessagingTemplate messaging,
            SessionRegistry sessionRegistry,
            Clock clock) {
        this.clock = clock;
        this.messaging = messaging;
        this.sessionRegistry = sessionRegistry;
    }

    @Override
    public void publishPost(Post post, List<String> followers) {

        FeedEvent event = new FeedEvent(FeedEvent.EventType.POST, post.username(), post.content(), post.postedAt());

        for (String follower : followers) {
            if (!sessionRegistry.getSessions(follower).isEmpty()) {
                messaging.convertAndSendToUser(follower, FEED_DESTINATION, event);
            }
        }
    }

    @Override
    public void publishFollow(String follower, String followee) {

        FeedEvent event = new FeedEvent(FeedEvent.EventType.FOLLOW, follower, null, Instant.now(clock));

        if (!sessionRegistry.getSessions(followee).isEmpty()) {
            messaging.convertAndSendToUser(followee, FEED_DESTINATION, event);
        }
    }
}
