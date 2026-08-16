package com.mstan.shop.product.dto;

public record ProductShareResponse(
        String url,
        String title,
        String description,
        String imageUrl
) {
}
