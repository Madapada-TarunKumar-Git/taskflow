package com.example.taskflow.infrastructure.storage;

import com.example.taskflow.application.port.FileStoragePort;
import com.example.taskflow.application.port.FileUploadResult;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.UUID;

@Component
public class LocalFileStorageAdapter implements FileStoragePort {
    private static final String UPLOAD_DIR = "uploads";

    @Override
    public FileUploadResult storeFile(MultipartFile file) {
        try {
            Files.createDirectories(Paths.get(UPLOAD_DIR));
            String originalFileName = file.getOriginalFilename();
            String storedFileName = UUID.randomUUID() + "_" + originalFileName;

            Path filePath = Paths.get(UPLOAD_DIR, storedFileName);
            Files.copy(file.getInputStream(), filePath, StandardCopyOption.REPLACE_EXISTING);

            return new FileUploadResult(
                    originalFileName,
                    storedFileName,
                    filePath.toString(),
                    file.getSize()
            );
        } catch (IOException ioe) {
            throw new RuntimeException("Failed to store file",ioe);
        }
    }
}
