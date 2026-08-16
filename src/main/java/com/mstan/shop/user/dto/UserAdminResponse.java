package com.mstan.shop.user.dto;

import com.mstan.shop.user.entity.RoleName;

import java.time.Instant;
import java.util.Set;
import java.util.UUID;

public record UserAdminResponse(
        UUID id,
        String email,
        String firstName,
        String lastName,
        boolean enabled,
        boolean deleted,
        String authProvider,
        Set<RoleName> roles,
        Instant createdAt
) {
}
