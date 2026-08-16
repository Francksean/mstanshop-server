package com.mstan.shop.product.service;

import com.mstan.shop.common.exception.DuplicateResourceException;
import com.mstan.shop.common.exception.ResourceNotFoundException;
import com.mstan.shop.product.dto.VariantRequest;
import com.mstan.shop.product.dto.VariantResponse;
import com.mstan.shop.product.entity.Product;
import com.mstan.shop.product.entity.ProductVariant;
import com.mstan.shop.product.mapper.ProductMapper;
import com.mstan.shop.product.repository.ProductRepository;
import com.mstan.shop.product.repository.ProductVariantRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class ProductVariantService {

    private final ProductRepository productRepository;
    private final ProductVariantRepository productVariantRepository;
    private final ProductService productService;
    private final ProductMapper productMapper;

    @Transactional(readOnly = true)
    public List<VariantResponse> list(UUID productId) {
        Product product = productService.findProduct(productId);
        return product.getVariants().stream().map(productMapper::toVariantResponse).toList();
    }

    @Transactional
    public VariantResponse create(UUID productId, VariantRequest request) {
        Product product = productService.findProduct(productId);
        String size = normalizeSize(request.size());

        if (productVariantRepository.existsByProductIdAndColorHexIgnoreCaseAndSizeIgnoreCase(productId, request.colorHex(), size)) {
            throw new DuplicateResourceException("Une variante avec cette couleur et cette taille existe déjà pour ce produit");
        }

        ProductVariant variant = new ProductVariant();
        variant.setProduct(product);
        variant.setColorName(request.colorName());
        variant.setColorHex(request.colorHex());
        variant.setSize(size);
        variant.setStock(request.stock());
        variant.setActive(request.active());
        product.getVariants().add(variant);

        productRepository.save(product);

        return productMapper.toVariantResponse(variant);
    }

    @Transactional
    public VariantResponse update(UUID productId, UUID variantId, VariantRequest request) {
        Product product = productService.findProduct(productId);
        ProductVariant variant = findVariant(product, variantId);
        String size = normalizeSize(request.size());

        boolean colorOrSizeChanged = !variant.getColorHex().equalsIgnoreCase(request.colorHex())
                || !variant.getSize().equalsIgnoreCase(size);
        if (colorOrSizeChanged
                && productVariantRepository.existsByProductIdAndColorHexIgnoreCaseAndSizeIgnoreCase(productId, request.colorHex(), size)) {
            throw new DuplicateResourceException("Une variante avec cette couleur et cette taille existe déjà pour ce produit");
        }

        variant.setColorName(request.colorName());
        variant.setColorHex(request.colorHex());
        variant.setSize(size);
        variant.setStock(request.stock());
        variant.setActive(request.active());

        productRepository.save(product);

        return productMapper.toVariantResponse(variant);
    }

    private String normalizeSize(String size) {
        return size == null ? "" : size.trim();
    }

    @Transactional
    public void delete(UUID productId, UUID variantId) {
        Product product = productService.findProduct(productId);
        ProductVariant variant = findVariant(product, variantId);
        product.getVariants().remove(variant);
        productRepository.save(product);
    }

    private ProductVariant findVariant(Product product, UUID variantId) {
        return product.getVariants().stream()
                .filter(variant -> variant.getId().equals(variantId))
                .findFirst()
                .orElseThrow(() -> ResourceNotFoundException.of("Variante", variantId));
    }
}
