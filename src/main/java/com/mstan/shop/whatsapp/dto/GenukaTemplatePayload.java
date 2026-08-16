package com.mstan.shop.whatsapp.dto;

import java.util.List;

public record GenukaTemplatePayload(
        String name,
        String language,
        List<String> variables
) {
}
