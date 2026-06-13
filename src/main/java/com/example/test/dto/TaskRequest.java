package com.example.test.dto;

import lombok.Data;

@Data
public class TaskRequest {
    private String payload;
    private int durationSec;  // имитация работы
}
