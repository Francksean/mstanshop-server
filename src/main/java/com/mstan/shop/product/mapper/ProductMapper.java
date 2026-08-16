package com.mstan.shop.product.mapper;

import com.mstan.shop.common.util.CategoryPromotionResolver;
import com.mstan.shop.product.dto.ProductDetailResponse;
import com.mstan.shop.product.dto.ProductImageResponse;
import com.mstan.shop.product.dto.ProductResponse;
import com.mstan.shop.product.dto.VariantResponse;
import com.mstan.shop.product.entity.Product;
import com.mstan.shop.product.entity.ProductImage;
import com.mstan.shop.product.entity.ProductVariant;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.util.List;

@Mapper(componentModel = "spring")
public interface ProductMapper {

    @Mapping(target = "categoryName", source = "product.category.name")
    @Mapping(target = "thumbnailUrl", expression = "java(resolveThumbnailUrl(product))")
    @Mapping(target = "stock", expression = "java(resolveStock(product))")
    @Mapping(target = "price", expression = "java(resolveEffectivePrice(product))")
    @Mapping(target = "oldPrice", expression = "java(resolveOldPrice(product))")
    @Mapping(target = "purchasePrice", expression = "java(includePurchasePrice ? product.getPurchasePrice() : null)")
    @Mapping(target = "averageRating", source = "averageRating")
    @Mapping(target = "reviewCount", source = "reviewCount")
    ProductResponse toResponse(Product product, Double averageRating, long reviewCount, boolean includePurchasePrice);

    @Mapping(target = "categoryId", source = "product.category.id")
    @Mapping(target = "categoryName", source = "product.category.name")
    @Mapping(target = "supplierId", expression = "java(product.getSupplier() != null ? product.getSupplier().getId() : null)")
    @Mapping(target = "supplierName", expression = "java(product.getSupplier() != null ? product.getSupplier().getName() : null)")
    @Mapping(target = "stock", expression = "java(resolveStock(product))")
    @Mapping(target = "price", expression = "java(resolveEffectivePrice(product))")
    @Mapping(target = "oldPrice", expression = "java(resolveOldPrice(product))")
    @Mapping(target = "purchasePrice", expression = "java(includePurchasePrice ? product.getPurchasePrice() : null)")
    @Mapping(target = "averageRating", source = "averageRating")
    @Mapping(target = "reviewCount", source = "reviewCount")
    ProductDetailResponse toDetailResponse(Product product, Double averageRating, long reviewCount, boolean includePurchasePrice);

    @Mapping(target = "thumbnail", source = "thumbnail")
    @Mapping(target = "variantId", expression = "java(image.getVariant() != null ? image.getVariant().getId() : null)")
    ProductImageResponse toImageResponse(ProductImage image);

    @Mapping(target = "images", expression = "java(resolveVariantImages(variant))")
    VariantResponse toVariantResponse(ProductVariant variant);

    default String resolveThumbnailUrl(Product product) {
        return product.getImages().stream()
                .filter(ProductImage::isThumbnail)
                .findFirst()
                .or(() -> product.getImages().stream().findFirst())
                .map(ProductImage::getUrl)
                .orElse(null);
    }

    default int resolveStock(Product product) {
        if (product.getVariants().isEmpty()) {
            return product.getStock();
        }
        return product.getVariants().stream().mapToInt(ProductVariant::getStock).sum();
    }

    default java.math.BigDecimal resolveEffectivePrice(Product product) {
        return CategoryPromotionResolver.resolveEffectivePrice(product);
    }

    default java.math.BigDecimal resolveOldPrice(Product product) {
        return CategoryPromotionResolver.resolveOldPrice(product);
    }

    default List<ProductImageResponse> resolveVariantImages(ProductVariant variant) {
        return variant.getProduct().getImages().stream()
                .filter(image -> image.getVariant() != null && image.getVariant().getId().equals(variant.getId()))
                .map(this::toImageResponse)
                .toList();
    }
}
