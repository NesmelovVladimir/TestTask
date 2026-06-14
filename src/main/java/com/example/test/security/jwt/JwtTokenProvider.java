package com.example.test.security.jwt;

import com.example.test.dto.UserDTO;
import com.example.test.entity.User;
import io.jsonwebtoken.*;
import io.jsonwebtoken.jackson.io.JacksonDeserializer;
import io.jsonwebtoken.jackson.io.JacksonSerializer;
import io.jsonwebtoken.security.Keys;
import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.util.Date;
import java.util.Map;
import java.util.UUID;

@Component
public class JwtTokenProvider {

    private static final long VALIDITY_MS = 3600000; // 1 час

    private static final String USERNAME_KEY = "username";

    @Value("${jwt.secret}")
    private String secret;

    private SecretKey secretKey;

    private final JacksonSerializer<Map<String, ?>> serializer = new JacksonSerializer<>();
    private JwtParser jwtParser;

    @PostConstruct
    public void init() {
        this.secretKey = Keys.hmacShaKeyFor(secret.getBytes());
        jwtParser = Jwts.parser()
                .json(new JacksonDeserializer<>())
                .verifyWith(secretKey)
                .build();
    }

    /**
     * Создать токен для пользователя
     * @param user Пользователь
     * @return Токен
     */
    public String createToken(User user) {
        Date now = new Date();
        Date expiry = new Date(now.getTime() + VALIDITY_MS);
        Claims claims = Jwts.claims()
                .subject(user.getId().toString())
                .add(USERNAME_KEY, user.getUsername())
                .build();
        return Jwts.builder()
                .json(serializer)
                .claims(claims)
                .issuedAt(now)
                .expiration(expiry)
                .signWith(secretKey)
                .compact();
    }

    /**
     * Получить пользователя из токена
     * @param token Токен
     * @return Пользователя
     */
    public UserDTO getUserByToken(String token) {
        Jws<Claims> claims = jwtParser.parseSignedClaims(token);
        UUID userId = UUID.fromString(claims.getPayload().getSubject());
        return new UserDTO(userId, claims.getPayload().get(USERNAME_KEY, String.class));
    }

    /**
     * Валидация токена
     * @param token Токен
     * @return true - валидный токен, false - невалидный
     */
    public boolean validateToken(String token) {
        try {
            Jws<Claims> claims = jwtParser.parseSignedClaims(token);
            return claims != null
                    && claims.getPayload().getSubject() != null
                    && claims.getPayload().containsKey(USERNAME_KEY);
        } catch (JwtException _) {
            return false;
        }
    }
}
