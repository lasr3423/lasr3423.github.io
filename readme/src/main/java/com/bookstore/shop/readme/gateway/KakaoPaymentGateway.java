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
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;

import java.util.Map;

@Component
@RequiredArgsConstructor
public class KakaoPaymentGateway implements PaymentGateway {

    @Value("${payment.kakao.admin-key}")
    private String adminKey;

    @Value("${payment.kakao.cid}")
    private String cid;

    private final RestTemplate restTemplate;

    private static final String KAKAO_READY_URL = "https://kapi.kakao.com/v1/payment/ready";
    private static final String KAKAO_APPROVE_URL = "https://kapi.kakao.com/v1/payment/approve";
    private static final String KAKAO_CANCEL_URL = "https://kapi.kakao.com/v1/payment/cancel";

    @Override
    public PaymentReadyResponse ready(PaymentReadyRequest request) {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);
        headers.set("Authorization", "KakaoAK " + adminKey);

        MultiValueMap<String, String> body = new LinkedMultiValueMap<>();
        body.add("cid", cid);
        body.add("partner_order_id", String.valueOf(request.getOrderId()));
        body.add("partner_user_id", String.valueOf(request.getMemberId()));
        body.add("item_name", request.getItemName());
        body.add("quantity", "1");
        body.add("total_amount", String.valueOf(request.getAmount()));
        body.add("tax_free_amount", "0");
        body.add("approval_url", request.getApprovalUrl());
        body.add("cancel_url", request.getCancelUrl());
        body.add("fail_url", request.getFailUrl());

        HttpEntity<MultiValueMap<String, String>> entity = new HttpEntity<>(body, headers);
        ResponseEntity<Map> response = restTemplate.postForEntity(KAKAO_READY_URL, entity, Map.class);
        Map<String, Object> result = response.getBody();

        return PaymentReadyResponse.builder()
                .tid((String) result.get("tid"))
                .redirectPcUrl((String) result.get("next_redirect_pc_url"))
                .redirectMobileUrl((String) result.get("next_redirect_mobile_url"))
                .build();
    }

    @Override
    public PaymentConfirmResponse confirm(PaymentConfirmRequest request) {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);
        headers.set("Authorization", "KakaoAK " + adminKey);

        MultiValueMap<String, String> body = new LinkedMultiValueMap<>();
        body.add("cid", cid);
        body.add("tid", request.getTid());
        body.add("partner_order_id", String.valueOf(request.getOrderId()));
        body.add("partner_user_id", String.valueOf(request.getMemberId()));
        body.add("pg_token", request.getPgToken());

        HttpEntity<MultiValueMap<String, String>> entity = new HttpEntity<>(body, headers);
        restTemplate.postForEntity(KAKAO_APPROVE_URL, entity, Map.class);

        return PaymentConfirmResponse.builder()
                .status("DONE")
                .build();
    }

    @Override
    public void cancel(PaymentCancelRequest request) {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);
        headers.set("Authorization", "KakaoAK " + adminKey);

        MultiValueMap<String, String> body = new LinkedMultiValueMap<>();
        body.add("cid", cid);
        body.add("tid", request.getTid());
        body.add("cancel_amount", String.valueOf(request.getCancelAmount()));
        body.add("cancel_tax_free_amount", "0");

        HttpEntity<MultiValueMap<String, String>> entity = new HttpEntity<>(body, headers);
        restTemplate.postForEntity(KAKAO_CANCEL_URL, entity, Map.class);
    }
}
