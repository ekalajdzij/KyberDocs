package com.kyberdocs.docs.security;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.Cipher;
import javax.crypto.spec.GCMParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import java.nio.ByteBuffer;
import java.security.SecureRandom;
import java.util.Base64;

@Component
public class SecurityUtil {

    @Value("${app.master-key}")
    private String masterKey;

    private static final int GCM_IV_LENGTH = 12;
    private static final int GCM_TAG_LENGTH = 128;

    private static final String KEY_ALGORITHM = "AES";
    private static final String CIPHER_ALGORITHM = "AES/GCM/NoPadding";

    public String encrypt(String value) {
        try {
            // Generate a random IV
            byte[] iv = new byte[GCM_IV_LENGTH];
            new SecureRandom().nextBytes(iv);

            // Setup Cipher
            SecretKeySpec keySpec = new SecretKeySpec(masterKey.getBytes(), KEY_ALGORITHM);
            Cipher cipher = Cipher.getInstance(CIPHER_ALGORITHM);
            GCMParameterSpec gcmParameterSpec = new GCMParameterSpec(GCM_TAG_LENGTH, iv);
            cipher.init(Cipher.ENCRYPT_MODE, keySpec, gcmParameterSpec);

            // Encrypt
            byte[] cipherText = cipher.doFinal(value.getBytes());

            // Combine IV + Ciphertext (so we can save just one string in DB)
            ByteBuffer byteBuffer = ByteBuffer.allocate(iv.length + cipherText.length);
            byteBuffer.put(iv);
            byteBuffer.put(cipherText);

            // Return as Base64
            return Base64.getEncoder().encodeToString(byteBuffer.array());
        } catch (Exception e) {
            throw new RuntimeException("Error encrypting private key", e);
        }
    }

    public String decrypt(String encryptedValueBase64) {
        try {
            // Decode Base64
            byte[] decodedBytes = Base64.getDecoder().decode(encryptedValueBase64);

            // Extract IV
            ByteBuffer byteBuffer = ByteBuffer.wrap(decodedBytes);
            byte[] iv = new byte[GCM_IV_LENGTH];
            byteBuffer.get(iv); // Reads first 12 bytes

            // Extract Ciphertext
            byte[] cipherText = new byte[byteBuffer.remaining()];
            byteBuffer.get(cipherText); // Reads the rest

            // Decrypt
            SecretKeySpec keySpec = new SecretKeySpec(masterKey.getBytes(), KEY_ALGORITHM);
            Cipher cipher = Cipher.getInstance(CIPHER_ALGORITHM);
            GCMParameterSpec gcmParameterSpec = new GCMParameterSpec(GCM_TAG_LENGTH, iv);
            cipher.init(Cipher.DECRYPT_MODE, keySpec, gcmParameterSpec);

            return new String(cipher.doFinal(cipherText));
        } catch (Exception e) {
            throw new RuntimeException("Error decrypting private key", e);
        }
    }

    /*
        File encryption - AES
     */
    public byte[] encryptFile(byte[] fileData, String sharedSecretHex) {
        try {
            byte[] keyBytes = hexStringToByteArray(sharedSecretHex);
            SecretKeySpec keySpec = new SecretKeySpec(keyBytes, KEY_ALGORITHM);

            byte[] iv = new byte[GCM_IV_LENGTH];
            new SecureRandom().nextBytes(iv);

            Cipher cipher = Cipher.getInstance(CIPHER_ALGORITHM);
            GCMParameterSpec gcmParameterSpec = new GCMParameterSpec(GCM_TAG_LENGTH, iv);
            cipher.init(Cipher.ENCRYPT_MODE, keySpec, gcmParameterSpec);

            byte[] cipherText = cipher.doFinal(fileData);

            byte[] output = new byte[iv.length + cipherText.length];
            System.arraycopy(iv, 0, output, 0, iv.length);
            System.arraycopy(cipherText, 0, output, iv.length, cipherText.length);

            return output;
        } catch (Exception e) {
            throw new RuntimeException("Error encrypting file with Kyber secret", e);
        }
    }

    public byte[] decryptFile(byte[] encryptedDataWithIv, String sharedSecretHex) {
        try {
            byte[] keyBytes = hexStringToByteArray(sharedSecretHex);
            SecretKeySpec keySpec = new SecretKeySpec(keyBytes, KEY_ALGORITHM);

            byte[] iv = new byte[GCM_IV_LENGTH];
            System.arraycopy(encryptedDataWithIv, 0, iv, 0, iv.length);

            int cipherTextSize = encryptedDataWithIv.length - GCM_IV_LENGTH;
            byte[] cipherText = new byte[cipherTextSize];
            System.arraycopy(encryptedDataWithIv, GCM_IV_LENGTH, cipherText, 0, cipherTextSize);

            Cipher cipher = Cipher.getInstance(CIPHER_ALGORITHM);
            GCMParameterSpec gcmParameterSpec = new GCMParameterSpec(GCM_TAG_LENGTH, iv);
            cipher.init(Cipher.DECRYPT_MODE, keySpec, gcmParameterSpec);

            return cipher.doFinal(cipherText);
        } catch (Exception e) {
            throw new RuntimeException("Error decrypting file", e);
        }
    }

    private byte[] hexStringToByteArray(String s) {
        int len = s.length();
        byte[] data = new byte[len / 2];
        for (int i = 0; i < len; i += 2) {
            data[i / 2] = (byte) ((Character.digit(s.charAt(i), 16) << 4)
                    + Character.digit(s.charAt(i + 1), 16));
        }
        return data;
    }
}