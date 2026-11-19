package com.mstan.shop.payment.dto;

import com.fasterxml.jackson.annotation.JsonInclude;

/** Corps de POST /direct-pay (Fapshi). */
@JsonInclude(JsonInclude.Include.NON_NULL)
public record FapshiDirectPayRequest(
        long amount,
        String phone,
        String medium,
        String name,
        String email,
        String userId,
        String externalId,
        String message
) {
}
