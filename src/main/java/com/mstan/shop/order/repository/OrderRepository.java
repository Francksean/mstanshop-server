package com.mstan.shop.order.repository;

import com.mstan.shop.order.entity.Order;
import com.mstan.shop.user.entity.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import java.util.Optional;
import java.util.UUID;

public interface OrderRepository extends JpaRepository<Order, UUID>, JpaSpecificationExecutor<Order> {

    Page<Order> findByUser(User user, Pageable pageable);

    Optional<Order> findByIdAndUser(UUID id, User user);

    Optional<Order> findByOrderNumberAndUserIsNull(String orderNumber);
}
