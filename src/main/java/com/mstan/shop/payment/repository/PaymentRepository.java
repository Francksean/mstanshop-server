package com.mstan.shop.payment.repository;

import com.mstan.shop.payment.entity.Payment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface PaymentRepository extends JpaRepository<Payment, UUID> {

    Optional<Payment> findByProviderReference(String providerReference);

    Optional<Payment> findByExternalReference(UUID externalReference);

    List<Payment> findByOrderId(UUID orderId);

    Optional<Payment> findFirstByOrderIdOrderByCreatedAtDesc(UUID orderId);

    @Query("SELECT p.method, p.status, COUNT(p), COALESCE(SUM(p.amount), 0) FROM Payment p " +
            "WHERE p.createdAt BETWEEN :from AND :to GROUP BY p.method, p.status")
    List<Object[]> breakdownBetween(@Param("from") Instant from, @Param("to") Instant to);
}
