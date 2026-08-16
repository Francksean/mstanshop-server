package com.mstan.shop.cart.dto;

import java.math.BigDecimal;

public record PromoCodePreviewResponse(
        String code,
        boolean valid,
        String reason,
        BigDecimal subtotal,
        BigDecimal discountAmount,
        BigDecimal totalAmount
) {
}
