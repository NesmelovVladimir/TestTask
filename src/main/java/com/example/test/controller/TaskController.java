package com.example.test.controller;

import com.example.test.dto.TaskRequest;
import com.example.test.dto.TaskResponse;
import com.example.test.entity.TaskEntity;
import com.example.test.service.TaskService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/tasks")
public class TaskController {
    private final TaskService taskService;

    public TaskController(TaskService taskService) {
        this.taskService = taskService;
    }

    @GetMapping
    public ResponseEntity<List<TaskResponse>> getTasks(Authentication authentication) {
        return ResponseEntity.ok(taskService.getUserTasks(authentication.getName()));
    }

    @GetMapping("/{taskId}")
    public ResponseEntity<TaskResponse> getTask(@PathVariable String taskId,
            Authentication authentication) {
        try {
            return ResponseEntity.ok(taskService.getTask(authentication.getName(), taskId));
        } catch (RuntimeException _) {
            return ResponseEntity.notFound().build();
        }
    }

    @PostMapping
    public ResponseEntity<TaskResponse> createTask(@RequestBody TaskRequest request,
            Authentication authentication) {
        TaskEntity task = taskService.createTask(authentication.getName(), request.getPayload());
        return ResponseEntity.status(201).body(
                new TaskResponse(task.getId(), task.getStatus(), task.getProgress(),
                        task.getPayload(), null,
                        task.getCreatedAt(), null)
        );
    }
}