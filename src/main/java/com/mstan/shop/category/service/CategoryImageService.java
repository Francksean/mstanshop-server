package com.mstan.shop.category.service;

import com.mstan.shop.category.dto.CategoryResponse;
import com.mstan.shop.category.entity.Category;
import com.mstan.shop.category.mapper.CategoryMapper;
import com.mstan.shop.category.repository.CategoryRepository;
import com.mstan.shop.common.storage.service.R2StorageService;
import com.mstan.shop.product.repository.ProductRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class CategoryImageService {

    private final CategoryRepository categoryRepository;
    private final ProductRepository productRepository;
    private final CategoryService categoryService;
    private final CategoryMapper categoryMapper;
    private final R2StorageService r2StorageService;

    @Value("${app.upload.max-size-bytes}")
    private long maxSizeBytes;

    @Transactional
    public CategoryResponse confirmBanner(UUID categoryId, String objectKey) {
        Category category = categoryService.findCategory(categoryId);
        validateObjectKey(categoryId, objectKey, "banner_");
        validateSize(objectKey);

        r2StorageService.deleteObject(category.getBannerObjectKey());
        category.setBannerObjectKey(objectKey);
        category.setBannerUrl(r2StorageService.publicUrlFor(objectKey));
        categoryRepository.save(category);

        return toResponse(category);
    }

    @Transactional
    public CategoryResponse confirmThumbnail(UUID categoryId, String objectKey) {
        Category category = categoryService.findCategory(categoryId);
        validateObjectKey(categoryId, objectKey, "thumbnail_");
        validateSize(objectKey);

        r2StorageService.deleteObject(category.getThumbnailObjectKey());
        category.setThumbnailObjectKey(objectKey);
        category.setThumbnailUrl(r2StorageService.publicUrlFor(objectKey));
        categoryRepository.save(category);

        return toResponse(category);
    }

    private void validateObjectKey(UUID categoryId, String objectKey, String expectedFilePrefix) {
        String expectedPrefix = "categories/" + categoryId + "/" + expectedFilePrefix;
        if (!objectKey.startsWith(expectedPrefix)) {
            throw new IllegalArgumentException("La clé d'objet ne correspond pas à la catégorie/au type d'image indiqué");
        }
    }

    private void validateSize(String objectKey) {
        r2StorageService.enforceMaxSize(objectKey, maxSizeBytes);
    }

    private CategoryResponse toResponse(Category category) {
        return categoryMapper.toResponse(category, productRepository.countByCategoryIdAndActiveTrue(category.getId()));
    }
}
