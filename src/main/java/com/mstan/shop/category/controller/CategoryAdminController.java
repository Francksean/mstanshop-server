package com.mstan.shop.category.controller;

import com.mstan.shop.category.dto.CategoryPromotionRequest;
import com.mstan.shop.category.dto.CategoryRequest;
import com.mstan.shop.category.dto.CategoryResponse;
import com.mstan.shop.category.dto.ConfirmImageRequest;
import com.mstan.shop.category.service.CategoryImageService;
import com.mstan.shop.category.service.CategoryService;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

@Tag(name = "Admin - Categories", description = "CRUD des catégories réservé aux administrateurs")
@RestController
@RequestMapping("/api/admin/categories")
@RequiredArgsConstructor
public class CategoryAdminController {

    private final CategoryService categoryService;
    private final CategoryImageService categoryImageService;

    @PostMapping
    public ResponseEntity<CategoryResponse> create(@Valid @RequestBody CategoryRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(categoryService.create(request));
    }

    @PutMapping("/{id}")
    public CategoryResponse update(@PathVariable UUID id, @Valid @RequestBody CategoryRequest request) {
        return categoryService.update(id, request);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable UUID id) {
        categoryService.delete(id);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/{id}/banner/confirm")
    public CategoryResponse confirmBanner(@PathVariable UUID id, @Valid @RequestBody ConfirmImageRequest request) {
        return categoryImageService.confirmBanner(id, request.objectKey());
    }

    @PostMapping("/{id}/thumbnail/confirm")
    public CategoryResponse confirmThumbnail(@PathVariable UUID id, @Valid @RequestBody ConfirmImageRequest request) {
        return categoryImageService.confirmThumbnail(id, request.objectKey());
    }

    @PutMapping("/{id}/promotion")
    public CategoryResponse setPromotion(@PathVariable UUID id, @Valid @RequestBody CategoryPromotionRequest request) {
        return categoryService.setPromotion(id, request);
    }

    @DeleteMapping("/{id}/promotion")
    public CategoryResponse clearPromotion(@PathVariable UUID id) {
        return categoryService.clearPromotion(id);
    }
}
