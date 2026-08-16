package com.mstan.shop.cart.repository;

import com.mstan.shop.cart.entity.Cart;
import com.mstan.shop.user.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface CartRepository extends JpaRepository<Cart, UUID> {

    Optional<Cart> findByUser(User user);
}
