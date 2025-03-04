package com.clara.ops.challenge.document_management_service_challenge.repository.contract;


import com.clara.ops.challenge.document_management_service_challenge.repository.entities.Document;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface DocumentRepository extends JpaRepository<Document, Long> {
}