package com.example.test.websocket.config;

import com.example.test.security.jwt.JwtHandshakeInterceptor;
import com.example.test.websocket.handler.TaskWebSocketHandler;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.socket.config.annotation.EnableWebSocket;
import org.springframework.web.socket.config.annotation.WebSocketConfigurer;
import org.springframework.web.socket.config.annotation.WebSocketHandlerRegistry;

@Configuration
@EnableWebSocket
public class WebSocketConfig implements WebSocketConfigurer {
    private final TaskWebSocketHandler taskHandler;
    private final JwtHandshakeInterceptor jwtInterceptor;

    public WebSocketConfig(TaskWebSocketHandler taskHandler, JwtHandshakeInterceptor jwtInterceptor) {
        this.taskHandler = taskHandler;
        this.jwtInterceptor = jwtInterceptor;
    }

    @Override
    public void registerWebSocketHandlers(WebSocketHandlerRegistry registry) {
        registry.addHandler(taskHandler, "/ws/tasks")
                .addInterceptors(jwtInterceptor)
                .setAllowedOrigins("*");
    }
}
