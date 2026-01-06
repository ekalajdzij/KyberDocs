package com.kyberdocs.docs.documents.dto;

import com.kyberdocs.docs.users.User;
import java.sql.Timestamp;

public class DocumentSummaryDto {
    private Long id;
    private String filename;
    private Long fileSize;
    private String mimeType;
    private Timestamp createdAt;
    private OwnerInfo owner;

    public DocumentSummaryDto(Long id, String filename, Long fileSize, String mimeType, Timestamp createdAt, User owner) {
        this.id = id;
        this.filename = filename;
        this.fileSize = fileSize;
        this.mimeType = mimeType;
        this.createdAt = createdAt;
        if (owner != null) {
            this.owner = new OwnerInfo(owner);
        }
    }

    public Long getId() {
        return id;
    }
    public String getFilename() {
        return filename;
    }
    public Long getFileSize() {
        return fileSize;
    }
    public String getMimeType() {
        return mimeType;
    }
    public Timestamp getCreatedAt() {
        return createdAt;
    }
    public OwnerInfo getOwner() {
        return owner;
    }

    public static class OwnerInfo {
        private Long id;
        private String username;
        private String email;

        public OwnerInfo(User user) {
            this.id = user.getId();
            this.username = user.getUsername();
            this.email = user.getEmail();
        }

        public Long getId() {
            return id;
        }
        public String getUsername() {
            return username;
        }
        public String getEmail() {
            return email;
        }
    }
}