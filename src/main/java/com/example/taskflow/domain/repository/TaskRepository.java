package com.example.taskflow.domain.repository;

import com.example.taskflow.domain.enums.TaskStatus;
import com.example.taskflow.domain.model.Task;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.Optional;

public interface TaskRepository {
    Task save(Task task);

    Optional<Task> findById(Long id);

    List<Task> findByStatus(TaskStatus status);

    Page<Task> findTasks(Pageable pageable, TaskStatus status);

    boolean claimTaskForProcessing(Long taskId);
}
