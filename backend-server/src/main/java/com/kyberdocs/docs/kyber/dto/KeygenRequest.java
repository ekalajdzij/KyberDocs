package com.kyberdocs.docs.kyber.dto;


public class KeygenRequest {
    private String parameterSet;

    public KeygenRequest() {}

    public KeygenRequest(String parameterSet) {
        this.parameterSet = parameterSet;
    }

    public String getParameterSet() {
        return parameterSet;
    }

    public void setParameterSet(String parameterSet) {
        this.parameterSet = parameterSet;
    }
}
