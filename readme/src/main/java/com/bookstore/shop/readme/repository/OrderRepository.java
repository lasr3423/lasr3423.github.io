package com.bookstore.shop.readme.repository;

import com.bookstore.shop.readme.domain.Order;
import com.bookstore.shop.readme.domain.OrderStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

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
}
