package com.mstan.shop.product.controller;

import com.mstan.shop.product.dto.ConfirmProductImageRequest;
import com.mstan.shop.product.dto.ProductImageResponse;
import com.mstan.shop.product.service.ProductImageService;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

@Tag(name = "Admin - Product Images", description = "Confirmation des uploads d'images produits (après upload direct vers R2), réservé aux administrateurs")
@RestController
@RequestMapping("/api/admin/products/{productId}/images")
@RequiredArgsConstructor
public class ProductImageController {

    private final ProductImageService productImageService;

    @PostMapping("/confirm")
    public ResponseEntity<ProductImageResponse> confirm(
            @PathVariable UUID productId,
            @Valid @RequestBody ConfirmProductImageRequest request
    ) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(productImageService.confirmUpload(productId, request));
    }

    @DeleteMapping("/{imageId}")
    public ResponseEntity<Void> delete(@PathVariable UUID productId, @PathVariable UUID imageId) {
        productImageService.deleteImage(productId, imageId);
        return ResponseEntity.noContent().build();
    }
}
