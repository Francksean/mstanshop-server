package com.mstan.shop.common.util;

import com.mstan.shop.category.entity.Category;
import com.mstan.shop.product.entity.Product;
import com.mstan.shop.promo.entity.PromoDiscountType;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Instant;

/**
 * Resolves the real, payable price of a product given its category's promotion (if any) —
 * used consistently wherever a price is computed (product responses, cart, order) so a
 * category promo always reflects the actual amount charged, not just a display label.
 */
public final class CategoryPromotionResolver {

    private CategoryPromotionResolver() {
    }

    public static boolean isPromotionActive(Category category) {
        if (category == null || !category.isPromoActive()) {
            return false;
        }
        if (category.getPromoDiscountType() == null || category.getPromoDiscountValue() == null) {
            return false;
        }

        Instant now = Instant.now();
        if (category.getPromoStartsAt() != null && category.getPromoStartsAt().isAfter(now)) {
            return false;
        }
        return category.getPromoEndsAt() == null || !category.getPromoEndsAt().isBefore(now);
    }

    /** The price actually charged — the product's normal price if no promotion is active. */
    public static BigDecimal resolveEffectivePrice(Product product) {
        if (!isPromotionActive(product.getCategory())) {
            return product.getPrice();
        }
        Category category = product.getCategory();
        return applyDiscount(product.getPrice(), category.getPromoDiscountType(), category.getPromoDiscountValue());
    }

    /** The struck-through "old price" to display, null when no promotion is active. */
    public static BigDecimal resolveOldPrice(Product product) {
        if (!isPromotionActive(product.getCategory())) {
            return null;
        }
        return product.getPrice();
    }

    private static BigDecimal applyDiscount(BigDecimal price, PromoDiscountType type, BigDecimal value) {
        if (type == PromoDiscountType.PERCENTAGE) {
            BigDecimal discount = price.multiply(value).divide(BigDecimal.valueOf(100), 2, RoundingMode.HALF_UP);
            return price.subtract(discount).max(BigDecimal.ZERO);
        }
        return price.subtract(value).max(BigDecimal.ZERO);
    }
}
