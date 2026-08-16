package com.mstan.shop.category.service;

import com.mstan.shop.category.dto.CategoryPromotionRequest;
import com.mstan.shop.category.dto.CategoryRequest;
import com.mstan.shop.category.dto.CategoryResponse;
import com.mstan.shop.category.entity.Category;
import com.mstan.shop.category.mapper.CategoryMapper;
import com.mstan.shop.category.repository.CategoryRepository;
import com.mstan.shop.common.exception.DuplicateResourceException;
import com.mstan.shop.common.exception.ResourceNotFoundException;
import com.mstan.shop.common.storage.service.R2StorageService;
import com.mstan.shop.common.util.SlugUtil;
import com.mstan.shop.product.repository.CategoryProductCountProjection;
import com.mstan.shop.product.repository.ProductRepository;
import com.mstan.shop.promo.entity.PromoDiscountType;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class CategoryService {

    private final CategoryRepository categoryRepository;
    private final ProductRepository productRepository;
    private final CategoryMapper categoryMapper;
    private final R2StorageService r2StorageService;

    @Value("${app.upload.max-size-bytes}")
    private long maxSizeBytes;

    @Transactional(readOnly = true)
    public List<CategoryResponse> getAll() {
        Map<UUID, Long> countsByCategoryId = productRepository.countActiveProductsGroupedByCategory().stream()
                .collect(Collectors.toMap(CategoryProductCountProjection::getCategoryId, CategoryProductCountProjection::getCnt));

        return categoryRepository.findAll().stream()
                .map(category -> categoryMapper.toResponse(category, countsByCategoryId.getOrDefault(category.getId(), 0L)))
                .toList();
    }

    @Transactional(readOnly = true)
    public CategoryResponse getById(UUID id) {
        Category category = findCategory(id);
        return categoryMapper.toResponse(category, productRepository.countByCategoryIdAndActiveTrue(id));
    }

    @Transactional
    public CategoryResponse create(CategoryRequest request) {
        if (categoryRepository.existsByNameIgnoreCase(request.name())) {
            throw new DuplicateResourceException("Une catégorie avec ce nom existe déjà");
        }

        Category category = new Category();
        category.setName(request.name());
        category.setDescription(request.description());
        category.setSlug(SlugUtil.slugify(request.name()));
        category.setParent(resolveParent(request.parentId(), null));

        attachImagesAtCreation(category, request.bannerObjectKey(), request.thumbnailObjectKey());

        categoryRepository.save(category);

        return categoryMapper.toResponse(category, 0);
    }

    private Category resolveParent(UUID parentId, UUID currentCategoryId) {
        if (parentId == null) {
            return null;
        }
        if (parentId.equals(currentCategoryId)) {
            throw new IllegalArgumentException("Une catégorie ne peut pas être sa propre catégorie parente");
        }
        Category parent = findCategory(parentId);
        if (parent.getParent() != null) {
            throw new IllegalArgumentException("Une sous-catégorie ne peut pas être assignée à une autre sous-catégorie (un seul niveau autorisé)");
        }
        return parent;
    }

    private void attachImagesAtCreation(Category category, String bannerObjectKey, String thumbnailObjectKey) {
        if (bannerObjectKey == null && thumbnailObjectKey == null) {
            return;
        }

        try {
            if (bannerObjectKey != null) {
                if (!bannerObjectKey.startsWith("categories/") || !bannerObjectKey.contains("/banner_")) {
                    throw new IllegalArgumentException("La clé d'objet ne correspond pas à une image de bannière");
                }
                r2StorageService.enforceMaxSize(bannerObjectKey, maxSizeBytes);
                category.setBannerObjectKey(bannerObjectKey);
                category.setBannerUrl(r2StorageService.publicUrlFor(bannerObjectKey));
            }

            if (thumbnailObjectKey != null) {
                if (!thumbnailObjectKey.startsWith("categories/") || !thumbnailObjectKey.contains("/thumbnail_")) {
                    throw new IllegalArgumentException("La clé d'objet ne correspond pas à une image de miniature");
                }
                r2StorageService.enforceMaxSize(thumbnailObjectKey, maxSizeBytes);
                category.setThumbnailObjectKey(thumbnailObjectKey);
                category.setThumbnailUrl(r2StorageService.publicUrlFor(thumbnailObjectKey));
            }
        } catch (RuntimeException e) {
            if (bannerObjectKey != null) {
                r2StorageService.deleteObject(bannerObjectKey);
            }
            if (thumbnailObjectKey != null) {
                r2StorageService.deleteObject(thumbnailObjectKey);
            }
            throw e;
        }
    }

    @Transactional
    public CategoryResponse update(UUID id, CategoryRequest request) {
        Category category = findCategory(id);
        if (request.parentId() != null && categoryRepository.existsByParentId(id)) {
            throw new IllegalArgumentException("Impossible d'assigner un parent à une catégorie qui a elle-même des sous-catégories");
        }
        category.setName(request.name());
        category.setDescription(request.description());
        category.setSlug(SlugUtil.slugify(request.name()));
        category.setParent(resolveParent(request.parentId(), id));
        categoryRepository.save(category);

        return categoryMapper.toResponse(category, productRepository.countByCategoryIdAndActiveTrue(id));
    }

    @Transactional
    public void delete(UUID id) {
        Category category = findCategory(id);
        if (categoryRepository.existsByParentId(id)) {
            throw new IllegalArgumentException("Impossible de supprimer une catégorie qui a des sous-catégories — réassignez-les d'abord");
        }
        categoryRepository.delete(category);
    }

    @Transactional
    public CategoryResponse setPromotion(UUID id, CategoryPromotionRequest request) {
        Category category = findCategory(id);

        if (request.discountType() == PromoDiscountType.PERCENTAGE
                && request.discountValue().compareTo(BigDecimal.valueOf(100)) > 0) {
            throw new IllegalArgumentException("Une remise en pourcentage ne peut pas dépasser 100");
        }

        category.setPromoDiscountType(request.discountType());
        category.setPromoDiscountValue(request.discountValue());
        category.setPromoStartsAt(request.startsAt());
        category.setPromoEndsAt(request.endsAt());
        category.setPromoActive(request.active());
        categoryRepository.save(category);

        return categoryMapper.toResponse(category, productRepository.countByCategoryIdAndActiveTrue(id));
    }

    @Transactional
    public CategoryResponse clearPromotion(UUID id) {
        Category category = findCategory(id);

        category.setPromoDiscountType(null);
        category.setPromoDiscountValue(null);
        category.setPromoStartsAt(null);
        category.setPromoEndsAt(null);
        category.setPromoActive(false);
        categoryRepository.save(category);

        return categoryMapper.toResponse(category, productRepository.countByCategoryIdAndActiveTrue(id));
    }

    Category findCategory(UUID id) {
        return categoryRepository.findById(id)
                .orElseThrow(() -> ResourceNotFoundException.of("Catégorie", id));
    }
}
