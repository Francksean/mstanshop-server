package com.mstan.shop.payment.controller;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.mstan.shop.order.service.OrderService;
import com.mstan.shop.payment.entity.Payment;
import com.mstan.shop.payment.entity.PaymentStatus;
import com.mstan.shop.payment.repository.PaymentRepository;
import com.mstan.shop.payment.service.CamPayPaymentService;
import com.mstan.shop.payment.service.CamPayWebhookVerifier;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

/**
 * L'app CamPay est configurée en callback POST/JSON. Chaque payload porte un champ "signature"
 * (JWT HS256 signé avec la clé webhook de l'app) — vérifié avant tout traitement. Une fois la
 * signature validée, le statut est tout de même re-vérifié server-to-server via
 * {@link CamPayPaymentService#checkStatus} plutôt que de faire confiance au seul champ "status"
 * du payload, par défense en profondeur.
 */
@Slf4j
@Tag(name = "Payments - CamPay Webhook", description = "Callback CamPay (signature JWT vérifiée, puis statut re-confirmé côté serveur)")
@RestController
@RequestMapping("/api/payments/campay/webhook")
@RequiredArgsConstructor
public class CamPayWebhookController {

    private final PaymentRepository paymentRepository;
    private final CamPayPaymentService camPayPaymentService;
    private final CamPayWebhookVerifier camPayWebhookVerifier;
    private final OrderService orderService;
    private final ObjectMapper objectMapper;

    @PostMapping
    public ResponseEntity<Void> handleWebhook(@RequestBody String rawPayload) {
        JsonNode node;
        try {
            node = objectMapper.readTree(rawPayload);
        } catch (Exception e) {
            log.warn("Impossible de parser le payload du webhook CamPay", e);
            return ResponseEntity.ok().build();
        }

        String signature = node.hasNonNull("signature") ? node.get("signature").asText() : null;
        if (!camPayWebhookVerifier.isValid(signature)) {
            log.warn("Signature CamPay invalide, webhook rejeté");
            return ResponseEntity.status(401).build();
        }

        Payment payment = resolvePayment(node);
        if (payment == null) {
            log.warn("Webhook CamPay reçu sans référence exploitable ou inconnue, ignoré : {}", rawPayload);
            return ResponseEntity.ok().build();
        }
        if (payment.getProviderReference() == null) {
            log.warn("Paiement {} sans providerReference, impossible de vérifier le statut", payment.getId());
            return ResponseEntity.ok().build();
        }

        PaymentStatus status = camPayPaymentService.checkStatus(payment.getProviderReference());
        if (status == PaymentStatus.SUCCESSFUL) {
            orderService.confirmPayment(payment.getOrder().getId(), payment.getProviderReference());
        } else if (status == PaymentStatus.FAILED) {
            orderService.failPayment(payment.getOrder().getId(), payment.getProviderReference(), "Paiement refusé ou expiré (CamPay)");
        }

        return ResponseEntity.ok().build();
    }

    /** Essaie d'abord la "reference" CamPay, puis notre "external_reference" en repli. */
    private Payment resolvePayment(JsonNode node) {
        if (node.hasNonNull("reference")) {
            Payment payment = paymentRepository.findByProviderReference(node.get("reference").asText()).orElse(null);
            if (payment != null) {
                return payment;
            }
        }
        if (node.hasNonNull("external_reference")) {
            try {
                return paymentRepository.findByExternalReference(UUID.fromString(node.get("external_reference").asText()))
                        .orElse(null);
            } catch (IllegalArgumentException e) {
                return null;
            }
        }
        return null;
    }
}
