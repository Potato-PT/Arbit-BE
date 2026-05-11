package com.arbit.app.storage;

import java.io.InputStream;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;

@Service
@Profile("aws")
public class S3StorageService implements StorageService {

    @Override
    public String upload(String objectName, InputStream inputStream, long contentLength, String contentType) {
        throw new UnsupportedOperationException("AWS S3 storage client is not configured yet.");
    }
}
