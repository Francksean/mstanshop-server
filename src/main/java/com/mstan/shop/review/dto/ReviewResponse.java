package com.mstan.shop.review.dto;

import java.time.Instant;
import java.util.UUID;

public record ReviewResponse(
        UUID id,
        UUID productId,
        UUID userId,
        String userFirstName,
        int rating,
        String comment,
        Instant createdAt
) {
}
