package com.bookstore.shop.readme.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

// 결제 준비(ready) 응답 — 카카오/네이버 결제창으로 리다이렉트할 URL과 거래 ID 포함
// @Builder: PaymentService에서 .builder() 패턴으로 생성
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PaymentReadyResponse {

    private String tid;                     // 카카오: 거래 고유번호 (프론트에서 sessionStorage에 저장 → approve 시 재전송 필요)

    private String redirectPcUrl;           // 카카오: PC용 결제창 URL — 프론트에서 PC 감지 시 이 URL로 location.href 이동

    private String redirectMobileUrl;       // 카카오: 모바일용 결제창 URL — 프론트에서 모바일 감지 시 이 URL로 이동

    private String paymentId;               // 네이버페이: 결제 ID — approve 시 다시 서버로 전송해야 함

    private String redirectUrl;             // 네이버페이: 결제창 URL (카카오의 redirectPcUrl과 동일한 역할)
}
