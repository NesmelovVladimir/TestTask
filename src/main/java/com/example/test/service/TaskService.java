package com.example.test.service;

import com.example.test.dto.TaskResponse;
import com.example.test.dto.TaskStatus;
import com.example.test.dto.UserDTO;
import com.example.test.entity.Task;
import com.example.test.repository.TaskRepository;
import com.example.test.websocket.WebSocketSessionManager;
import jakarta.annotation.Resource;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Pageable;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.stereotype.Service;
import tools.jackson.databind.ObjectMapper;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

/**
 * Сервис для работы с задачами
 */
@Slf4j
@RequiredArgsConstructor
@Service
public class TaskService {

    private final TaskRepository taskRepository;
    private final WebSocketSessionManager wsManager;
    private final ObjectMapper objectMapper;
    @Resource(name = "taskExecutor")
    private final ThreadPoolTaskExecutor taskExecutor;

    /**
     * Задача по расписанию, которая вытаскивает новые задачи от пользователей и запускает их в фоне. Данные
     * пользователям отправляются через WebSocket
     */
    @Scheduled(fixedDelay = 1000)
    public void processPendingTasks() {
        doProcessPendingTasks();
    }

    /**
     * Отправить задачу пользователя в очередь на выполнение
     * @param user    Пользователь, создавший задачу
     * @param payload Нагрузка для задачи
     * @return Новая задача
     */
    public Task createTask(UserDTO user, String payload) {
        Task task = new Task();
        task.setId(UUID.randomUUID().toString().substring(0, 8));
        task.setUserId(user.getId());
        task.setStatus(TaskStatus.CREATED);
        task.setPayload(payload);
        task.setProgress(0);
        task = taskRepository.save(task);

        // Отправляем начальное состояние через WebSocket
        sendUpdate(task);
        return task;
    }

    /**
     * Получить список задач по идентификатору пользователя
     * @param userId Идентификатор пользователя
     * @return Список задач
     */
    public List<TaskResponse> getUserTasks(UUID userId) {
        return taskRepository.findByUserId(userId).stream()
                .map(t -> new TaskResponse(
                        t.getId(), t.getStatus(), t.getProgress(),
                        t.getPayload(), t.getResult(),
                        t.getCreatedAt(), t.getCompletedAt()))
                .toList();
    }

    /**
     * Получение и запуск задач в фоне
     */
    public void doProcessPendingTasks() {
        try {
            List<Task> pendingTasks = taskRepository.findAllByStatusOrderByCreatedAt(TaskStatus.CREATED,
                    Pageable.ofSize(5));

            CompletableFuture<?>[] tasks = pendingTasks.stream()
                    .map(task -> {
                        task.setStatus(TaskStatus.PROCESSING);
                        taskRepository.save(task);
                        sendUpdate(task);
                        return CompletableFuture.runAsync(() -> processTask(task), taskExecutor);
                    })
                    .toArray(CompletableFuture[]::new);
            CompletableFuture.allOf(tasks);
        } catch (Exception e) {
            log.error("Start new tasks error", e);
        }
    }

    /**
     * Выполнить задачу
     * @param task Задача
     */
    private void processTask(Task task) {
        if (task.getStatus() != TaskStatus.PROCESSING) {
            // Уже обрабатывается или завершена – тихо выходим
            return;
        }
        try {
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

    /**
     * Отправить изменение по задаче через WebSocket пользователю
     * @param task Задача
     */
    private void sendUpdate(Task task) {
        TaskResponse response = new TaskResponse(
                task.getId(), task.getStatus(), task.getProgress(),
                task.getPayload(), task.getResult(),
                task.getCreatedAt(), task.getCompletedAt()
        );
        String json = objectMapper.writeValueAsString(response);
        wsManager.sendToUser(task.getUserId().toString(), json);
    }
}
