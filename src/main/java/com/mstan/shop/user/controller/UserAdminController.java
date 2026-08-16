package com.mstan.shop.user.controller;

import com.mstan.shop.common.security.SecurityUser;
import com.mstan.shop.common.util.PagedResponse;
import com.mstan.shop.user.dto.UpdateUserRolesRequest;
import com.mstan.shop.user.dto.UpdateUserStatusRequest;
import com.mstan.shop.user.dto.UserAdminResponse;
import com.mstan.shop.user.entity.RoleName;
import com.mstan.shop.user.service.UserService;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

@Tag(name = "Admin - Users", description = "Gestion des comptes utilisateurs réservée aux administrateurs")
@RestController
@RequestMapping("/api/admin/users")
@RequiredArgsConstructor
public class UserAdminController {

    private final UserService userService;

    @GetMapping
    public PagedResponse<UserAdminResponse> getAllUsers(
            @RequestParam(required = false) RoleName role,
            @RequestParam(required = false) Boolean enabled,
            @RequestParam(required = false) String search,
            Pageable pageable
    ) {
        return userService.getAllUsers(role, enabled, search, pageable);
    }

    @GetMapping("/{id}")
    public UserAdminResponse getUser(@PathVariable UUID id) {
        return userService.getUserByIdAdmin(id);
    }

    @PatchMapping("/{id}/status")
    public UserAdminResponse updateStatus(@PathVariable UUID id, @Valid @RequestBody UpdateUserStatusRequest request) {
        return userService.updateUserStatus(id, request);
    }

    @PatchMapping("/{id}/roles")
    public UserAdminResponse updateRoles(
            @PathVariable UUID id,
            @Valid @RequestBody UpdateUserRolesRequest request,
            @AuthenticationPrincipal SecurityUser currentAdmin
    ) {
        return userService.updateUserRoles(id, request, currentAdmin.getId());
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteUser(@PathVariable UUID id, @AuthenticationPrincipal SecurityUser currentAdmin) {
        userService.softDeleteUser(id, currentAdmin.getId());
        return ResponseEntity.noContent().build();
    }
}
