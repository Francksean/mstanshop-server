package com.mstan.shop.payment.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;

/**
 * Vérifie le header "x-wh-secret" des webhooks Fapshi : secret statique défini dans le dashboard
 * (pas une signature HMAC du corps). Comparaison en temps constant.
 */
@Slf4j
@Component
public class FapshiWebhookVerifier {

    private final byte[] secret;

    public FapshiWebhookVerifier(@Value("${app.payment.fapshi.webhook-secret}") String webhookSecret) {
        this.secret = (webhookSecret != null && !webhookSecret.isBlank())
                ? webhookSecret.getBytes(StandardCharsets.UTF_8)
                : null;
    }

    public boolean isValid(String headerValue) {
        if (secret == null) {
            log.warn("app.payment.fapshi.webhook-secret non configuré, webhook refusé par défaut");
            return false;
        }
        if (headerValue == null || headerValue.isBlank()) {
            return false;
        }
        return MessageDigest.isEqual(secret, headerValue.getBytes(StandardCharsets.UTF_8));
    }
}
