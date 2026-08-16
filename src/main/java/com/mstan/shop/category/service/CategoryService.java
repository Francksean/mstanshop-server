package com.mstan.shop.category.service;

import com.mstan.shop.category.dto.CategoryPromotionRequest;
import com.mstan.shop.category.dto.CategoryRequest;
import com.mstan.shop.category.dto.CategoryResponse;
import com.mstan.shop.category.entity.Category;
import com.mstan.shop.category.mapper.CategoryMapper;
import com.mstan.shop.category.repository.CategoryRepository;
import com.mstan.shop.common.exception.DuplicateResourceException;
import com.mstan.shop.common.exception.ResourceNotFoundException;
import com.mstan.shop.common.util.SlugUtil;
import com.mstan.shop.product.repository.CategoryProductCountProjection;
import com.mstan.shop.product.repository.ProductRepository;
import com.mstan.shop.promo.entity.PromoDiscountType;
import lombok.RequiredArgsConstructor;
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
        categoryRepository.save(category);

        return categoryMapper.toResponse(category, 0);
    }

    @Transactional
    public CategoryResponse update(UUID id, CategoryRequest request) {
        Category category = findCategory(id);
        category.setName(request.name());
        category.setDescription(request.description());
        category.setSlug(SlugUtil.slugify(request.name()));
        categoryRepository.save(category);

        return categoryMapper.toResponse(category, productRepository.countByCategoryIdAndActiveTrue(id));
    }

    @Transactional
    public void delete(UUID id) {
        Category category = findCategory(id);
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
