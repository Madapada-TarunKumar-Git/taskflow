package com.example.taskflow.infrastructure.persistence.adapter;

import com.example.taskflow.domain.enums.TaskStatus;
import com.example.taskflow.domain.model.Task;
import com.example.taskflow.domain.repository.TaskRepository;
import com.example.taskflow.infrastructure.persistence.entity.TaskEntity;
import com.example.taskflow.infrastructure.persistence.mapper.TaskEntityMapper;
import com.example.taskflow.infrastructure.persistence.repository.JpaTaskRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
@RequiredArgsConstructor
public class TaskRepositoryAdapter implements TaskRepository {
    private final JpaTaskRepository jpaTaskRepository;

    @Override
    public Task save(Task task) {
        TaskEntity entity = TaskEntityMapper.toEntity(task);
        TaskEntity savedEntity = jpaTaskRepository.save(entity);
        return TaskEntityMapper.toDomain(savedEntity);
    }

    @Override
    public Optional<Task> findById(Long id) {
        return jpaTaskRepository.findById(id)
                .map(TaskEntityMapper::toDomain);
    }

    @Override
    public List<Task> findByStatus(TaskStatus status) {
        return jpaTaskRepository.findByStatus(status)
                .stream()
                .map(TaskEntityMapper::toDomain)
                .toList();
    }

    @Override
    public Page<Task> findTasks(Pageable pageable, TaskStatus status) {
        Page<TaskEntity> taskPage;
        if (status != null) {
            taskPage = jpaTaskRepository.findByStatus(status, pageable);
        } else {
            taskPage = jpaTaskRepository.findAll(pageable);
        }

        return taskPage.map(TaskEntityMapper::toDomain);
    }

}
