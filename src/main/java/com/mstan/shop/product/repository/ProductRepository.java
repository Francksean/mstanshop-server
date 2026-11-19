package com.mstan.shop.product.repository;

import com.mstan.shop.product.entity.Product;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface ProductRepository extends JpaRepository<Product, UUID>, JpaSpecificationExecutor<Product> {

    boolean existsBySlug(String slug);

    Optional<Product> findBySlug(String slug);

    long countByCategoryIdAndActiveTrue(UUID categoryId);

    @Query("select p.category.id as categoryId, count(p) as cnt from Product p where p.active = true group by p.category.id")
    List<CategoryProductCountProjection> countActiveProductsGroupedByCategory();
}
