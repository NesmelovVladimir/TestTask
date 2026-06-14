package com.example.test.repository;

import com.example.test.dto.TaskStatus;
import com.example.test.entity.Task;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface TaskRepository extends JpaRepository<Task, String> {

    List<Task> findByUserId(UUID userId);

    List<Task> findAllByStatusOrderByCreatedAt(TaskStatus status, Pageable pageable);
}
