package com.example.taskflow.presentation.controller;

import com.example.taskflow.application.dto.AuditResponse;
import com.example.taskflow.application.usecase.TaskAuditQueryUseCase;
import com.example.taskflow.shared.response.APIResponse;
import com.example.taskflow.shared.util.SecurityUtil;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/v1/tasks")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "TaskAuditController", description = "Retrieve task audit history details")
public class TaskAuditController {
    private final TaskAuditQueryUseCase taskAuditQueryUseCase;
    private final SecurityUtil securityUtil;

    @Operation(summary = "Task audits", description = "Retrieve task flow by id")
    @ApiResponse(responseCode = "200", description = "Task audit history retrieved successfully")
    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping("/{taskId}/audits")
    public ResponseEntity<APIResponse<List<AuditResponse>>> getTaskAudits(@PathVariable Long taskId) {
        log.info("Task audit requested. taskId = {}, user = {}",taskId, securityUtil.getUsername());
        List<AuditResponse> response = taskAuditQueryUseCase.getTaskAudits(taskId);
        return ResponseEntity.ok(APIResponse.success("Task audit history retrieved successfully", response));
    }
}
