package com.kyberdocs.docs.kyber;

import com.kyberdocs.docs.kyber.dto.*;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

@Service
public class KyberClient {
    private final RestClient restClient;

    public KyberClient(@Value("${kyber.service.base-url}") String baseUrl,
                       RestClient.Builder restClientBuilder) {
        this.restClient = restClientBuilder
                .baseUrl(baseUrl)
                .build();
    }

    public KyberKeypair keygen(String parameterSet) {
        return restClient.post()
                .uri("/api/kyber/keygen")
                .body(new KeygenRequest(parameterSet))
                .retrieve()
                .body(KyberKeypair.class);
    }

    public KyberEncapsulateResponse encapsulate(String parameterSet, String publicKeyHex) {
        return restClient.post()
                .uri("/api/kyber/encapsulate")
                .body(new EncapsulateRequest(parameterSet, publicKeyHex))
                .retrieve()
                .body(KyberEncapsulateResponse.class);
    }

    public KyberDecapsulateResponse decapsulate(String parameterSet, String ciphertextHex, String secretKeyHex) {
        return restClient.post()
                .uri("/api/kyber/decapsulate")
                .body(new DecapsulateRequest(parameterSet, ciphertextHex, secretKeyHex))
                .retrieve()
                .body(KyberDecapsulateResponse.class);
    }
}
