package com.kyberdocs.docs.kyber;

import com.kyberdocs.docs.kyber.KyberClient;
import com.kyberdocs.docs.kyber.dto.*;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/kyber-server")
public class KyberController {

    private final KyberClient kyberClient;

    public KyberController(KyberClient kyberClient) {
        this.kyberClient = kyberClient;
    }

    @PostMapping("/keygen")
    public ResponseEntity<KyberKeypair> keygen(@RequestBody KeygenRequest body) {
        String parameterSet = body.getParameterSet() != null ? body.getParameterSet() : "kyber1024";
        KyberKeypair kp = kyberClient.keygen(parameterSet);
        return ResponseEntity.ok(kp);
    }

    @PostMapping("/encapsulate")
    public ResponseEntity<KyberEncapsulateResponse> encapsulate(@RequestBody EncapsulateRequest body) {
        String parameterSet = body.getParameterSet() != null ? body.getParameterSet() : "kyber1024";
        KyberEncapsulateResponse resp = kyberClient.encapsulate(parameterSet, body.getPublicKeyHex());
        return ResponseEntity.ok(resp);
    }

    @PostMapping("/decapsulate")
    public ResponseEntity<KyberDecapsulateResponse> decapsulate(@RequestBody DecapsulateRequest body) {
        String parameterSet = body.getParameterSet() != null ? body.getParameterSet() : "kyber1024";
        KyberDecapsulateResponse resp = kyberClient.decapsulate(
                parameterSet,
                body.getCiphertextHex(),
                body.getSecretKeyHex()
        );
        return ResponseEntity.ok(resp);
    }


}
