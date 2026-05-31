package com.example.taskflow.infrastructure.persistence.entity;

import com.example.taskflow.domain.enums.TaskAuditAction;
import com.example.taskflow.domain.enums.TaskStatus;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.Instant;

@Entity
@Table(name = "task_audit")
@Getter @Setter
public class TaskAuditEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private Long taskId;

    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    private TaskAuditAction action;

    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    private TaskStatus oldStatus;

    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    private TaskStatus newStatus;

    @Column(columnDefinition = "TEXT")
    private String message;

    @Column(nullable = false)
    private String performedBy;

    @Column(nullable = false)
    private Instant createdAt;

    public TaskAuditEntity() {}

}
