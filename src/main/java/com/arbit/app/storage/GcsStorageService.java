package com.arbit.app.storage;

import com.google.cloud.storage.BlobInfo;
import com.google.cloud.storage.Storage;
import com.google.cloud.storage.StorageOptions;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.stream.Collectors;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.util.UriUtils;

@Service
@Profile("gcp")
public class GcsStorageService implements StorageService {

    private static final Logger log = LoggerFactory.getLogger(GcsStorageService.class);

    private final Storage storage;
    private final String bucketName;
    private final String publicBaseUrl;

    public GcsStorageService(@Value("${storage.gcs.bucket-name}") String bucketName,
                             @Value("${storage.gcs.project-id:}") String projectId,
                             @Value("${storage.gcs.public-base-url:}") String publicBaseUrl) {
        StorageOptions.Builder storageOptionsBuilder = StorageOptions.newBuilder();
        if (StringUtils.hasText(projectId)) {
            storageOptionsBuilder.setProjectId(projectId);
        }
        this.storage = storageOptionsBuilder.build().getService();
        this.bucketName = bucketName;
        this.publicBaseUrl = StringUtils.hasText(publicBaseUrl)
                ? removeTrailingSlash(publicBaseUrl)
                : "https://storage.googleapis.com/" + bucketName;
        log.info("storage.gcs.initialized bucketName={} projectIdConfigured={} publicBaseUrl={}",
                bucketName, StringUtils.hasText(projectId), this.publicBaseUrl);
    }

    @Override
    public String upload(String objectName, InputStream inputStream, long contentLength, String contentType) {
        long startedAt = System.nanoTime();
        log.info("storage.gcs.upload.start bucketName={} objectName={} contentType={} contentLength={}",
                bucketName, objectName, contentType, contentLength);
        BlobInfo blobInfo = BlobInfo.newBuilder(bucketName, objectName)
                .setContentType(contentType)
                .setCacheControl("public, max-age=31536000")
                .build();
        try {
            storage.createFrom(blobInfo, inputStream);
            String imageUrl = publicBaseUrl + "/" + encodeObjectName(objectName);
            log.info("storage.gcs.upload.complete bucketName={} objectName={} imageUrl={} elapsedMs={}",
                    bucketName, objectName, imageUrl, elapsedMillis(startedAt));
            return imageUrl;
        } catch (IOException exception) {
            log.error("storage.gcs.upload.failed bucketName={} objectName={} exceptionType={} elapsedMs={}",
                    bucketName, objectName, exception.getClass().getName(), elapsedMillis(startedAt), exception);
            throw new IllegalStateException("GCS upload failed.", exception);
        } catch (RuntimeException exception) {
            log.error("storage.gcs.upload.failed bucketName={} objectName={} exceptionType={} elapsedMs={}",
                    bucketName, objectName, exception.getClass().getName(), elapsedMillis(startedAt), exception);
            throw new IllegalStateException("GCS upload failed.", exception);
        }
    }

    private String encodeObjectName(String objectName) {
        return Arrays.stream(objectName.split("/"))
                .map(segment -> UriUtils.encodePathSegment(segment, StandardCharsets.UTF_8))
                .collect(Collectors.joining("/"));
    }

    private String removeTrailingSlash(String value) {
        return value.endsWith("/") ? value.substring(0, value.length() - 1) : value;
    }

    private long elapsedMillis(long startedAt) {
        return (System.nanoTime() - startedAt) / 1_000_000;
    }
}
