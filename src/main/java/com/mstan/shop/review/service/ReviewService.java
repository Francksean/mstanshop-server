package com.mstan.shop.review.service;

import com.mstan.shop.common.exception.DuplicateResourceException;
import com.mstan.shop.common.exception.ResourceNotFoundException;
import com.mstan.shop.common.util.PagedResponse;
import com.mstan.shop.order.entity.OrderStatus;
import com.mstan.shop.order.repository.OrderItemRepository;
import com.mstan.shop.product.entity.Product;
import com.mstan.shop.product.repository.ProductRepository;
import com.mstan.shop.review.dto.CreateReviewRequest;
import com.mstan.shop.review.dto.ReviewResponse;
import com.mstan.shop.review.entity.Review;
import com.mstan.shop.review.mapper.ReviewMapper;
import com.mstan.shop.review.repository.ReviewRepository;
import com.mstan.shop.user.entity.User;
import com.mstan.shop.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class ReviewService {

    private final ReviewRepository reviewRepository;
    private final ProductRepository productRepository;
    private final UserRepository userRepository;
    private final OrderItemRepository orderItemRepository;
    private final ReviewMapper reviewMapper;

    @Transactional(readOnly = true)
    public PagedResponse<ReviewResponse> getReviews(UUID productId, Pageable pageable) {
        return PagedResponse.from(reviewRepository.findByProductId(productId, pageable).map(reviewMapper::toResponse));
    }

    @Transactional
    public ReviewResponse createReview(UUID userId, UUID productId, CreateReviewRequest request) {
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> ResourceNotFoundException.of("Produit", productId));
        User user = userRepository.findById(userId)
                .orElseThrow(() -> ResourceNotFoundException.of("Utilisateur", userId));

        if (!orderItemRepository.existsByOrder_UserIdAndOrder_StatusAndProduct_Id(userId, OrderStatus.LIVREE, productId)) {
            throw new IllegalArgumentException("Vous ne pouvez laisser un avis que sur un produit que vous avez reçu");
        }

        if (reviewRepository.existsByProductIdAndUserId(productId, userId)) {
            throw new DuplicateResourceException("Vous avez déjà laissé un avis pour ce produit");
        }

        Review review = new Review();
        review.setProduct(product);
        review.setUser(user);
        review.setRating(request.rating());
        review.setComment(request.comment());
        reviewRepository.save(review);

        return reviewMapper.toResponse(review);
    }

    @Transactional
    public void deleteReview(UUID reviewId) {
        Review review = reviewRepository.findById(reviewId)
                .orElseThrow(() -> ResourceNotFoundException.of("Avis", reviewId));
        reviewRepository.delete(review);
    }
}
