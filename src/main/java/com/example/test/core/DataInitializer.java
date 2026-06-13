package com.example.test.core;

import com.example.test.entity.UserEntity;
import com.example.test.repository.UserRepository;
import org.jspecify.annotations.NonNull;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

@Component
public class DataInitializer implements CommandLineRunner {
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public DataInitializer(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public void run(String @NonNull ... args) {
        if (userRepository.count() == 0) {
            UserEntity user1 = new UserEntity();
            user1.setUsername("user");
            user1.setPassword(passwordEncoder.encode("password"));
            userRepository.save(user1);

            UserEntity user2 = new UserEntity();
            user2.setUsername("admin");
            user2.setPassword(passwordEncoder.encode("admin"));
            userRepository.save(user2);

            System.out.println("Тестовые пользователи созданы");
        }
    }
}
