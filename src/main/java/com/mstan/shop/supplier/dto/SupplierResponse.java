package com.mstan.shop.supplier.dto;

import java.util.UUID;

public record SupplierResponse(
        UUID id,
        String name,
        String primaryPhone,
        String secondaryPhone,
        String location
) {
}
