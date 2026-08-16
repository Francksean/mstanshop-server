package com.mstan.shop.supplier.mapper;

import com.mstan.shop.supplier.dto.SupplierResponse;
import com.mstan.shop.supplier.entity.Supplier;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface SupplierMapper {

    SupplierResponse toResponse(Supplier supplier);
}
