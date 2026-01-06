package com.kyberdocs.docs.documents;

import jakarta.persistence.*;

@Entity
@Table(
        name = "document_keys",
        uniqueConstraints = {
                @UniqueConstraint(columnNames = "document_id")
        }
)
public class DocumentKeys {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(
            name = "document_id",
            referencedColumnName = "id"
    )
    private Document document;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String kyberCapsule;

    @Column(nullable = false, length = 100)
    private String aesIv;

    @Column(nullable = false, length = 50)
    private String algoIdentifier;

    public Long getId() {
        return id;
    }

    public Document getDocument() {
        return document;
    }

    public void setDocument(Document document) {
        this.document = document;
    }

    public String getKyberCapsule() {
        return kyberCapsule;
    }

    public void setKyberCapsule(String kyberCapsule) {
        this.kyberCapsule = kyberCapsule;
    }

    public String getAesIv() {
        return aesIv;
    }

    public void setAesIv(String aesIv) {
        this.aesIv = aesIv;
    }

    public String getAlgoIdentifier() {
        return algoIdentifier;
    }

    public void setAlgoIdentifier(String algoIdentifier) {
        this.algoIdentifier = algoIdentifier;
    }
}
