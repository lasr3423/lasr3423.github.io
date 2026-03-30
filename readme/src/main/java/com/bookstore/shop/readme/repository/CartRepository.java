package com.bookstore.shop.readme.repository;

import com.bookstore.shop.readme.domain.Cart;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CartRepository extends JpaRepository<Long, Cart> {
}
