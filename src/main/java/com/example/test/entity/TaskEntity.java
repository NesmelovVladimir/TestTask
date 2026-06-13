package com.example.test.entity;

import com.example.test.dto.TaskStatus;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Entity
@Table(name = "tasks")
@Getter @Setter
public class TaskEntity {
    @Id
    private String id;

    @Column(nullable = false)
    private String userId;

    @Enumerated(EnumType.STRING)
    private TaskStatus status;

    @Column(length = 1000)
    private String payload;

    @Column(length = 4000)
    private String result;

    private int progress;  // 0-100

    private LocalDateTime createdAt;
    private LocalDateTime completedAt;

    @PrePersist
    void onCreate() {
        createdAt = LocalDateTime.now();
    }
}
