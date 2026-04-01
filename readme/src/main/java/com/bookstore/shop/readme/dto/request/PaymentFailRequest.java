package com.bookstore.shop.readme.dto.request;

import lombok.Getter;
import lombok.NoArgsConstructor;

// 토스 결제 실패 처리 DTO
// 토스 failUrl 리다이렉트 시 ?code=&message= 쿼리파라미터로 전달받은 값
@Getter
@NoArgsConstructor
public class PaymentFailRequest {

    private Long orderId;      // 어떤 주문의 결제가 실패했는지 식별
    private String code;       // 토스 에러 코드 (예: "USER_CANCEL", "INVALID_CARD_EXPIRY_DATE")
    private String message;    // 에러 메시지 — Payment.failureReason 필드에 저장
}