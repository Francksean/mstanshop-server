package com.mstan.shop.product.controller;

import com.mstan.shop.product.dto.VariantCreateRequest;
import com.mstan.shop.product.dto.VariantRequest;
import com.mstan.shop.product.dto.VariantResponse;
import com.mstan.shop.product.service.ProductVariantService;
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

@Tag(name = "Admin - Product Variants", description = "Gestion des variantes de couleur des produits, réservé aux administrateurs")
@RestController
@RequestMapping("/api/admin/products/{productId}/variants")
@RequiredArgsConstructor
public class ProductVariantController {

    private final ProductVariantService productVariantService;

    @GetMapping
    public ResponseEntity<List<VariantResponse>> list(@PathVariable UUID productId) {
        return ResponseEntity.ok(productVariantService.list(productId));
    }

    @PostMapping
    public ResponseEntity<List<VariantResponse>> create(@PathVariable UUID productId, @Valid @RequestBody VariantCreateRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(productVariantService.create(productId, request));
    }

    @PutMapping("/{variantId}")
    public ResponseEntity<VariantResponse> update(
            @PathVariable UUID productId,
            @PathVariable UUID variantId,
            @Valid @RequestBody VariantRequest request
    ) {
        return ResponseEntity.ok(productVariantService.update(productId, variantId, request));
    }

    @DeleteMapping("/{variantId}")
    public ResponseEntity<Void> delete(@PathVariable UUID productId, @PathVariable UUID variantId) {
        productVariantService.delete(productId, variantId);
        return ResponseEntity.noContent().build();
    }
}
