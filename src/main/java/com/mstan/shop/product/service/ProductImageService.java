package com.mstan.shop.product.service;

import com.mstan.shop.common.exception.ResourceNotFoundException;
import com.mstan.shop.common.storage.service.R2StorageService;
import com.mstan.shop.product.dto.ConfirmProductImageRequest;
import com.mstan.shop.product.dto.ProductImageResponse;
import com.mstan.shop.product.entity.Product;
import com.mstan.shop.product.entity.ProductImage;
import com.mstan.shop.product.entity.ProductVariant;
import com.mstan.shop.product.mapper.ProductMapper;
import com.mstan.shop.product.repository.ProductRepository;
import com.mstan.shop.product.repository.ProductVariantRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class ProductImageService {

    private final ProductRepository productRepository;
    private final ProductVariantRepository productVariantRepository;
    private final ProductService productService;
    private final ProductMapper productMapper;
    private final R2StorageService r2StorageService;

    @Value("${app.upload.max-size-bytes}")
    private long maxSizeBytes;

    @Transactional
    public ProductImageResponse confirmUpload(UUID productId, ConfirmProductImageRequest request) {
        Product product = productService.findProduct(productId);
        String expectedPrefix = "products/" + productId + "/";
        if (!request.objectKey().startsWith(expectedPrefix)) {
            throw new IllegalArgumentException("La clé d'objet ne correspond pas au produit indiqué");
        }

        long size = r2StorageService.headObjectSize(request.objectKey());
        if (size > maxSizeBytes) {
            r2StorageService.deleteObject(request.objectKey());
            throw new IllegalArgumentException("Le fichier envoyé dépasse la taille maximale autorisée");
        }

        ProductVariant variant = resolveVariant(product, request.variantId());
        boolean hasExistingThumbnail = product.getImages().stream().anyMatch(ProductImage::isThumbnail);

        ProductImage image = new ProductImage();
        image.setProduct(product);
        image.setVariant(variant);
        image.setObjectKey(request.objectKey());
        image.setUrl(r2StorageService.publicUrlFor(request.objectKey()));
        image.setThumbnail(!hasExistingThumbnail && request.markAsThumbnail());
        product.getImages().add(image);

        productRepository.save(product);

        return productMapper.toImageResponse(image);
    }

    private ProductVariant resolveVariant(Product product, UUID variantId) {
        if (variantId == null) {
            return null;
        }
        return productVariantRepository.findById(variantId)
                .filter(v -> v.getProduct().getId().equals(product.getId()))
                .orElseThrow(() -> ResourceNotFoundException.of("Variante", variantId));
    }

    @Transactional
    public void deleteImage(UUID productId, UUID imageId) {
        Product product = productService.findProduct(productId);
        product.getImages().stream()
                .filter(image -> image.getId().equals(imageId))
                .findFirst()
                .ifPresent(image -> r2StorageService.deleteObject(image.getObjectKey()));
        product.getImages().removeIf(image -> image.getId().equals(imageId));
        productRepository.save(product);
    }
}
