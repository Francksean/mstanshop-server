package com.mstan.shop.payment.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

public record CamPayCollectRequest(
        String amount,
        String currency,
        String from,
        String description,

        @JsonProperty("external_reference")
        String externalReference,

        @JsonProperty("external_user")
        String externalUser
) {
}
