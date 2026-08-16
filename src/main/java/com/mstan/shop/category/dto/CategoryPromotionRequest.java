package com.mstan.shop.category.dto;

import com.mstan.shop.promo.entity.PromoDiscountType;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;
import java.time.Instant;

public record CategoryPromotionRequest(

        @NotNull(message = "Le type de remise est obligatoire")
        PromoDiscountType discountType,

        @NotNull(message = "La valeur de la remise est obligatoire")
        @DecimalMin(value = "0.01", message = "La valeur de la remise doit être positive")
        BigDecimal discountValue,

        Instant startsAt,

        Instant endsAt,

        boolean active
) {
}
