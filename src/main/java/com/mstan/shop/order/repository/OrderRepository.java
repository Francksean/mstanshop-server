package com.mstan.shop.order.repository;

import com.mstan.shop.order.entity.Order;
import com.mstan.shop.order.entity.OrderStatus;
import com.mstan.shop.user.entity.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface OrderRepository extends JpaRepository<Order, UUID>, JpaSpecificationExecutor<Order> {

    Page<Order> findByUser(User user, Pageable pageable);

    Optional<Order> findByIdAndUser(UUID id, User user);

    Optional<Order> findByOrderNumberAndUserIsNull(String orderNumber);

    long countByStatus(OrderStatus status);

    @Query("SELECT COALESCE(SUM(o.totalAmount), 0) FROM Order o " +
            "WHERE o.status IN :statuses AND o.createdAt BETWEEN :from AND :to")
    BigDecimal sumRevenueBetween(@Param("statuses") List<OrderStatus> statuses, @Param("from") Instant from, @Param("to") Instant to);

    @Query("SELECT COUNT(o) FROM Order o WHERE o.status IN :statuses AND o.createdAt BETWEEN :from AND :to")
    long countBetween(@Param("statuses") List<OrderStatus> statuses, @Param("from") Instant from, @Param("to") Instant to);

    @Query("SELECT o.status, COUNT(o) FROM Order o GROUP BY o.status")
    List<Object[]> countGroupedByStatus();

    @Query(value = "SELECT date_trunc('day', o.created_at) AS day, SUM(o.total_amount), COUNT(o.id) " +
            "FROM orders o WHERE o.status IN :statuses AND o.created_at BETWEEN :from AND :to " +
            "GROUP BY day ORDER BY day", nativeQuery = true)
    List<Object[]> revenueByDay(@Param("statuses") List<String> statuses, @Param("from") Instant from, @Param("to") Instant to);

    @Query("SELECT o.promoCode, COALESCE(SUM(o.discountAmount), 0) FROM Order o " +
            "WHERE o.promoCode IS NOT NULL GROUP BY o.promoCode")
    List<Object[]> discountByPromoCode();
}
