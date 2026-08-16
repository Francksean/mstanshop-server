package com.mstan.shop.payment.service;

import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;

/**
 * Vérifie le champ "signature" des callbacks CamPay : un JWT HS256 signé avec la clé webhook de
 * l'app CamPay (dashboard > paramètres de l'app), distincte de la clé API utilisée pour les
 * appels sortants.
 */
@Slf4j
@Component
public class CamPayWebhookVerifier {

    private final SecretKey signingKey;

    public CamPayWebhookVerifier(@Value("${app.payment.campay.webhook-key}") String webhookKey) {
        this.signingKey = (webhookKey != null && !webhookKey.isBlank())
                ? Keys.hmacShaKeyFor(webhookKey.getBytes())
                : null;
    }

    public boolean isValid(String signatureJwt) {
        if (signingKey == null) {
            log.warn("app.payment.campay.webhook-key non configuré, signature refusée par défaut");
            return false;
        }
        if (signatureJwt == null || signatureJwt.isBlank()) {
            return false;
        }
        try {
            Jwts.parser().verifyWith(signingKey).build().parseSignedClaims(signatureJwt);
            return true;
        } catch (Exception e) {
            log.warn("Signature JWT CamPay invalide", e);
            return false;
        }
    }
}
