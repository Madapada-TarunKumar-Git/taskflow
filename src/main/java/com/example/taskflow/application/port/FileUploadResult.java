package com.example.taskflow.application.port;

public record FileUploadResult(
        String originalFileName,
        String storedFilename,
        String filePath,
        Long fileSize
) {
}
