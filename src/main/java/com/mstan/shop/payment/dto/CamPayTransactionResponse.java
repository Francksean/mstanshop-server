package com.mstan.shop.payment.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

/** Response shape for GET /transaction/{reference}/ — inferred from the documented /collect/ fields. */
@JsonIgnoreProperties(ignoreUnknown = true)
public record CamPayTransactionResponse(
        String reference,

        @JsonProperty("external_reference")
        String externalReference,

        String status,
        String amount,
        String currency,
        String operator,
        String code,

        @JsonProperty("operator_reference")
        String operatorReference
) {
}
