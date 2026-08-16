package com.mstan.shop.auth.service;

import com.mstan.shop.user.entity.AuthProvider;
import com.mstan.shop.user.entity.Role;
import com.mstan.shop.user.entity.RoleName;
import com.mstan.shop.user.entity.User;
import com.mstan.shop.user.repository.RoleRepository;
import com.mstan.shop.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.oauth2.client.oidc.userinfo.OidcUserRequest;
import org.springframework.security.oauth2.client.oidc.userinfo.OidcUserService;
import org.springframework.security.oauth2.core.oidc.user.OidcUser;
import org.springframework.security.oauth2.core.oidc.user.DefaultOidcUser;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Set;

@Slf4j
@Service
@RequiredArgsConstructor
public class CustomOidcUserService extends OidcUserService {

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;

    @Override
    @Transactional
    public OidcUser loadUser(OidcUserRequest userRequest) {
        OidcUser oidcUser = super.loadUser(userRequest);

        String providerId = oidcUser.getSubject();
        String email = oidcUser.getEmail();
        String firstName = oidcUser.getGivenName() != null ? oidcUser.getGivenName() : "";
        String lastName = oidcUser.getFamilyName() != null ? oidcUser.getFamilyName() : "";

        User user = userRepository.findByAuthProviderAndProviderId(AuthProvider.GOOGLE, providerId)
                .or(() -> userRepository.findByEmail(email))
                .map(existing -> updateExistingUser(existing, providerId))
                .orElseGet(() -> createNewUser(email, firstName, lastName, providerId));

        userRepository.save(user);

        return new DefaultOidcUser(oidcUser.getAuthorities(), userRequest.getIdToken(), oidcUser.getUserInfo(), "email");
    }

    private User updateExistingUser(User user, String providerId) {
        if (user.getAuthProvider() == AuthProvider.LOCAL) {
            user.setAuthProvider(AuthProvider.GOOGLE);
            user.setProviderId(providerId);
        }
        return user;
    }

    private User createNewUser(String email, String firstName, String lastName, String providerId) {
        log.info("Création d'un nouvel utilisateur via Google OAuth2 : {}", email);

        Role userRole = roleRepository.findByName(RoleName.ROLE_USER)
                .orElseThrow(() -> new IllegalStateException("Rôle ROLE_USER introuvable, vérifier le seed data"));

        User user = new User();
        user.setEmail(email);
        user.setFirstName(firstName);
        user.setLastName(lastName);
        user.setPassword(null);
        user.setEnabled(true);
        user.setAuthProvider(AuthProvider.GOOGLE);
        user.setProviderId(providerId);
        user.setRoles(Set.of(userRole));
        return user;
    }
}
