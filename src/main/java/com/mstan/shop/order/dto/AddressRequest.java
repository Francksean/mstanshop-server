package com.mstan.shop.order.dto;

import jakarta.validation.constraints.NotBlank;

public record AddressRequest(

        @NotBlank(message = "Le prénom est obligatoire")
        String firstName,

        @NotBlank(message = "Le nom est obligatoire")
        String lastName,

        @NotBlank(message = "La rue est obligatoire")
        String street,

        @NotBlank(message = "La ville est obligatoire")
        String city,

        @NotBlank(message = "Le code postal est obligatoire")
        String postalCode,

        @NotBlank(message = "Le pays est obligatoire")
        String country,

        @NotBlank(message = "Le téléphone est obligatoire")
        String phone,

        String whatsapp
) {
}
