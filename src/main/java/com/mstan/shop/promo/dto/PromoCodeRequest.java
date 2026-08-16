package com.mstan.shop.promo.dto;

import com.mstan.shop.promo.entity.PromoDiscountType;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;
import java.time.Instant;

public record PromoCodeRequest(

        @NotBlank(message = "Le code est obligatoire")
        String code,

        @NotNull(message = "Le type de remise est obligatoire")
        PromoDiscountType discountType,

        @NotNull(message = "La valeur de la remise est obligatoire")
        @DecimalMin(value = "0.01", message = "La valeur de la remise doit être positive")
        BigDecimal discountValue,

        Instant expiresAt,

        @DecimalMin(value = "0.0", message = "Le montant minimum ne peut pas être négatif")
        BigDecimal minOrderAmount,

        @Min(value = 1, message = "Le nombre d'utilisations maximum doit être positif")
        Integer maxUses,

        boolean active
) {
}
