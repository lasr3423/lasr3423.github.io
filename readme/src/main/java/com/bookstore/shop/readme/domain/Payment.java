package com.bookstore.shop.readme.domain;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.ColumnDefault;

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
    @ColumnDefault("null")
    private PaymentProvider paymentProvider;

    @Column(nullable = false, length = 30)
    private String method;

    @Enumerated(EnumType.STRING)
    @ColumnDefault("'READY'")
    private PaymentStatus paymentStatus = PaymentStatus.READY;

    @Column(nullable = false)
    private int amount;



}
