package com.bookstore.shop.readme.gateway;

import com.bookstore.shop.readme.dto.request.PaymentCancelRequest;
import com.bookstore.shop.readme.dto.request.PaymentConfirmRequest;
import com.bookstore.shop.readme.dto.request.PaymentReadyRequest;
import com.bookstore.shop.readme.dto.response.PaymentConfirmResponse;
import com.bookstore.shop.readme.dto.response.PaymentReadyResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import java.util.HashMap;
import java.util.Map;

/**
 * 카카오페이 결제 게이트웨이 구현체 (신버전 API 기준)
 *
 * 카카오페이 결제는 3단계로 이루어집니다.
 *   1. ready()   : 결제 준비 — 카카오페이 서버에 결제 정보를 등록하고 결제창 URL(tid)을 받아옴
 *   2. confirm() : 결제 승인 — 사용자가 결제창에서 승인 후 전달된 pg_token으로 최종 결제 확정
 *   3. cancel()  : 결제 취소 — 결제 완료된 건을 환불 처리
 *
 * [구버전 → 신버전 변경 포인트]
 *   - 기본 URL : kapi.kakao.com → open-api.kakaopay.com
 *   - 인증 방식: KakaoAK {어드민키} → SECRET_KEY {시크릿키}
 *   - Content-Type: application/x-www-form-urlencoded → application/json
 */
@Component
@RequiredArgsConstructor
public class KakaoPaymentGateway implements PaymentGateway {

    // application.yml의 payment.kakao.secret-key 값 주입
    // 카카오페이 개발자센터(https://developers.kakaopay.com) → 내 앱 → 키 관리에서 발급
    @Value("${payment.kakao.secret-key}")
    private String secretKey;

    // 결제 구분자 (CID)
    // 테스트: TC0ONETIME (단건 결제 테스트용 고정값)
    // 운영: 카카오페이 계약 후 발급받은 실제 CID로 교체 필요
    @Value("${payment.kakao.cid}")
    private String cid;

    // HTTP 요청을 보내는 스프링 내장 클라이언트
    private final RestTemplate restTemplate;

    // 카카오페이 신버전 API 엔드포인트
    private static final String KAKAO_READY_URL   = "https://open-api.kakaopay.com/online/v1/payment/ready";
    private static final String KAKAO_APPROVE_URL = "https://open-api.kakaopay.com/online/v1/payment/approve";
    private static final String KAKAO_CANCEL_URL  = "https://open-api.kakaopay.com/online/v1/payment/cancel";

    /**
     * 공통 HTTP 헤더 생성
     *
     * 신버전에서는 반드시 아래 두 가지를 지켜야 합니다.
     *   - Authorization: "SECRET_KEY " + 시크릿키  (콜론 없이 띄어쓰기로 구분)
     *   - Content-Type : application/json          (구버전의 form-urlencoded와 다름)
     */
    private HttpHeaders buildHeaders() {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.set("Authorization", "SECRET_KEY " + secretKey);
        return headers;
    }

    /**
     * [1단계] 결제 준비 (ready)
     *
     * 프론트에서 "카카오페이로 결제하기" 버튼을 누르면 백엔드가 이 메서드를 호출합니다.
     * 카카오페이 서버에 결제 정보를 등록하고, 사용자를 보낼 결제창 URL을 받아옵니다.
     *
     * 응답값:
     *   - tid                    : 결제 고유번호 (승인/취소 시 반드시 필요 — DB에 저장해야 함)
     *   - next_redirect_pc_url   : PC용 카카오페이 결제창 URL (프론트에서 이 URL로 리다이렉트)
     *   - next_redirect_mobile_url: 모바일용 결제창 URL
     */
    @Override
    public PaymentReadyResponse ready(PaymentReadyRequest request) {
        Map<String, Object> body = new HashMap<>();
        body.put("cid", cid);
        body.put("partner_order_id", String.valueOf(request.getOrderId()));   // 우리 서비스의 주문 ID
        body.put("partner_user_id", request.getMemberId() != null            // 우리 서비스의 회원 ID
                ? String.valueOf(request.getMemberId()) : "guest");
        body.put("item_name", request.getItemName());                        // 결제창에 표시될 상품명
        body.put("quantity", 1);                                             // 수량 (Integer 타입 필수)
        body.put("total_amount", request.getAmount());                       // 결제 금액 (Integer 타입 필수)
        body.put("tax_free_amount", 0);                                      // 비과세 금액 (없으면 0)
        body.put("approval_url", request.getApprovalUrl());                  // 결제 성공 시 리다이렉트될 우리 서버 URL
        body.put("cancel_url", request.getCancelUrl());                      // 결제창에서 취소 시 리다이렉트 URL
        body.put("fail_url", request.getFailUrl());                          // 결제 실패 시 리다이렉트 URL

        HttpEntity<Map<String, Object>> entity = new HttpEntity<>(body, buildHeaders());
        ResponseEntity<Map> response = restTemplate.postForEntity(KAKAO_READY_URL, entity, Map.class);
        Map<String, Object> result = response.getBody();

        return PaymentReadyResponse.builder()
                .tid((String) result.get("tid"))
                .redirectPcUrl((String) result.get("next_redirect_pc_url"))
                .redirectMobileUrl((String) result.get("next_redirect_mobile_url"))
                .build();
    }

    /**
     * [2단계] 결제 승인 (approve)
     *
     * 사용자가 카카오페이 결제창에서 결제를 완료하면,
     * 카카오가 approval_url로 리다이렉트하면서 pg_token을 쿼리 파라미터로 전달합니다.
     * 프론트에서 이 pg_token을 받아 백엔드로 보내면, 백엔드가 최종 승인을 요청합니다.
     *
     * 필수 파라미터:
     *   - tid       : ready() 때 발급받아 DB에 저장해뒀던 결제 고유번호
     *   - pg_token  : 카카오가 approval_url에 붙여준 일회용 토큰 (카카오 결제 승인에 필수)
     */
    @Override
    public PaymentConfirmResponse confirm(PaymentConfirmRequest request) {
        // 승인에 필수 파라미터가 없으면 즉시 예외 처리
        if (request.getMemberId() == null) {
            throw new RuntimeException("카카오 결제 승인용 회원 ID가 없습니다.");
        }
        if (request.getTid() == null || request.getTid().isBlank()) {
            throw new RuntimeException("카카오 결제 승인용 TID가 없습니다.");
        }
        if (request.getPgToken() == null || request.getPgToken().isBlank()) {
            throw new RuntimeException("카카오 결제 승인용 pg_token이 없습니다.");
        }

        Map<String, Object> body = new HashMap<>();
        body.put("cid", cid);
        body.put("tid", request.getTid());                                   // ready() 때 저장한 TID
        body.put("partner_order_id", String.valueOf(request.getOrderId()));  // ready() 때와 동일한 주문 ID 사용
        body.put("partner_user_id", String.valueOf(request.getMemberId())); // ready() 때와 동일한 회원 ID 사용
        body.put("pg_token", request.getPgToken());                          // 카카오가 approval_url로 전달한 토큰

        HttpEntity<Map<String, Object>> entity = new HttpEntity<>(body, buildHeaders());
        restTemplate.postForEntity(KAKAO_APPROVE_URL, entity, Map.class);

        // 승인 성공 시 status = "DONE" 반환
        return PaymentConfirmResponse.builder()
                .status("DONE")
                .build();
    }

    /**
     * [3단계] 결제 취소 (cancel)
     *
     * 결제 완료된 건을 전액 취소(환불)합니다.
     * 카카오페이는 부분 취소도 지원하나, 현재는 전액 취소만 구현되어 있습니다.
     *
     * 필수 파라미터:
     *   - tid           : 취소할 결제의 고유번호 (ready 시 발급, DB에서 조회)
     *   - cancel_amount : 취소할 금액 (전액이면 원래 결제 금액과 동일하게)
     */
    @Override
    public void cancel(PaymentCancelRequest request) {
        Map<String, Object> body = new HashMap<>();
        body.put("cid", cid);
        body.put("tid", request.getTid());                  // 취소할 결제의 TID (DB에서 조회한 값)
        body.put("cancel_amount", request.getCancelAmount()); // 취소 금액 (Integer 타입 필수)
        body.put("cancel_tax_free_amount", 0);              // 취소 비과세 금액 (없으면 0)

        HttpEntity<Map<String, Object>> entity = new HttpEntity<>(body, buildHeaders());
        restTemplate.postForEntity(KAKAO_CANCEL_URL, entity, Map.class);
    }
}
