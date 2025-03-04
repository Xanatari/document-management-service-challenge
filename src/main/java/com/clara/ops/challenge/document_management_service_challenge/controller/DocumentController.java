package com.clara.ops.challenge.document_management_service_challenge.controller;


import com.clara.ops.challenge.document_management_service_challenge.dtos.response.DocumentResponse;
import com.clara.ops.challenge.document_management_service_challenge.service.DocumentService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

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
}
