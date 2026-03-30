package com.bookstore.shop.readme.dto.request;

import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class PaymentReadyRequest {
    private Long orderId;
    private String provider;    // 결제수단: "KAKAO" or "NAVER"
    private String itemName;    // 상품명 - 카카오 API required 필드 (ex: 교보문고 도서 외 2권)
    private Integer amount;     // 결제 금액 - 서버에서 Order.finalPrice와 비교 검증용
    private Long memberId;      // 카카오 partner_user_id 에 전달 (서버에서 SecurityContext로 채움)
    private String approvalUrl; // 결제성공 -> 성공 리다이렉트 URL
    private String cancelUrl;   // 결제취소 -> 취소 리다이렉트 URL
    private String failUrl;     // 결제실패 -> 실패 리다이렉트 URL
}
