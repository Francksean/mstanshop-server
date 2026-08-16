package com.mstan.shop.whatsapp.repository;

import com.mstan.shop.whatsapp.entity.WhatsAppTemplate;
import com.mstan.shop.whatsapp.entity.WhatsAppTemplateEventCode;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface WhatsAppTemplateRepository extends JpaRepository<WhatsAppTemplate, UUID> {

    Optional<WhatsAppTemplate> findByEventCode(WhatsAppTemplateEventCode eventCode);

    Optional<WhatsAppTemplate> findByGenukaTemplateId(String genukaTemplateId);
}
