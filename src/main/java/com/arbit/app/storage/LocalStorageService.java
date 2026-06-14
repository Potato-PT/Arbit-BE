package com.arbit.app.storage;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.stereotype.Service;

@Service
@ConditionalOnMissingBean(StorageService.class)
public class LocalStorageService implements StorageService {

    private static final Logger log = LoggerFactory.getLogger(LocalStorageService.class);

    private final Path uploadRoot;
    private final String publicBasePath;

    public LocalStorageService(@Value("${storage.local.upload-dir:uploads}") String uploadDir,
                               @Value("${storage.local.public-base-path:/uploads}") String publicBasePath) {
        this.uploadRoot = Paths.get(uploadDir).toAbsolutePath().normalize();
        this.publicBasePath = publicBasePath;
    }

    @Override
    public String upload(String objectName, InputStream inputStream, long contentLength, String contentType) {
        long startedAt = System.nanoTime();
        log.info("storage.local.upload.start objectName={} contentType={} contentLength={}",
                objectName, contentType, contentLength);
        try {
            Path targetPath = uploadRoot.resolve(objectName).normalize();
            if (!targetPath.startsWith(uploadRoot)) {
                throw new IllegalArgumentException("Invalid upload path.");
            }
            Files.createDirectories(targetPath.getParent());
            Files.copy(inputStream, targetPath, java.nio.file.StandardCopyOption.REPLACE_EXISTING);
            String imageUrl = publicBasePath + "/" + objectName.replace("\\", "/");
            log.info("storage.local.upload.complete objectName={} targetPath={} imageUrl={} elapsedMs={}",
                    objectName, targetPath, imageUrl, elapsedMillis(startedAt));
            return imageUrl;
        } catch (IOException exception) {
            log.error("storage.local.upload.failed objectName={} exceptionType={} elapsedMs={}",
                    objectName, exception.getClass().getName(), elapsedMillis(startedAt), exception);
            throw new IllegalStateException("Local file upload failed.", exception);
        } catch (RuntimeException exception) {
            log.error("storage.local.upload.failed objectName={} exceptionType={} elapsedMs={}",
                    objectName, exception.getClass().getName(), elapsedMillis(startedAt), exception);
            throw exception;
        }
    }

    private long elapsedMillis(long startedAt) {
        return (System.nanoTime() - startedAt) / 1_000_000;
    }
}
