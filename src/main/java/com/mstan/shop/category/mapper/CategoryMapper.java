package com.mstan.shop.category.mapper;

import com.mstan.shop.category.dto.CategoryResponse;
import com.mstan.shop.category.entity.Category;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface CategoryMapper {

    @Mapping(target = "productCount", source = "productCount")
    @Mapping(target = "promoActive", expression = "java(com.mstan.shop.common.util.CategoryPromotionResolver.isPromotionActive(category))")
    CategoryResponse toResponse(Category category, long productCount);
}
