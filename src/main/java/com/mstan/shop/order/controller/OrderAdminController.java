package com.mstan.shop.order.controller;

import com.mstan.shop.common.util.PagedResponse;
import com.mstan.shop.order.dto.OrderResponse;
import com.mstan.shop.order.dto.OrderSummaryResponse;
import com.mstan.shop.order.dto.UpdateOrderStatusRequest;
import com.mstan.shop.order.entity.OrderStatus;
import com.mstan.shop.order.service.OrderService;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

@Tag(name = "Admin - Orders", description = "Gestion et consultation des commandes réservées aux administrateurs")
@RestController
@RequestMapping("/api/admin/orders")
@RequiredArgsConstructor
public class OrderAdminController {

    private final OrderService orderService;

    @GetMapping
    public PagedResponse<OrderSummaryResponse> getAllOrders(
            @RequestParam(required = false) OrderStatus status,
            @RequestParam(required = false) UUID userId,
            @RequestParam(required = false) String orderNumber,
            Pageable pageable
    ) {
        return orderService.getAllOrders(status, userId, orderNumber, pageable);
    }

    @GetMapping("/{id}")
    public OrderResponse getOrder(@PathVariable UUID id) {
        return orderService.getOrderByIdAdmin(id);
    }

    @PatchMapping("/{id}/status")
    public OrderResponse updateStatus(@PathVariable UUID id, @Valid @RequestBody UpdateOrderStatusRequest request) {
        return orderService.updateStatus(id, request);
    }

    @PostMapping("/{id}/cancel")
    public OrderResponse cancel(@PathVariable UUID id) {
        return orderService.cancelOrderAsAdmin(id);
    }

    @PostMapping("/{id}/confirm-cash-payment")
    public OrderResponse confirmCashPayment(@PathVariable UUID id) {
        return orderService.confirmCashPayment(id);
    }
}
