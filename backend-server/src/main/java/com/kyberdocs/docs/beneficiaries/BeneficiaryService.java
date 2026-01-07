package com.kyberdocs.docs.beneficiaries;

import com.kyberdocs.docs.beneficiaries.dto.BeneficiaryDto;
import com.kyberdocs.docs.converters.HexConverter;
import com.kyberdocs.docs.documents.Document;
import com.kyberdocs.docs.documents.DocumentKeys;
import com.kyberdocs.docs.documents.DocumentKeysRepository;
import com.kyberdocs.docs.documents.DocumentRepository;
import com.kyberdocs.docs.kyber.dto.KyberDecapsulateResponse;
import com.kyberdocs.docs.kyber.dto.KyberEncapsulateResponse;
import com.kyberdocs.docs.security.SecurityUtil;
import com.kyberdocs.docs.users.User;
import com.kyberdocs.docs.users.UserRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestTemplate;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class BeneficiaryService {

    private final BeneficiaryRepository beneficiaryRepository;
    private final UserRepository userRepository;
    private final DocumentRepository documentRepository;
    private final DocumentKeysRepository documentKeysRepository;
    private final SecurityUtil securityUtil;
    private final RestTemplate restTemplate;

    @Value("${python.server.url}")
    private String pythonServerUrl;

    public BeneficiaryService(BeneficiaryRepository beneficiaryRepository,
                              UserRepository userRepository,
                              DocumentRepository documentRepository,
                              DocumentKeysRepository documentKeysRepository,
                              SecurityUtil securityUtil,
                              RestTemplate restTemplate) {
        this.beneficiaryRepository = beneficiaryRepository;
        this.userRepository = userRepository;
        this.documentRepository = documentRepository;
        this.documentKeysRepository = documentKeysRepository;
        this.securityUtil = securityUtil;
        this.restTemplate = restTemplate;
    }

    public List<BeneficiaryDto> getBeneficiariesForUser(User user) {
        return beneficiaryRepository.findByOwner(user).stream()
                .map(BeneficiaryDto::new)
                .collect(Collectors.toList());
    }

    @Transactional
    public BeneficiaryDto addBeneficiary(User owner, Long documentId, String targetUsername) {
        Document doc = documentRepository.findById(documentId)
                .orElseThrow(() -> new RuntimeException("Document not found"));

        if (!doc.getOwner().getId().equals(owner.getId())) {
            throw new RuntimeException("You are not the owner of this document");
        }

        User targetUser = userRepository.findByUsername(targetUsername)
                .orElseThrow(() -> new RuntimeException("Target user not found"));

        if (beneficiaryRepository.existsByOwnerAndLinkedUserAndDocument(owner, targetUser, doc)) {
            throw new RuntimeException("User is already a beneficiary");
        }

        DocumentKeys ownerKeys = documentKeysRepository.findByDocument(doc)
                .orElseThrow(() -> new RuntimeException("Keys not found"));

        // Unwrap Owner's Private Key
        byte[] ownerPrivateKey = securityUtil.decrypt(owner.getKyberSecretKey());
        byte[] masterFileKey = null;

        try {
            // Decapsulate to get the original File Master Key (in bytes)
            masterFileKey = decapsulateKey(ownerKeys.getKyberCapsule(), ownerPrivateKey);

            //  Encapsulate for Target User (creates a new Wrapper Shared Secret)
            String targetPkHex = HexConverter.bytesToHex(targetUser.getKyberPublicKey());
            KyberEncapsulateResponse wrapper = encapsulateForKey(targetPkHex);

            // This is the key we use to wrap the master key
            byte[] wrapperSharedSecret = HexConverter.hexToBytes(wrapper.getSharedSecretHashHex());

            // Encrypt (Wrap) the Master Key using Target's Shared Secret
            byte[] encryptedMasterKeyBytes = securityUtil.encryptFile(masterFileKey, wrapperSharedSecret);

            // Save everything
            Beneficiary beneficiary = new Beneficiary();
            beneficiary.setOwner(owner);
            beneficiary.setLinkedUser(targetUser);
            beneficiary.setDocument(doc);

            // Store as bytes (HexConverter handles DB)
            beneficiary.setKyberCapsule(HexConverter.hexToBytes(wrapper.getCiphertextHex()));
            beneficiary.setEncryptedKey(encryptedMasterKeyBytes);

            return new BeneficiaryDto(beneficiaryRepository.save(beneficiary));

        } finally {
            //(ZEROING)
            if (ownerPrivateKey != null) Arrays.fill(ownerPrivateKey, (byte) 0);
            if (masterFileKey != null) Arrays.fill(masterFileKey, (byte) 0);
        }
    }

    @Transactional
    public void deleteBeneficiary(Long beneficiaryId, User owner) {
        Beneficiary b = beneficiaryRepository.findById(beneficiaryId)
                .orElseThrow(() -> new RuntimeException("Beneficiary not found"));

        boolean isOwner = b.getOwner().getId().equals(owner.getId());
        boolean isAdmin = owner.getRole().name().contains("ADMIN");

        if (!isOwner && !isAdmin) {
            throw new RuntimeException("Unauthorized");
        }
        beneficiaryRepository.delete(b);
    }

    private byte[] decapsulateKey(byte[] capsule, byte[] privateKey) {
        String requestUrl = pythonServerUrl + "api/kyber/decapsulate";
        Map<String, String> requestBody = new HashMap<>();

        // Convert bytes to Hex for Python API
        requestBody.put("ciphertextHex", HexConverter.bytesToHex(capsule));
        requestBody.put("secretKeyHex", HexConverter.bytesToHex(privateKey));
        requestBody.put("parameterSet", "kyber512");

        KyberDecapsulateResponse response = restTemplate.postForObject(requestUrl, requestBody, KyberDecapsulateResponse.class);
        if (response == null) throw new RuntimeException("Decapsulation failed");

        // Return bytes
        return HexConverter.hexToBytes(response.getSharedSecretHashHex());
    }

    private KyberEncapsulateResponse encapsulateForKey(String publicKeyHex) {
        String requestUrl = pythonServerUrl + "api/kyber/encapsulate";
        Map<String, String> requestBody = new HashMap<>();
        requestBody.put("publicKeyHex", publicKeyHex);
        requestBody.put("parameterSet", "kyber512");

        KyberEncapsulateResponse response = restTemplate.postForObject(requestUrl, requestBody, KyberEncapsulateResponse.class);
        if (response == null) throw new RuntimeException("Encapsulation failed");
        return response;
    }

    public List<BeneficiaryDto> getAllSystemBeneficiaries() {
        return beneficiaryRepository.findAll().stream()
                .map(BeneficiaryDto::new)
                .collect(Collectors.toList());
    }

    public List<BeneficiaryDto> getSharedWithMe(User user) {
        return beneficiaryRepository.findByLinkedUser(user).stream()
                .map(BeneficiaryDto::new)
                .collect(Collectors.toList());
    }
}