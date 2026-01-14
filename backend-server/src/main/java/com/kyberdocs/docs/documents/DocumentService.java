package com.kyberdocs.docs.documents;

import com.kyberdocs.docs.audit.AuditService;
import com.kyberdocs.docs.beneficiaries.AccessCondition;
import com.kyberdocs.docs.beneficiaries.Beneficiary;
import com.kyberdocs.docs.beneficiaries.BeneficiaryRepository;
import com.kyberdocs.docs.converters.HexConverter;
import com.kyberdocs.docs.documents.dto.DocumentSummaryDto;
import com.kyberdocs.docs.kyber.dto.KyberDecapsulateResponse;
import com.kyberdocs.docs.kyber.dto.KyberEncapsulateResponse;
import com.kyberdocs.docs.security.SecurityUtil;
import com.kyberdocs.docs.users.User;
import com.kyberdocs.docs.users.UserStatus;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class DocumentService {

    private final DocumentRepository documentRepository;
    private final DocumentKeysRepository documentKeysRepository;
    private final BeneficiaryRepository beneficiaryRepository;
    private final RestTemplate restTemplate;
    private final SecurityUtil securityUtil;
    private final AuditService auditService;

    @Value("${python.server.url}")
    private String pythonServerUrl;

    public DocumentService(DocumentRepository documentRepository,
                           DocumentKeysRepository documentKeysRepository,
                           BeneficiaryRepository beneficiaryRepository,
                           RestTemplate restTemplate,
                           SecurityUtil securityUtil,
                           AuditService auditService) {
        this.documentRepository = documentRepository;
        this.documentKeysRepository = documentKeysRepository;
        this.beneficiaryRepository = beneficiaryRepository;
        this.restTemplate = restTemplate;
        this.securityUtil = securityUtil;
        this.auditService = auditService;
    }

    public String getFilename(Long id) {
        return documentRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Document not found"))
                .getFilename();
    }

    @Transactional(readOnly = true)
    public List<DocumentSummaryDto> getAllDocuments(User user) {
        return documentRepository.findSummariesByOwner(user);
    }

    @Transactional(readOnly = true)
    public List<DocumentSummaryDto> getAllSystemDocuments() {
        return documentRepository.findAllSummaries();
    }

    @Transactional
    public void deleteDocument(Long id, User user) {
        Document doc = documentRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Document not found"));

        boolean isOwner = doc.getOwner().getId().equals(user.getId());
        boolean isAdmin = user.getRole().name().contains("ADMIN");

        if (!isOwner && !isAdmin) {
            throw new RuntimeException("Unauthorized: You do not own this document");
        }

        // Delete associated keys (owner's keys)
        documentKeysRepository.findByDocument(doc)
                .ifPresent(documentKeysRepository::delete);

        // Delete associated beneficiaries (shared keys)
        beneficiaryRepository.deleteByDocument(doc);

        // Delete the document itself
        documentRepository.delete(doc);

        auditService.logEvent(user, "DELETE", "Deleted document: " + doc.getFilename());
    }

    @Transactional
    public Document uploadDocument(User user, MultipartFile file) throws IOException {
        String requestUrl = pythonServerUrl + "api/kyber/encapsulate";
        Map<String, String> requestBody = new HashMap<>();
        requestBody.put("publicKeyHex", HexConverter.bytesToHex(user.getKyberPublicKey()));
        requestBody.put("parameterSet", "kyber512");

        KyberEncapsulateResponse kyberResponse = restTemplate.postForObject(
                requestUrl, requestBody, KyberEncapsulateResponse.class
        );

        if (kyberResponse == null) throw new RuntimeException("Kyber Encapsulation failed");

        byte[] fileBytes = file.getBytes();

        // Encrypt the file (IV is prepended to the output automatically by SecurityUtil)
        byte[] sharedSecret = HexConverter.hexToBytes(kyberResponse.getSharedSecretHashHex());
        byte[] encryptedFileBytes = securityUtil.encryptFile(fileBytes, sharedSecret);
        byte[] ivBytes = Arrays.copyOfRange(encryptedFileBytes, 0, 12);

        Document doc = new Document();
        doc.setOwner(user);
        doc.setFilename(file.getOriginalFilename());
        doc.setMimeType(file.getContentType());
        doc.setFileSize(file.getSize());
        doc.setEncryptedData(encryptedFileBytes);
        doc = documentRepository.save(doc);

        DocumentKeys keys = new DocumentKeys();
        keys.setDocument(doc);
        keys.setKyberCapsule(HexConverter.hexToBytes(kyberResponse.getCiphertextHex()));
        keys.setAlgoIdentifier("kyber512");
        keys.setAesIv(HexConverter.bytesToHex(ivBytes));
        documentKeysRepository.save(keys);

        auditService.logEvent(user, "UPLOAD", "Uploaded encrypted file: " + doc.getFilename());

        return doc;
    }

    @Transactional
    public byte[] downloadAndDecryptDocument(Long documentId, User user) {
        Document doc = documentRepository.findById(documentId)
                .orElseThrow(() -> new RuntimeException("Document not found"));

        byte[] finalFileKey = null;

        // User is the OWNER
        if (doc.getOwner().getId().equals(user.getId())) {
            DocumentKeys keys = documentKeysRepository.findByDocument(doc)
                    .orElseThrow(() -> new RuntimeException("Keys not found"));

            finalFileKey = getSharedSecretFromKyber(user, keys.getKyberCapsule());
        }
        // User is a BENEFICIARY
        else {
            Optional<Beneficiary> benOpt = beneficiaryRepository.findByDocumentAndLinkedUser(doc, user);

            if (benOpt.isPresent()) {
                Beneficiary ben = benOpt.get();

                // Dead Man's Switch check
                if(ben.getAccessCondition() == AccessCondition.ON_INACTIVITY) {
                    if (doc.getOwner().getStatus() == UserStatus.STATUS_ACTIVE || doc.getOwner().getStatus() == UserStatus.STATUS_WARNING) {
                        throw new RuntimeException("Access Denied: This document is time-locked by the Dead Man's Switch protocol.");
                    }
                }

                // A. Decrypt the Wrapper using Beneficiary's Private Key
                byte[] wrapperKey = getSharedSecretFromKyber(user, ben.getKyberCapsule());

                // B. Unwrap the Master Key using the Wrapper Key
                finalFileKey = securityUtil.decryptFile(ben.getEncryptedKey(), wrapperKey);
            } else if (user.getRole().name().contains("ADMIN")) {
                throw new RuntimeException("Admins cannot decrypt user files without explicit sharing");
            } else {
                throw new RuntimeException("Unauthorized access to document");
            }
        }

        byte[] decryptedBytes = securityUtil.decryptFile(
                doc.getEncryptedData(),
                finalFileKey
        );

        auditService.logEvent(user, "DOWNLOAD", "Decrypted file: " + doc.getFilename());
        return decryptedBytes;
    }

    private byte[] getSharedSecretFromKyber(User user, byte[] capsuleBytes) {
        byte[] userPrivateKeyPlain = securityUtil.decrypt(user.getKyberSecretKey());

        try {
            String requestUrl = pythonServerUrl + "api/kyber/decapsulate";
            Map<String, String> requestBody = new HashMap<>();

            requestBody.put("ciphertextHex", HexConverter.bytesToHex(capsuleBytes));
            requestBody.put("secretKeyHex", HexConverter.bytesToHex(userPrivateKeyPlain));
            requestBody.put("parameterSet", "kyber512");

            KyberDecapsulateResponse kyberResponse = restTemplate.postForObject(requestUrl, requestBody, KyberDecapsulateResponse.class);
            if (kyberResponse == null) throw new RuntimeException("Decapsulation failed");

            return HexConverter.hexToBytes(kyberResponse.getSharedSecretHashHex());
        } finally {
            // ZEROING: Clear plain private key from memory
            Arrays.fill(userPrivateKeyPlain, (byte) 0);
        }
    }
}