package com.kyberdocs.docs.beneficiaries.dto;

public class AddBeneficiaryRequestDto {

    private Long documentId;
    private String targetUsername;

    public AddBeneficiaryRequestDto() {}

    public Long getDocumentId() {
        return documentId;
    }

    public void setDocumentId(Long documentId) {
        this.documentId = documentId;
    }

    public String getTargetUsername() {
        return targetUsername;
    }

    public void setTargetUsername(String targetUsername) {
        this.targetUsername = targetUsername;
    }
}