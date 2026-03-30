package com.bookstore.shop.readme.dto.request;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

// 토스 결제 최종 승인 요청 DTO
// 토스 successUrl 리다이렉트 시 쿼리파라미터로 전달된 값들을 담아 /confirm API 호출
// 카카오/네이버 approve 흐름에서도 재사용 가능하도록 추가 필드 포함
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PaymentConfirmRequest {
    private String paymentKey;  // 토스: successUrl 쿼리파라미터 ?paymentKey= 로 전달받은 고유 결제 키
                                // 취소 API URL에도 사용: POST /api/payments/{paymentKey}/cancel
    private Long orderId;       // 어떤 주문의 결제인지 식별 - Order.id와 매핑
    private Integer amount;     // 클라이언트에서 전달받은 금액 — 반드시 Order.finalPrice와 서버에서 비교 검증
    private String tid;         // 카카오 거래 ID (ready 응답에서 받은 값)
    private String pgToken;     // 카카오: approval_url 쿼리파라미터 ?pg_token= 값
    private String paymentId;   // 네이버: returnUrl 쿼리파라미터 ?paymentId= 값
    private Long memberId;      // 카카오 approve API의 partner_user_id 필드에 사용
}
