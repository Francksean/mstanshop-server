package com.mstan.shop.auth.controller;

import com.mstan.shop.auth.dto.AuthResponse;
import com.mstan.shop.auth.dto.ForgotPasswordRequest;
import com.mstan.shop.auth.dto.LoginRequest;
import com.mstan.shop.auth.dto.RefreshRequest;
import com.mstan.shop.auth.dto.RegisterRequest;
import com.mstan.shop.auth.dto.ResetPasswordRequest;
import com.mstan.shop.auth.service.AuthService;
import com.mstan.shop.auth.service.PasswordResetService;
import com.mstan.shop.common.security.SecurityUser;
import com.mstan.shop.user.entity.User;
import com.mstan.shop.user.repository.UserRepository;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "Auth", description = "Inscription, connexion, rafraîchissement de token et réinitialisation de mot de passe")
@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;
    private final PasswordResetService passwordResetService;
    private final UserRepository userRepository;

    @Operation(
            summary = "Inscription",
            description = "Crée un compte et retourne les tokens JWT. Si `cartItems` est fourni (panier local/invité "
                    + "constitué avant inscription), ses lignes sont fusionnées dans le panier du compte créé — "
                    + "les lignes invalides (produit/variante introuvable, stock insuffisant) sont ignorées "
                    + "individuellement sans faire échouer l'inscription."
    )
    @PostMapping("/register")
    public ResponseEntity<AuthResponse> register(@Valid @RequestBody RegisterRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(authService.register(request));
    }

    @Operation(
            summary = "Connexion",
            description = "Authentifie l'utilisateur et retourne les tokens JWT. Si `cartItems` est fourni (panier "
                    + "local/invité constitué avant connexion), ses lignes sont fusionnées dans le panier du compte "
                    + "(addition des quantités si le produit/la variante est déjà dans le panier serveur) — les "
                    + "lignes invalides sont ignorées individuellement sans faire échouer la connexion."
    )
    @PostMapping("/login")
    public ResponseEntity<AuthResponse> login(@Valid @RequestBody LoginRequest request) {
        return ResponseEntity.ok(authService.login(request));
    }

    @PostMapping("/refresh")
    public ResponseEntity<AuthResponse> refresh(@Valid @RequestBody RefreshRequest request) {
        return ResponseEntity.ok(authService.refresh(request.refreshToken()));
    }

    @PostMapping("/logout")
    public ResponseEntity<Void> logout(@AuthenticationPrincipal SecurityUser securityUser) {
        User user = userRepository.findById(securityUser.getId())
                .orElseThrow();
        authService.logout(user);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/forgot-password")
    public ResponseEntity<Void> forgotPassword(@Valid @RequestBody ForgotPasswordRequest request) {
        passwordResetService.requestReset(request.email());
        return ResponseEntity.accepted().build();
    }

    @PostMapping("/reset-password")
    public ResponseEntity<Void> resetPassword(@Valid @RequestBody ResetPasswordRequest request) {
        passwordResetService.resetPassword(request.token(), request.newPassword());
        return ResponseEntity.noContent().build();
    }
}
