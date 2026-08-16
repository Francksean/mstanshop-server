package com.mstan.shop.dashboard.dto;

import com.mstan.shop.payment.entity.PaymentMethod;
import com.mstan.shop.payment.entity.PaymentStatus;

import java.math.BigDecimal;

public record PaymentBreakdownResponse(
        PaymentMethod method,
        PaymentStatus status,
        long count,
        BigDecimal amount
) {
}
