package com.clara.ops.challenge.document_management_service_challenge.integrations.minio;

import java.io.InputStream;

public interface MinioService {
    void uploadFile(InputStream inputStream, long size, String path, String contentType) throws Exception;
    String generatePreSignedUrl(String path, int expiryInMinutes) throws Exception;
}