package com.clara.ops.challenge.document_management_service_challenge.repository.entities;

import jakarta.persistence.*;
import lombok.*;


import java.time.LocalDateTime;
import java.util.List;

@Entity
@Table(name = "documents")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Document {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "user_id")
    private String user;

    @Column(name = "document_name")
    private String documentName;

    @Column(name = "tags", columnDefinition = "text[]")
    private String[] tags;

    @Column(name = "minio_path")
    private String minioPath;

    @Column(name = "file_size")
    private Long fileSize;

    @Column(name = "file_type")
    private String fileType;

    @Column(name = "created_at")
    private LocalDateTime createdAt;
}