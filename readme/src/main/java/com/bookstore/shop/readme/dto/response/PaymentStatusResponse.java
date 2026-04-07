package com.bookstore.shop.readme.dto.response;

import com.bookstore.shop.readme.domain.Payment;
import lombok.Getter;

import java.time.LocalDateTime;

// [신규] 결제 내역 응답 DTO — 마이페이지/관리자 결제 목록에 사용
@Getter
public class PaymentStatusResponse {

    private final Long paymentId;
    private final Long orderId;
    private final String orderNumber;       // 주문번호 (Order.number)
    private final String paymentProvider;   // TOSS / KAKAO / NAVER
    private final String paymentStatus;     // READY / PAID / FAILED / CANCELLED
    private final int amount;
    private final String method;            // 결제 수단 (카드, 간편결제 등)
    private final LocalDateTime paidAt;
    private final LocalDateTime cancelledAt;
    private final String failureReason;

    public PaymentStatusResponse(Payment payment) {
        this.paymentId       = payment.getId();
        this.orderId         = payment.getOrder().getId();
        this.orderNumber     = payment.getOrder().getNumber();
        this.paymentProvider = payment.getPaymentProvider() != null
                ? payment.getPaymentProvider().name() : null;
        this.paymentStatus   = payment.getPaymentStatus() != null
                ? payment.getPaymentStatus().name() : null;
        this.amount          = payment.getAmount();
        this.method          = payment.getMethod();
        this.paidAt          = payment.getPaidAt();
        this.cancelledAt     = payment.getCancelledAt();
        this.failureReason   = payment.getFailureReason();
    }
}
