package com.example.test.controller;

import com.example.test.dto.TaskRequest;
import com.example.test.dto.TaskResponse;
import com.example.test.dto.UserDTO;
import com.example.test.entity.TaskEntity;
import com.example.test.service.TaskService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Objects;

@RestController
@RequestMapping("/api/tasks")
public class TaskController {

    private final TaskService taskService;

    public TaskController(TaskService taskService) {
        this.taskService = taskService;
    }

    @GetMapping
    public ResponseEntity<List<TaskResponse>> getTasks(Authentication authentication) {
        return ResponseEntity.ok(taskService.getUserTasks(((UserDTO)
                Objects.requireNonNull(authentication.getCredentials())).getId()));
    }

    @PostMapping
    public ResponseEntity<TaskResponse> createTask(@RequestBody TaskRequest request,
            Authentication authentication) {
        TaskEntity task = taskService.createTask((UserDTO) Objects.requireNonNull(authentication.getCredentials()),
                request.getPayload());
        return ResponseEntity.status(201).body(
                new TaskResponse(task.getId(), task.getStatus(), task.getProgress(),
                        task.getPayload(), null,
                        task.getCreatedAt(), null)
        );
    }
}