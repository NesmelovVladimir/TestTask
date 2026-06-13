package com.example.test.websocket;

import org.springframework.stereotype.Component;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;

import java.io.IOException;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

@Component
public class WebSocketSessionManager {
    private final ConcurrentHashMap<String, Set<WebSocketSession>> userSessions = new ConcurrentHashMap<>();
    private final ConcurrentHashMap<WebSocketSession, Object> sessionLocks = new ConcurrentHashMap<>();

    public void registerSession(String userId, WebSocketSession session) {
        userSessions.computeIfAbsent(userId, k -> ConcurrentHashMap.newKeySet()).add(session);
        sessionLocks.putIfAbsent(session, new Object());
    }

    public void removeSession(String userId, WebSocketSession session) {
        Set<WebSocketSession> sessions = userSessions.get(userId);
        if (sessions != null) {
            sessions.remove(session);
            if (sessions.isEmpty()) {
                userSessions.remove(userId);
            }
        }
        sessionLocks.remove(session);
    }

    public void sendToUser(String userId, String message) {
        Set<WebSocketSession> sessions = userSessions.get(userId);
        if (sessions == null) return;
        for (WebSocketSession session : sessions) {
            if (session.isOpen()) {
                Object lock = sessionLocks.get(session);
                if (lock != null) {
                    synchronized (lock) {
                        try {
                            session.sendMessage(new TextMessage(message));
                        } catch (IOException _) {
                            sessions.remove(session);
                            sessionLocks.remove(session);
                        }
                    }
                }
            }
        }
    }
}
