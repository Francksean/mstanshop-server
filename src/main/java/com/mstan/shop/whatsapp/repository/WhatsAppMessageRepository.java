package com.mstan.shop.whatsapp.repository;

import com.mstan.shop.whatsapp.entity.WhatsAppMessage;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface WhatsAppMessageRepository extends JpaRepository<WhatsAppMessage, UUID> {

    Optional<WhatsAppMessage> findByProviderMessageId(String providerMessageId);
}
