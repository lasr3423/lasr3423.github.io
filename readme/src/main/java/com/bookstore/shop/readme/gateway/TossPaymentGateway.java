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

import java.util.Base64;
import java.util.HashMap;
import java.util.Map;

// @Component : 스프링 빈으로 등록 — PaymentService에서 주입받아 사용
// @RequiredArgsConstructor : final 필드(restTemplate)에 대한 생성자 주입 자동 생성
@Component
@RequiredArgsConstructor
public class TossPaymentGateway implements PaymentGateway {

    // @Value : application.yaml의 toss.secret-key 값을 주입받음
    // ⚠️ application.yaml에 값이 없으면 애플리케이션 시작 시 오류 발생
    @Value("${toss.secret-key}")
    private String secretKey;

    // RestTemplateConfig에서 @Bean으로 등록한 RestTemplate 주입
    private final RestTemplate restTemplate;

    // 토스 결제 승인 API URL — paymentKey, orderId, amount를 POST body로 전송
    private static final String TOSS_CONFIRM_URL = "https://api.tosspayments.com/v1/payments/confirm";

    // 토스 결제 취소 API URL — {paymentKey} 부분을 실제 값으로 교체해서 호출
    private static final String TOSS_CANCEL_URL  = "https://api.tosspayments.com/v1/payments/{paymentKey}/cancel";

    // 토스는 프론트 SDK에서 직접 결제창을 열므로 서버 ready() 불필요
    // 호출 시 UnsupportedOperationException 발생 → PaymentService에서 토스에 ready() 호출 금지
    @Override
    public PaymentReadyResponse ready(PaymentReadyRequest request) {
        throw new UnsupportedOperationException("토스페이먼츠는 ready()를 사용하지 않습니다.");
    }

    // 토스 결제 최종 승인 — 프론트 successUrl 이후 호출
    @Override
    public PaymentConfirmResponse confirm(PaymentConfirmRequest request) {
        // Authorization 헤더에 Basic 인증 설정 (secretKey + ":" 를 Base64 인코딩)
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.set("Authorization", buildBasicAuthHeader());

        // 토스 confirm API body: paymentKey, orderId(String), amount 필수
        // orderId는 토스에서 String 타입 → String.valueOf()로 변환
        Map<String, Object> body = new HashMap<>();
        body.put("paymentKey", request.getPaymentKey());
        body.put("orderId", String.valueOf(request.getOrderId()));
        body.put("amount", request.getAmount());

        // HTTP 요청 생성: body + headers 묶음
        HttpEntity<Map<String, Object>> entity = new HttpEntity<>(body, headers);

        // 토스 API 호출 — Map으로 응답받아 필요한 필드만 추출
        ResponseEntity<Map> response = restTemplate.postForEntity(TOSS_CONFIRM_URL, entity, Map.class);

        // 응답에서 paymentKey와 status 추출 → PaymentConfirmResponse로 빌드
        Map<String, Object> result = response.getBody();
        return PaymentConfirmResponse.builder()
                .paymentKey((String) result.get("paymentKey"))  // 취소 API 호출 시 필요
                .status((String) result.get("status"))          // "DONE"이면 성공
                .build();
    }

    // 토스 결제 취소 API 호출
    @Override
    public void cancel(PaymentCancelRequest request) {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.set("Authorization", buildBasicAuthHeader());

        // 취소 사유 필수 — 부분취소 시 cancelAmount도 body에 추가
        Map<String, Object> body = new HashMap<>();
        body.put("cancelReason", request.getCancelReason());

        HttpEntity<Map<String, Object>> entity = new HttpEntity<>(body, headers);

        // URL의 {paymentKey} 를 실제 값으로 교체
        String url = TOSS_CANCEL_URL.replace("{paymentKey}", request.getPaymentKey());
        restTemplate.postForEntity(url, entity, Map.class);
    }

    // 토스 인증 헤더 생성 메서드
    // 토스 인증 방식: Basic 인증 — "secretKey:" 를 Base64로 인코딩
    // 콜론(:)을 반드시 뒤에 붙여야 함 (username:password 형식, password는 공백)
    private String buildBasicAuthHeader() {
        String credentials = secretKey + ":";
        String encoded = Base64.getEncoder().encodeToString(credentials.getBytes());
        return "Basic " + encoded;
    }
}