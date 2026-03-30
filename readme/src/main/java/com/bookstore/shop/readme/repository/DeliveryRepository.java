package com.bookstore.shop.readme.repository;

import com.bookstore.shop.readme.domain.Delivery;
import com.bookstore.shop.readme.domain.DeliveryStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface DeliveryRepository extends JpaRepository<Delivery, Long> {
    Optional<Delivery> findByOrderId(Long orderId);

    Page<Delivery> findAllByDeliveryStatus(DeliveryStatus status, Pageable pageable);
}
