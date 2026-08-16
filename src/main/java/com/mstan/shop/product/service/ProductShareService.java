package com.mstan.shop.product.service;

import com.mstan.shop.product.dto.ProductShareResponse;
import com.mstan.shop.product.entity.Product;
import com.mstan.shop.product.entity.ProductImage;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class ProductShareService {

    private final ProductService productService;

    @Value("${app.frontend.base-url}")
    private String frontendBaseUrl;

    @Transactional(readOnly = true)
    public ProductShareResponse getShareInfo(UUID productId) {
        Product product = productService.findProduct(productId);

        String canonicalUrl = frontendBaseUrl + "/products/" + product.getSlug();
        String thumbnailUrl = product.getImages().stream()
                .filter(ProductImage::isThumbnail)
                .findFirst()
                .or(() -> product.getImages().stream().findFirst())
                .map(ProductImage::getUrl)
                .map(url -> url.startsWith("http") ? url : frontendBaseUrl + url)
                .orElse(null);

        return new ProductShareResponse(canonicalUrl, product.getName(), product.getDescription(), thumbnailUrl);
    }
}
