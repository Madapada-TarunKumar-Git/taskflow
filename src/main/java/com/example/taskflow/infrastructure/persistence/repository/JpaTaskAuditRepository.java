package com.example.taskflow.infrastructure.persistence.repository;

import com.example.taskflow.infrastructure.persistence.entity.TaskAuditEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface JpaTaskAuditRepository extends JpaRepository<TaskAuditEntity, Long> {
    List<TaskAuditEntity> findByTaskIdOrderByCreatedAtAsc(Long taskId);
}
