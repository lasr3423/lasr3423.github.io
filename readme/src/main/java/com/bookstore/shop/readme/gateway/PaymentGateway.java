package com.bookstore.shop.readme.gateway;

import com.bookstore.shop.readme.dto.request.PaymentCancelRequest;
import com.bookstore.shop.readme.dto.request.PaymentConfirmRequest;
import com.bookstore.shop.readme.dto.request.PaymentReadyRequest;
import com.bookstore.shop.readme.dto.response.PaymentConfirmResponse;
import com.bookstore.shop.readme.dto.response.PaymentReadyResponse;

// ─── 전략 패턴 인터페이스 ─────────────────────────────────────────────────────
// 목적: Toss/Kakao/Naver 구현체를 동일한 타입으로 추상화
// 효과: PaymentService는 이 인터페이스에만 의존 → PG사 추가/교체 시 Service 코드 변경 불필요
// 사용: PaymentService.resolveGateway() 에서 provider 문자열로 구현체 선택
public interface PaymentGateway {
    // 결제 준비 메서드
    // 카카오/네이버: PG사 API 호출해서 거래 ID(tid/paymentId) + 결제창 URL 발급
    // 토스: 프론트 SDK에서 직접 결제창 오픈 → 서버에서 호출 필요 없음
    //       → TossPaymentGateway에서 UnsupportedOperationException 발생
    PaymentReadyResponse ready(PaymentReadyRequest request);

    // 결제 최종 승인 메서드
    // 각 PG사의 승인 API를 호출해서 결제를 최종 확정
    // 성공 시 PaymentConfirmResponse 반환 → PaymentService에서 Payment 상태 업데이트
    PaymentConfirmResponse confirm(PaymentConfirmRequest request);

    // 결제 취소/환불 메서드
    // 각 PG사의 취소 API를 호출
    // 반환값 없음(void) — 실패 시 RestClientException 발생
    void cancel(PaymentCancelRequest request);
}
