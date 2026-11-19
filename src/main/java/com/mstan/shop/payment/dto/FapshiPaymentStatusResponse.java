package com.mstan.shop.payment.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

/** Réponse de GET /payment-status/{transId}. Statuts : CREATED, PENDING, SUCCESSFUL, FAILED, EXPIRED. */
@JsonIgnoreProperties(ignoreUnknown = true)
public record FapshiPaymentStatusResponse(
        String transId,
        String status,
        String externalId,
        String userId,
        Long amount
) {
}
