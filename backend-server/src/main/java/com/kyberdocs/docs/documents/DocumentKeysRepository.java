package com.kyberdocs.docs.documents;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface DocumentKeysRepository extends JpaRepository<DocumentKeys, Long> {
    Optional<DocumentKeys> findByDocument(Document document);
}
