package com.mstan.shop.newsletter.dto;

import jakarta.validation.constraints.Email;

public record NewsletterSubscribeRequest(

        @Email(message = "L'adresse email est invalide")
        String email,

        String phoneNumber
) {
}
