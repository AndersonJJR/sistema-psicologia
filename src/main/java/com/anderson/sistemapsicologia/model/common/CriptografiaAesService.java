package com.anderson.sistemapsicologia.model.common;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.Cipher;
import javax.crypto.spec.GCMParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.SecureRandom;
import java.util.Base64;

/**
 * Serviço de Criptografia no Nível da Aplicação (ALE - Application-Level Encryption) utilizando AES-256-GCM.
 * Garante confidencialidade e integridade autenticada dos dados sensíveis antes da persistência no MySQL.
 */
@Component
public class CriptografiaAesService {

    private static final String ALGORITHM = "AES/GCM/NoPadding";
    private static final int IV_LENGTH_BYTES = 12;
    private static final int TAG_LENGTH_BITS = 128;
    private static final String PREFIX = "ENC:AES256:";

    private static volatile SecretKeySpec activeSecretKey;
    private final SecureRandom secureRandom = new SecureRandom();

    public CriptografiaAesService(
            @Value("${app.security.aes-key:MDEyMzQ1Njc4OWFiY2RlZjAxMjM0NTY3ODlhYmNkZWY=}") String configuredKey
    ) {
        configurarChave(configuredKey);
    }

    public static synchronized void configurarChave(String rawKey) {
        try {
            MessageDigest sha256 = MessageDigest.getInstance("SHA-256");
            byte[] keyBytes = sha256.digest(rawKey.getBytes(StandardCharsets.UTF_8));
            activeSecretKey = new SecretKeySpec(keyBytes, "AES");
        } catch (Exception e) {
            throw new IllegalStateException("Falha ao inicializar chave simétrica AES-256", e);
        }
    }

    private static SecretKeySpec obterChave() {
        if (activeSecretKey == null) {
            configurarChave("MDEyMzQ1Njc4OWFiY2RlZjAxMjM0NTY3ODlhYmNkZWY=");
        }
        return activeSecretKey;
    }

    public String criptografar(String textoPlano) {
        if (textoPlano == null) {
            return null;
        }
        if (textoPlano.startsWith(PREFIX)) {
            return textoPlano;
        }
        try {
            byte[] iv = new byte[IV_LENGTH_BYTES];
            secureRandom.nextBytes(iv);

            Cipher cipher = Cipher.getInstance(ALGORITHM);
            GCMParameterSpec parameterSpec = new GCMParameterSpec(TAG_LENGTH_BITS, iv);
            cipher.init(Cipher.ENCRYPT_MODE, obterChave(), parameterSpec);

            byte[] textoCifrado = cipher.doFinal(textoPlano.getBytes(StandardCharsets.UTF_8));
            ByteBuffer byteBuffer = ByteBuffer.allocate(iv.length + textoCifrado.length);
            byteBuffer.put(iv);
            byteBuffer.put(textoCifrado);

            return PREFIX + Base64.getEncoder().encodeToString(byteBuffer.array());
        } catch (Exception e) {
            throw new IllegalStateException("Erro ao criptografar campo sensível com AES-256", e);
        }
    }

    public String descriptografar(String valorArmazenado) {
        if (valorArmazenado == null) {
            return null;
        }
        String payloadBase64 = valorArmazenado.startsWith(PREFIX)
                ? valorArmazenado.substring(PREFIX.length())
                : valorArmazenado;
        try {
            byte[] decoded = Base64.getDecoder().decode(payloadBase64);
            ByteBuffer byteBuffer = ByteBuffer.wrap(decoded);

            byte[] iv = new byte[IV_LENGTH_BYTES];
            byteBuffer.get(iv);

            byte[] cipherBytes = new byte[byteBuffer.remaining()];
            byteBuffer.get(cipherBytes);

            Cipher cipher = Cipher.getInstance(ALGORITHM);
            GCMParameterSpec parameterSpec = new GCMParameterSpec(TAG_LENGTH_BITS, iv);
            cipher.init(Cipher.DECRYPT_MODE, obterChave(), parameterSpec);

            byte[] decryptedBytes = cipher.doFinal(cipherBytes);
            return new String(decryptedBytes, StandardCharsets.UTF_8);
        } catch (Exception e) {
            throw new IllegalStateException("Erro ao descriptografar campo sensível AES-256", e);
        }
    }
}
