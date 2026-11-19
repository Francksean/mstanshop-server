package com.mstan.shop.category.mapper;

import com.mstan.shop.category.dto.CategoryResponse;
import com.mstan.shop.category.entity.Category;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.util.List;

@Mapper(componentModel = "spring")
public interface CategoryMapper {

    @Mapping(target = "productCount", source = "productCount")
    @Mapping(target = "promoActive", expression = "java(com.mstan.shop.common.util.CategoryPromotionResolver.isPromotionActive(category))")
    @Mapping(target = "parentId", expression = "java(category.getParent() != null ? category.getParent().getId() : null)")
    @Mapping(target = "parentName", expression = "java(category.getParent() != null ? category.getParent().getName() : null)")
    @Mapping(target = "subcategories", expression = "java(java.util.List.of())")
    CategoryResponse toResponse(Category category, long productCount);

    @Mapping(target = "productCount", source = "productCount")
    @Mapping(target = "promoActive", expression = "java(com.mstan.shop.common.util.CategoryPromotionResolver.isPromotionActive(category))")
    @Mapping(target = "parentId", expression = "java(category.getParent() != null ? category.getParent().getId() : null)")
    @Mapping(target = "parentName", expression = "java(category.getParent() != null ? category.getParent().getName() : null)")
    @Mapping(target = "subcategories", source = "subcategories")
    CategoryResponse toResponse(Category category, long productCount, List<CategoryResponse> subcategories);
}
