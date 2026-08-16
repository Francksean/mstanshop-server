package com.mstan.shop.whatsapp.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.time.Instant;
import java.util.HexFormat;
import java.util.Map;

/**
 * Vérifie l'en-tête "X-Genuka-Signature" (format "t=<timestamp>,v1=<hmac-sha256 hex>") envoyé
 * par les webhooks Genuka : HMAC-SHA256 de "${t}.${rawBody}" avec le secret du endpoint, comparé
 * en temps constant, avec une tolérance de rejeu de 300s sur le timestamp.
 */
@Slf4j
@Component
public class GenukaWebhookSignatureVerifier {

    private static final long TOLERANCE_SECONDS = 300;
    private static final String HMAC_ALGORITHM = "HmacSHA256";

    private final String webhookSecret;

    public GenukaWebhookSignatureVerifier(@Value("${app.notification.genuka.webhook-secret}") String webhookSecret) {
        this.webhookSecret = webhookSecret;
    }

    public boolean isValid(String signatureHeader, String rawBody) {
        if (webhookSecret == null || webhookSecret.isBlank()) {
            log.warn("app.notification.genuka.webhook-secret non configuré, signature refusée par défaut");
            return false;
        }
        if (signatureHeader == null || signatureHeader.isBlank()) {
            return false;
        }

        Map<String, String> parts = parseHeader(signatureHeader);
        String timestampPart = parts.get("t");
        String signaturePart = parts.get("v1");
        if (timestampPart == null || signaturePart == null) {
            return false;
        }

        long timestamp;
        try {
            timestamp = Long.parseLong(timestampPart);
        } catch (NumberFormatException e) {
            return false;
        }

        if (Math.abs(Instant.now().getEpochSecond() - timestamp) > TOLERANCE_SECONDS) {
            log.warn("Timestamp de signature Genuka hors tolérance ({}s)", TOLERANCE_SECONDS);
            return false;
        }

        String expectedSignature = computeHmac(timestampPart + "." + rawBody);
        return MessageDigest.isEqual(
                expectedSignature.getBytes(StandardCharsets.UTF_8),
                signaturePart.getBytes(StandardCharsets.UTF_8)
        );
    }

    private Map<String, String> parseHeader(String signatureHeader) {
        return java.util.Arrays.stream(signatureHeader.split(","))
                .map(part -> part.split("=", 2))
                .filter(kv -> kv.length == 2)
                .collect(java.util.stream.Collectors.toMap(kv -> kv[0].trim(), kv -> kv[1].trim()));
    }

    private String computeHmac(String data) {
        try {
            Mac mac = Mac.getInstance(HMAC_ALGORITHM);
            mac.init(new SecretKeySpec(webhookSecret.getBytes(StandardCharsets.UTF_8), HMAC_ALGORITHM));
            byte[] hash = mac.doFinal(data.getBytes(StandardCharsets.UTF_8));
            return HexFormat.of().formatHex(hash);
        } catch (Exception e) {
            throw new IllegalStateException("Impossible de calculer la signature HMAC Genuka", e);
        }
    }
}
