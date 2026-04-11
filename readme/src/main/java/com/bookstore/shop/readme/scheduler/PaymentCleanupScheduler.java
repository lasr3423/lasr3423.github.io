package com.bookstore.shop.readme.scheduler;

import com.bookstore.shop.readme.domain.*;
import com.bookstore.shop.readme.repository.OrderRepository;
import com.bookstore.shop.readme.repository.PaymentRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * 결제 자동 만료 스케줄러
 *
 * 목적: 결제창을 열었지만 30분 내에 완료하지 않은 주문을 자동 취소
 *
 * 실행 주기: 5분마다 (서버 구동 1분 후 첫 실행)
 *
 * 처리 시나리오:
 *   A) 카카오페이 / 네이버페이
 *      → requestReady() 호출 시 Payment(READY) 레코드 생성됨
 *      → 30분 지난 READY 결제 탐지 → Payment.CANCELLED + Order.CANCELED
 *
 *   B) 토스페이먼츠
 *      → Payment 레코드가 결제 성공 시에만 생성됨 (READY 단계 없음)
 *      → PAYMENT_PENDING 상태 주문 중 Payment 레코드가 없는 것 탐지 → Order.CANCELED
 *
 * 상태 전이:
 *   Payment:  READY          → CANCELLED
 *   Order:    PAYMENT_PENDING → CANCELED
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class PaymentCleanupScheduler {

    private final PaymentRepository paymentRepository;
    private final OrderRepository   orderRepository;

    /**
     * 만료된 결제/주문 자동 취소
     *
     * fixedDelay: 이전 실행이 끝난 후 5분 뒤 다시 실행 (동시 실행 방지)
     * initialDelay: 서버 구동 1분 후 첫 실행 (애플리케이션 완전 초기화 대기)
     */
    @Scheduled(fixedDelay = 5 * 60 * 1000, initialDelay = 60 * 1000)
    @Transactional
    public void cancelExpiredPayments() {
        LocalDateTime threshold = LocalDateTime.now().minusMinutes(30);
        int canceledCount = 0;

        // ─── 시나리오 A: 카카오/네이버 — READY 결제 만료 처리 ─────────────────
        List<Payment> expiredPayments = paymentRepository
                .findByPaymentStatusAndCreatedAtBefore(PaymentStatus.READY, threshold);

        // 이미 처리된 주문 ID 추적 (시나리오 B에서 중복 처리 방지)
        Set<Long> processedOrderIds = new HashSet<>();

        for (Payment payment : expiredPayments) {
            try {
                payment.setPaymentStatus(PaymentStatus.CANCELLED);
                payment.setCancelReason("결제 대기 시간 초과 자동 취소 (30분)");
                payment.setCancelledAt(LocalDateTime.now());
                paymentRepository.save(payment);

                Order order = payment.getOrder();
                processedOrderIds.add(order.getId());

                if (order.getOrderStatus() == OrderStatus.PAYMENT_PENDING) {
                    order.setOrderStatus(OrderStatus.CANCELED);
                    order.setCancelledAt(LocalDateTime.now());
                    orderRepository.save(order);
                    canceledCount++;
                }
            } catch (Exception e) {
                log.error("[결제 자동 만료 A] paymentId={} 처리 실패: {}",
                        payment.getId(), e.getMessage());
            }
        }

        // ─── 시나리오 B: 토스 — Payment 레코드 없이 대기 중인 주문 만료 처리 ──
        List<Order> staleOrders = orderRepository
                .findByOrderStatusAndCreatedAtBefore(OrderStatus.PAYMENT_PENDING, threshold);

        for (Order order : staleOrders) {
            // 시나리오 A에서 이미 처리된 주문은 건너뜀
            if (processedOrderIds.contains(order.getId())) {
                continue;
            }
            try {
                order.setOrderStatus(OrderStatus.CANCELED);
                order.setCancelledAt(LocalDateTime.now());
                orderRepository.save(order);
                canceledCount++;
            } catch (Exception e) {
                log.error("[결제 자동 만료 B] orderId={} 처리 실패: {}",
                        order.getId(), e.getMessage());
            }
        }

        if (canceledCount > 0) {
            log.info("[결제 자동 만료] {}건 취소 처리 완료 (기준: {} 이전 생성)", canceledCount, threshold);
        }
    }
}
