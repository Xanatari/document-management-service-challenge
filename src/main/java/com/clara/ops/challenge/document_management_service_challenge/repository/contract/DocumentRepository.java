package com.clara.ops.challenge.document_management_service_challenge.repository.contract;


import com.clara.ops.challenge.document_management_service_challenge.repository.entities.Document;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface DocumentRepository extends JpaRepository<Document, Long> {


    @Query(value = """
      SELECT * 
      FROM documents d
      WHERE (:user IS NULL OR d.user = :user)
        AND (:documentName IS NULL OR d.document_name = :documentName)
        AND (:tags IS NULL OR :tags = '{}' OR d.tags && CAST(:tags AS text[]))
      """,
            countQuery = """
      SELECT count(*) 
      FROM documents d
      WHERE (:user IS NULL OR d.user = :user)
        AND (:documentName IS NULL OR d.document_name = :documentName)
        AND (:tags IS NULL OR :tags = '{}' OR d.tags && CAST(:tags AS text[]))
      """,
            nativeQuery = true
    )
    Page<Document> searchDocuments(
            @Param("user") String user,
            @Param("documentName") String documentName,
            @Param("tags") String[] tags,
            Pageable pageable
    );
}