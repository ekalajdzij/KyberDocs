package com.kyberdocs.docs.kyber.dto;

public class KyberKeygenResponse {
    private String publicKeyHex;

    private String secretKeyHex;

    private int publicKeyBytes;
    private int secretKeyBytes;

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

    public int getPublicKeyBytes() {
        return publicKeyBytes;
    }

    public void setPublicKeyBytes(int publicKeyBytes) {
        this.publicKeyBytes = publicKeyBytes;
    }

    public int getSecretKeyBytes() {
        return secretKeyBytes;
    }

    public void setSecretKeyBytes(int secretKeyBytes) {
        this.secretKeyBytes = secretKeyBytes;
    }
}
