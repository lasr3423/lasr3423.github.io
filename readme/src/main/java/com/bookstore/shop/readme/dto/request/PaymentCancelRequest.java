package com.bookstore.shop.readme.dto.request;

import lombok.Builder;
import lombok.Getter;

// 결제 취소/환불 요청 DTO — 프론트에서 직접 받지 않고 PaymentService 내부에서 빌드
// 각 PG사 취소 API에 전달할 파라미터를 하나의 DTO로 통합
@Getter
@Builder  // PaymentService.cancelPayment()에서 .builder()로 생성
public class PaymentCancelRequest {

    private String paymentKey;    // 토스: 취소 API URL 경로 /v1/payments/{paymentKey}/cancel
    private String tid;           // 카카오: 취소 API body의 "tid" 필드
    private String paymentId;     // 네이버: 취소 API body의 "paymentId" 필드
    private Integer cancelAmount; // 부분취소 금액 (null이면 전액 취소)
    private String cancelReason;  // 취소 사유 (필수) — 사용자 입력값
    private String provider;      // "TOSS" / "KAKAO" / "NAVER" — resolveGateway() 분기에 사용
}