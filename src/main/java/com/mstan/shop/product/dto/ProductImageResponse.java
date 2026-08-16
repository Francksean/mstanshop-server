package com.mstan.shop.product.dto;

import java.util.UUID;

public record ProductImageResponse(
        UUID id,
        String url,
        boolean thumbnail,
        UUID variantId
) {
}
