package com.lsn.client.websocket;

import org.junit.jupiter.api.Test;

import java.time.Instant;

import static org.assertj.core.api.Assertions.assertThat;

class FeedListenerTest {

    @Test
    void feedEvent_postType_formatsCorrectly() {
        FeedEvent event = new FeedEvent(FeedEvent.EventType.POST, "alice", "hello world", Instant.now());

        assertThat(event.toString()).isEqualTo("[feed] alice: hello world");
    }

    @Test
    void feedEvent_followType_formatsCorrectly() {
        FeedEvent event = new FeedEvent(FeedEvent.EventType.FOLLOW, "alice", null, Instant.now());

        assertThat(event.toString()).isEqualTo("[feed] alice started following you");
    }
}
