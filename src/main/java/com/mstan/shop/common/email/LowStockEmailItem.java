package com.mstan.shop.common.email;

public record LowStockEmailItem(
        String productName,
        String colorLabel,
        int currentStock,
        int threshold
) {
}
