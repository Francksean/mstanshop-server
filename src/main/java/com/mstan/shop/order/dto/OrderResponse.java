package com.mstan.shop.order.dto;

import com.mstan.shop.order.entity.OrderStatus;
import com.mstan.shop.payment.entity.PaymentMethod;
import com.mstan.shop.payment.entity.PaymentStatus;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

public record OrderResponse(
        UUID id,
        String orderNumber,
        OrderStatus status,
        BigDecimal totalAmount,
        BigDecimal subtotalAmount,
        BigDecimal discountAmount,
        String promoCode,
        PaymentMethod paymentMethod,
        String paymentReference,
        PaymentStatus paymentStatus,
        OrderCustomerResponse customer,
        AddressResponse address,
        List<OrderItemResponse> items,
        Instant createdAt
) {
}
