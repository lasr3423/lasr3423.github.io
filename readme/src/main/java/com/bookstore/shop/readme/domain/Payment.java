package com.bookstore.shop.readme.domain;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.ColumnDefault;

import java.time.LocalDateTime;

@Entity
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Table(name = "payment")
public class Payment extends BaseEntity {

    @ManyToOne
    @JoinColumn(name = "order_id")
    private Order order;

    @Enumerated(EnumType.STRING)
    private PaymentProvider paymentProvider;

    @Column(nullable = false, length = 30)
    private String method;

    @Enumerated(EnumType.STRING)
    @ColumnDefault("'READY'")
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
