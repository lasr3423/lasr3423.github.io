package com.bookstore.shop.readme.repository;

import com.bookstore.shop.readme.domain.Order;
import com.bookstore.shop.readme.domain.OrderStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface OrderRepository extends JpaRepository<Order, Long> {

    // 회원 주문 내역 최신순 조회
    List<Order> findByMemberIdOrderByCreatedAtDesc(Long memberId);

    // 주문번호로 특정 주문 조회
    Optional<Order> findByNumber(String number);

    Optional<Order> findByIdAndMemberId(Long id, Long memberId);
    Page<Order> findByMemberId(Long memberId, Pageable pageable);

    // 관리자 - 주문 상태별 수 (대시보드)
    long countByOrderStatus(OrderStatus orderStatus);

    // [신규] 관리자 - 특정 상태 주문 목록 (승인 대기 페이지 등에서 사용)
    Page<Order> findAllByOrderStatus(OrderStatus orderStatus, Pageable pageable);

    // 대시보드 — 기간별 주문 수
    long countByCreatedAtBetween(LocalDateTime start, LocalDateTime end);

    // 대시보드 — 기간별 매출 합계 (finalPrice 합산, PAYED/APPROVAL 상태만)
    @Query("SELECT COALESCE(SUM(o.finalPrice), 0) FROM Order o " +
           "WHERE o.createdAt BETWEEN :start AND :end " +
           "AND o.orderStatus IN ('PAYED', 'APPROVAL')")
    long sumFinalPriceBetween(@Param("start") LocalDateTime start,
                              @Param("end") LocalDateTime end);

    // 자동 만료 스케줄러용 — PAYMENT_PENDING 상태이면서 생성 시각이 기준 시각 이전인 주문 조회
    // → 토스 결제의 경우 Payment 레코드가 없으므로 Order 레벨에서 직접 만료 처리
    List<Order> findByOrderStatusAndCreatedAtBefore(OrderStatus status, LocalDateTime before);
}
