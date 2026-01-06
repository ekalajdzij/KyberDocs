package com.kyberdocs.docs.kyber.dto;

public class KyberEncapsulateResponse {
    private String ciphertextHex;
    private String sharedSecretHashHex;

    public String getCiphertextHex() {
        return ciphertextHex;
    }

    public void setCiphertextHex(String ciphertextHex) {
        this.ciphertextHex = ciphertextHex;
    }

    public String getSharedSecretHashHex() {
        return sharedSecretHashHex;
    }

    public void setSharedSecretHashHex(String sharedSecretHashHex) {
        this.sharedSecretHashHex = sharedSecretHashHex;
    }

}
