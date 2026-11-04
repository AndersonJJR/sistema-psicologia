package com.anderson.sistemapsicologia.model.usuario;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.time.Instant;
import java.util.Base64;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Serviço para geração e validação de tokens JWT (HS256) contendo perfil RBAC e vínculo profissional.
 */
@Service
public class JwtTokenService {

    private static final String HMAC_ALG = "HmacSHA256";
    private static final String HEADER_JSON = "{\"alg\":\"HS256\",\"typ\":\"JWT\"}";

    private final byte[] secretBytes;
    private final long expirationMillis;

    public JwtTokenService(
            @Value("${app.security.jwt-secret:c2lzdGVtYS1wc2ljb2xvZ2lhLWp3dC1zZWNyZXQta2V5LTI1Ni1iaXRzLXNlY3VyZQ==}") String jwtSecret,
            @Value("${app.security.jwt-expiration-ms:28800000}") long expirationMillis
    ) {
        this.secretBytes = jwtSecret.getBytes(StandardCharsets.UTF_8);
        this.expirationMillis = expirationMillis;
    }

    public String gerarToken(Usuario usuario) {
        Instant agora = Instant.now();
        Instant expira = agora.plusMillis(expirationMillis);

        String headerEncoded = base64UrlEncode(HEADER_JSON.getBytes(StandardCharsets.UTF_8));
        StringBuilder payload = new StringBuilder("{");
        payload.append("\"sub\":\"").append(escapeJson(usuario.getLogin())).append("\",");
        payload.append("\"role\":\"").append(usuario.getPerfil().name()).append("\",");
        payload.append("\"uid\":").append(usuario.getIdUsuario() != null ? "\"" + usuario.getIdUsuario() + "\"" : "null").append(",");
        payload.append("\"psicologoId\":").append(usuario.getIdPsicologoVinculado() != null ? "\"" + usuario.getIdPsicologoVinculado() + "\"" : "null").append(",");
        payload.append("\"iat\":").append(agora.getEpochSecond()).append(",");
        payload.append("\"exp\":").append(expira.getEpochSecond());
        payload.append("}");

        String payloadEncoded = base64UrlEncode(payload.toString().getBytes(StandardCharsets.UTF_8));
        String signingInput = headerEncoded + "." + payloadEncoded;
        String signatureEncoded = assinarHmacSha256(signingInput);

        return signingInput + "." + signatureEncoded;
    }

    public Instant calcularExpiracao() {
        return Instant.now().plusMillis(expirationMillis);
    }

    public Optional<ClaimsJwt> validarToken(String token) {
        if (token == null || token.isBlank()) {
            return Optional.empty();
        }
        String[] partes = token.split("\\.");
        if (partes.length != 3) {
            return Optional.empty();
        }
        String signingInput = partes[0] + "." + partes[1];
        String assinaturaEsperada = assinarHmacSha256(signingInput);

        if (!MessageDigest.isEqual(
                assinaturaEsperada.getBytes(StandardCharsets.UTF_8),
                partes[2].getBytes(StandardCharsets.UTF_8)
        )) {
            return Optional.empty();
        }

        try {
            String payloadJson = new String(Base64.getUrlDecoder().decode(partes[1]), StandardCharsets.UTF_8);
            Map<String, String> campos = extrairCamposJson(payloadJson);

            long exp = Long.parseLong(campos.getOrDefault("exp", "0"));
            if (Instant.now().getEpochSecond() > exp) {
                return Optional.empty();
            }

            String sub = campos.get("sub");
            String roleStr = campos.get("role");
            String uidStr = campos.get("uid");
            String psicologoIdStr = campos.get("psicologoId");

            if (sub == null || roleStr == null) {
                return Optional.empty();
            }

            PerfilUsuario perfil = PerfilUsuario.valueOf(roleStr);
            UUID uid = uidStr != null && !uidStr.equals("null") && !uidStr.isBlank() ? UUID.fromString(uidStr) : null;
            UUID psicologoId = psicologoIdStr != null && !psicologoIdStr.equals("null") && !psicologoIdStr.isBlank()
                    ? UUID.fromString(psicologoIdStr)
                    : null;

            return Optional.of(new ClaimsJwt(sub, perfil, uid, psicologoId, Instant.ofEpochSecond(exp)));
        } catch (Exception e) {
            return Optional.empty();
        }
    }

    private String assinarHmacSha256(String input) {
        try {
            Mac mac = Mac.getInstance(HMAC_ALG);
            mac.init(new SecretKeySpec(secretBytes, HMAC_ALG));
            byte[] signature = mac.doFinal(input.getBytes(StandardCharsets.UTF_8));
            return base64UrlEncode(signature);
        } catch (Exception e) {
            throw new IllegalStateException("Erro ao assinar token JWT", e);
        }
    }

    private String base64UrlEncode(byte[] bytes) {
        return Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);
    }

    private String escapeJson(String value) {
        return value.replace("\\", "\\\\").replace("\"", "\\\"");
    }

    private Map<String, String> extrairCamposJson(String json) {
        Map<String, String> mapa = new LinkedHashMap<>();
        Pattern pattern = Pattern.compile("\"([^\"]+)\"\\s*:\\s*(?:\"([^\"]*)\"|([^,}]+))");
        Matcher matcher = pattern.matcher(json);
        while (matcher.find()) {
            String chave = matcher.group(1);
            String valor = matcher.group(2) != null ? matcher.group(2) : matcher.group(3).trim();
            mapa.put(chave, valor);
        }
        return mapa;
    }

    public record ClaimsJwt(
            String login,
            PerfilUsuario perfil,
            UUID idUsuario,
            UUID idPsicologoVinculado,
            Instant expiraEm
    ) {
    }
}
