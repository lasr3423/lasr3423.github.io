package com.bookstore.shop.readme.gateway;

import com.bookstore.shop.readme.dto.request.PaymentCancelRequest;
import com.bookstore.shop.readme.dto.request.PaymentConfirmRequest;
import com.bookstore.shop.readme.dto.request.PaymentReadyRequest;
import com.bookstore.shop.readme.dto.response.PaymentConfirmResponse;
import com.bookstore.shop.readme.dto.response.PaymentReadyResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;
import java.util.HashMap;
import java.util.Map;

@Component
@RequiredArgsConstructor
public class KakaoPaymentGateway implements PaymentGateway {

    // 카카오 Admin 키 — Authorization 헤더: "KakaoAK {adminKey}"
    // ⚠️ REST API 키가 아닌 Admin 키임에 주의
    @Value("${payment.kakao.admin-key}")
    private String adminKey;

    // 결제 수단 식별자 — 테스트: TC0ONETIME, 실서비스: 카카오 심사 후 발급
    @Value("${payment.kakao.cid}")
    private String cid;

    private final RestTemplate restTemplate;

    // 카카오페이 API 서버 URL
    private static final String KAKAO_READY_URL   = "https://kapi.kakao.com/v1/payment/ready";    // 결제 준비
    private static final String KAKAO_APPROVE_URL = "https://kapi.kakao.com/v1/payment/approve";  // 결제 승인
    private static final String KAKAO_CANCEL_URL  = "https://kapi.kakao.com/v1/payment/cancel";   // 결제 취소

    // 카카오 결제 준비: tid + 결제창 URL 발급
    // 프론트에서 이 응답의 redirectPcUrl/redirectMobileUrl로 location.href 이동
    @Override
    public PaymentReadyResponse ready(PaymentReadyRequest request) {
        // Authorization 헤더: "KakaoAK {adminKey}" (토스의 Basic 인증과 다름)
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.set("Authorization", "KakaoAK " + adminKey);

        // 카카오 ready API body 파라미터
        Map<String, Object> body = new HashMap<>();
        body.put("cid", cid);                                               // 결제 수단 식별자
        body.put("partner_order_id", String.valueOf(request.getOrderId())); // 가맹점 주문 ID (String)
        body.put("partner_user_id", String.valueOf(request.getMemberId())); // 가맹점 회원 ID (String)
        body.put("item_name", request.getItemName());                       // 상품명 (필수)
        body.put("quantity", 1);                                            // 수량 (전체 주문을 1개로 처리)
        body.put("total_amount", request.getAmount());                      // 총 결제 금액
        body.put("tax_free_amount", 0);                                     // 비과세 금액 (도서는 면세)
        body.put("approval_url", request.getApprovalUrl());                 // 성공 리다이렉트 URL
        body.put("cancel_url", request.getCancelUrl());                     // 취소 리다이렉트 URL
        body.put("fail_url", request.getFailUrl());                         // 실패 리다이렉트 URL

        HttpEntity<Map<String, Object>> entity = new HttpEntity<>(body, headers);
        ResponseEntity<Map> response = restTemplate.postForEntity(KAKAO_READY_URL, entity, Map.class);
        Map<String, Object> result = response.getBody();

        // 응답에서 tid와 결제창 URL 추출
        return PaymentReadyResponse.builder()
                .tid((String) result.get("tid"))                                    // 거래 ID — 반드시 저장했다가 approve 시 재사용
                .redirectPcUrl((String) result.get("next_redirect_pc_url"))         // PC용 결제창 URL
                .redirectMobileUrl((String) result.get("next_redirect_mobile_url")) // 모바일용 결제창 URL
                .build();
    }

    // 카카오 결제 최종 승인 — approval_url 리다이렉트 이후 pg_token을 받아서 호출
    @Override
    public PaymentConfirmResponse confirm(PaymentConfirmRequest request) {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.set("Authorization", "KakaoAK " + adminKey);

        // 카카오 approve API body — tid와 pg_token이 반드시 필요
        Map<String, Object> body = new HashMap<>();
        body.put("cid", cid);
        body.put("tid", request.getTid());                                           // ready 시 발급받은 tid
        body.put("partner_order_id", String.valueOf(request.getOrderId()));
        body.put("partner_user_id", String.valueOf(request.getMemberId()));
        body.put("pg_token", request.getPgToken());                                  // approval_url에서 받은 pg_token

        HttpEntity<Map<String, Object>> entity = new HttpEntity<>(body, headers);
        restTemplate.postForEntity(KAKAO_APPROVE_URL, entity, Map.class);           // 응답 필드 필요 없어 Map 그대로 사용

        // 카카오 승인 성공 시 "DONE" 상태 반환 (토스와 동일한 형식)
        return PaymentConfirmResponse.builder()
                .status("DONE")
                .build();
    }

    // 카카오 결제 취소 API 호출
    @Override
    public void cancel(PaymentCancelRequest request) {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.set("Authorization", "KakaoAK " + adminKey);

        Map<String, Object> body = new HashMap<>();
        body.put("cid", cid);
        body.put("tid", request.getTid());                     // 취소할 거래 ID
        body.put("cancel_amount", request.getCancelAmount());  // 취소 금액
        body.put("cancel_tax_free_amount", 0);                 // 비과세 취소 금액 (도서는 0)

        HttpEntity<Map<String, Object>> entity = new HttpEntity<>(body, headers);
        restTemplate.postForEntity(KAKAO_CANCEL_URL, entity, Map.class);
    }
}