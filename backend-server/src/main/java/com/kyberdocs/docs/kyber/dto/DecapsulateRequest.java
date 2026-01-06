package com.kyberdocs.docs.kyber.dto;

public class DecapsulateRequest {
    private String parameterSet;
    private String ciphertextHex;
    private String secretKeyHex;

    public DecapsulateRequest() {}

    public DecapsulateRequest(String parameterSet, String ciphertextHex, String secretKeyHex) {
        this.ciphertextHex = ciphertextHex;
        this.parameterSet = parameterSet;
        this.secretKeyHex = secretKeyHex;
    }

    public String getParameterSet() {
        return parameterSet;
    }

    public void setParameterSet(String parameterSet) {
        this.parameterSet = parameterSet;
    }

    public String getCiphertextHex() {
        return ciphertextHex;
    }

    public void setCiphertextHex(String ciphertextHex) {
        this.ciphertextHex = ciphertextHex;
    }

    public String getSecretKeyHex() {
        return secretKeyHex;
    }

    public void setSecretKeyHex(String secretKeyHex) {
        this.secretKeyHex = secretKeyHex;
    }
}
