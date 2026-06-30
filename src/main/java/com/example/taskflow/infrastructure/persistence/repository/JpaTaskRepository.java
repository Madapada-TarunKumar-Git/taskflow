package com.example.taskflow.infrastructure.persistence.repository;

import com.example.taskflow.domain.enums.TaskStatus;
import com.example.taskflow.infrastructure.persistence.entity.TaskEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface JpaTaskRepository extends JpaRepository<TaskEntity, Long> {
    List<TaskEntity> findByStatus(TaskStatus status);

    long countByStatus(TaskStatus status);

    Page<TaskEntity> findByStatus(TaskStatus status, Pageable pageable);

    @Modifying
    @Query("""
             UPDATE TaskEntity task
             SET task.status = :processingStatus,
                 task.processingStartedAt = CURRENT_TIMESTAMP()
             WHERE task.id = :taskId
             AND
             task.status = :queuedStatus
            """)
    int claimTaskForProcessing(
            @Param("taskId")
            Long taskId,
            @Param("queuedStatus")
            TaskStatus queuedStatus,
            @Param("processingStatus")
            TaskStatus processingStatus

    );

    List<TaskEntity> findTop100ByStatusOrderByCreatedAtAsc(TaskStatus status);
}
