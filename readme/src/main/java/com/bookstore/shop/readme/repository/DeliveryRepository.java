package com.bookstore.shop.readme.repository;

import com.bookstore.shop.readme.domain.Delivery;
import org.springframework.data.jpa.repository.JpaRepository;

public interface DeliveryRepository extends JpaRepository<Long, Delivery> {
}
