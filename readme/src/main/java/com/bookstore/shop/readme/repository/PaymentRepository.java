package com.bookstore.shop.readme.repository;

import com.bookstore.shop.readme.domain.Payment;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PaymentRepository extends JpaRepository<Long, Payment> {
}
