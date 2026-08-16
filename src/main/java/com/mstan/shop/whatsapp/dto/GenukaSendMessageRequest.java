package com.mstan.shop.whatsapp.dto;

public record GenukaSendMessageRequest(
        String connectionId,
        String to,
        GenukaTemplatePayload template
) {
}
