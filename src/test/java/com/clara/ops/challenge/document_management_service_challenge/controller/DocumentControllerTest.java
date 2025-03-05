package com.clara.ops.challenge.document_management_service_challenge.controller;


import com.clara.ops.challenge.document_management_service_challenge.dtos.response.DocumentResponse;
import com.clara.ops.challenge.document_management_service_challenge.exceptions.InvalidFileTypeException;
import com.clara.ops.challenge.document_management_service_challenge.repository.entities.Document;
import com.clara.ops.challenge.document_management_service_challenge.service.DocumentService;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;

import org.springframework.context.annotation.Import;

import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;

import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(DocumentController.class)
@Import(DocumentControllerTestConfig.class)
public class DocumentControllerTest {

    @Autowired
    private MockMvc mockMvc;

    // Inyecta un mock del servicio para que el controlador pueda autoconfigurarse
    @Autowired
    private DocumentService documentService;

    @Test
    public void testUploadDocumentSuccess() throws Exception {
        // Prepara un archivo PDF de prueba
        MockMultipartFile file = new MockMultipartFile("file", "sample.pdf",
                "application/pdf", "Test Content".getBytes());

        String user = "user1";
        String documentName = "sample";
        // Simula una respuesta exitosa del servicio
        DocumentResponse response = new DocumentResponse(1L, "Upload successful");
        when(documentService.uploadDocument(Mockito.any(), Mockito.eq(user),
                Mockito.eq(documentName), Mockito.anyList())).thenReturn(response);

        mockMvc.perform(multipart("/documents")
                        .file(file)
                        .param("user", user)
                        .param("documentName", documentName)
                        .param("tags", "tag1", "tag2"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.message").value("Upload successful"));
    }

    @Test
    public void testUploadInvalidFileType() throws Exception {
        // Prepara un archivo que no sea PDF (por ejemplo, un archivo de texto)
        MockMultipartFile file = new MockMultipartFile("file", "sample.txt",
                "text/plain", "Test Content".getBytes());
        String user = "user1";
        String documentName = "sample";

        // Simula que el servicio lance la excepción correspondiente
        when(documentService.uploadDocument(Mockito.any(), Mockito.eq(user),
                        Mockito.eq(documentName), Mockito.anyList()))
                .thenThrow(new InvalidFileTypeException("Only PDF files are allowed."));

        mockMvc.perform(multipart("/documents")
                        .file(file)
                        .param("user", user)
                        .param("documentName", documentName)
                        .param("tags", "tag1", "tag2"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errorCode").value("INVALID_FILE_TYPE"));
    }

    @Test
    public void testDownloadDocument_Success() throws Exception {
        // Simular documento encontrado
        Document document = Document.builder()
                .id(1L)
                .documentName("Contrato")
                .tags(List.of("legal", "importante"))
                .build();

        when(documentService.downloadDocument(1L)).thenReturn(document);

        // Ejecutar la petición GET
        mockMvc.perform(get("/documents/1/download"))
                .andExpect(status().isOk())
                .andExpect(content().string("Documento: Contrato\nTags: legal, importante"))
                .andExpect(header().string("Content-Disposition", "attachment; filename=Contrato.txt"))
                .andExpect(header().string("Content-Type", "text/plain; charset=UTF-8"));

        verify(documentService, times(1)).downloadDocument(1L);
    }

    @Test
    public void testDownloadDocument_NotFound() throws Exception {
        when(documentService.downloadDocument(99L)).thenThrow(new RuntimeException("Documento no encontrado"));

        // Ejecutar la petición GET para un documento inexistente
        mockMvc.perform(get("/documents/99/download"))
                .andExpect(status().isNotFound());

        verify(documentService, times(1)).downloadDocument(99L);
    }
}