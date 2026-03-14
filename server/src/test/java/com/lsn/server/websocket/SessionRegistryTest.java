package com.lsn.server.websocket;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class SessionRegistryTest {

    private SessionRegistry registry;

    @BeforeEach
    void setUp() {
        registry = new SessionRegistry();
    }

    @Test
    void register_makesUserOnline() {
        registry.register("alice", "session-1");

        assertThat(registry.isOnline("alice")).isTrue();
        assertThat(registry.getSessions("alice")).containsExactly("session-1");
    }

    @Test
    void unregister_removesSession_userGoesOffline() {
        registry.register("alice", "session-1");
        registry.unregister("session-1");

        assertThat(registry.isOnline("alice")).isFalse();
        assertThat(registry.getSessions("alice")).isEmpty();
    }

    @Test
    void unregister_unknownSession_doesNothing() {
        registry.unregister("unknown-session");

        assertThat(registry.isOnline("alice")).isFalse();
    }

    @Test
    void register_multipleSessions_allTracked() {
        registry.register("alice", "session-1");
        registry.register("alice", "session-2");

        assertThat(registry.isOnline("alice")).isTrue();
        assertThat(registry.getSessions("alice")).containsExactlyInAnyOrder("session-1", "session-2");
    }

    @Test
    void unregister_oneOfTwoSessions_userStillOnline() {
        registry.register("alice", "session-1");
        registry.register("alice", "session-2");
        registry.unregister("session-1");

        assertThat(registry.isOnline("alice")).isTrue();
        assertThat(registry.getSessions("alice")).containsExactly("session-2");
    }
}
