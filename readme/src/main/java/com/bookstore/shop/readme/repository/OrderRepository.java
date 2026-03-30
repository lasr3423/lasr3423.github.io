package com.bookstore.shop.readme.repository;

import com.bookstore.shop.readme.domain.Order;
import org.springframework.data.jpa.repository.JpaRepository;

public interface OrderRepository extends JpaRepository<Long, Order> {
}
