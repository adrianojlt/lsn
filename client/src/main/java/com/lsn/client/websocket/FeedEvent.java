package com.lsn.client.websocket;

import org.springframework.lang.NonNull;
import java.time.Instant;

public record FeedEvent(EventType type, String username, String content, Instant postedAt) {

    public enum EventType { POST, FOLLOW }

    @Override
    public @NonNull String toString() {
        return switch (type) {
            case POST   -> "[feed] " + username + ": " + content;
            case FOLLOW -> "[feed] " + username + " started following you";
        };
    }
}
