package com.mstan.shop.product.dto;

import java.util.List;
import java.util.UUID;

public record VariantResponse(
        UUID id,
        String colorName,
        String colorHex,
        String size,
        int stock,
        boolean active,
        List<ProductImageResponse> images
) {
}
