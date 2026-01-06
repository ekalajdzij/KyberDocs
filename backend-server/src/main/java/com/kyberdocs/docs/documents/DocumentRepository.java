package com.kyberdocs.docs.documents;

import com.kyberdocs.docs.users.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface DocumentRepository extends JpaRepository<Document, Long> {
    List<Document> findByOwner(User owner);
}
