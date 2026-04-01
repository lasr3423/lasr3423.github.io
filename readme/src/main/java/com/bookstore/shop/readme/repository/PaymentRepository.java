package com.bookstore.shop.readme.repository;

import com.bookstore.shop.readme.domain.Payment;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

// JpaRepository<Payment, Long> : Payment 엔티티, PK 타입 Long
// → save, findById, delete 등 기본 CRUD 자동 제공
public interface PaymentRepository extends JpaRepository<Payment, Long> {
    // 주문 ID로 결제 정보 단건 조회
    // confirm/approve/cancel 처리 시 주문 ID만 있을 때 결제 정보 조회에 사용
    // Payment ↔ Order = 1:1 → Optional(0 또는 1개)
    // → SELECT * FROM payment WHERE order_id = ?
    Optional<Payment> findByOrderId(Long orderId);
}
