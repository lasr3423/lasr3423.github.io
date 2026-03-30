package com.bookstore.shop.readme.domain;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.ColumnDefault;
import org.springframework.data.annotation.CreatedDate;

import java.time.LocalDateTime;

@Entity
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Table(name = "\"order\"")
public class Order extends BaseEntity {

    @ManyToOne
    @JoinColumn(name = "member_id")
    private Member member;

    @Column(nullable = false, unique = true)
    private String number;

    @Enumerated(EnumType.STRING)
    @ColumnDefault("'PENDING'")
    private OrderStatus orderStatus = OrderStatus.PENDING;

    @Column(nullable = false)
    @ColumnDefault("0")
    private int totalPrice = 0;

    @Column(nullable = false)
    @ColumnDefault("0")
    private int discountAmount = 0;

    @Column(nullable = false)
    @ColumnDefault("0")
    private int finalPrice = 0;

    @Column(nullable = false, length = 50)
    private String receiverName;

    @Column(nullable = false, length = 20)
    private String receiverPhone;

    @Column(nullable = false, length = 255)
    private String deliveryAddress;

    @Column(length = 255)
    @ColumnDefault("null")
    private String deliveryAddressDetail;

    @Column(nullable = false, length = 10)
    private String deliveryZipCode;

    @Column(length = 300)
    @ColumnDefault("null")
    private String deliveryMemo;

    @CreatedDate
    @Column(name = "ordered_at", nullable = false, updatable = false)
    private LocalDateTime orderAt;

    @Column(name = "cancelled_at")
    private LocalDateTime cancelledAt;



}
