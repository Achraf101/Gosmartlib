package be.ap.backend.service;

import be.ap.backend.config.EncryptionProperties;
import be.ap.backend.exception.EncryptionException;

import org.springframework.stereotype.Service;
import javax.crypto.Cipher;
import javax.crypto.spec.GCMParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.security.GeneralSecurityException;
import java.security.SecureRandom;
import java.util.Arrays;
import java.util.Base64;

@Service
public class EncryptionService {

    private static final String ALGORITHM = "AES/GCM/NoPadding";
    private static final int NONCE_LENGTH_BYTES = 12;
    private static final int TAG_LENGTH_BITS = 128;

    private final SecretKeySpec secretKey;

    public EncryptionService(EncryptionProperties props) {
        byte[] keyBytes = Base64.getDecoder().decode(props.key());
        if (keyBytes.length != 32) {
            throw new IllegalArgumentException("Encryption key must be 256 bits (32 bytes)");
        }
        this.secretKey = new SecretKeySpec(keyBytes, "AES");
    }

    public String encrypt(String plaintext) {
        try {
            byte[] nonce = new byte[NONCE_LENGTH_BYTES];
            SecureRandom.getInstanceStrong().nextBytes(nonce);

            Cipher cipher = Cipher.getInstance(ALGORITHM);
            cipher.init(Cipher.ENCRYPT_MODE, secretKey, new GCMParameterSpec(TAG_LENGTH_BITS, nonce));

            byte[] ciphertext = cipher.doFinal(plaintext.getBytes(StandardCharsets.UTF_8));
            byte[] combined = new byte[NONCE_LENGTH_BYTES + ciphertext.length];
            System.arraycopy(nonce, 0, combined, 0, NONCE_LENGTH_BYTES);
            System.arraycopy(ciphertext, 0, combined, NONCE_LENGTH_BYTES, ciphertext.length);

            return Base64.getEncoder().encodeToString(combined);
        } catch (GeneralSecurityException e) {
            throw new EncryptionException("Versleuteling mislukt", e);
        }
    }

    public String decrypt(String encryptedBase64) {
        try {
            byte[] combined = Base64.getDecoder().decode(encryptedBase64);
            byte[] nonce = Arrays.copyOfRange(combined, 0, NONCE_LENGTH_BYTES);
            byte[] ciphertext = Arrays.copyOfRange(combined, NONCE_LENGTH_BYTES, combined.length);

            Cipher cipher = Cipher.getInstance(ALGORITHM);
            cipher.init(Cipher.DECRYPT_MODE, secretKey, new GCMParameterSpec(TAG_LENGTH_BITS, nonce));

            return new String(cipher.doFinal(ciphertext), StandardCharsets.UTF_8);
        } catch (GeneralSecurityException e) {
            throw new EncryptionException("Ontsleuteling mislukt", e);
        }
    }
}