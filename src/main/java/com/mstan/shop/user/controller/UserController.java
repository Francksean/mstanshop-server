package com.mstan.shop.user.controller;

import com.mstan.shop.common.security.SecurityUser;
import com.mstan.shop.user.dto.UpdateProfileRequest;
import com.mstan.shop.user.dto.UserProfileResponse;
import com.mstan.shop.user.service.UserService;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "Users", description = "Gestion du profil utilisateur")
@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    @GetMapping("/me")
    public UserProfileResponse getProfile(@AuthenticationPrincipal SecurityUser securityUser) {
        return userService.getProfile(securityUser.getId());
    }

    @PutMapping("/me")
    public UserProfileResponse updateProfile(
            @AuthenticationPrincipal SecurityUser securityUser,
            @Valid @RequestBody UpdateProfileRequest request
    ) {
        return userService.updateProfile(securityUser.getId(), request);
    }
}
