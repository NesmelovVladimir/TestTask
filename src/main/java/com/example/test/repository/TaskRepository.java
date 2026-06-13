package com.example.test.repository;

import com.example.test.entity.TaskEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface TaskRepository extends JpaRepository<TaskEntity, String> {
    List<TaskEntity> findByUserId(UUID userId);
}
