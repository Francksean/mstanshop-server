package com.mstan.shop.user.dto;

import java.time.Instant;
import java.util.UUID;

public record UserProfileResponse(
        UUID id,
        String email,
        String firstName,
        String lastName,
        String authProvider,
        Instant createdAt
) {
}
