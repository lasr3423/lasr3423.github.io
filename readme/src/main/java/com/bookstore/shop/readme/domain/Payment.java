package com.bookstore.shop.readme.domain;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

@Entity
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Table(name = "payment")
public class Payment extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "order_id", nullable = false)
    private Order order;

    @Enumerated(EnumType.STRING)
    private PaymentProvider paymentProvider;

    @Column(length = 30)
    private String method;

    @Enumerated(EnumType.STRING)
    @Builder.Default
    private PaymentStatus paymentStatus = PaymentStatus.READY;

    @Column(nullable = false)
    private int amount;

    @Column(length = 100)
    private String pgTid;

    @Column(length = 200)
    private String paymentKey;

    private Integer installmentMonths;

    @Column(name = "paid_at")
    private LocalDateTime paidAt;

    @Column(length = 300)
    private String cancelReason;

    @Column(length = 300)
    private String failureReason;

    @Column(name = "cancelled_at")
    private LocalDateTime cancelledAt;

}
