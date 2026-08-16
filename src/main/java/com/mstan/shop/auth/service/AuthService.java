package com.mstan.shop.auth.service;

import com.mstan.shop.auth.dto.AuthResponse;
import com.mstan.shop.auth.dto.LoginRequest;
import com.mstan.shop.auth.dto.RegisterRequest;
import com.mstan.shop.auth.entity.RefreshToken;
import com.mstan.shop.cart.dto.AddCartItemRequest;
import com.mstan.shop.cart.service.CartService;
import com.mstan.shop.common.email.EmailService;
import com.mstan.shop.common.exception.DuplicateResourceException;
import com.mstan.shop.common.security.JwtService;
import com.mstan.shop.common.security.SecurityUser;
import com.mstan.shop.user.entity.AuthProvider;
import com.mstan.shop.user.entity.Role;
import com.mstan.shop.user.entity.RoleName;
import com.mstan.shop.user.entity.User;
import com.mstan.shop.user.repository.RoleRepository;
import com.mstan.shop.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Set;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final PasswordEncoder passwordEncoder;
    private final AuthenticationManager authenticationManager;
    private final JwtService jwtService;
    private final RefreshTokenService refreshTokenService;
    private final EmailService emailService;
    private final CartService cartService;

    @Transactional
    public AuthResponse register(RegisterRequest request) {
        if (userRepository.existsByEmail(request.email())) {
            throw new DuplicateResourceException("Un compte existe déjà avec cet email");
        }

        Role userRole = roleRepository.findByName(RoleName.ROLE_USER)
                .orElseThrow(() -> new IllegalStateException("Rôle ROLE_USER introuvable, vérifier le seed data"));

        User user = new User();
        user.setEmail(request.email());
        user.setPassword(passwordEncoder.encode(request.password()));
        user.setFirstName(request.firstName());
        user.setLastName(request.lastName());
        user.setAuthProvider(AuthProvider.LOCAL);
        user.setEnabled(true);
        user.setRoles(Set.of(userRole));
        userRepository.save(user);

        emailService.sendRegistrationConfirmation(user.getEmail(), user.getFirstName());
        mergeGuestCartIfPresent(user.getId(), request.cartItems());

        return generateAuthResponse(user);
    }

    @Transactional
    public AuthResponse login(LoginRequest request) {
        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(request.email(), request.password())
        );

        User user = userRepository.findByEmail(request.email())
                .orElseThrow(() -> new IllegalStateException("Utilisateur authentifié introuvable"));

        mergeGuestCartIfPresent(user.getId(), request.cartItems());

        return generateAuthResponse(user);
    }

    private void mergeGuestCartIfPresent(UUID userId, List<AddCartItemRequest> cartItems) {
        if (cartItems != null && !cartItems.isEmpty()) {
            cartService.mergeGuestItems(userId, cartItems);
        }
    }

    @Transactional
    public AuthResponse refresh(String refreshTokenValue) {
        RefreshToken refreshToken = refreshTokenService.verify(refreshTokenValue);
        User user = refreshToken.getUser();

        SecurityUser securityUser = new SecurityUser(user);
        String accessToken = jwtService.generateAccessToken(securityUser);

        return new AuthResponse(accessToken, refreshToken.getToken());
    }

    @Transactional
    public void logout(User user) {
        refreshTokenService.revokeAllForUser(user);
    }

    private AuthResponse generateAuthResponse(User user) {
        SecurityUser securityUser = new SecurityUser(user);
        String accessToken = jwtService.generateAccessToken(securityUser);
        RefreshToken refreshToken = refreshTokenService.createRefreshToken(user);
        return new AuthResponse(accessToken, refreshToken.getToken());
    }
}
