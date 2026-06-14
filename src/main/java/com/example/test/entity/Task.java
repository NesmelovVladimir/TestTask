package com.example.test.entity;

import com.example.test.dto.TaskStatus;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "tasks")
@Getter @Setter
public class Task {
    @Id
    @Column(name = "id", length = 8)
    private String id;

    @Column(name = "user_id", nullable = false)
    private UUID userId;

    @Enumerated(EnumType.STRING)
    @Column(name = "status")
    private TaskStatus status;

    @Column(name = "payload", length = 1000)
    private String payload;

    @Column(name = "result", length = 4000)
    private String result;

    @Column(name = "progress")
    private int progress;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @Column(name = "completed_at")
    private LocalDateTime completedAt;
}
