package com.bookstore.shop.readme.repository;

import com.bookstore.shop.readme.domain.OrderItem;
import org.springframework.data.jpa.repository.JpaRepository;

public interface OrderItemRepository extends JpaRepository<Long, OrderItem> {
}
