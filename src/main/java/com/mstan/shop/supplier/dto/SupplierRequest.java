package com.mstan.shop.supplier.dto;

import jakarta.validation.constraints.NotBlank;

public record SupplierRequest(

        @NotBlank(message = "Le nom est obligatoire")
        String name,

        @NotBlank(message = "Le numéro de téléphone principal est obligatoire")
        String primaryPhone,

        String secondaryPhone,

        String location
) {
}
