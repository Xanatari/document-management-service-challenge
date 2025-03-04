package com.clara.ops.challenge.document_management_service_challenge.integrations.minio;
import io.minio.BucketExistsArgs;
import io.minio.GetPresignedObjectUrlArgs;
import io.minio.MakeBucketArgs;
import io.minio.MinioClient;
import io.minio.PutObjectArgs;
import io.minio.errors.MinioException;
import io.minio.http.Method;
import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;


import java.io.InputStream;

@Service
@Slf4j
public class MinioServiceImpl implements MinioService {

    private final MinioClient minioClient;
    private final String bucketName;

    public MinioServiceImpl(
            @Value("${minio.url}") String minioUrl,
            @Value("${minio.accessKey}") String minioAccessKey,
            @Value("${minio.secretKey}") String minioSecretKey,
            @Value("${minio.bucketName:document-bucket}") String bucketName) {
        this.minioClient = MinioClient.builder()
                .endpoint(minioUrl)
                .credentials(minioAccessKey, minioSecretKey)
                .build();
        this.bucketName = bucketName;
    }

    // Ensure the bucket exists on startup.
    @PostConstruct
    public void init() {
        try {
            boolean exists = minioClient.bucketExists(BucketExistsArgs.builder().bucket(bucketName).build());
            if (!exists) {
                minioClient.makeBucket(MakeBucketArgs.builder().bucket(bucketName).build());
                log.info("Bucket '{}' created.", bucketName);
            } else {
                log.info("Bucket '{}' already exists.", bucketName);
            }
        } catch (Exception e) {
            log.error("Error initializing bucket: {}", e.getMessage());
            throw new RuntimeException("Could not initialize MinIO bucket", e);
        }
    }

    @Override
    public void uploadFile(InputStream inputStream, long size, String path, String contentType) throws Exception {
        try {
            // Upload file to MinIO using streaming to ensure low memory usage.
            minioClient.putObject(
                    PutObjectArgs.builder()
                            .bucket(bucketName)
                            .object(path)
                            .stream(inputStream, size, -1)
                            .contentType(contentType)
                            .build()
            );
            log.info("File uploaded successfully to path: {}/{}", bucketName, path);
        } catch (MinioException e) {
            log.error("MinIO upload error: {}", e.getMessage());
            throw new Exception("Error uploading file to storage", e);
        }
    }

    @Override
    public String generatePreSignedUrl(String path, int expiryInMinutes) throws Exception {
        try {
            // Convert expiry time to seconds.
            int expirySeconds = expiryInMinutes * 60;
            String url = minioClient.getPresignedObjectUrl(
                    GetPresignedObjectUrlArgs.builder()
                            .method(Method.GET)
                            .bucket(bucketName)
                            .object(path)
                            .expiry(expirySeconds)
                            .build()
            );
            log.info("Generated pre-signed URL for path {}: {}", path, url);
            return url;
        } catch (MinioException e) {
            log.error("MinIO URL generation error: {}", e.getMessage());
            throw new Exception("Error generating pre-signed URL", e);
        }
    }
}