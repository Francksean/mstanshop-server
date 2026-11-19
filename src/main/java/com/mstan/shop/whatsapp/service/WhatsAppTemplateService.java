package com.mstan.shop.whatsapp.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.mstan.shop.common.exception.ResourceNotFoundException;
import com.mstan.shop.whatsapp.dto.GenukaTemplateResponse;
import com.mstan.shop.whatsapp.dto.WhatsAppTemplateResponse;
import com.mstan.shop.whatsapp.entity.WhatsAppTemplate;
import com.mstan.shop.whatsapp.entity.WhatsAppTemplateCategory;
import com.mstan.shop.whatsapp.entity.WhatsAppTemplateEventCode;
import com.mstan.shop.whatsapp.entity.WhatsAppTemplateStatus;
import com.mstan.shop.whatsapp.repository.WhatsAppTemplateRepository;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;

/**
 * Les templates WhatsApp sont créés et édités directement dans Genuka (UI + workflow
 * d'approbation Meta) — ce service ne fait plus que refléter localement ce qui existe côté
 * Genuka (via {@link #syncFromGenuka()}) et porter le lien métier propre à MSTAN entre un
 * template et un événement de commande ({@code eventCode}), consommé par
 * {@link GenukaWhatsAppService}.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class WhatsAppTemplateService {

    private final WhatsAppTemplateRepository whatsAppTemplateRepository;
    private final GenukaTemplateClient genukaTemplateClient;
    private final ObjectMapper objectMapper;

    public List<WhatsAppTemplateResponse> list() {
        return whatsAppTemplateRepository.findAll().stream().map(this::toResponse).toList();
    }

    @Transactional
    public WhatsAppTemplateResponse get(UUID id) {
        WhatsAppTemplate template = findTemplate(id);

        GenukaTemplateResponse genukaResponse = genukaTemplateClient.get(template.getGenukaTemplateId());
        WhatsAppTemplateStatus refreshedStatus = mapStatus(genukaResponse.status());
        if (refreshedStatus != template.getStatus()) {
            template.setStatus(refreshedStatus);
            whatsAppTemplateRepository.save(template);
        }

        return toResponse(template);
    }

    @Transactional
    public WhatsAppTemplateResponse assignEventCode(UUID id, WhatsAppTemplateEventCode eventCode) {
        WhatsAppTemplate template = findTemplate(id);
        template.setEventCode(eventCode);
        return toResponse(whatsAppTemplateRepository.save(template));
    }

    @Transactional
    public void syncFromGenuka() {
        List<GenukaTemplateResponse> genukaTemplates = genukaTemplateClient.list();
        Set<String> genukaTemplateIds = new HashSet<>();

        for (GenukaTemplateResponse genukaTemplate : genukaTemplates) {
            genukaTemplateIds.add(genukaTemplate.id());
            WhatsAppTemplate template = whatsAppTemplateRepository.findByGenukaTemplateId(genukaTemplate.id())
                    .orElseGet(() -> {
                        WhatsAppTemplate created = new WhatsAppTemplate();
                        created.setGenukaTemplateId(genukaTemplate.id());
                        created.setLanguage("fr");
                        created.setCategory(WhatsAppTemplateCategory.UTILITY);
                        return created;
                    });
            template.setName(genukaTemplate.name());
            template.setStatus(mapStatus(genukaTemplate.status()));
            whatsAppTemplateRepository.save(template);
        }

        List<WhatsAppTemplate> orphans = whatsAppTemplateRepository.findAll().stream()
                .filter(template -> !genukaTemplateIds.contains(template.getGenukaTemplateId()))
                .toList();
        if (!orphans.isEmpty()) {
            log.info("Suppression de {} template(s) WhatsApp locaux absents de Genuka", orphans.size());
            whatsAppTemplateRepository.deleteAll(orphans);
        }
    }

    private WhatsAppTemplate findTemplate(UUID id) {
        return whatsAppTemplateRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Template WhatsApp introuvable : " + id));
    }

    private WhatsAppTemplateStatus mapStatus(String genukaStatus) {
        if (genukaStatus == null) {
            return WhatsAppTemplateStatus.PENDING;
        }
        return switch (genukaStatus.toUpperCase()) {
            case "APPROVED" -> WhatsAppTemplateStatus.APPROVED;
            case "REJECTED" -> WhatsAppTemplateStatus.REJECTED;
            case "PAUSED" -> WhatsAppTemplateStatus.PAUSED;
            default -> WhatsAppTemplateStatus.PENDING;
        };
    }

    @SneakyThrows(JsonProcessingException.class)
    private JsonNode readComponents(String components) {
        return components != null ? objectMapper.readTree(components) : null;
    }

    private WhatsAppTemplateResponse toResponse(WhatsAppTemplate template) {
        return new WhatsAppTemplateResponse(
                template.getId(),
                template.getGenukaTemplateId(),
                template.getName(),
                template.getLanguage(),
                template.getCategory(),
                template.getEventCode(),
                readComponents(template.getComponents()),
                template.getStatus(),
                template.getCreatedAt(),
                template.getUpdatedAt()
        );
    }
}
