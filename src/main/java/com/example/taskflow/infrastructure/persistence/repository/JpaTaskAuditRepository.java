package com.example.taskflow.infrastructure.persistence.repository;

import com.example.taskflow.infrastructure.persistence.entity.TaskAuditEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface JpaTaskAuditRepository extends JpaRepository<TaskAuditEntity, Long> {

    @Query("SELECT tae FROM TaskAuditEntity tae WHERE tae.taskId = :taskId")
    List<TaskAuditEntity> findByTaskIdOrderByCreatedAtAsc(Long taskId);
}
