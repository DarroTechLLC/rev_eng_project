package com.darro_tech.revengproject.services;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

@Service
public class FileStorageService {

    @Value("${file.upload-dir:uploads}")
    private String uploadDir;

    public String saveFile(MultipartFile file, String prefix) throws IOException {
        Path uploadPath = Paths.get(uploadDir);

        if (!Files.exists(uploadPath)) {
            Files.createDirectories(uploadPath);
        }

        String uniqueFileName = prefix + "_" + UUID.randomUUID() + "_" + file.getOriginalFilename();
        Path destinationPath = uploadPath.resolve(uniqueFileName);

        Files.copy(file.getInputStream(), destinationPath, StandardCopyOption.REPLACE_EXISTING);

        return "/uploads/" + uniqueFileName;
    }

    public void deleteFile(String fileName) throws IOException {
        if (fileName != null && !fileName.isEmpty()) {
            String fileNamePart = fileName.substring(fileName.lastIndexOf('/') + 1);
            Path path = Paths.get(uploadDir).resolve(fileNamePart);
            Files.deleteIfExists(path);
        }
    }
}
