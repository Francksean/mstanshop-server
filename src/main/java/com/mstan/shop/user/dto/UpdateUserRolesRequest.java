package com.mstan.shop.user.dto;

import com.mstan.shop.user.entity.RoleName;
import jakarta.validation.constraints.NotEmpty;

import java.util.Set;

public record UpdateUserRolesRequest(
        @NotEmpty(message = "Au moins un rôle est requis")
        Set<RoleName> roles
) {
}
