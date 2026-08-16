package com.mstan.shop.whatsapp.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.databind.JsonNode;

import java.time.Instant;

@JsonIgnoreProperties(ignoreUnknown = true)
public record GenukaWebhookEvent(
        String id,
        String type,
        Instant createdAt,
        String companyId,
        JsonNode data
) {
}
