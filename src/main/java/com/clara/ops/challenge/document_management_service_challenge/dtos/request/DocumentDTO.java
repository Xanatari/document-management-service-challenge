package com.clara.ops.challenge.document_management_service_challenge.dtos.request;

import com.clara.ops.challenge.document_management_service_challenge.repository.entities.Document;
import lombok.*;

import java.time.LocalDateTime;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class DocumentDTO {

    private Long id;
    private String user;
    private String documentName;
    private List<String> tags;
    private String minioPath;
    private Long fileSize;
    private String fileType;
    private LocalDateTime createdAt;

    public static DocumentDTO fromEntity(Document doc) {
        DocumentDTO dto = new DocumentDTO();
        dto.setId(doc.getId());
        dto.setUser(doc.getUser());
        dto.setDocumentName(doc.getDocumentName());
        dto.setTags(doc.getTags());
        dto.setMinioPath(doc.getMinioPath());
        dto.setFileSize(doc.getFileSize());
        dto.setFileType(doc.getFileType());
        dto.setCreatedAt(doc.getCreatedAt());
        return dto;
    }
}