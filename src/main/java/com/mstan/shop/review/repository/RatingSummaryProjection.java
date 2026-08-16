package com.mstan.shop.review.repository;

import java.util.UUID;

public interface RatingSummaryProjection {

    UUID getProductId();

    Double getAvgRating();

    Long getCnt();
}
