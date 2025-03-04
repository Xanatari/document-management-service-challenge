package com.clara.ops.challenge.document_management_service_challenge.dtos.response;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class DocumentResponse {
    private Long id;
    private String message;
}