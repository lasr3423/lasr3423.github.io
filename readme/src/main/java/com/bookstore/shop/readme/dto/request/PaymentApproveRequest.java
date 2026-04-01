package com.bookstore.shop.readme.dto.request;

import lombok.Getter;
import lombok.NoArgsConstructor;

// 카카오/네이버 결제 최종 승인 요청 DTO
// 프론트에서 approval_url(카카오) 또는 returnUrl(네이버) 처리 후 서버로 전송
@Getter
@NoArgsConstructor
public class PaymentApproveRequest {

    private Long orderId;       // 어떤 주문의 결제인지 식별
    private String tid;         // 카카오: ready 응답에서 받아 sessionStorage에 저장한 거래 ID
    private String pgToken;     // 카카오: approval_url 리다이렉트 시 ?pg_token= 쿼리파라미터
    private String paymentId;   // 네이버: returnUrl 리다이렉트 시 ?paymentId= 쿼리파라미터
    private String resultCode;  // 네이버: "Success" 이외의 값이면 결제 실패로 처리
    private String provider;    // "KAKAO" 또는 "NAVER" — 어떤 Gateway 구현체 사용할지 결정
}