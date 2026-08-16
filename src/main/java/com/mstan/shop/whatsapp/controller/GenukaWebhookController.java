package com.mstan.shop.whatsapp.controller;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.mstan.shop.whatsapp.entity.WhatsAppMessage;
import com.mstan.shop.whatsapp.entity.WhatsAppMessageStatus;
import com.mstan.shop.whatsapp.repository.WhatsAppMessageRepository;
import com.mstan.shop.whatsapp.service.GenukaWebhookSignatureVerifier;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Reçoit les statuts de livraison WhatsApp (sent/delivered/read/failed) envoyés par Genuka.
 * Signature vérifiée avant tout traitement (contrairement au webhook CamPay, non signé, qui
 * re-vérifie server-to-server à la place) — une signature invalide est rejetée en 401 pour
 * rester visible dans le journal de livraisons Genuka. Une fois la signature validée, toute
 * erreur de traitement interne est journalisée et absorbée (retour 200) pour éviter une
 * tempête de retries côté Genuka.
 */
@Slf4j
@Tag(name = "WhatsApp - Genuka Webhook", description = "Callback Genuka pour les statuts de messages WhatsApp")
@RestController
@RequestMapping("/api/webhooks/genuka")
@RequiredArgsConstructor
public class GenukaWebhookController {

    private final GenukaWebhookSignatureVerifier signatureVerifier;
    private final WhatsAppMessageRepository whatsAppMessageRepository;
    private final ObjectMapper objectMapper;

    @PostMapping
    public ResponseEntity<Void> handle(
            @RequestHeader("X-Genuka-Signature") String signature,
            @RequestBody String rawPayload
    ) {
        if (!signatureVerifier.isValid(signature, rawPayload)) {
            log.warn("Signature Genuka invalide, webhook rejeté");
            return ResponseEntity.status(401).build();
        }

        try {
            processEvent(rawPayload);
        } catch (Exception e) {
            log.warn("Impossible de traiter le webhook Genuka, ignoré : {}", rawPayload, e);
        }

        return ResponseEntity.ok().build();
    }

    private void processEvent(String rawPayload) throws com.fasterxml.jackson.core.JsonProcessingException {
        JsonNode root = objectMapper.readTree(rawPayload);
        String type = root.path("type").asText(null);
        if (!"messages".equals(type)) {
            log.info("Événement Genuka de type {} ignoré (non exploité)", type);
            return;
        }

        JsonNode data = root.path("data");
        String providerMessageId = data.path("id").asText(null);
        String status = data.path("status").asText(null);
        if (providerMessageId == null || status == null) {
            log.warn("Webhook Genuka de type messages sans id/status exploitable : {}", rawPayload);
            return;
        }

        WhatsAppMessageStatus newStatus = mapStatus(status);
        if (newStatus == null) {
            log.info("Statut Genuka inconnu '{}' ignoré", status);
            return;
        }

        WhatsAppMessage message = whatsAppMessageRepository.findByProviderMessageId(providerMessageId).orElse(null);
        if (message == null) {
            log.warn("Aucun WhatsAppMessage trouvé pour providerMessageId {}", providerMessageId);
            return;
        }

        if (newStatus == WhatsAppMessageStatus.FAILED || newStatus.progressRank() > message.getStatus().progressRank()) {
            message.setStatus(newStatus);
            whatsAppMessageRepository.save(message);
        } else {
            log.info("Statut Genuka '{}' ignoré pour {} (déjà à un statut plus avancé)", status, providerMessageId);
        }
    }

    private WhatsAppMessageStatus mapStatus(String status) {
        return switch (status) {
            case "sent" -> WhatsAppMessageStatus.SENT;
            case "delivered" -> WhatsAppMessageStatus.DELIVERED;
            case "read" -> WhatsAppMessageStatus.READ;
            case "failed" -> WhatsAppMessageStatus.FAILED;
            default -> null;
        };
    }
}
