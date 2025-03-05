--The script to initialize the schema was sourced from the Spring Batch Core dependency: org.springframework.batch.core.

CREATE TABLE documents (
    id SERIAL PRIMARY KEY,
    user_id VARCHAR(255) NOT NULL,
    document_name VARCHAR(255) NOT NULL,
    tags TEXT[], -- Using PostgreSQL array type; alternatively, use a join table for normalized tags
    minio_path VARCHAR(512) NOT NULL,
    file_size BIGINT NOT NULL,
    file_type VARCHAR(100) NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- Indexes to improve query performance
CREATE INDEX idx_documents_user ON documents (user_id);
CREATE INDEX idx_documents_document_name ON documents (document_name);
CREATE INDEX idx_documents_created_at ON documents (created_at DESC);