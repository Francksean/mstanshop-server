package com.mstan.shop.whatsapp.dto;

import com.fasterxml.jackson.databind.JsonNode;
import com.mstan.shop.whatsapp.entity.WhatsAppTemplateCategory;
import com.mstan.shop.whatsapp.entity.WhatsAppTemplateEventCode;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record WhatsAppTemplateRequest(

        @NotBlank(message = "Le nom du template est obligatoire")
        String name,

        String language,

        WhatsAppTemplateCategory category,

        WhatsAppTemplateEventCode eventCode,

        @NotNull(message = "Les composants du template sont obligatoires")
        JsonNode components
) {
}
