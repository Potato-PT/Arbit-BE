package com.arbit.app.storage;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.stereotype.Service;

@Service
@ConditionalOnMissingBean(StorageService.class)
public class LocalStorageService implements StorageService {

    private final Path uploadRoot;
    private final String publicBasePath;

    public LocalStorageService(@Value("${storage.local.upload-dir:uploads}") String uploadDir,
                               @Value("${storage.local.public-base-path:/uploads}") String publicBasePath) {
        this.uploadRoot = Paths.get(uploadDir).toAbsolutePath().normalize();
        this.publicBasePath = publicBasePath;
    }

    @Override
    public String upload(String objectName, InputStream inputStream, long contentLength, String contentType) {
        try {
            Path targetPath = uploadRoot.resolve(objectName).normalize();
            if (!targetPath.startsWith(uploadRoot)) {
                throw new IllegalArgumentException("Invalid upload path.");
            }
            Files.createDirectories(targetPath.getParent());
            Files.copy(inputStream, targetPath, java.nio.file.StandardCopyOption.REPLACE_EXISTING);
            return publicBasePath + "/" + objectName.replace("\\", "/");
        } catch (IOException exception) {
            throw new IllegalStateException("Local file upload failed.", exception);
        }
    }
}
