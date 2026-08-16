package com.mstan.shop.order.dto;

import com.mstan.shop.payment.entity.PaymentMethod;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;

import java.util.List;

public record GuestOrderCreateRequest(

        @NotNull(message = "L'adresse de livraison est obligatoire")
        @Valid
        AddressRequest address,

        String email,

        @NotNull(message = "Le mode de paiement est obligatoire")
        PaymentMethod paymentMethod,

        String phoneNumber,

        String promoCode,

        @NotEmpty(message = "Le panier est vide, impossible de passer commande")
        @Valid
        List<GuestOrderItemRequest> items
) {
}
