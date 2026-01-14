package com.kyberdocs.docs.beneficiaries;

import com.kyberdocs.docs.converters.HexConverter;
import com.kyberdocs.docs.documents.Document;
import com.kyberdocs.docs.users.User;
import jakarta.persistence.*;

@Entity
@Table(name = "beneficiaries")
public class Beneficiary {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "owner_id", nullable = false)
    private User owner;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "linked_user_id", nullable = false)
    private User linkedUser;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "document_id", nullable = false)
    private Document document;

    @Convert(converter = HexConverter.class)
    @Column(name = "kyber_capsule", nullable = false, columnDefinition = "TEXT")
    private byte[] kyberCapsule;

    @Convert(converter = HexConverter.class)
    @Column(name = "encrypted_key", nullable = false, columnDefinition = "TEXT")
    private byte[] encryptedKey; // The wrapped file key


    @Enumerated(EnumType.STRING)
    @Column(name = "access_condition", nullable = false)
    private AccessCondition accessCondition;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public User getOwner() { return owner; }
    public void setOwner(User owner) { this.owner = owner; }
    public User getLinkedUser() { return linkedUser; }
    public void setLinkedUser(User linkedUser) { this.linkedUser = linkedUser; }
    public Document getDocument() { return document; }
    public void setDocument(Document document) { this.document = document; }
    public byte[] getKyberCapsule() { return kyberCapsule; }
    public void setKyberCapsule(byte[] kyberCapsule) { this.kyberCapsule = kyberCapsule; }
    public byte[] getEncryptedKey() { return encryptedKey; }
    public void setEncryptedKey(byte[] encryptedKey) { this.encryptedKey = encryptedKey; }

    public AccessCondition getAccessCondition() { return accessCondition; }
    public void setAccessCondition(AccessCondition accessCondition) { this.accessCondition = accessCondition; }
}