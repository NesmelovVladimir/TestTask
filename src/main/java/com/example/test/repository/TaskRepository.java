package com.example.test.repository;

import com.example.test.entity.TaskEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.UUID;

public interface TaskRepository extends JpaRepository<TaskEntity, String> {
    List<TaskEntity> findByUserId(UUID userId);

    // Выбрать старейшие задачи со статусом CREATED, ограничить количество
    @Query(value = "SELECT * FROM tasks WHERE status = 'CREATED' ORDER BY created_at LIMIT :limit FOR UPDATE", nativeQuery = true)
    List<TaskEntity> findPendingTasksForProcessing(@Param("limit") int limit);
}
