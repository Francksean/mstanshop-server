package com.mstan.shop.order.dto;

import com.mstan.shop.payment.entity.PaymentMethod;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;

public record CreateOrderRequest(

        @NotNull(message = "L'adresse de livraison est obligatoire")
        @Valid
        AddressRequest address,

        @NotNull(message = "Le mode de paiement est obligatoire")
        PaymentMethod paymentMethod,

        String phoneNumber,

        String promoCode
) {
}
