package com.example.taskflow.infrastructure.persistence.repository;

import com.example.taskflow.domain.enums.TaskStatus;
import com.example.taskflow.infrastructure.persistence.entity.TaskEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface JpaTaskRepository extends JpaRepository<TaskEntity, Long> {
    List<TaskEntity> findByStatus(TaskStatus status);
    Page<TaskEntity> findByStatus(TaskStatus status, Pageable pageable);
}
