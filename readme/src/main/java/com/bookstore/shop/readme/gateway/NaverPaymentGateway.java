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
public class NaverPaymentGateway implements PaymentGateway {

    // 네이버페이 인증 헤더에 사용
    // 헤더 형식: X-Naver-Client-Id: {clientId} / X-Naver-Client-Secret: {clientSecret}
    @Value("${naver.client-id}")
    private String clientId;

    @Value("${naver.client-secret}")
    private String clientSecret;

    // 결제 수단 식별자 (카카오의 cid에 해당)
    @Value("${naver.chain-id}")
    private String chainId;

    private final RestTemplate restTemplate;

    // 네이버페이 샌드박스 API URL
    private static final String NAVER_APPLY_URL  = "https://dev.apis.naver.com/naverpay-partner/naverpay/payments/v2.2/apply";
    private static final String NAVER_CANCEL_URL = "https://dev.apis.naver.com/naverpay-partner/naverpay/payments/v1/cancel";

    // 네이버페이 결제 준비: 프론트 JS SDK에서 paymentId를 받아 처리
    // 서버에서는 결제 정보를 저장하고 orderId를 paymentId로 반환
    // 실제 네이버 결제창은 프론트 NaverPayButton.apply()에서 오픈
    @Override
    public PaymentReadyResponse ready(PaymentReadyRequest request) {
        // ⚠️ 현재는 orderId를 paymentId로 사용하는 단순화된 구현
        // 실제 네이버 연동 시 네이버 API를 호출해서 실제 paymentId를 받아야 함
        return PaymentReadyResponse.builder()
                .paymentId(String.valueOf(request.getOrderId()))  // 임시: orderId를 paymentId로 사용
                .build();
    }

    // 네이버 결제 최종 승인 — returnUrl 리다이렉트 이후 paymentId를 받아서 호출
    @Override
    public PaymentConfirmResponse confirm(PaymentConfirmRequest request) {
        // 네이버 인증 헤더: X-Naver-Client-Id / X-Naver-Client-Secret 사용
        // (토스의 Basic 인증, 카카오의 KakaoAK와 다른 방식)
        HttpHeaders headers = new HttpHeaders();
        headers.set("X-Naver-Client-Id", clientId);
        headers.set("X-Naver-Client-Secret", clientSecret);
        headers.setContentType(MediaType.APPLICATION_JSON);

        // 네이버 apply API body — paymentId만 전달
        Map<String, Object> body = new HashMap<>();
        body.put("paymentId", request.getPaymentId()); // returnUrl에서 받은 paymentId

        HttpEntity<Map<String, Object>> entity = new HttpEntity<>(body, headers);
        restTemplate.postForEntity(NAVER_APPLY_URL, entity, Map.class);

        // 네이버 승인 성공 시 "SUCCESS" 상태 반환
        return PaymentConfirmResponse.builder()
                .status("SUCCESS")
                .build();
    }

    // 네이버 결제 취소 API 호출
    @Override
    public void cancel(PaymentCancelRequest request) {
        HttpHeaders headers = new HttpHeaders();
        headers.set("X-Naver-Client-Id", clientId);
        headers.set("X-Naver-Client-Secret", clientSecret);
        headers.setContentType(MediaType.APPLICATION_JSON);

        Map<String, Object> body = new HashMap<>();
        body.put("paymentId", request.getPaymentId());         // 취소할 paymentId
        body.put("cancelAmount", request.getCancelAmount());    // 취소 금액
        body.put("cancelReason", request.getCancelReason());    // 취소 사유

        HttpEntity<Map<String, Object>> entity = new HttpEntity<>(body, headers);
        restTemplate.postForEntity(NAVER_CANCEL_URL, entity, Map.class);
    }
}