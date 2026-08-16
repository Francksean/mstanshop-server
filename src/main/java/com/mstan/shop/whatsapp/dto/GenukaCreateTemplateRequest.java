package com.mstan.shop.whatsapp.dto;

import com.fasterxml.jackson.databind.JsonNode;

public record GenukaCreateTemplateRequest(
        String connectionId,
        String name,
        String language,
        String category,
        String parameterFormat,
        JsonNode components
) {
}
