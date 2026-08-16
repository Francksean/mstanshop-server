package com.mstan.shop.cart.dto;

import java.math.BigDecimal;
import java.util.UUID;

public record CartItemResponse(
        UUID id,
        UUID productId,
        String productName,
        String productThumbnailUrl,
        UUID variantId,
        String variantColorName,
        String variantColorHex,
        String variantSize,
        BigDecimal unitPrice,
        int quantity,
        BigDecimal lineTotal
) {
}
