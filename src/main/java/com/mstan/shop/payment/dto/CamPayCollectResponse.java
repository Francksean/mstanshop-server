package com.mstan.shop.payment.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public record CamPayCollectResponse(
        String reference
) {
}
