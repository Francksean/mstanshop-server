package com.mstan.shop.auth.service;

import com.mstan.shop.auth.entity.PasswordResetToken;
import com.mstan.shop.auth.repository.PasswordResetTokenRepository;
import com.mstan.shop.common.email.EmailService;
import com.mstan.shop.common.exception.InvalidTokenException;
import com.mstan.shop.user.entity.User;
import com.mstan.shop.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class PasswordResetService {

    private static final long RESET_TOKEN_VALIDITY_HOURS = 1;

    private final PasswordResetTokenRepository passwordResetTokenRepository;
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final EmailService emailService;

    @Transactional
    public void requestReset(String email) {
        userRepository.findByEmail(email).ifPresent(user -> {
            PasswordResetToken resetToken = new PasswordResetToken();
            resetToken.setUser(user);
            resetToken.setToken(UUID.randomUUID().toString());
            resetToken.setExpiryDate(Instant.now().plus(RESET_TOKEN_VALIDITY_HOURS, ChronoUnit.HOURS));
            resetToken.setUsed(false);
            passwordResetTokenRepository.save(resetToken);

            emailService.sendPasswordResetLink(user.getEmail(), resetToken.getToken());
        });
        // Silently no-op if the email is unknown, to avoid leaking account existence.
    }

    @Transactional
    public void resetPassword(String token, String newPassword) {
        PasswordResetToken resetToken = passwordResetTokenRepository.findByToken(token)
                .orElseThrow(() -> new InvalidTokenException("Token de réinitialisation invalide"));

        if (resetToken.isUsed() || resetToken.getExpiryDate().isBefore(Instant.now())) {
            throw new InvalidTokenException("Token de réinitialisation expiré ou déjà utilisé");
        }

        User user = resetToken.getUser();
        user.setPassword(passwordEncoder.encode(newPassword));
        userRepository.save(user);

        resetToken.setUsed(true);
        passwordResetTokenRepository.save(resetToken);
    }
}
