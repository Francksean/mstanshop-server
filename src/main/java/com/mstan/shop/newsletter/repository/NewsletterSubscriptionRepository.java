package com.mstan.shop.newsletter.repository;

import com.mstan.shop.newsletter.entity.NewsletterSubscription;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface NewsletterSubscriptionRepository extends JpaRepository<NewsletterSubscription, UUID> {

    boolean existsByEmail(String email);

    boolean existsByPhoneNumber(String phoneNumber);
}
