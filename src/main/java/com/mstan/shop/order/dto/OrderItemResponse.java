package com.mstan.shop.order.dto;

import java.math.BigDecimal;
import java.util.UUID;

public record OrderItemResponse(
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
