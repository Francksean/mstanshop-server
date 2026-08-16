package com.mstan.shop.whatsapp.controller;

import com.mstan.shop.whatsapp.dto.WhatsAppTemplateRequest;
import com.mstan.shop.whatsapp.dto.WhatsAppTemplateResponse;
import com.mstan.shop.whatsapp.service.WhatsAppTemplateService;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.UUID;

@Tag(name = "Admin - WhatsApp Templates", description = "CRUD des templates WhatsApp (Genuka/Meta), réservé aux administrateurs")
@RestController
@RequestMapping("/api/admin/whatsapp/templates")
@RequiredArgsConstructor
public class WhatsAppTemplateAdminController {

    private final WhatsAppTemplateService whatsAppTemplateService;

    @PostMapping
    public ResponseEntity<WhatsAppTemplateResponse> create(@Valid @RequestBody WhatsAppTemplateRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(whatsAppTemplateService.create(request));
    }

    @GetMapping
    public List<WhatsAppTemplateResponse> list() {
        return whatsAppTemplateService.list();
    }

    @GetMapping("/{id}")
    public WhatsAppTemplateResponse get(@PathVariable UUID id) {
        return whatsAppTemplateService.get(id);
    }

    @PutMapping("/{id}")
    public WhatsAppTemplateResponse update(@PathVariable UUID id, @Valid @RequestBody WhatsAppTemplateRequest request) {
        return whatsAppTemplateService.update(id, request);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable UUID id) {
        whatsAppTemplateService.delete(id);
        return ResponseEntity.noContent().build();
    }
}
