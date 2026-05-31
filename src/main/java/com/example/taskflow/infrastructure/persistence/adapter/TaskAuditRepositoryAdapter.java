package com.example.taskflow.infrastructure.persistence.adapter;

import com.example.taskflow.domain.model.TaskAudit;
import com.example.taskflow.domain.repository.TaskAuditRepository;
import com.example.taskflow.infrastructure.persistence.entity.TaskAuditEntity;
import com.example.taskflow.infrastructure.persistence.mapper.TaskAuditEntityMapper;
import com.example.taskflow.infrastructure.persistence.repository.JpaTaskAuditRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
@RequiredArgsConstructor
public class TaskAuditRepositoryAdapter implements TaskAuditRepository {
    private final JpaTaskAuditRepository jpaTaskAuditRepository;

    @Override
    public TaskAudit save(TaskAudit taskAudit) {
        TaskAuditEntity entity = TaskAuditEntityMapper.toEntity(taskAudit);
        TaskAuditEntity savedEntity = jpaTaskAuditRepository.save(entity);

        return TaskAuditEntityMapper.toDomain(savedEntity);
    }

    @Override
    public List<TaskAudit> findByTaskId(Long taskId) {
        return jpaTaskAuditRepository.findById(taskId)
                .stream()
                .map(TaskAuditEntityMapper::toDomain)
                .toList();
    }
}
