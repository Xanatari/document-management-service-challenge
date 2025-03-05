package com.clara.ops.challenge.document_management_service_challenge.controller;

import com.clara.ops.challenge.document_management_service_challenge.service.DocumentService;
import org.mockito.Mockito;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;

@TestConfiguration
public class DocumentControllerTestConfig {

    @Bean
    public DocumentService documentService() {
        // Retorna un mock de DocumentService usando Mockito
        return Mockito.mock(DocumentService.class);
    }
}