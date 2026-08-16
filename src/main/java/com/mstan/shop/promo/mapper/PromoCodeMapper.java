package com.mstan.shop.promo.mapper;

import com.mstan.shop.promo.dto.PromoCodeResponse;
import com.mstan.shop.promo.entity.PromoCode;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface PromoCodeMapper {

    PromoCodeResponse toResponse(PromoCode promoCode);
}
