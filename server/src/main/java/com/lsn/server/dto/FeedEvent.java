package com.lsn.server.dto;

import java.time.Instant;

public record FeedEvent(EventType type, String username, String content, Instant postedAt) {
    public enum EventType { POST, FOLLOW }
}
