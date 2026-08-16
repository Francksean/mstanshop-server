package com.mstan.shop.order.repository;

import com.mstan.shop.order.entity.OrderItem;
import com.mstan.shop.order.entity.OrderStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface OrderItemRepository extends JpaRepository<OrderItem, UUID> {

    boolean existsByOrder_UserIdAndOrder_StatusAndProduct_Id(UUID userId, OrderStatus status, UUID productId);
}
