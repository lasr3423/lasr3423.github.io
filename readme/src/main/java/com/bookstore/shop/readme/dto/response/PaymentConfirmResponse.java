package com.bookstore.shop.readme.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

// PG사 결제 승인 응답을 담는 DTO
// Gateway 구현체의 confirm() 반환값으로 사용
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PaymentConfirmResponse {

    // 토스: PG사가 발급한 결제 고유 키 — Payment.paymentKey 필드에 저장
    private String paymentKey;

    // 결제 상태 문자열 — 토스: "DONE", 카카오: "DONE", 네이버: "SUCCESS"
    // PaymentService에서 이 값으로 PaymentStatus.PAID 로 변경
    private String status;

    // 실제 결제된 금액 — 검증용으로 사용 (옵션)
    private Integer amount;
}
