package com.mstan.shop.review.controller;

import com.mstan.shop.common.security.SecurityUser;
import com.mstan.shop.common.util.PagedResponse;
import com.mstan.shop.review.dto.CreateReviewRequest;
import com.mstan.shop.review.dto.ReviewResponse;
import com.mstan.shop.review.service.ReviewService;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

@Tag(name = "Product Reviews", description = "Avis clients sur les produits")
@RestController
@RequestMapping("/api/products/{productId}/reviews")
@RequiredArgsConstructor
public class ReviewController {

    private final ReviewService reviewService;

    @GetMapping
    public PagedResponse<ReviewResponse> getReviews(@PathVariable UUID productId, Pageable pageable) {
        return reviewService.getReviews(productId, pageable);
    }

    @PostMapping
    public ResponseEntity<ReviewResponse> create(
            @AuthenticationPrincipal SecurityUser user,
            @PathVariable UUID productId,
            @Valid @RequestBody CreateReviewRequest request
    ) {
        return ResponseEntity.status(HttpStatus.CREATED).body(reviewService.createReview(user.getId(), productId, request));
    }
}
