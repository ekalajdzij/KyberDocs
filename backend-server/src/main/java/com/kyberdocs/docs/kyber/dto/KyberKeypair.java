package com.kyberdocs.docs.kyber.dto;

public class KyberKeypair {
    private String publicKeyHex;
    private String secretKeyHex;
    private Integer publicKeyBytes;
    private Integer secretKeyBytes;

    public String getPublicKeyHex() {
        return publicKeyHex;
    }

    public void setPublicKeyHex(String publicKeyHex) {
        this.publicKeyHex = publicKeyHex;
    }

    public String getSecretKeyHex() {
        return secretKeyHex;
    }

    public void setSecretKeyHex(String secretKeyHex) {
        this.secretKeyHex = secretKeyHex;
    }

    public Integer getPublicKeyBytes() {
        return publicKeyBytes;
    }

    public void setPublicKeyBytes(Integer publicKeyBytes) {
        this.publicKeyBytes= publicKeyBytes;
    }

    public Integer getSecretKeyBytes() {
        return secretKeyBytes;
    }

    public void setSecretKeyBytes(Integer secretKeyBytes) {
        this.secretKeyBytes = secretKeyBytes;
    }
}
