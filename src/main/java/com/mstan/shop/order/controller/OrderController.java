package com.mstan.shop.order.controller;

import com.mstan.shop.common.security.SecurityUser;
import com.mstan.shop.common.util.PagedResponse;
import com.mstan.shop.order.dto.CreateOrderRequest;
import com.mstan.shop.order.dto.OrderResponse;
import com.mstan.shop.order.dto.OrderSummaryResponse;
import com.mstan.shop.order.service.OrderService;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

@Tag(name = "Orders", description = "Passage et consultation des commandes de l'utilisateur authentifié")
@RestController
@RequestMapping("/api/orders")
@RequiredArgsConstructor
public class OrderController {

    private final OrderService orderService;

    @PostMapping
    public ResponseEntity<OrderResponse> createOrder(
            @AuthenticationPrincipal SecurityUser user,
            @Valid @RequestBody CreateOrderRequest request
    ) {
        return ResponseEntity.status(HttpStatus.CREATED).body(orderService.createOrder(user.getId(), request));
    }

    @GetMapping
    public PagedResponse<OrderSummaryResponse> getHistory(@AuthenticationPrincipal SecurityUser user, Pageable pageable) {
        return orderService.getOrderHistory(user.getId(), pageable);
    }

    @GetMapping("/{id}")
    public OrderResponse getOrder(@AuthenticationPrincipal SecurityUser user, @PathVariable UUID id) {
        return orderService.getOrder(user.getId(), id);
    }

    @PostMapping("/{id}/cancel")
    public OrderResponse cancel(@AuthenticationPrincipal SecurityUser user, @PathVariable UUID id) {
        return orderService.cancelOrderAsClient(user.getId(), id);
    }

    @GetMapping("/{id}/payment-status")
    public OrderResponse getPaymentStatus(@AuthenticationPrincipal SecurityUser user, @PathVariable UUID id) {
        return orderService.refreshPaymentStatus(user.getId(), id);
    }
}
