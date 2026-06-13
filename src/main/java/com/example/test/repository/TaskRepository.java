package com.example.test.repository;

import com.example.test.dto.TaskStatus;
import com.example.test.entity.TaskEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface TaskRepository extends JpaRepository<TaskEntity, String> {
    List<TaskEntity> findByUserId(String userId);
    List<TaskEntity> findByUserIdAndStatus(String userId, TaskStatus status);
}
