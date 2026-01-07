package com.kyberdocs.docs.documents;

import com.kyberdocs.docs.converters.HexConverter;
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

    @Convert(converter = HexConverter.class)
    @Column(name = "kyber_capsule", nullable = false, columnDefinition = "TEXT")
    private byte[] kyberCapsule;

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

    public byte[] getKyberCapsule() { return kyberCapsule; }

    public void setKyberCapsule(byte[] kyberCapsule) { this.kyberCapsule = kyberCapsule; }

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
