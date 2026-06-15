package com.example.test.websocket;

import com.example.test.dto.SessionAndLock;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.locks.ReentrantLock;

@Slf4j
@Component
public class WebSocketSessionManager {

    private final ConcurrentHashMap<String, Set<SessionAndLock>> userSessions = new ConcurrentHashMap<>();

    /**
     * Добавить новую сессию в кэш сессий пользователей
     * @param userId  Идентификатор пользователя
     * @param session WebSocket сессия
     */
    public void registerSession(String userId, WebSocketSession session) {
        userSessions.computeIfAbsent(userId, k -> ConcurrentHashMap.newKeySet())
                .add(SessionAndLock.create(session));
    }

    /**
     * Удаление сессия из кэша
     * @param userId  Идентификатор пользователя
     * @param session Сессия
     */
    public void removeSession(String userId, WebSocketSession session) {
        Set<SessionAndLock> sessions = userSessions.get(userId);
        if (sessions != null) {
            sessions.removeIf(x -> x.session().equals(session));
            if (sessions.isEmpty()) {
                userSessions.remove(userId);
            }
        }
    }

    /**
     * Отправка сообщения открытым сессиям пользователя
     * @param userId  Идентификатор пользователя
     * @param message Сообщение
     */
    public void sendToUser(String userId, String message) {
        Set<SessionAndLock> sessions = userSessions.get(userId);
        if (sessions == null || sessions.isEmpty()) {
            return;
        }
        List<SessionAndLock> sessionsForRemove = new ArrayList<>();
        for (SessionAndLock session : sessions) {
            if (session.session().isOpen()) {
                ReentrantLock sessionLock = session.sessionLock();
                try {
                    sessionLock.lock();
                    session.session().sendMessage(new TextMessage(message));
                } catch (IOException e) {
                    log.error("Send message to user {} failed", userId, e);
                    sessionsForRemove.add(session);
                } finally {
                    sessionLock.unlock();
                }
            }
        }
        sessionsForRemove.forEach(sessions::remove);
    }
}
