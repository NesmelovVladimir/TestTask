package com.example.test.controller;

import com.example.test.dto.AuthRequest;
import com.example.test.dto.AuthResponse;
import com.example.test.entity.User;
import com.example.test.security.jwt.JwtTokenProvider;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/auth")
public class AuthController {
    private final UserDetailsService userDetailsService;
    private final PasswordEncoder passwordEncoder;
    private final JwtTokenProvider tokenProvider;

    public AuthController(UserDetailsService userDetailsService,
            PasswordEncoder passwordEncoder,
            JwtTokenProvider tokenProvider) {
        this.userDetailsService = userDetailsService;
        this.passwordEncoder = passwordEncoder;
        this.tokenProvider = tokenProvider;
    }

    @PostMapping("/login")
    public ResponseEntity<AuthResponse> login(@RequestBody AuthRequest request) {
        User userDetails;
        try {
            userDetails = (User) userDetailsService.loadUserByUsername(request.getUsername());
        } catch (UsernameNotFoundException _) {
            return ResponseEntity.status(401).build();
        }
        if (!passwordEncoder.matches(request.getPassword(), userDetails.getPassword())) {
            return ResponseEntity.status(401).build();
        }
        String token = tokenProvider.createToken(userDetails);
        return ResponseEntity.ok(new AuthResponse(token));
    }
}
