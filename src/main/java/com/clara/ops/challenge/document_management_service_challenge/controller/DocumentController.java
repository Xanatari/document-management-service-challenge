package com.clara.ops.challenge.document_management_service_challenge.controller;


import com.clara.ops.challenge.document_management_service_challenge.dtos.request.DocumentDTO;
import com.clara.ops.challenge.document_management_service_challenge.dtos.response.DocumentResponse;
import com.clara.ops.challenge.document_management_service_challenge.repository.entities.Document;
import com.clara.ops.challenge.document_management_service_challenge.service.DocumentService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.nio.charset.StandardCharsets;
import java.util.List;

@RestController
@RequestMapping("/documents")
public class DocumentController {

    private final DocumentService documentService;

    @Autowired
    public DocumentController(DocumentService documentService) {
        this.documentService = documentService;
    }

    @PostMapping
    public ResponseEntity<DocumentResponse> uploadDocument(
            @RequestParam("file") MultipartFile file,
            @RequestParam("user") String user,
            @RequestParam("documentName") String documentName,
            @RequestParam("tags") List<String> tags
    ) throws Exception {
        DocumentResponse response = documentService.uploadDocument(file, user, documentName, tags);
        return ResponseEntity.ok(response);
    }


    @GetMapping
    public ResponseEntity<Page<DocumentDTO>> searchDocuments(
            @RequestParam(value = "user", required = false) String user,
            @RequestParam(value = "documentName", required = false) String documentName,
            @RequestParam(value = "tags", required = false) List<String> tags,
            @RequestParam(value = "page", defaultValue = "0") int page,
            @RequestParam(value = "size", defaultValue = "10") int size
    ) {
        Page<Document> documentsPage = documentService.searchDocuments(user, documentName, tags, page, size);

        // Convertimos cada Document en un DocumentDTO
        Page<DocumentDTO> dtoPage = documentsPage.map(DocumentDTO::fromEntity);

        return ResponseEntity.ok(dtoPage);
    }

    @Operation(summary = "Descargar un documento por ID", description = "Retorna el documento en formato texto.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Documento encontrado"),
            @ApiResponse(responseCode = "404", description = "Documento no encontrado")
    })
    @GetMapping("/{id}/download")
    public ResponseEntity<byte[]> downloadDocument(@PathVariable Long id) {
        Document document = documentService.downloadDocument(id);

        String content = "Documento: " + document.getDocumentName() + "\nTags: " + String.join(", ", document.getTags());
        byte[] contentBytes = content.getBytes(StandardCharsets.UTF_8);

        HttpHeaders headers = new HttpHeaders();
        headers.add(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=" + document.getDocumentName() + ".txt");
        headers.add(HttpHeaders.CONTENT_TYPE, "text/plain; charset=UTF-8");

        return new ResponseEntity<>(contentBytes, headers, HttpStatus.OK);
    }
}
