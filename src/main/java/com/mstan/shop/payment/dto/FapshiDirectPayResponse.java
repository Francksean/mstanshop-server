package com.mstan.shop.payment.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

/** Réponse 200 de POST /direct-pay : le prompt a été envoyé, pas encore payé. */
@JsonIgnoreProperties(ignoreUnknown = true)
public record FapshiDirectPayResponse(
        String message,
        String transId,
        String dateInitiated
) {
}
