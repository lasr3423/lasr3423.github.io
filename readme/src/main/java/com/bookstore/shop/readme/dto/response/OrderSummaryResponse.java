package com.bookstore.shop.readme.dto.response;

import com.bookstore.shop.readme.domain.Order;
import com.bookstore.shop.readme.domain.OrderStatus;
import lombok.Getter;

import java.time.LocalDateTime;

// [수정] @Getter 누락으로 Jackson이 JSON 직렬화 불가 → 빈 {} 반환 버그 수정
// [수정] 프론트 MyOrderView.vue 필드명 맞춤: number→orderNumber, orderAt→createdAt
@Getter
public class OrderSummaryResponse {

    private final Long orderId;            // 주문 DB PK (상세 조회 시 사용)
    private final String orderNumber;      // 주문번호 (프론트: order.orderNumber)
    private final int totalPrice;          // 상품 원가 합계
    private final int discountAmount;      // 할인 금액
    private final int finalPrice;          // 최종 결제 금액
    private final OrderStatus orderStatus; // PENDING / PAYED / APPROVAL / CANCELED
    private final LocalDateTime createdAt; // 주문 일시 (프론트: order.createdAt)

    // 엔티티 → DTO 변환 생성자
    public OrderSummaryResponse(Order order) {
        this.orderId       = order.getId();
        this.orderNumber   = order.getNumber();
        this.totalPrice    = order.getTotalPrice();
        this.discountAmount = order.getDiscountAmount();
        this.finalPrice    = order.getFinalPrice();
        this.orderStatus   = order.getOrderStatus();
        this.createdAt     = order.getOrderAt();
    }

}
