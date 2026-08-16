package com.mstan.shop.payment.service;

import com.mstan.shop.order.entity.Order;
import com.mstan.shop.payment.dto.CamPayCollectRequest;
import com.mstan.shop.payment.dto.CamPayCollectResponse;
import com.mstan.shop.payment.dto.CamPayTransactionResponse;
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
public class CamPayPaymentService {

    private final PaymentRepository paymentRepository;
    private final RestClient restClient;

    public CamPayPaymentService(
            PaymentRepository paymentRepository,
            @Value("${app.payment.campay.base-url}") String baseUrl,
            @Value("${app.payment.campay.api-key}") String apiKey
    ) {
        this.paymentRepository = paymentRepository;
        this.restClient = RestClient.builder()
                .baseUrl(baseUrl)
                .defaultHeader("Authorization", "Token " + apiKey)
                .build();
    }

    /** Initiates a MoMo/Orange Money collection and records the attempt as a PENDING Payment. */
    public Payment initiateCollection(Order order, String phoneNumber) {
        UUID externalReference = UUID.randomUUID();

        CamPayCollectRequest request = new CamPayCollectRequest(
                order.getTotalAmount().toBigInteger().toString(),
                "XAF",
                phoneNumber,
                "Commande " + order.getOrderNumber(),
                externalReference.toString(),
                ""
        );

        CamPayCollectResponse response = restClient.post()
                .uri("/collect/")
                .body(request)
                .retrieve()
                .body(CamPayCollectResponse.class);

        Payment payment = new Payment();
        payment.setOrder(order);
        payment.setMethod(PaymentMethod.MOBILE_PAYMENT);
        payment.setExternalReference(externalReference);
        payment.setProviderReference(response != null ? response.reference() : null);
        payment.setStatus(PaymentStatus.PENDING);
        payment.setAmount(order.getTotalAmount());
        payment.setCurrency("XAF");
        payment.setPhoneNumber(phoneNumber);

        return paymentRepository.save(payment);
    }

    /** Re-checks the authoritative status server-to-server — never trust a webhook payload alone. */
    public PaymentStatus checkStatus(String providerReference) {
        try {
            CamPayTransactionResponse response = restClient.get()
                    .uri("/transaction/{reference}/", providerReference)
                    .retrieve()
                    .body(CamPayTransactionResponse.class);

            if (response == null || response.status() == null) {
                return PaymentStatus.PENDING;
            }

            return switch (response.status()) {
                case "SUCCESSFUL" -> PaymentStatus.SUCCESSFUL;
                case "FAILED" -> PaymentStatus.FAILED;
                default -> PaymentStatus.PENDING;
            };
        } catch (Exception e) {
            log.error("Échec de la vérification du statut CamPay pour la référence {}", providerReference, e);
            return PaymentStatus.PENDING;
        }
    }
}
