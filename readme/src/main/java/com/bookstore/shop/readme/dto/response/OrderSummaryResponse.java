package com.bookstore.shop.readme.dto.response;

import com.bookstore.shop.readme.domain.Order;
import com.bookstore.shop.readme.domain.OrderStatus;

import java.time.LocalDateTime;

public class OrderSummaryResponse {

    private final Long orderId;            // 주문 DB PK (상세 조회 시 사용)
    // ⚠️ Order 엔티티의 주문번호 필드는 "number" — "orderNumber" 아님
    private final String number;           // 주문번호 (사용자에게 표시)
    private final int totalPrice;          // 상품 원가 합계
    private final int discountAmount;      // 할인 금액
    private final int finalPrice;          // 최종 결제 금액
    private final OrderStatus orderStatus; // PENDING / PAYED / APPROVAL / CANCELED
    private final LocalDateTime orderAt;   // 주문 일시

    // 엔티티 → DTO 변환 생성자
    public OrderSummaryResponse(Order order) {
        this.orderId        = order.getId();
        // ⚠️ order.getNumber() — getOrderNumber() 아님
        this.number         = order.getNumber();
        this.totalPrice     = order.getTotalPrice();
        this.discountAmount = order.getDiscountAmount();
        this.finalPrice     = order.getFinalPrice();
        this.orderStatus    = order.getOrderStatus();
        // ⚠️ Order 엔티티의 주문 일시 필드는 "orderAt" — "createdAt" 아님
        this.orderAt        = order.getOrderAt();
    }

}
