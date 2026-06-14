package com.example.taskflow.application.service;

import com.example.taskflow.application.dto.StatusResponse;
import com.example.taskflow.presentation.response.TaskResponseDto;
import com.example.taskflow.application.mapper.TaskMapper;
import com.example.taskflow.application.usecase.TaskQueryUseCase;
import com.example.taskflow.domain.enums.TaskStatus;
import com.example.taskflow.domain.exception.TaskNotFoundException;
import com.example.taskflow.domain.model.Task;
import com.example.taskflow.domain.repository.TaskRepository;
import com.example.taskflow.shared.response.PageResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class TaskQueryService implements TaskQueryUseCase {
    private final TaskRepository taskRepository;

    @Override
    public TaskResponseDto getTaskById(Long taskId) {
        Task task = taskRepository.findById(taskId)
                .orElseThrow(() -> new TaskNotFoundException("Task not found with id: " + taskId));

        return TaskMapper.toResponseDto(task);
    }

    @Override
    public PageResponse<TaskResponseDto> getTasks(int page, int size, String sortBy, String direction, TaskStatus status) {
        Sort sort = direction.equalsIgnoreCase("desc")
                ? Sort.by(sortBy).descending()
                : Sort.by(sortBy).ascending();

        Pageable pageable = PageRequest.of(page, size, sort);
        Page<Task> taskPage = taskRepository.findTasks(pageable, status);
        List<TaskResponseDto> content = taskPage.getContent()
                .stream()
                .map(TaskMapper::toResponseDto)
                .toList();

        return TaskMapper.toPageResponse(content,taskPage);
    }

    @Override
    public StatusResponse getTaskStatistics() {
        return new StatusResponse(
                taskRepository.countAll(),
                taskRepository.countTasks(TaskStatus.COMPLETED),
                taskRepository.countTasks(TaskStatus.PROCESSING),
                taskRepository.countTasks(TaskStatus.PERMANENT_FAILURE),
                taskRepository.countTasks(TaskStatus.RETRY_PENDING),
                taskRepository.countTasks(TaskStatus.QUEUED),
                taskRepository.countTasks(TaskStatus.CREATED)
        );
    }

}
