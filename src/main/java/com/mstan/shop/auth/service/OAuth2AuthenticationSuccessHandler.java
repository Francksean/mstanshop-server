package com.mstan.shop.auth.service;

import com.mstan.shop.auth.entity.RefreshToken;
import com.mstan.shop.common.security.JwtService;
import com.mstan.shop.common.security.SecurityUser;
import com.mstan.shop.user.entity.User;
import com.mstan.shop.user.repository.UserRepository;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.core.oidc.user.OidcUser;
import org.springframework.security.web.authentication.SimpleUrlAuthenticationSuccessHandler;
import org.springframework.stereotype.Component;
import org.springframework.web.util.UriComponentsBuilder;

import java.io.IOException;

@Component
@RequiredArgsConstructor
public class OAuth2AuthenticationSuccessHandler extends SimpleUrlAuthenticationSuccessHandler {

    private final UserRepository userRepository;
    private final JwtService jwtService;
    private final RefreshTokenService refreshTokenService;

    @Value("${app.frontend.oauth-redirect-url}")
    private String oauthRedirectUrl;

    @Override
    public void onAuthenticationSuccess(
            HttpServletRequest request,
            HttpServletResponse response,
            Authentication authentication
    ) throws IOException, ServletException {
        OidcUser oidcUser = (OidcUser) authentication.getPrincipal();
        User user = userRepository.findByEmail(oidcUser.getEmail())
                .orElseThrow(() -> new IllegalStateException("Utilisateur OAuth2 introuvable après connexion"));

        SecurityUser securityUser = new SecurityUser(user);
        String accessToken = jwtService.generateAccessToken(securityUser);
        RefreshToken refreshToken = refreshTokenService.createRefreshToken(user);

        String targetUrl = UriComponentsBuilder.fromUriString(oauthRedirectUrl)
                .queryParam("token", accessToken)
                .queryParam("refresh", refreshToken.getToken())
                .build().toUriString();

        clearAuthenticationAttributes(request);
        getRedirectStrategy().sendRedirect(request, response, targetUrl);
    }
}
