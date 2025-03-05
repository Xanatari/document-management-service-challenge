package com.clara.ops.challenge.document_management_service_challenge.service;


import com.clara.ops.challenge.document_management_service_challenge.dtos.response.DocumentResponse;
import com.clara.ops.challenge.document_management_service_challenge.exceptions.InvalidFileTypeException;
import com.clara.ops.challenge.document_management_service_challenge.integrations.minio.MinioService;
import com.clara.ops.challenge.document_management_service_challenge.repository.contract.DocumentRepository;
import com.clara.ops.challenge.document_management_service_challenge.repository.entities.Document;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.mock.web.MockMultipartFile;

import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

public class DocumentServiceTest {

    private final DocumentRepository documentRepository = Mockito.mock(DocumentRepository.class);
    private final MinioService minioService = Mockito.mock(MinioService.class);
    private final DocumentService documentService = new DocumentService(documentRepository, minioService);

    @Test
    public void testUploadDocumentSuccess() throws Exception {
        byte[] content = "Test PDF Content".getBytes();
        MockMultipartFile file = new MockMultipartFile("file", "sample.pdf",
                "application/pdf", content);
        String user = "user1";
        String documentName = "sample";
        List<String> tags = List.of("tag1", "tag2");

        // Prepare a saved Document instance
        Document savedDocument = Document.builder()
                .id(1L)
                .user(user)
                .documentName(documentName)
                .tags(tags)
                .minioPath(String.format("%s/%s.pdf", user, documentName))
                .fileSize((long) content.length)
                .fileType("application/pdf")
                .createdAt(LocalDateTime.now())
                .build();
        Mockito.when(documentRepository.save(Mockito.any(Document.class)))
                .thenReturn(savedDocument);

        DocumentResponse response = documentService.uploadDocument(file, user, documentName, tags);
        assertNotNull(response);
        assertEquals(1L, response.getId());
        assertEquals("Upload successful", response.getMessage());

        // Verify that the MinioService was called with the correct parameters.
        Mockito.verify(minioService).uploadFile(Mockito.any(),
                Mockito.eq((long) content.length),
                Mockito.eq(user + "/" + documentName + ".pdf"),
                Mockito.eq("application/pdf"));
    }

    @Test
    public void testUploadInvalidFileType() {
        byte[] content = "Test content".getBytes();
        MockMultipartFile file = new MockMultipartFile("file", "sample.txt",
                "text/plain", content);
        String user = "user1";
        String documentName = "sample";
        List<String> tags = List.of("tag1", "tag2");

        Exception exception = assertThrows(InvalidFileTypeException.class, () -> {
            documentService.uploadDocument(file, user, documentName, tags);
        });
        assertEquals("Only PDF files are allowed.", exception.getMessage());
    }
}
