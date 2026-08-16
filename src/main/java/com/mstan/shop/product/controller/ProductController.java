package com.mstan.shop.product.controller;

import com.mstan.shop.common.util.PagedResponse;
import com.mstan.shop.product.dto.ProductDetailResponse;
import com.mstan.shop.product.dto.ProductResponse;
import com.mstan.shop.product.dto.ProductShareResponse;
import com.mstan.shop.product.service.ProductService;
import com.mstan.shop.product.service.ProductShareService;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

@Tag(name = "Products", description = "Consultation publique du catalogue produits")
@RestController
@RequestMapping("/api/products")
@RequiredArgsConstructor
public class ProductController {

    private final ProductService productService;
    private final ProductShareService productShareService;

    @GetMapping
    public PagedResponse<ProductResponse> search(
            @RequestParam(required = false) String query,
            @RequestParam(required = false) List<UUID> categoryId,
            @RequestParam(required = false) BigDecimal minPrice,
            @RequestParam(required = false) BigDecimal maxPrice,
            Pageable pageable
    ) {
        return productService.search(query, categoryId, minPrice, maxPrice, pageable);
    }

    @GetMapping("/{id}")
    public ProductDetailResponse getById(@PathVariable UUID id) {
        return productService.getById(id);
    }

    @GetMapping("/{id}/share")
    public ProductShareResponse getShareInfo(@PathVariable UUID id) {
        return productShareService.getShareInfo(id);
    }
}
