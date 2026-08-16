package com.mstan.shop.order.controller;

import com.mstan.shop.order.dto.GuestOrderCreateRequest;
import com.mstan.shop.order.dto.OrderResponse;
import com.mstan.shop.order.service.OrderService;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "Guest Orders", description = "Passage et suivi de commande sans compte")
@RestController
@RequestMapping("/api/guest/orders")
@RequiredArgsConstructor
public class GuestOrderController {

    private final OrderService orderService;

    @PostMapping
    public ResponseEntity<OrderResponse> createGuestOrder(@Valid @RequestBody GuestOrderCreateRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(orderService.createGuestOrder(request));
    }

    @GetMapping("/{orderNumber}")
    public OrderResponse getByOrderNumber(@PathVariable String orderNumber) {
        return orderService.getGuestOrderByNumber(orderNumber);
    }
}
