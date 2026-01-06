#include <stdio.h>
#include <stdint.h>
#include <string.h>
#include <stdlib.h>

#include "api.h"   // from pq-crystals/kyber/ref/api.h [web:120]

typedef enum {
    KYBER512,
    KYBER768,
    KYBER1024
} kyber_param_t;

static int parse_param(const char *s, kyber_param_t *out) {
    if (strcmp(s, "kyber512") == 0) {
        *out = KYBER512;
        return 0;
    } else if (strcmp(s, "kyber768") == 0) {
        *out = KYBER768;
        return 0;
    } else if (strcmp(s, "kyber1024") == 0) {
        *out = KYBER1024;
        return 0;
    }
    return -1;
}

// Helper: hex encode/decode
static void bytes_to_hex(const unsigned char *in, size_t len, char *out) {
    static const char *hex = "0123456789abcdef";
    for (size_t i = 0; i < len; i++) {
        out[2*i]     = hex[in[i] >> 4];
        out[2*i + 1] = hex[in[i] & 0x0f];
    }
    out[2*len] = '\0';
}

static int hex_to_bytes(const char *in, unsigned char *out, size_t out_max, size_t *out_len) {
    size_t in_len = strlen(in);
    if (in_len % 2 != 0) return -1;
    size_t len = in_len / 2;
    if (len > out_max) return -1;

    for (size_t i = 0; i < len; i++) {
        char c1 = in[2*i];
        char c2 = in[2*i + 1];
        int v1 = (c1 >= '0' && c1 <= '9') ? c1 - '0' :
                 (c1 >= 'a' && c1 <= 'f') ? c1 - 'a' + 10 :
                 (c1 >= 'A' && c1 <= 'F') ? c1 - 'A' + 10 : -1;
        int v2 = (c2 >= '0' && c2 <= '9') ? c2 - '0' :
                 (c2 >= 'a' && c2 <= 'f') ? c2 - 'a' + 10 :
                 (c2 >= 'A' && c2 <= 'F') ? c2 - 'A' + 10 : -1;
        if (v1 < 0 || v2 < 0) return -1;
        out[i] = (unsigned char)((v1 << 4) | v2);
    }
    *out_len = len;
    return 0;
}

static void print_usage(const char *prog) {
    fprintf(stderr, "Usage:\n");
    fprintf(stderr, "  %s keygen <kyber512|kyber768|kyber1024>\n", prog);
    fprintf(stderr, "  %s encapsulate <kyber512|kyber768|kyber1024>   # reads publicKeyHex from stdin (one line)\n", prog);
    fprintf(stderr, "  %s decapsulate <kyber512|kyber768|kyber1024>   # reads ciphertextHex and secretKeyHex from stdin (two lines)\n", prog);
}

// Helper: read one line from stdin, strip newline
static int read_line_strip(char *buf, size_t buf_size) {
    if (!fgets(buf, buf_size, stdin)) {
        return -1;
    }
    size_t len = strlen(buf);
    while (len > 0 && (buf[len-1] == '\n' || buf[len-1] == '\r')) {
        buf[len-1] = '\0';
        len--;
    }
    return 0;
}

int main(int argc, char **argv) {
    if (argc != 3) {
        print_usage(argv[0]);
        return 1;
    }

    const char *mode = argv[1];
    const char *param_str = argv[2];
    kyber_param_t param;

    if (parse_param(param_str, &param) != 0) {
        fprintf(stderr, "{\"error\":\"invalid parameterSet, use kyber512|kyber768|kyber1024\"}\n");
        return 1;
    }

    // Odabir veličina po setu (BYTES makroi iz api.h) [web:120][web:133]
    size_t PK_BYTES, SK_BYTES, CT_BYTES, SS_BYTES;
    switch (param) {
        case KYBER512:
            PK_BYTES = pqcrystals_kyber512_PUBLICKEYBYTES;
            SK_BYTES = pqcrystals_kyber512_SECRETKEYBYTES;
            CT_BYTES = pqcrystals_kyber512_CIPHERTEXTBYTES;
            SS_BYTES = pqcrystals_kyber512_BYTES;
            break;
        case KYBER768:
            PK_BYTES = pqcrystals_kyber768_PUBLICKEYBYTES;
            SK_BYTES = pqcrystals_kyber768_SECRETKEYBYTES;
            CT_BYTES = pqcrystals_kyber768_CIPHERTEXTBYTES;
            SS_BYTES = pqcrystals_kyber768_BYTES;
            break;
        case KYBER1024:
            PK_BYTES = pqcrystals_kyber1024_PUBLICKEYBYTES;
            SK_BYTES = pqcrystals_kyber1024_SECRETKEYBYTES;
            CT_BYTES = pqcrystals_kyber1024_CIPHERTEXTBYTES;
            SS_BYTES = pqcrystals_kyber1024_BYTES;
            break;
        default:
            fprintf(stderr, "{\"error\":\"internal param error\"}\n");
            return 1;
    }

    if (strcmp(mode, "keygen") == 0) {
        unsigned char pk[4096];
        unsigned char sk[4096];

        int rc;
        switch (param) {
            case KYBER512:
                rc = pqcrystals_kyber512_ref_keypair(pk, sk);
                break;
            case KYBER768:
                rc = pqcrystals_kyber768_ref_keypair(pk, sk);
                break;
            case KYBER1024:
                rc = pqcrystals_kyber1024_ref_keypair(pk, sk);
                break;
            default:
                rc = -1;
        }
        if (rc != 0) {
            fprintf(stderr, "{\"error\":\"keypair failed\"}\n");
            return 1;
        }

        char pk_hex[2 * 4096 + 1];
        char sk_hex[2 * 4096 + 1];

        bytes_to_hex(pk, PK_BYTES, pk_hex);
        bytes_to_hex(sk, SK_BYTES, sk_hex);

        printf(
            "{\"publicKeyHex\":\"%s\",\"secretKeyHex\":\"%s\","
            "\"publicKeyBytes\":%zu,\"secretKeyBytes\":%zu}\n",
            pk_hex, sk_hex,
            PK_BYTES, SK_BYTES
        );
        return 0;
    }
    else if (strcmp(mode, "encapsulate") == 0) {
        char pk_hex_buf[8192];
        if (read_line_strip(pk_hex_buf, sizeof(pk_hex_buf)) != 0) {
            fprintf(stderr, "{\"error\":\"failed to read publicKeyHex\"}\n");
            return 1;
        }

        unsigned char pk[4096];
        size_t pk_len = 0;
        if (hex_to_bytes(pk_hex_buf, pk, sizeof(pk), &pk_len) != 0 ||
            pk_len != PK_BYTES) {
            fprintf(stderr, "{\"error\":\"invalid publicKeyHex length\"}\n");
            return 1;
        }

        unsigned char ct[4096];
        unsigned char ss[64];

        int rc;
        switch (param) {
            case KYBER512:
                rc = pqcrystals_kyber512_ref_enc(ct, ss, pk);
                break;
            case KYBER768:
                rc = pqcrystals_kyber768_ref_enc(ct, ss, pk);
                break;
            case KYBER1024:
                rc = pqcrystals_kyber1024_ref_enc(ct, ss, pk);
                break;
            default:
                rc = -1;
        }
        if (rc != 0) {
            fprintf(stderr, "{\"error\":\"encaps failed\"}\n");
            return 1;
        }

        char ct_hex[2 * 4096 + 1];
        char ss_hex[2 * 64 + 1];

        bytes_to_hex(ct, CT_BYTES, ct_hex);
        bytes_to_hex(ss, SS_BYTES, ss_hex);

        printf("{\"ciphertextHex\":\"%s\",\"sharedSecretHex\":\"%s\"}\n",
               ct_hex, ss_hex);
        return 0;
    }
    else if (strcmp(mode, "decapsulate") == 0) {
        char ct_hex_buf[8192];
        char sk_hex_buf[8192];

        if (read_line_strip(ct_hex_buf, sizeof(ct_hex_buf)) != 0) {
            fprintf(stderr, "{\"error\":\"failed to read ciphertextHex\"}\n");
            return 1;
        }
        if (read_line_strip(sk_hex_buf, sizeof(sk_hex_buf)) != 0) {
            fprintf(stderr, "{\"error\":\"failed to read secretKeyHex\"}\n");
            return 1;
        }

        unsigned char ct[4096];
        unsigned char sk[4096];
        size_t ct_len = 0, sk_len = 0;

        if (hex_to_bytes(ct_hex_buf, ct, sizeof(ct), &ct_len) != 0 ||
            ct_len != CT_BYTES) {
            fprintf(stderr, "{\"error\":\"invalid ciphertextHex length\"}\n");
            return 1;
        }
        if (hex_to_bytes(sk_hex_buf, sk, sizeof(sk), &sk_len) != 0 ||
            sk_len != SK_BYTES) {
            fprintf(stderr, "{\"error\":\"invalid secretKeyHex length\"}\n");
            return 1;
        }

        unsigned char ss[64];

        int rc;
        switch (param) {
            case KYBER512:
                rc = pqcrystals_kyber512_ref_dec(ss, ct, sk);
                break;
            case KYBER768:
                rc = pqcrystals_kyber768_ref_dec(ss, ct, sk);
                break;
            case KYBER1024:
                rc = pqcrystals_kyber1024_ref_dec(ss, ct, sk);
                break;
            default:
                rc = -1;
        }
        if (rc != 0) {
            fprintf(stderr, "{\"error\":\"decaps failed\"}\n");
            return 1;
        }

        char ss_hex[2 * 64 + 1];
        bytes_to_hex(ss, SS_BYTES, ss_hex);

        printf("{\"sharedSecretHex\":\"%s\"}\n", ss_hex);
        return 0;
    }
    else {
        print_usage(argv[0]);
        return 1;
    }
}
