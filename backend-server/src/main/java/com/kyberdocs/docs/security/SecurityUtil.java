package com.kyberdocs.docs.security;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.Cipher;
import javax.crypto.spec.GCMParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import java.nio.ByteBuffer;
import java.security.SecureRandom;
import java.util.Arrays;

@Component
public class SecurityUtil {

    @Value("${app.master-key}")
    private String masterKey; // Ideally this should also be injected as bytes/char[]

    private static final int GCM_IV_LENGTH = 12;
    private static final int GCM_TAG_LENGTH = 128;
    private static final String KEY_ALGORITHM = "AES";
    private static final String CIPHER_ALGORITHM = "AES/GCM/NoPadding";

    /*
     * Encrypts the Private Key (wrapping).
     * Returns raw bytes (IV + Ciphertext).
     * Clears input 'value' from memory.
     */
    public byte[] encrypt(byte[] value) {
        try {
            byte[] iv = new byte[GCM_IV_LENGTH];
            new SecureRandom().nextBytes(iv);

            SecretKeySpec keySpec = new SecretKeySpec(masterKey.getBytes(), KEY_ALGORITHM);
            Cipher cipher = Cipher.getInstance(CIPHER_ALGORITHM);
            GCMParameterSpec gcmParameterSpec = new GCMParameterSpec(GCM_TAG_LENGTH, iv);
            cipher.init(Cipher.ENCRYPT_MODE, keySpec, gcmParameterSpec);

            byte[] cipherText = cipher.doFinal(value);

            ByteBuffer byteBuffer = ByteBuffer.allocate(iv.length + cipherText.length);
            byteBuffer.put(iv);
            byteBuffer.put(cipherText);

            return byteBuffer.array();
        } catch (Exception e) {
            throw new RuntimeException("Error encrypting private key", e);
        } finally {
            // ZEROING: Wipe the input sensitive data
            if (value != null) Arrays.fill(value, (byte) 0);
        }
    }

    /*
     * Decrypts the Private Key (unwrapping).
     * Returns raw bytes (Plaintext Private Key).
     */
    public byte[] decrypt(byte[] encryptedDataWithIv) {
        try {
            ByteBuffer byteBuffer = ByteBuffer.wrap(encryptedDataWithIv);
            byte[] iv = new byte[GCM_IV_LENGTH];
            byteBuffer.get(iv);

            byte[] cipherText = new byte[byteBuffer.remaining()];
            byteBuffer.get(cipherText);

            SecretKeySpec keySpec = new SecretKeySpec(masterKey.getBytes(), KEY_ALGORITHM);
            Cipher cipher = Cipher.getInstance(CIPHER_ALGORITHM);
            GCMParameterSpec gcmParameterSpec = new GCMParameterSpec(GCM_TAG_LENGTH, iv);
            cipher.init(Cipher.DECRYPT_MODE, keySpec, gcmParameterSpec);

            return cipher.doFinal(cipherText);
        } catch (Exception e) {
            throw new RuntimeException("Error decrypting private key", e);
        }
    }

    /*
     * File Encryption.
     * Clears 'sharedSecret' from memory.
     */
    public byte[] encryptFile(byte[] fileData, byte[] sharedSecret) {
        try {
            SecretKeySpec keySpec = new SecretKeySpec(sharedSecret, KEY_ALGORITHM);

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
            throw new RuntimeException("Error encrypting file", e);
        } finally {
            // ZEROING: Wipe shared secret
            if (sharedSecret != null) Arrays.fill(sharedSecret, (byte) 0);
        }
    }

    /*
     * File Decryption.
     * Clears 'sharedSecret' from memory.
     */
    public byte[] decryptFile(byte[] encryptedDataWithIv, byte[] sharedSecret) {
        try {
            SecretKeySpec keySpec = new SecretKeySpec(sharedSecret, KEY_ALGORITHM);

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
        } finally {
            // ZEROING: Wipe shared secret
            if (sharedSecret != null) Arrays.fill(sharedSecret, (byte) 0);
        }
    }
}