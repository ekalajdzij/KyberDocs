package com.kyberdocs.docs.beneficiaries;

import com.kyberdocs.docs.documents.Document;
import com.kyberdocs.docs.users.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface BeneficiaryRepository extends JpaRepository<Beneficiary, Long> {

    List<Beneficiary> findByOwner(User owner);

    Optional<Beneficiary> findByDocumentAndLinkedUser(Document document, User linkedUser);

    boolean existsByOwnerAndLinkedUserAndDocument(User owner, User linkedUser, Document document);

    List<Beneficiary> findByLinkedUser(User linkedUser);

    void deleteByDocument(Document document);

}