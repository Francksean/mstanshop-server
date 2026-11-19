package com.mstan.shop.whatsapp.controller;

import com.mstan.shop.whatsapp.dto.WhatsAppTemplateEventCodeRequest;
import com.mstan.shop.whatsapp.dto.WhatsAppTemplateResponse;
import com.mstan.shop.whatsapp.service.WhatsAppTemplateService;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.UUID;

/**
 * Les templates eux-mêmes (contenu, langue, catégorie) sont créés et édités dans Genuka —
 * cet endpoint est en lecture seule sur leur contenu, et n'autorise que l'assignation de
 * l'événement de commande MSTAN ({@code eventCode}) qui déclenche leur envoi.
 */
@Tag(name = "Admin - WhatsApp Templates", description = "Lecture des templates WhatsApp synchronisés depuis Genuka et assignation d'un eventCode, réservé aux administrateurs")
@RestController
@RequestMapping("/api/admin/whatsapp/templates")
@RequiredArgsConstructor
public class WhatsAppTemplateAdminController {

    private final WhatsAppTemplateService whatsAppTemplateService;

    @GetMapping
    public List<WhatsAppTemplateResponse> list() {
        return whatsAppTemplateService.list();
    }

    @GetMapping("/{id}")
    public WhatsAppTemplateResponse get(@PathVariable UUID id) {
        return whatsAppTemplateService.get(id);
    }

    @PatchMapping("/{id}/event-code")
    public WhatsAppTemplateResponse assignEventCode(@PathVariable UUID id, @RequestBody WhatsAppTemplateEventCodeRequest request) {
        return whatsAppTemplateService.assignEventCode(id, request.eventCode());
    }

    @PostMapping("/sync")
    public ResponseEntity<Void> sync() {
        whatsAppTemplateService.syncFromGenuka();
        return ResponseEntity.noContent().build();
    }
}
