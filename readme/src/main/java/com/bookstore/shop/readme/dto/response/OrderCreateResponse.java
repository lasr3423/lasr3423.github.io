package com.bookstore.shop.readme.dto.response;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class OrderCreateResponse {
    private final Long orderId;      // 생성된 주문 ID → PaymentView에서 결제 시 사용
    private final int finalPrice;    // 최종 결제 금액 → 결제창 금액 표시
    private final String itemName;   // 대표 상품명 (예: "스프링 부트 외 2건") → 결제창 표시
}
