package com.clara.ops.challenge.document_management_service_challenge.service;



import com.clara.ops.challenge.document_management_service_challenge.dtos.response.DocumentResponse;
import com.clara.ops.challenge.document_management_service_challenge.exceptions.InvalidFileTypeException;
import com.clara.ops.challenge.document_management_service_challenge.integrations.minio.MinioService;
import com.clara.ops.challenge.document_management_service_challenge.repository.contract.DocumentRepository;
import com.clara.ops.challenge.document_management_service_challenge.repository.entities.Document;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.time.LocalDateTime;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;


@Service
public class DocumentService {


    private final ExecutorService executorService = Executors.newFixedThreadPool(10); // 10 uploads concurrentes

    private final DocumentRepository documentRepository;
    private final MinioService minioService;


    @Autowired
    public DocumentService(DocumentRepository documentRepository, MinioService minioService) {
        this.documentRepository = documentRepository;
        this.minioService = minioService;
    }

    public DocumentResponse uploadDocument(MultipartFile file, String user, String documentName, List<String> tags) throws Exception {
        // Validate file type
        if (!"application/pdf".equalsIgnoreCase(file.getContentType())) {
            throw new InvalidFileTypeException("Only PDF files are allowed.");
        }

        // Construct the MinIO storage path (e.g., user/documentName.pdf)
        String minioPath = String.format("%s/%s.pdf", user, documentName);

        // Stream file upload to MinIO
        minioService.uploadFile(file.getInputStream(), file.getSize(), minioPath, file.getContentType());

        // Build and persist document metadata
        Document document = Document.builder()
                .user(user)
                .documentName(documentName)
                .tags(tags)
                .minioPath(minioPath)
                .fileSize(file.getSize())
                .fileType(file.getContentType())
                .createdAt(LocalDateTime.now())
                .build();

        document = documentRepository.save(document);

        return new DocumentResponse(document.getId(), "Upload successful");
    }


    public Page<Document> searchDocuments(
            String user,
            String documentName,
            List<String> tags,
            int page,
            int size
    ) {
        // Creamos un Pageable con orden DESC por createdAt
        Pageable pageable = PageRequest.of(page, size, Sort.by("createdAt").descending());

        // Si tags es null o está vacío, usamos un array vacío
        String[] tagsArray = (tags == null || tags.isEmpty())
                ? new String[] {}
                : tags.toArray(new String[0]);

        return documentRepository.searchDocuments(user, documentName, tagsArray, pageable);
    }

    public Document downloadDocument(Long documentId) {
        return documentRepository.findById(documentId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Documento no encontrado"));
    }

    public CompletableFuture<String> uploadDocumentPharaller(MultipartFile file) {
        return CompletableFuture.supplyAsync(() -> {
            try {
                return saveFile(file);
            } catch (IOException e) {
                throw new RuntimeException("Error al guardar el archivo: " + file.getOriginalFilename(), e);
            }
        }, executorService);
    }

    private String saveFile(MultipartFile file) throws IOException {
        String uploadDir = "uploads/";
        File directory = new File(uploadDir);
        if (!directory.exists()) directory.mkdirs();

        Path filePath = Path.of(uploadDir, file.getOriginalFilename());
        Files.copy(file.getInputStream(), filePath, StandardCopyOption.REPLACE_EXISTING);
        return "Archivo guardado: " + file.getOriginalFilename();
    }
}
