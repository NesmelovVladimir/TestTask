package com.example.test.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.UUID;

@AllArgsConstructor
@Getter
public class UserDTO {
    private UUID id;
    private String username;
}
