package com.example.test.controller;

import com.example.test.dto.AuthRequest;
import com.example.test.dto.AuthResponse;
import com.example.test.security.jwt.JwtTokenProvider;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/auth")
public class AuthController {
    private final JwtTokenProvider tokenProvider;

    // В реальном проекте – UserDetailsService
    private final Map<String, String> users = Map.of("user", "password", "admin", "admin");

    public AuthController(JwtTokenProvider tokenProvider) {
        this.tokenProvider = tokenProvider;
    }

    @PostMapping("/login")
    public ResponseEntity<AuthResponse> login(@RequestBody AuthRequest request) {
        if (users.containsKey(request.getUsername()) &&
                users.get(request.getUsername()).equals(request.getPassword())) {
            String token = tokenProvider.createToken(request.getUsername());
            return ResponseEntity.ok(new AuthResponse(token));
        }
        return ResponseEntity.status(401).build();
    }
}
