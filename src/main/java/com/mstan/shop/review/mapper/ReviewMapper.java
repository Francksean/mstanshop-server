package com.mstan.shop.review.mapper;

import com.mstan.shop.review.dto.ReviewResponse;
import com.mstan.shop.review.entity.Review;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface ReviewMapper {

    @Mapping(target = "productId", source = "product.id")
    @Mapping(target = "userId", source = "user.id")
    @Mapping(target = "userFirstName", source = "user.firstName")
    ReviewResponse toResponse(Review review);
}
