package com.example.taskflow.application.service;

import com.example.taskflow.application.dto.AuditResponse;
import com.example.taskflow.application.mapper.TaskAuditMapper;
import com.example.taskflow.application.usecase.TaskAuditQueryUseCase;
import com.example.taskflow.domain.model.TaskAudit;
import com.example.taskflow.domain.repository.TaskAuditRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class TaskAuditQueryService implements TaskAuditQueryUseCase {
    private final TaskAuditRepository taskAuditRepository;

    @Override
    public List<AuditResponse> getTaskAudits(Long taskId) {
        List<TaskAudit> audits = taskAuditRepository.findByTaskId(taskId);
        audits.forEach(System.out::println);
        return audits.stream().map(TaskAuditMapper::toAuditResponse).toList();
    }
}
