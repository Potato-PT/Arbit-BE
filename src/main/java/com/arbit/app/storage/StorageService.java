package com.arbit.app.storage;

import java.io.InputStream;

public interface StorageService {

    String upload(String objectName, InputStream inputStream, long contentLength, String contentType);
}
