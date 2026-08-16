package com.mstan.shop.whatsapp.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

/**
 * La doc Genuka ne détaille pas exactement le corps de réponse de POST /messages — champs
 * volontairement minimaux et tolérants (propriétés inconnues ignorées), à ajuster une fois un
 * envoi réel testé en sandbox.
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public record GenukaSendMessageResponse(
        String id,
        String status
) {
}
