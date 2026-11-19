package com.mstan.shop.product.service;

import com.mstan.shop.category.entity.Category;
import com.mstan.shop.category.repository.CategoryRepository;
import com.mstan.shop.common.exception.DuplicateResourceException;
import com.mstan.shop.common.exception.ResourceNotFoundException;
import com.mstan.shop.common.storage.service.R2StorageService;
import com.mstan.shop.common.util.PagedResponse;
import com.mstan.shop.common.util.SlugUtil;
import com.mstan.shop.product.dto.LowStockItemResponse;
import com.mstan.shop.product.dto.ProductCreateRequest;
import com.mstan.shop.product.dto.ProductDetailResponse;
import com.mstan.shop.product.dto.ProductImageCreateItem;
import com.mstan.shop.product.dto.ProductResponse;
import com.mstan.shop.product.dto.ProductUpdateRequest;
import com.mstan.shop.product.dto.VariantCreateRequest;
import com.mstan.shop.product.entity.Product;
import com.mstan.shop.product.entity.ProductImage;
import com.mstan.shop.product.entity.ProductVariant;
import com.mstan.shop.product.mapper.ProductMapper;
import com.mstan.shop.product.repository.ProductRepository;
import com.mstan.shop.product.repository.ProductSpecifications;
import com.mstan.shop.product.repository.ProductVariantRepository;
import com.mstan.shop.review.repository.RatingSummaryProjection;
import com.mstan.shop.review.repository.ReviewRepository;
import com.mstan.shop.supplier.entity.Supplier;
import com.mstan.shop.supplier.repository.SupplierRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
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
    private final R2StorageService r2StorageService;
    private final ProductMapper productMapper;

    @Value("${app.upload.max-size-bytes}")
    private long maxSizeBytes;

    @Transactional(readOnly = true)
    public PagedResponse<ProductResponse> search(String query, List<UUID> categoryIds, BigDecimal minPrice, BigDecimal maxPrice, Pageable pageable, boolean includePurchasePrice) {
        List<UUID> effectiveCategoryIds = categoryIds;
        if (categoryIds != null && !categoryIds.isEmpty()) {
            List<UUID> childIds = categoryRepository.findIdsByParentIn(categoryIds);
            if (!childIds.isEmpty()) {
                effectiveCategoryIds = java.util.stream.Stream.concat(categoryIds.stream(), childIds.stream()).distinct().toList();
            }
        }

        Specification<Product> spec = Specification.allOf(
                ProductSpecifications.isActive(),
                ProductSpecifications.nameOrDescriptionContains(query),
                ProductSpecifications.hasCategory(effectiveCategoryIds),
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
            return productMapper.toResponse(product, averageRating, reviewCount, includePurchasePrice);
        }));
    }

    @Transactional(readOnly = true)
    public ProductDetailResponse getById(UUID id, boolean includePurchasePrice) {
        Product product = findProduct(id);
        Double averageRating = reviewRepository.findAverageRatingByProductId(id);
        long reviewCount = reviewRepository.countByProductId(id);
        return productMapper.toDetailResponse(product, averageRating, reviewCount, includePurchasePrice);
    }

    @Transactional(readOnly = true)
    public ProductDetailResponse getBySlug(String slug, boolean includePurchasePrice) {
        Product product = productRepository.findBySlug(slug)
                .orElseThrow(() -> ResourceNotFoundException.of("Produit", slug));
        Double averageRating = reviewRepository.findAverageRatingByProductId(product.getId());
        long reviewCount = reviewRepository.countByProductId(product.getId());
        return productMapper.toDetailResponse(product, averageRating, reviewCount, includePurchasePrice);
    }

    @Transactional
    public ProductDetailResponse create(ProductCreateRequest request) {
        Category category = findCategory(request.categoryId());
        Supplier supplier = findSupplier(request.supplierId());

        Product product = new Product();
        product.setName(request.name());
        product.setDescription(request.description());
        product.setPrice(request.price());
        product.setPurchasePrice(request.purchasePrice());
        product.setStock(request.stock());
        product.setCategory(category);
        product.setSupplier(supplier);
        product.setSlug(generateUniqueSlug(request.name()));
        product.setActive(true);
        productRepository.save(product);

        attachImagesAtCreation(product, request.images());
        attachVariantsAtCreation(product, request.variants());

        return productMapper.toDetailResponse(product, null, 0, true);
    }

    private void attachVariantsAtCreation(Product product, List<VariantCreateRequest> variants) {
        if (variants == null || variants.isEmpty()) {
            return;
        }

        java.util.Set<String> seen = new java.util.HashSet<>();
        for (VariantCreateRequest variant : variants) {
            for (String size : variant.sizes()) {
                String normalizedSize = size == null ? "" : size.trim();
                String colorKey = variant.colorHex() == null ? "" : variant.colorHex().toLowerCase();
                String key = colorKey + "|" + normalizedSize.toLowerCase();
                if (!seen.add(key)) {
                    throw new IllegalArgumentException("Deux variantes de la liste ont la même couleur et la même taille");
                }
                buildVariant(product, variant.colorName(), variant.colorHex(), size, variant.stock(), variant.active());
            }
        }
        productRepository.save(product);
    }

    /**
     * Validates and attaches a new variant (one color + one size) to the given (already-managed)
     * product, without saving. Shared with the standalone variant-creation endpoint (ProductVariantService),
     * which calls this once per size when a request carries multiple sizes for the same color.
     */
    ProductVariant buildVariant(Product product, String colorName, String colorHex, String size, int stock, boolean active) {
        String normalizedSize = size == null ? "" : size.trim();
        String normalizedColorHex = (colorHex == null || colorHex.isBlank()) ? null : colorHex;

        boolean duplicate = normalizedColorHex == null
                ? productVariantRepository.existsByProductIdAndColorHexIsNullAndSizeIgnoreCase(product.getId(), normalizedSize)
                : productVariantRepository.existsByProductIdAndColorHexIgnoreCaseAndSizeIgnoreCase(product.getId(), normalizedColorHex, normalizedSize);
        if (duplicate) {
            throw new DuplicateResourceException("Une variante avec cette couleur et cette taille existe déjà pour ce produit");
        }

        ProductVariant variant = new ProductVariant();
        variant.setProduct(product);
        variant.setColorName(colorName);
        variant.setColorHex(normalizedColorHex);
        variant.setSize(normalizedSize);
        variant.setStock(stock);
        variant.setActive(active);
        product.getVariants().add(variant);

        return variant;
    }

    private void attachImagesAtCreation(Product product, List<ProductImageCreateItem> items) {
        if (items == null || items.isEmpty()) {
            return;
        }

        try {
            boolean thumbnailAssigned = false;
            for (ProductImageCreateItem item : items) {
                if (!item.objectKey().startsWith("products/")) {
                    throw new IllegalArgumentException("La clé d'objet ne correspond pas à une image produit");
                }
                r2StorageService.enforceMaxSize(item.objectKey(), maxSizeBytes);

                ProductImage image = new ProductImage();
                image.setProduct(product);
                image.setObjectKey(item.objectKey());
                image.setUrl(r2StorageService.publicUrlFor(item.objectKey()));
                image.setThumbnail(!thumbnailAssigned && item.markAsThumbnail());
                thumbnailAssigned = thumbnailAssigned || image.isThumbnail();
                product.getImages().add(image);
            }
            productRepository.save(product);
        } catch (RuntimeException e) {
            items.forEach(item -> r2StorageService.deleteObject(item.objectKey()));
            throw e;
        }
    }

    @Transactional
    public ProductDetailResponse update(UUID id, ProductUpdateRequest request) {
        Product product = findProduct(id);
        Category category = findCategory(request.categoryId());
        Supplier supplier = findSupplier(request.supplierId());

        product.setName(request.name());
        product.setDescription(request.description());
        product.setPrice(request.price());
        product.setPurchasePrice(request.purchasePrice());
        product.setStock(request.stock());
        product.setCategory(category);
        product.setSupplier(supplier);
        product.setSlug(generateUniqueSlugExcluding(request.name(), id));
        product.setActive(request.active());
        productRepository.save(product);

        Double averageRating = reviewRepository.findAverageRatingByProductId(id);
        long reviewCount = reviewRepository.countByProductId(id);
        return productMapper.toDetailResponse(product, averageRating, reviewCount, true);
    }

    @Transactional
    public void delete(UUID id) {
        Product product = findProduct(id);
        product.getImages().forEach(image -> r2StorageService.deleteObject(image.getObjectKey()));
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

    private String generateUniqueSlug(String name) {
        String baseSlug = SlugUtil.slugify(name);
        String candidateSlug = baseSlug;
        int counter = 2;

        while (productRepository.existsBySlug(candidateSlug)) {
            candidateSlug = baseSlug + "-" + counter;
            counter++;
        }

        return candidateSlug;
    }

    private String generateUniqueSlugExcluding(String name, UUID excludeProductId) {
        String baseSlug = SlugUtil.slugify(name);
        Product currentProduct = findProduct(excludeProductId);

        if (currentProduct.getSlug().equals(baseSlug)) {
            return baseSlug;
        }

        String candidateSlug = baseSlug;
        int counter = 2;

        while (productRepository.existsBySlug(candidateSlug)) {
            candidateSlug = baseSlug + "-" + counter;
            counter++;
        }

        return candidateSlug;
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
