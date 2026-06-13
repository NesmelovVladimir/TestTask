package com.example.test.security.jwt;

import com.example.test.entity.UserEntity;
import com.example.test.repository.UserRepository;
import org.jspecify.annotations.Nullable;
import org.springframework.http.server.ServerHttpRequest;
import org.springframework.http.server.ServerHttpResponse;
import org.springframework.http.server.ServletServerHttpRequest;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.WebSocketHandler;
import org.springframework.web.socket.server.HandshakeInterceptor;

import java.util.Map;

@Component
public class JwtHandshakeInterceptor implements HandshakeInterceptor {
    private final JwtTokenProvider tokenProvider;
    private final UserRepository userRepository;

    public JwtHandshakeInterceptor(JwtTokenProvider tokenProvider, UserRepository userRepository) {
        this.tokenProvider = tokenProvider;
        this.userRepository = userRepository;
    }

    @Override
    public boolean beforeHandshake(ServerHttpRequest request, ServerHttpResponse response,
            WebSocketHandler wsHandler, Map<String, Object> attributes) {
        if (request instanceof ServletServerHttpRequest servletRequest) {
            String token = servletRequest.getServletRequest().getParameter("token");
            if (token != null && tokenProvider.validateToken(token)) {
                String username = tokenProvider.getUsername(token);
                UserEntity user = userRepository.findByUsername(username).orElse(null);
                if (user != null) {
                    attributes.put("userId", user.getId());  // теперь ID пользователя
                    return true;
                }
            }
        }
        return false; // отклонить подключение
    }

    @Override
    public void afterHandshake(ServerHttpRequest request, ServerHttpResponse response, WebSocketHandler wsHandler,
            @Nullable Exception exception) {

    }
}
