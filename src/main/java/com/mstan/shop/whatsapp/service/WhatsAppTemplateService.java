package com.mstan.shop.whatsapp.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.mstan.shop.common.exception.ResourceNotFoundException;
import com.mstan.shop.whatsapp.dto.GenukaTemplateResponse;
import com.mstan.shop.whatsapp.dto.WhatsAppTemplateRequest;
import com.mstan.shop.whatsapp.dto.WhatsAppTemplateResponse;
import com.mstan.shop.whatsapp.entity.WhatsAppTemplate;
import com.mstan.shop.whatsapp.entity.WhatsAppTemplateCategory;
import com.mstan.shop.whatsapp.entity.WhatsAppTemplateStatus;
import com.mstan.shop.whatsapp.repository.WhatsAppTemplateRepository;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class WhatsAppTemplateService {

    private final WhatsAppTemplateRepository whatsAppTemplateRepository;
    private final GenukaTemplateClient genukaTemplateClient;
    private final ObjectMapper objectMapper;

    @Transactional
    public WhatsAppTemplateResponse create(WhatsAppTemplateRequest request) {
        WhatsAppTemplateCategory category = request.category() != null ? request.category() : WhatsAppTemplateCategory.UTILITY;
        String language = request.language() != null ? request.language() : "fr";

        GenukaTemplateResponse genukaResponse = genukaTemplateClient.create(
                request.name(), language, category.name(), request.components()
        );

        WhatsAppTemplate template = new WhatsAppTemplate();
        template.setGenukaTemplateId(genukaResponse.id());
        template.setName(request.name());
        template.setLanguage(language);
        template.setCategory(category);
        template.setEventCode(request.eventCode());
        template.setComponents(writeComponents(request.components()));
        template.setStatus(mapStatus(genukaResponse.status()));

        return toResponse(whatsAppTemplateRepository.save(template));
    }

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
    public WhatsAppTemplateResponse update(UUID id, WhatsAppTemplateRequest request) {
        WhatsAppTemplate existing = findTemplate(id);
        genukaTemplateClient.delete(existing.getGenukaTemplateId());
        whatsAppTemplateRepository.delete(existing);
        return create(request);
    }

    @Transactional
    public void delete(UUID id) {
        WhatsAppTemplate template = findTemplate(id);
        genukaTemplateClient.delete(template.getGenukaTemplateId());
        whatsAppTemplateRepository.delete(template);
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
    private String writeComponents(JsonNode components) {
        return objectMapper.writeValueAsString(components);
    }

    @SneakyThrows(JsonProcessingException.class)
    private JsonNode readComponents(String components) {
        return objectMapper.readTree(components);
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
