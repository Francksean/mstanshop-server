package com.mstan.shop.order.dto;

import java.util.UUID;

public record OrderCustomerResponse(
        UUID id,
        String firstName,
        String lastName,
        String email
) {
    public static OrderCustomerResponse guest(String email) {
        return new OrderCustomerResponse(null, null, null, email);
    }
}
