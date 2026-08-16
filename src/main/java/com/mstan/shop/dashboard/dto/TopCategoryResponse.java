package com.mstan.shop.dashboard.dto;

import java.math.BigDecimal;
import java.util.UUID;

public record TopCategoryResponse(
        UUID categoryId,
        String categoryName,
        BigDecimal revenue
) {
}
