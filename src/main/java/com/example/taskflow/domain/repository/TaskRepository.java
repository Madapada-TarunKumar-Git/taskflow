package com.example.taskflow.domain.repository;

import com.example.taskflow.domain.enums.TaskStatus;
import com.example.taskflow.domain.model.Task;

import java.util.List;
import java.util.Optional;

public interface TaskRepository{
    Task save(Task task);

    Optional<Task> findById(Long id);

    List<Task> findByStatus(TaskStatus status);
}
