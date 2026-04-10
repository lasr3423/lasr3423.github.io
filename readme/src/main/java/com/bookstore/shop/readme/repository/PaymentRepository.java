package com.bookstore.shop.readme.repository;

import com.bookstore.shop.readme.domain.Payment;
import com.bookstore.shop.readme.domain.PaymentStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

// JpaRepository<Payment, Long> : Payment 엔티티, PK 타입 Long
// → save, findById, delete 등 기본 CRUD 자동 제공
public interface PaymentRepository extends JpaRepository<Payment, Long> {
    // 주문 ID로 결제 정보 단건 조회
    // confirm/approve/cancel 처리 시 주문 ID만 있을 때 결제 정보 조회에 사용
    // Payment ↔ Order = 1:1 → Optional(0 또는 1개)
    // → SELECT * FROM payment WHERE order_id = ?
    Optional<Payment> findByOrderId(Long orderId);

    // [신규] 회원의 결제 내역 목록 — /api/member/me/payments 에서 사용
    // Payment → Order → Member 경로로 조회 (Spring Data JPA 네이밍 규칙)
    Page<Payment> findByOrder_MemberId(Long memberId, Pageable pageable);

    // [신규] 관리자 전체 결제 목록 — findAll(pageable) 로 대체 가능하지만
    //        추후 상태/기간 필터 추가를 고려해 별도 선언
    Page<Payment> findAllByPaymentStatus(PaymentStatus status, Pageable pageable);

    // [신규] 자동 취소 스케줄러용 — 특정 상태이면서 생성 시각이 기준 시각 이전인 결제 목록 조회
    // → SELECT * FROM payment WHERE payment_status = ? AND created_at < ?
    List<Payment> findByPaymentStatusAndCreatedAtBefore(PaymentStatus status, LocalDateTime before);
}
