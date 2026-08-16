package com.mstan.shop.promo.controller;

import com.mstan.shop.promo.dto.PromoCodeRequest;
import com.mstan.shop.promo.dto.PromoCodeResponse;
import com.mstan.shop.promo.service.PromoCodeService;
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

@Tag(name = "Admin - Promo Codes", description = "CRUD des codes promo réservé aux administrateurs")
@RestController
@RequestMapping("/api/admin/promo-codes")
@RequiredArgsConstructor
public class PromoCodeAdminController {

    private final PromoCodeService promoCodeService;

    @GetMapping
    public List<PromoCodeResponse> getAll() {
        return promoCodeService.getAll();
    }

    @GetMapping("/{id}")
    public PromoCodeResponse getById(@PathVariable UUID id) {
        return promoCodeService.getById(id);
    }

    @PostMapping
    public ResponseEntity<PromoCodeResponse> create(@Valid @RequestBody PromoCodeRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(promoCodeService.create(request));
    }

    @PutMapping("/{id}")
    public PromoCodeResponse update(@PathVariable UUID id, @Valid @RequestBody PromoCodeRequest request) {
        return promoCodeService.update(id, request);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable UUID id) {
        promoCodeService.delete(id);
        return ResponseEntity.noContent().build();
    }
}
