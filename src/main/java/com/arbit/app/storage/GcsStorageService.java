package com.arbit.app.storage;

import com.google.cloud.storage.BlobInfo;
import com.google.cloud.storage.Storage;
import com.google.cloud.storage.StorageOptions;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.stream.Collectors;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.util.UriUtils;

@Service
@Profile("gcp")
public class GcsStorageService implements StorageService {

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
    }

    @Override
    public String upload(String objectName, InputStream inputStream, long contentLength, String contentType) {
        BlobInfo blobInfo = BlobInfo.newBuilder(bucketName, objectName)
                .setContentType(contentType)
                .setCacheControl("public, max-age=31536000")
                .build();
        try {
            storage.createFrom(blobInfo, inputStream);
            return publicBaseUrl + "/" + encodeObjectName(objectName);
        } catch (IOException exception) {
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
}
