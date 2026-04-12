package com.bookstore.shop.readme.dto.response;

import com.bookstore.shop.readme.domain.Payment;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
public class PaymentStatusResponse {

    private final Long paymentId;
    private final Long orderId;
    private final String orderNumber;
    private final String paymentProvider;
    private final String paymentStatus;
    private final int amount;
    private final Integer refundedAmount;
    private final Integer returnFee;
    private final String method;
    private final String pgTid;
    private final String paymentKey;
    private final Integer installmentMonths;
    private final String memberName;
    private final String memberEmail;
    private final String cancelReason;
    private final String failureReason;
    private final LocalDateTime paidAt;
    private final LocalDateTime cancelledAt;
    private final LocalDateTime createdAt;
    private final LocalDateTime updatedAt;

    public PaymentStatusResponse(Payment payment) {
        this.paymentId = payment.getId();
        this.orderId = payment.getOrder().getId();
        this.orderNumber = payment.getOrder().getNumber();
        this.paymentProvider = payment.getPaymentProvider() != null ? payment.getPaymentProvider().name() : null;
        this.paymentStatus = payment.getPaymentStatus() != null ? payment.getPaymentStatus().name() : null;
        this.amount = payment.getAmount();
        this.refundedAmount = payment.getRefundedAmount();
        this.returnFee = payment.getReturnFee();
        this.method = payment.getMethod();
        this.pgTid = payment.getPgTid();
        this.paymentKey = payment.getPaymentKey();
        this.installmentMonths = payment.getInstallmentMonths();
        this.memberName = payment.getOrder().getMember() != null ? payment.getOrder().getMember().getName() : null;
        this.memberEmail = payment.getOrder().getMember() != null ? payment.getOrder().getMember().getEmail() : null;
        this.cancelReason = payment.getCancelReason();
        this.failureReason = payment.getFailureReason();
        this.paidAt = payment.getPaidAt();
        this.cancelledAt = payment.getCancelledAt();
        this.createdAt = payment.getCreatedAt();
        this.updatedAt = payment.getUpdatedAt();
    }
}
