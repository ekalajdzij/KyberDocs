package com.kyberdocs.docs.beneficiaries.dto;

import com.kyberdocs.docs.beneficiaries.Beneficiary;

public class BeneficiaryDto {

    private Long id;
    private String ownerUsername;
    private String beneficiaryUsername;
    private Long documentId;
    private String documentName;

    public BeneficiaryDto() {
    }

    public BeneficiaryDto(Beneficiary beneficiary) {
        this.id = beneficiary.getId();

        // 1. Map Owner
        if (beneficiary.getOwner() != null) {
            this.ownerUsername = beneficiary.getOwner().getUsername();
        }

        // 2. Map Beneficiary (The 'Linked User')
        if (beneficiary.getLinkedUser() != null) {
            this.beneficiaryUsername = beneficiary.getLinkedUser().getUsername();
        }

        // 3. Map Document Details
        if (beneficiary.getDocument() != null) {
            this.documentId = beneficiary.getDocument().getId();
            this.documentName = beneficiary.getDocument().getFilename(); // or getTitle()
        }
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getOwnerUsername() {
        return ownerUsername;
    }

    public void setOwnerUsername(String ownerUsername) {
        this.ownerUsername = ownerUsername;
    }

    public String getBeneficiaryUsername() {
        return beneficiaryUsername;
    }

    public void setBeneficiaryUsername(String beneficiaryUsername) {
        this.beneficiaryUsername = beneficiaryUsername;
    }

    public Long getDocumentId() {
        return documentId;
    }

    public void setDocumentId(Long documentId) {
        this.documentId = documentId;
    }

    public String getDocumentName() {
        return documentName;
    }

    public void setDocumentName(String documentName) {
        this.documentName = documentName;
    }
}