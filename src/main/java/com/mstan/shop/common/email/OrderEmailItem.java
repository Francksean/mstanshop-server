package com.mstan.shop.common.email;

import java.math.BigDecimal;

public record OrderEmailItem(
        String name,
        String colorLabel,
        String thumbnailUrl,
        int quantity,
        BigDecimal unitPrice,
        BigDecimal lineTotal
) {
}
