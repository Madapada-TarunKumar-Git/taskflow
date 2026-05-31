package com.example.taskflow.application.port;

import org.springframework.web.multipart.MultipartFile;

public interface FileStoragePort {
    FileUploadResult storeFile(MultipartFile file);
}
