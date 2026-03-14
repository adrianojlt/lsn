package com.lsn.server.websocket;

import com.lsn.api.domain.Post;
import com.lsn.server.dto.FeedEvent;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.messaging.simp.SimpMessagingTemplate;

import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.List;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class EventPublisherTest {

    @Mock
    private SimpMessagingTemplate messaging;

    @Mock
    private SessionRegistry sessionRegistry;

    private WebSocketEventPublisher publisher;

    private static final Instant FIXED_TIME = Instant.parse("2024-01-01T10:00:00Z");

    @BeforeEach
    void setUp() {
        Clock clock = Clock.fixed(FIXED_TIME, ZoneOffset.UTC);
        publisher = new WebSocketEventPublisher(messaging, sessionRegistry, clock);
    }

    @Test
    void publishPost_toOnlineFollower_sendsEvent() {
        Post post = new Post("alice", "hello", FIXED_TIME);
        when(sessionRegistry.getSessions("bob")).thenReturn(Set.of("session-1"));

        publisher.publishPost(post, List.of("bob"));

        ArgumentCaptor<FeedEvent> captor = ArgumentCaptor.forClass(FeedEvent.class);
        verify(messaging).convertAndSendToUser(eq("bob"), eq("/queue/feed"), captor.capture());
        assertThat(captor.getValue().type()).isEqualTo(FeedEvent.EventType.POST);
        assertThat(captor.getValue().username()).isEqualTo("alice");
        assertThat(captor.getValue().content()).isEqualTo("hello");
    }

    @Test
    void publishPost_toOfflineFollower_doesNotSend() {
        Post post = new Post("alice", "hello", FIXED_TIME);
        when(sessionRegistry.getSessions("bob")).thenReturn(Set.of());

        publisher.publishPost(post, List.of("bob"));

        verify(messaging, never()).convertAndSendToUser(any(), any(), any());
    }

    @Test
    void publishFollow_toOnlineFollowee_sendsEvent() {
        when(sessionRegistry.getSessions("bob")).thenReturn(Set.of("session-1"));

        publisher.publishFollow("alice", "bob");

        ArgumentCaptor<FeedEvent> captor = ArgumentCaptor.forClass(FeedEvent.class);
        verify(messaging).convertAndSendToUser(eq("bob"), eq("/queue/feed"), captor.capture());
        assertThat(captor.getValue().type()).isEqualTo(FeedEvent.EventType.FOLLOW);
        assertThat(captor.getValue().username()).isEqualTo("alice");
    }

    @Test
    void publishFollow_toOfflineFollowee_doesNotSend() {
        when(sessionRegistry.getSessions("bob")).thenReturn(Set.of());

        publisher.publishFollow("alice", "bob");

        verify(messaging, never()).convertAndSendToUser(any(), any(), any());
    }
}
