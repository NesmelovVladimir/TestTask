package com.example.test.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@AllArgsConstructor
public class TaskResponse {
    private String id;
    private TaskStatus status;
    private int progress;
    private String payload;
    private String result;
    private LocalDateTime createdAt;
    private LocalDateTime completedAt;
}
