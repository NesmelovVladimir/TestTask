package com.example.test.dto;

import org.springframework.web.socket.WebSocketSession;

import java.util.concurrent.locks.ReentrantLock;

/**
 * Класс на сессии и блокировки одновременной отправки
 * @param session     Сессии
 * @param sessionLock Блокировка
 */
public record SessionAndLock(WebSocketSession session, ReentrantLock sessionLock) {

    public static SessionAndLock create(WebSocketSession session) {
        return new SessionAndLock(session, new ReentrantLock());
    }
}

