package com.kyberdocs.docs.kyber.dto;


public class EncapsulateRequest {
    private String parameterSet;
    private String publicKeyHex;

    public EncapsulateRequest() {}

    public EncapsulateRequest(String parameterSet, String publicKeyHex) {
        this.parameterSet = parameterSet;
        this.publicKeyHex = publicKeyHex;
    }

    public String getParameterSet() {
        return parameterSet;
    }

    public void setParameterSet(String parameterSet) {
        this.parameterSet = parameterSet;
    }

    public String getPublicKeyHex() {
        return publicKeyHex;
    }

    public void setPublicKeyHex(String publicKeyHex) {
        this.publicKeyHex = publicKeyHex;
    }
}
