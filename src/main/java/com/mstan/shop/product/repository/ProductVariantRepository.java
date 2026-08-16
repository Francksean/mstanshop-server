package com.mstan.shop.product.repository;

import com.mstan.shop.product.entity.ProductVariant;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface ProductVariantRepository extends JpaRepository<ProductVariant, UUID> {

    boolean existsByProductIdAndColorHexIgnoreCaseAndSizeIgnoreCase(UUID productId, String colorHex, String size);

    List<ProductVariant> findByProductId(UUID productId);

    List<ProductVariant> findByStockLessThan(int threshold);
}
