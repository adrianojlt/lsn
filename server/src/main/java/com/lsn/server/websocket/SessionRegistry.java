package com.lsn.server.websocket;

import org.springframework.stereotype.Component;

import java.util.Collections;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArraySet;

@Component
public class SessionRegistry {

    private final ConcurrentHashMap<String, String> sessionUser = new ConcurrentHashMap<>();
    private final ConcurrentHashMap<String, Set<String>> userSessions = new ConcurrentHashMap<>();

    public void register(String username, String sessionId) {
        userSessions.computeIfAbsent(username, k -> new CopyOnWriteArraySet<>()).add(sessionId);
        sessionUser.put(sessionId, username);
    }

    public void unregister(String sessionId) {

        String username = sessionUser.remove(sessionId);

        if (username == null) {
            return;
        }

        Set<String> sessions = userSessions.get(username);

        if (sessions != null) {
            sessions.remove(sessionId);
            if (sessions.isEmpty()) {
                userSessions.remove(username, sessions);
            }
        }
    }

    public boolean isOnline(String username) {
        Set<String> sessions = userSessions.get(username);
        return sessions != null && !sessions.isEmpty();
    }

    public Set<String> getSessions(String username) {
        return userSessions.getOrDefault(username, Collections.emptySet());
    }
}
