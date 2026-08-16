package com.mstan.shop.whatsapp.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

/**
 * La doc Genuka ne détaille pas exhaustivement le corps de réponse des endpoints /templates —
 * champs volontairement minimaux et tolérants (propriétés inconnues ignorées).
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public record GenukaTemplateResponse(
        String id,
        String name,
        String status
) {
}
