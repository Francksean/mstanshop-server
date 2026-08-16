package com.mstan.shop.newsletter.service;

import com.mstan.shop.common.exception.DuplicateResourceException;
import com.mstan.shop.newsletter.dto.NewsletterSubscribeRequest;
import com.mstan.shop.newsletter.entity.NewsletterSubscription;
import com.mstan.shop.newsletter.repository.NewsletterSubscriptionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class NewsletterService {

    private final NewsletterSubscriptionRepository newsletterSubscriptionRepository;

    @Transactional
    public void subscribe(NewsletterSubscribeRequest request) {
        boolean hasEmail = request.email() != null && !request.email().isBlank();
        boolean hasPhone = request.phoneNumber() != null && !request.phoneNumber().isBlank();

        if (!hasEmail && !hasPhone) {
            throw new IllegalArgumentException("Veuillez renseigner au moins une adresse email ou un numéro de téléphone");
        }

        if (hasEmail && newsletterSubscriptionRepository.existsByEmail(request.email())) {
            throw new DuplicateResourceException("Cette adresse email est déjà inscrite à la newsletter");
        }
        if (hasPhone && newsletterSubscriptionRepository.existsByPhoneNumber(request.phoneNumber())) {
            throw new DuplicateResourceException("Ce numéro est déjà inscrit à la newsletter");
        }

        NewsletterSubscription subscription = new NewsletterSubscription();
        subscription.setEmail(hasEmail ? request.email() : null);
        subscription.setPhoneNumber(hasPhone ? request.phoneNumber() : null);
        newsletterSubscriptionRepository.save(subscription);
    }
}
