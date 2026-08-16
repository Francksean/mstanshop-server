package com.mstan.shop.common.storage.dto;

import java.time.Instant;

public record PresignedUploadResponse(
        String uploadUrl,
        String objectKey,
        String publicUrl,
        Instant expiresAt
) {
}
