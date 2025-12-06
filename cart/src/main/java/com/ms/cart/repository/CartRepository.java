package com.ms.cart.repository;

import com.ms.cart.model.Cart;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface CartRepository extends JpaRepository<Cart, Long> {

    Optional<Cart> findCartByUserId(Long userId);

}
