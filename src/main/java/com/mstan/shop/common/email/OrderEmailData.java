package com.mstan.shop.common.email;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

public record OrderEmailData(
        UUID orderId,
        String orderNumber,
        List<OrderEmailItem> items,
        BigDecimal subtotalAmount,
        BigDecimal discountAmount,
        String promoCode,
        BigDecimal totalAmount,
        String addressStreet,
        String addressCity,
        String addressPostalCode,
        String addressCountry
) {
}
