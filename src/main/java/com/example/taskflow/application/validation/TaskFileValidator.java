package com.example.taskflow.application.validation;

import com.example.taskflow.domain.enums.TaskType;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@Component
public class TaskFileValidator {
    private static final long MAX_FILE_SIZE = 5 * 1024 * 1024; // 5 MB

    public void validate(MultipartFile file, TaskType taskType) {
        validateEmptyFile(file);
        validateFileSize(file);
        validateFileType(file, taskType);
    }

    private void validateEmptyFile(MultipartFile file) {
        if (file.isEmpty()) {
            throw new IllegalArgumentException("Upload file is empty");
        }
    }

    private void validateFileSize(MultipartFile file) {
        if (file.getSize() > MAX_FILE_SIZE) {
            throw new IllegalArgumentException("Upload file exceeds maximum allowed size");
        }
    }

    private void validateFileType(MultipartFile file, TaskType taskType) {
        String filename = file.getOriginalFilename();
        assert filename != null;
        String extension = extractExtension(filename);

        List<String> allowedExtensions = getAllowedExtension(taskType);
        if (!allowedExtensions.contains(extension)) {
            throw new IllegalArgumentException("Invalid file for task type " + taskType);
        }
    }

    private String extractExtension(String filename) {
        int lastDotIndex = filename.lastIndexOf(".");
        return lastDotIndex == -1
                ? ""
                : filename.substring(lastDotIndex + 1);
    }

    private List<String> getAllowedExtension(TaskType taskType) {
        return switch (taskType) {
            case CUSTOMER_IMPORT -> List.of("csv");
            case XML_IMPORT -> List.of("xml");
            case REPORT_GENERATION -> List.of("pdf");
            default -> List.of();
        };
    }
}
