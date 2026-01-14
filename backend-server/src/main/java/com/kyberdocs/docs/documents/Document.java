package com.kyberdocs.docs.documents;

import com.kyberdocs.docs.users.User;
import jakarta.persistence.*;
import org.hibernate.annotations.CreationTimestamp;

import java.sql.Timestamp;

@Entity
@Table(name = "documents")
public class Document {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(
            name = "owner_id",
            referencedColumnName = "id"
    )
    private User owner;

    @Column(nullable = false, length = 255)
    private String filename;

    @Column(name = "mime_type", nullable = false, length = 100)
    private String mimeType;

    @Column(nullable = false)
    private Long fileSize;

    @Basic(fetch = FetchType.LAZY)
    @Column(name = "encrypted_data", columnDefinition="bytea", nullable = false)
    private byte[] encryptedData;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private Timestamp createdAt;

    public Long getId() {
        return id;
    }

    public User getOwner() {
        return owner;
    }

    public void setOwner(User owner) {
        this.owner = owner;
    }

    public String getFilename() {
        return filename;
    }

    public void setFilename(String filename) {
        this.filename = filename;
    }

    public String getMimeType() {
        return mimeType;
    }

    public void setMimeType(String mimeType) {
        this.mimeType = mimeType;
    }

    public Long getFileSize() {
        return fileSize;
    }

    public void setFileSize(Long file_size) {
        this.fileSize = file_size;
    }

    public byte[] getEncryptedData() {
        return encryptedData;
    }

    public void setEncryptedData(byte[] encrypted_data) {
        this.encryptedData = encrypted_data;
    }

    public Timestamp getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Timestamp createdAt) {
        this.createdAt = createdAt;
    }
}
