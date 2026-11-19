package com.mstan.shop.payment.controller;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.mstan.shop.order.service.OrderService;
import com.mstan.shop.payment.entity.Payment;
import com.mstan.shop.payment.entity.PaymentStatus;
import com.mstan.shop.payment.repository.PaymentRepository;
import com.mstan.shop.payment.service.FapshiPaymentService;
import com.mstan.shop.payment.service.FapshiWebhookVerifier;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

/**
 * Webhook Fapshi : le header "x-wh-secret" (secret statique du dashboard) est vérifié avant tout
 * traitement. Le statut est ensuite re-confirmé server-to-server via
 * {@link FapshiPaymentService#checkStatus} plutôt que de faire confiance au payload. Fapshi ne
 * rejoue pas les webhooks : le polling ({@code OrderService#refreshPaymentStatus}) sert de filet.
 */
@Slf4j
@Tag(name = "Payments - Fapshi Webhook", description = "Callback Fapshi (secret vérifié, puis statut re-confirmé côté serveur)")
@RestController
@RequestMapping("/api/payments/fapshi/webhook")
@RequiredArgsConstructor
public class FapshiWebhookController {

    private final PaymentRepository paymentRepository;
    private final FapshiPaymentService fapshiPaymentService;
    private final FapshiWebhookVerifier fapshiWebhookVerifier;
    private final OrderService orderService;
    private final ObjectMapper objectMapper;

    @PostMapping
    public ResponseEntity<Void> handleWebhook(
            @RequestHeader(value = "x-wh-secret", required = false) String secret,
            @RequestBody String rawPayload
    ) {
        if (!fapshiWebhookVerifier.isValid(secret)) {
            log.warn("Secret webhook Fapshi invalide, webhook rejeté");
            return ResponseEntity.status(401).build();
        }

        JsonNode node;
        try {
            node = objectMapper.readTree(rawPayload);
        } catch (Exception e) {
            log.warn("Impossible de parser le payload du webhook Fapshi", e);
            return ResponseEntity.ok().build();
        }

        Payment payment = resolvePayment(node);
        if (payment == null) {
            log.warn("Webhook Fapshi reçu sans référence exploitable ou inconnue, ignoré : {}", rawPayload);
            return ResponseEntity.ok().build();
        }
        if (payment.getProviderReference() == null) {
            log.warn("Paiement {} sans providerReference, impossible de vérifier le statut", payment.getId());
            return ResponseEntity.ok().build();
        }

        PaymentStatus status = fapshiPaymentService.checkStatus(payment.getProviderReference());
        if (status == PaymentStatus.SUCCESSFUL) {
            orderService.confirmPayment(payment.getOrder().getId(), payment.getProviderReference());
        } else if (status == PaymentStatus.FAILED) {
            orderService.failPayment(payment.getOrder().getId(), payment.getProviderReference(), "Paiement refusé ou expiré (Fapshi)");
        }

        return ResponseEntity.ok().build();
    }

    /** Essaie d'abord le "transId" Fapshi, puis notre "externalId" en repli. */
    private Payment resolvePayment(JsonNode node) {
        if (node.hasNonNull("transId")) {
            Payment payment = paymentRepository.findByProviderReference(node.get("transId").asText()).orElse(null);
            if (payment != null) {
                return payment;
            }
        }
        if (node.hasNonNull("externalId")) {
            try {
                return paymentRepository.findByExternalReference(UUID.fromString(node.get("externalId").asText()))
                        .orElse(null);
            } catch (IllegalArgumentException e) {
                return null;
            }
        }
        return null;
    }
}
