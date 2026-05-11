package com.arbit.app.storage;

import java.io.InputStream;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;

@Service
@Profile("gcp")
public class GcsStorageService implements StorageService {

    @Override
    public String upload(String objectName, InputStream inputStream, long contentLength, String contentType) {
        throw new UnsupportedOperationException("Google Cloud Storage client is not configured yet.");
    }
}
