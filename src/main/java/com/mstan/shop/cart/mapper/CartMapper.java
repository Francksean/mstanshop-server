package com.mstan.shop.cart.mapper;

import com.mstan.shop.cart.dto.CartItemResponse;
import com.mstan.shop.cart.entity.CartItem;
import com.mstan.shop.product.entity.ProductImage;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.math.BigDecimal;

@Mapper(componentModel = "spring")
public interface CartMapper {

    @Mapping(target = "productId", source = "product.id")
    @Mapping(target = "productName", source = "product.name")
    @Mapping(target = "productThumbnailUrl", expression = "java(resolveThumbnailUrl(item))")
    @Mapping(target = "variantId", expression = "java(item.getVariant() != null ? item.getVariant().getId() : null)")
    @Mapping(target = "variantColorName", expression = "java(item.getVariant() != null ? item.getVariant().getColorName() : null)")
    @Mapping(target = "variantColorHex", expression = "java(item.getVariant() != null ? item.getVariant().getColorHex() : null)")
    @Mapping(target = "variantSize", expression = "java(item.getVariant() != null ? item.getVariant().getSize() : null)")
    @Mapping(target = "unitPrice", expression = "java(com.mstan.shop.common.util.CategoryPromotionResolver.resolveEffectivePrice(item.getProduct()))")
    @Mapping(target = "lineTotal", expression = "java(computeLineTotal(item))")
    CartItemResponse toItemResponse(CartItem item);

    default String resolveThumbnailUrl(CartItem item) {
        return item.getProduct().getImages().stream()
                .filter(ProductImage::isThumbnail)
                .findFirst()
                .or(() -> item.getProduct().getImages().stream().findFirst())
                .map(ProductImage::getUrl)
                .orElse(null);
    }

    default BigDecimal computeLineTotal(CartItem item) {
        return com.mstan.shop.common.util.CategoryPromotionResolver.resolveEffectivePrice(item.getProduct())
                .multiply(BigDecimal.valueOf(item.getQuantity()));
    }
}
