package com.kyberdocs.docs.documents;

import com.kyberdocs.docs.documents.dto.DocumentSummaryDto;
import com.kyberdocs.docs.users.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface DocumentRepository extends JpaRepository<Document, Long> {
    List<Document> findByOwner(User owner);

    @Query("SELECT new com.kyberdocs.docs.documents.dto.DocumentSummaryDto(" +
            "d.id, d.filename, d.fileSize, d.mimeType, d.createdAt, d.owner) " +
            "FROM Document d WHERE d.owner = :owner")
    List<DocumentSummaryDto> findSummariesByOwner(@Param("owner") User owner);

    @Query("SELECT new com.kyberdocs.docs.documents.dto.DocumentSummaryDto(" +
            "d.id, d.filename, d.fileSize, d.mimeType, d.createdAt, d.owner) " +
            "FROM Document d")
    List<DocumentSummaryDto> findAllSummaries();
}
