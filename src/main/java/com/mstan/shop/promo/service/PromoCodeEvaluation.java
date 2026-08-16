package com.mstan.shop.promo.service;

import com.mstan.shop.promo.entity.PromoCode;

import java.math.BigDecimal;

public record PromoCodeEvaluation(
        boolean valid,
        String reason,
        PromoCode promoCode,
        BigDecimal discountAmount
) {
}
