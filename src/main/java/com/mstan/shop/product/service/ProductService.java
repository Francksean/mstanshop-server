package com.mstan.shop.product.service;

import com.mstan.shop.category.entity.Category;
import com.mstan.shop.category.repository.CategoryRepository;
import com.mstan.shop.common.exception.DuplicateResourceException;
import com.mstan.shop.common.exception.ResourceNotFoundException;
import com.mstan.shop.common.util.PagedResponse;
import com.mstan.shop.common.util.SlugUtil;
import com.mstan.shop.product.dto.LowStockItemResponse;
import com.mstan.shop.product.dto.ProductCreateRequest;
import com.mstan.shop.product.dto.ProductDetailResponse;
import com.mstan.shop.product.dto.ProductResponse;
import com.mstan.shop.product.dto.ProductUpdateRequest;
import com.mstan.shop.product.entity.Product;
import com.mstan.shop.product.mapper.ProductMapper;
import com.mstan.shop.product.repository.ProductRepository;
import com.mstan.shop.product.repository.ProductSpecifications;
import com.mstan.shop.product.repository.ProductVariantRepository;
import com.mstan.shop.review.repository.RatingSummaryProjection;
import com.mstan.shop.review.repository.ReviewRepository;
import com.mstan.shop.supplier.entity.Supplier;
import com.mstan.shop.supplier.repository.SupplierRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.function.Function;

@Service
@RequiredArgsConstructor
public class ProductService {

    private final ProductRepository productRepository;
    private final ProductVariantRepository productVariantRepository;
    private final CategoryRepository categoryRepository;
    private final SupplierRepository supplierRepository;
    private final ReviewRepository reviewRepository;
    private final ProductMapper productMapper;

    @Transactional(readOnly = true)
    public PagedResponse<ProductResponse> search(String query, List<UUID> categoryIds, BigDecimal minPrice, BigDecimal maxPrice, Pageable pageable) {
        Specification<Product> spec = Specification.allOf(
                ProductSpecifications.isActive(),
                ProductSpecifications.nameOrDescriptionContains(query),
                ProductSpecifications.hasCategory(categoryIds),
                ProductSpecifications.priceGreaterThanOrEqual(minPrice),
                ProductSpecifications.priceLessThanOrEqual(maxPrice)
        );

        Page<Product> page = productRepository.findAll(spec, pageable);

        List<UUID> productIds = page.getContent().stream().map(Product::getId).toList();
        Map<UUID, RatingSummaryProjection> ratingsByProductId = productIds.isEmpty()
                ? Map.of()
                : reviewRepository.findRatingSummaryByProductIds(productIds).stream()
                        .collect(java.util.stream.Collectors.toMap(RatingSummaryProjection::getProductId, Function.identity()));

        return PagedResponse.from(page.map(product -> {
            RatingSummaryProjection summary = ratingsByProductId.get(product.getId());
            Double averageRating = summary != null ? summary.getAvgRating() : null;
            long reviewCount = summary != null ? summary.getCnt() : 0;
            return productMapper.toResponse(product, averageRating, reviewCount);
        }));
    }

    @Transactional(readOnly = true)
    public ProductDetailResponse getById(UUID id) {
        Product product = findProduct(id);
        Double averageRating = reviewRepository.findAverageRatingByProductId(id);
        long reviewCount = reviewRepository.countByProductId(id);
        return productMapper.toDetailResponse(product, averageRating, reviewCount);
    }

    @Transactional
    public ProductDetailResponse create(ProductCreateRequest request) {
        if (productRepository.existsByNameIgnoreCase(request.name())) {
            throw new DuplicateResourceException("Un produit avec ce nom existe déjà");
        }

        Category category = findCategory(request.categoryId());
        Supplier supplier = findSupplier(request.supplierId());

        Product product = new Product();
        product.setName(request.name());
        product.setDescription(request.description());
        product.setPrice(request.price());
        product.setStock(request.stock());
        product.setCategory(category);
        product.setSupplier(supplier);
        product.setSlug(SlugUtil.slugify(request.name()));
        product.setActive(true);
        productRepository.save(product);

        return productMapper.toDetailResponse(product, null, 0);
    }

    @Transactional
    public ProductDetailResponse update(UUID id, ProductUpdateRequest request) {
        Product product = findProduct(id);
        Category category = findCategory(request.categoryId());
        Supplier supplier = findSupplier(request.supplierId());

        product.setName(request.name());
        product.setDescription(request.description());
        product.setPrice(request.price());
        product.setStock(request.stock());
        product.setCategory(category);
        product.setSupplier(supplier);
        product.setSlug(SlugUtil.slugify(request.name()));
        product.setActive(request.active());
        productRepository.save(product);

        Double averageRating = reviewRepository.findAverageRatingByProductId(id);
        long reviewCount = reviewRepository.countByProductId(id);
        return productMapper.toDetailResponse(product, averageRating, reviewCount);
    }

    @Transactional
    public void delete(UUID id) {
        Product product = findProduct(id);
        productRepository.delete(product);
    }

    @Transactional(readOnly = true)
    public List<LowStockItemResponse> getLowStockItems(int threshold) {
        Specification<Product> spec = Specification.allOf(
                ProductSpecifications.hasNoVariants(),
                ProductSpecifications.stockBelow(threshold)
        );

        List<LowStockItemResponse> result = new java.util.ArrayList<>();

        productRepository.findAll(spec).forEach(product ->
                result.add(new LowStockItemResponse(product.getId(), product.getName(), null, null, null, product.getStock(), threshold)));

        productVariantRepository.findByStockLessThan(threshold).forEach(variant ->
                result.add(new LowStockItemResponse(variant.getProduct().getId(), variant.getProduct().getName(),
                        variant.getId(), variant.getColorName(), variant.getSize(), variant.getStock(), threshold)));

        result.sort(java.util.Comparator.comparing(LowStockItemResponse::productName));
        return result;
    }

    Product findProduct(UUID id) {
        return productRepository.findById(id)
                .orElseThrow(() -> ResourceNotFoundException.of("Produit", id));
    }

    private Category findCategory(UUID categoryId) {
        return categoryRepository.findById(categoryId)
                .orElseThrow(() -> ResourceNotFoundException.of("Catégorie", categoryId));
    }

    private Supplier findSupplier(UUID supplierId) {
        if (supplierId == null) {
            return null;
        }
        return supplierRepository.findById(supplierId)
                .orElseThrow(() -> ResourceNotFoundException.of("Fournisseur", supplierId));
    }
}
