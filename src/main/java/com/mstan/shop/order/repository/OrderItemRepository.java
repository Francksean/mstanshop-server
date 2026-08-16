package com.mstan.shop.order.repository;

import com.mstan.shop.order.entity.OrderItem;
import com.mstan.shop.order.entity.OrderStatus;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

public interface OrderItemRepository extends JpaRepository<OrderItem, UUID> {

    boolean existsByOrder_UserIdAndOrder_StatusAndProduct_Id(UUID userId, OrderStatus status, UUID productId);

    @Query("SELECT oi.product.id, oi.productName, SUM(oi.quantity), SUM(oi.unitPrice * oi.quantity) " +
            "FROM OrderItem oi WHERE oi.order.status IN :statuses AND oi.order.createdAt BETWEEN :from AND :to " +
            "GROUP BY oi.product.id, oi.productName ORDER BY SUM(oi.unitPrice * oi.quantity) DESC")
    List<Object[]> topProducts(@Param("statuses") List<OrderStatus> statuses, @Param("from") Instant from, @Param("to") Instant to, Pageable limit);

    @Query("SELECT oi.product.category.id, oi.product.category.name, SUM(oi.unitPrice * oi.quantity) " +
            "FROM OrderItem oi WHERE oi.order.status IN :statuses AND oi.order.createdAt BETWEEN :from AND :to " +
            "GROUP BY oi.product.category.id, oi.product.category.name ORDER BY SUM(oi.unitPrice * oi.quantity) DESC")
    List<Object[]> topCategories(@Param("statuses") List<OrderStatus> statuses, @Param("from") Instant from, @Param("to") Instant to, Pageable limit);
}
