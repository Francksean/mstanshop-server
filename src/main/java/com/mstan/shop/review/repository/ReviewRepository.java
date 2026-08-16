package com.mstan.shop.review.repository;

import com.mstan.shop.review.entity.Review;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.UUID;

public interface ReviewRepository extends JpaRepository<Review, UUID> {

    boolean existsByProductIdAndUserId(UUID productId, UUID userId);

    Page<Review> findByProductId(UUID productId, Pageable pageable);

    @Query("select avg(r.rating) from Review r where r.product.id = :productId")
    Double findAverageRatingByProductId(@Param("productId") UUID productId);

    long countByProductId(UUID productId);

    @Query("select r.product.id as productId, avg(r.rating) as avgRating, count(r) as cnt " +
            "from Review r where r.product.id in :productIds group by r.product.id")
    List<RatingSummaryProjection> findRatingSummaryByProductIds(@Param("productIds") List<UUID> productIds);
}
