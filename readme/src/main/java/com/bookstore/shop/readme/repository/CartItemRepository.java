package com.bookstore.shop.readme.repository;

import org.springframework.data.jpa.repository.JpaRepository;

public interface CartItemRepository extends JpaRepository<Long, CartItemRepository> {
}
