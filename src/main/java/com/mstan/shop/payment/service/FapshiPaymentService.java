package com.mstan.shop.payment.service;

import com.mstan.shop.order.entity.Order;
import com.mstan.shop.payment.dto.FapshiDirectPayRequest;
import com.mstan.shop.payment.dto.FapshiDirectPayResponse;
import com.mstan.shop.payment.dto.FapshiPaymentStatusResponse;
import com.mstan.shop.payment.entity.Payment;
import com.mstan.shop.payment.entity.PaymentMethod;
import com.mstan.shop.payment.entity.PaymentStatus;
import com.mstan.shop.payment.repository.PaymentRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

import java.util.UUID;

@Slf4j
@Service
public class FapshiPaymentService {

    private final PaymentRepository paymentRepository;
    private final RestClient restClient;

    public FapshiPaymentService(
            PaymentRepository paymentRepository,
            @Value("${app.payment.fapshi.base-url}") String baseUrl,
            @Value("${app.payment.fapshi.api-user}") String apiUser,
            @Value("${app.payment.fapshi.api-key}") String apiKey
    ) {
        this.paymentRepository = paymentRepository;
        this.restClient = RestClient.builder()
                .baseUrl(baseUrl)
                .defaultHeader("apiuser", apiUser)
                .defaultHeader("apikey", apiKey)
                .build();
    }

    /** Envoie le prompt MoMo/Orange Money (Direct Pay) et enregistre la tentative en PENDING. */
    public Payment initiateCollection(Order order, String phoneNumber) {
        UUID externalReference = UUID.randomUUID();

        FapshiDirectPayRequest request = new FapshiDirectPayRequest(
                order.getTotalAmount().toBigInteger().longValueExact(),
                normalizePhone(phoneNumber),
                null,
                null,
                null,
                order.getId().toString(),
                externalReference.toString(),
                "Commande " + order.getOrderNumber()
        );

        FapshiDirectPayResponse response = restClient.post()
                .uri("/direct-pay")
                .body(request)
                .retrieve()
                .body(FapshiDirectPayResponse.class);

        Payment payment = new Payment();
        payment.setOrder(order);
        payment.setMethod(PaymentMethod.MOBILE_PAYMENT);
        payment.setExternalReference(externalReference);
        payment.setProviderReference(response != null ? response.transId() : null);
        payment.setStatus(PaymentStatus.PENDING);
        payment.setAmount(order.getTotalAmount());
        payment.setCurrency("XAF");
        payment.setPhoneNumber(phoneNumber);

        return paymentRepository.save(payment);
    }

    /** Re-vérifie le statut faisant foi côté serveur — ne jamais se fier au seul payload du webhook. */
    public PaymentStatus checkStatus(String transId) {
        try {
            FapshiPaymentStatusResponse response = restClient.get()
                    .uri("/payment-status/{transId}", transId)
                    .retrieve()
                    .body(FapshiPaymentStatusResponse.class);

            if (response == null || response.status() == null) {
                return PaymentStatus.PENDING;
            }

            return switch (response.status().toUpperCase()) {
                case "SUCCESSFUL" -> PaymentStatus.SUCCESSFUL;
                case "FAILED", "EXPIRED" -> PaymentStatus.FAILED;
                default -> PaymentStatus.PENDING;
            };
        } catch (Exception e) {
            log.error("Échec de la vérification du statut Fapshi pour la transaction {}", transId, e);
            return PaymentStatus.PENDING;
        }
    }

    /** Fapshi attend le numéro local (ex. 677123456), sans espaces ni indicatif +237. */
    private String normalizePhone(String phone) {
        String digits = phone == null ? "" : phone.replaceAll("[^0-9]", "");
        if (digits.startsWith("237") && digits.length() > 9) {
            digits = digits.substring(3);
        }
        return digits;
    }
}
