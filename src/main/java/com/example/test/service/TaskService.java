package com.example.test.service;

import com.example.test.dto.TaskResponse;
import com.example.test.dto.TaskStatus;
import com.example.test.entity.TaskEntity;
import com.example.test.repository.TaskRepository;
import com.example.test.websocket.WebSocketSessionManager;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import tools.jackson.databind.ObjectMapper;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;
import java.util.stream.Collectors;

@Service
public class TaskService {

    private final TaskRepository taskRepository;
    private final WebSocketSessionManager wsManager;
    private final ObjectMapper objectMapper;
    private final Executor taskExecutor;

    public TaskService(TaskRepository taskRepository,
            WebSocketSessionManager wsManager,
            ObjectMapper objectMapper,
            @Qualifier("taskExecutor") Executor taskExecutor) {
        this.taskRepository = taskRepository;
        this.wsManager = wsManager;
        this.objectMapper = objectMapper;
        this.taskExecutor = taskExecutor;
    }

    public TaskEntity createTask(String userId, String payload) {
        TaskEntity task = new TaskEntity();
        task.setId(UUID.randomUUID().toString().substring(0, 8));
        task.setUserId(userId);
        task.setStatus(TaskStatus.CREATED);
        task.setPayload(payload);
        task.setProgress(0);
        task = taskRepository.save(task);

        final String taskId = task.getId();
        CompletableFuture.runAsync(() -> safeProcess(taskId), taskExecutor)
                .exceptionally(ex -> {
                    // Этот блок сработает, только если safeProcess не поймал исключение сам
                    // Подстраховка: переведём задачу в FAILED, если что-то пошло не так
                    TaskEntity t = taskRepository.findById(taskId).orElse(null);
                    if (t != null && t.getStatus() != TaskStatus.COMPLETED && t.getStatus() != TaskStatus.FAILED) {
                        t.setStatus(TaskStatus.FAILED);
                        t.setResult("Internal error: " + ex.getMessage());
                        t.setCompletedAt(LocalDateTime.now());
                        taskRepository.save(t);
                        sendUpdate(t);
                    }
                    return null;
                });
        return task;
    }

    @Transactional
    public void safeProcess(String taskId) {
        TaskEntity task = taskRepository.findById(taskId).orElseThrow();
        try {
            task.setStatus(TaskStatus.PROCESSING);
            taskRepository.save(task);
            sendUpdate(task);

            int totalSteps = 20;
            long stepDurationMs = Math.max(50, (task.getPayload().length() * 1000L) / totalSteps);

            for (int i = 1; i <= totalSteps; i++) {
                Thread.sleep(stepDurationMs);
                int progress = (i * 100) / totalSteps;
                task.setProgress(progress);
                taskRepository.save(task);
                sendUpdate(task);
            }

            task.setStatus(TaskStatus.COMPLETED);
            task.setResult("Processed: " + task.getPayload().toUpperCase());
            task.setCompletedAt(LocalDateTime.now());
            task.setProgress(100);
            taskRepository.save(task);
            sendUpdate(task);

        } catch (InterruptedException _) {
            Thread.currentThread().interrupt();
            task.setStatus(TaskStatus.FAILED);
            task.setResult("Interrupted");
            task.setCompletedAt(LocalDateTime.now());
            taskRepository.save(task);
            sendUpdate(task);
        } catch (Exception e) {
            task.setStatus(TaskStatus.FAILED);
            task.setResult("Error: " + e.getMessage());
            task.setCompletedAt(LocalDateTime.now());
            taskRepository.save(task);
            sendUpdate(task);
        }
    }

    private void sendUpdate(TaskEntity task) {
        TaskResponse response = new TaskResponse(
                task.getId(), task.getStatus(), task.getProgress(),
                task.getPayload(), task.getResult(),
                task.getCreatedAt(), task.getCompletedAt()
        );
        String json = objectMapper.writeValueAsString(response);
        wsManager.sendToUser(task.getUserId(), json);
    }

    public List<TaskResponse> getUserTasks(String userId) {
        return taskRepository.findByUserId(userId).stream()
                .map(t -> new TaskResponse(
                        t.getId(), t.getStatus(), t.getProgress(),
                        t.getPayload(), t.getResult(),
                        t.getCreatedAt(), t.getCompletedAt()))
                .collect(Collectors.toList());
    }

    public TaskResponse getTask(String userId, String taskId) {
        TaskEntity t = taskRepository.findById(taskId)
                .filter(task -> task.getUserId().equals(userId))
                .orElseThrow(() -> new RuntimeException("Task not found"));
        return new TaskResponse(
                t.getId(), t.getStatus(), t.getProgress(),
                t.getPayload(), t.getResult(),
                t.getCreatedAt(), t.getCompletedAt());
    }
}
