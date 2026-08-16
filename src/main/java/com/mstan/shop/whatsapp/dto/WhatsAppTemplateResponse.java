package com.mstan.shop.whatsapp.dto;

import com.fasterxml.jackson.databind.JsonNode;
import com.mstan.shop.whatsapp.entity.WhatsAppTemplateCategory;
import com.mstan.shop.whatsapp.entity.WhatsAppTemplateEventCode;
import com.mstan.shop.whatsapp.entity.WhatsAppTemplateStatus;

import java.time.Instant;
import java.util.UUID;

public record WhatsAppTemplateResponse(
        UUID id,
        String genukaTemplateId,
        String name,
        String language,
        WhatsAppTemplateCategory category,
        WhatsAppTemplateEventCode eventCode,
        JsonNode components,
        WhatsAppTemplateStatus status,
        Instant createdAt,
        Instant updatedAt
) {
}
