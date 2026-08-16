package com.mstan.shop.order.dto;

public record AddressResponse(
        String firstName,
        String lastName,
        String street,
        String city,
        String postalCode,
        String country,
        String phone,
        String whatsapp
) {
}
