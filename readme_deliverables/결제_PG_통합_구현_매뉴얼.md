# 결제 PG 통합 구현 매뉴얼

> 프로젝트: README 온라인 도서 쇼핑몰
> 기술 스택: Java 21 / Spring Boot 3.5 / PostgreSQL / Vue.js 3
> 결제 방식: 토스페이먼츠(필수) / 카카오페이 / 네이버페이
> 패턴: Strategy Pattern (PaymentGateway 인터페이스 + 구현체 3개)

---

## 📋 목차

1. [전체 구조 개요](#1-전체-구조-개요)
2. [외부 서비스 사전 설정](#2-외부-서비스-사전-설정)
3. [의존성 및 환경 설정](#3-의존성-및-환경-설정)
4. [DB 설계](#4-db-설계)
5. [Domain 계층](#5-domain-계층)
6. [DTO 계층](#6-dto-계층)
7. [Repository 계층](#7-repository-계층)
8. [Gateway 계층 — PaymentGateway 인터페이스 & 구현체](#8-gateway-계층)
9. [Service 계층](#9-service-계층)
10. [Controller 계층](#10-controller-계층)
11. [Config 계층](#11-config-계층)
12. [프론트엔드 구현 (Vue.js 3)](#12-프론트엔드-구현)
13. [PG별 전체 흐름 정리](#13-pg별-전체-흐름-정리)
14. [구현 순서 체크리스트](#14-구현-순서-체크리스트)

---

## 1. 전체 구조 개요

### 1-1. PG별 결제 흐름 비교

```
┌────────────────────────────────────────────────────────────────────┐
│  토스페이먼츠  프론트 SDK에서 직접 결제창 오픈                           │
│               → 결제 성공 시 successUrl로 리다이렉트                   │
│               → 백엔드 /confirm 호출 → 토스 API 최종 승인              │
│                                                                    │
│  카카오페이    백엔드에서 /ready 호출 → tid + redirectUrl 응답           │
│               → 프론트에서 카카오 결제창으로 리다이렉트                   │
│               → 결제 성공 시 approval_url에 pg_token 포함 리다이렉트     │
│               → 백엔드 /approve 호출 → 카카오 API 최종 승인             │
│                                                                    │
│  네이버페이    백엔드에서 결제 준비 → paymentId 발급                     │
│               → 프론트 JS SDK로 네이버 결제창 오픈                     │
│               → 결제 성공 시 returnUrl에 paymentId 포함 리다이렉트       │
│               → 백엔드 /approve 호출 → 네이버 API 최종 승인             │
│                                                                    │
│  ▶ 세 방식 모두 최종적으로 payment_status = PAID 로 동일하게 처리        │
│  ▶ PaymentGateway 인터페이스로 구현체를 교체 가능한 전략 패턴 적용        │
└────────────────────────────────────────────────────────────────────┘
```

### 1-2. 백엔드 패키지 구조 (결제 관련)

```
com.bookstore.shop.readme/
├── controller/
│   └── MyPaymentApiController.java     ← ready / confirm / approve / fail
├── domain/
│   ├── Payment.java                    ← 결제 엔티티 (v1.2)
│   ├── PaymentStatus.java              ← READY / PAID / CANCELLED / FAILED
│   └── PaymentProvider.java            ← TOSS / KAKAO / NAVER
├── dto/
│   ├── request/
│   │   ├── PaymentReadyRequest.java
│   │   ├── PaymentConfirmRequest.java
│   │   ├── PaymentApproveRequest.java
│   │   ├── PaymentFailRequest.java
│   │   └── PaymentCancelRequest.java
│   └── response/
│       ├── PaymentReadyResponse.java
│       └── PaymentConfirmResponse.java
├── gateway/                            ← 전략 패턴 핵심
│   ├── PaymentGateway.java             ← 인터페이스
│   ├── TossPaymentGateway.java
│   ├── KakaoPaymentGateway.java
│   └── NaverPaymentGateway.java
├── repository/
│   └── PaymentRepository.java
└── service/
    └── PaymentService.java
```

### 1-3. API 엔드포인트 정리

| 메서드 | URL | 설명 | 사용 PG |
|--------|-----|------|---------|
| POST | /order/payment/ready | 결제 준비 (tid/paymentId 발급) | 카카오, 네이버 |
| POST | /order/payment/confirm | 결제 최종 승인 | 토스 |
| POST | /order/payment/approve | 결제 최종 승인 | 카카오, 네이버 |
| POST | /order/payment/fail | 결제 실패 처리 | 토스 |
| PATCH | /mypage/order/{id}/cancel | 결제 취소 / 환불 | 전체 |

---

## 2. 외부 서비스 사전 설정

### 2-1. 토스페이먼츠 개발자 센터

1. [developers.tosspayments.com](https://developers.tosspayments.com) 접속 → 회원가입
2. `테스트` 환경 선택
3. 내 개발 정보에서 아래 두 키 복사

```
클라이언트 키 (clientKey)  → test_ck_로 시작  → 프론트엔드에서 사용
시크릿 키 (secretKey)     → test_sk_로 시작  → 백엔드에서만 사용 (절대 노출 금지)
```

4. `결제창` → `successUrl`, `failUrl` 은 코드에서 직접 지정 (콘솔 등록 불필요)

> ⚠️ 실서비스 전환 시 `live_` 키로 교체하고 사업자 인증 필요

---

### 2-2. 카카오페이 개발자 센터

1. [developers.kakao.com](https://developers.kakao.com) 접속 → 로그인
2. `내 애플리케이션` → `애플리케이션 추가`
3. **Admin 키** 복사 (REST API 키가 아닌 Admin 키)
4. `제품 설정` → `카카오페이` → 활성화
5. 테스트용 CID: `TC0ONETIME` (단건결제), `TCSUBSCRIP` (정기결제) — 별도 등록 불필요

```
Admin 키    → 백엔드 Authorization 헤더에 사용
cid         → TC0ONETIME (테스트), 실서비스 시 카카오 심사 후 발급
```

> ⚠️ 실서비스 시 카카오 비즈니스 채널 연결 및 검수 필요

---

### 2-3. 네이버페이 개발자 센터

1. [developer.pay.naver.com](https://developer.pay.naver.com) 접속
2. 샌드박스 신청 (사업자 등록 전 테스트 가능)
3. 발급되는 키:

```
clientId      → 가맹점 식별자
clientSecret  → 서명 생성용 시크릿
chainId       → 결제 수단 식별자
```

> ⚠️ 네이버페이는 샌드박스 신청 시 1~2 영업일 소요. 일정이 빠듯하면 토스/카카오 먼저 구현 권장.

---

## 3. 의존성 및 환경 설정

### 3-1. build.gradle 추가 의존성

기존 build.gradle에 아래 항목 추가 (JWT, Validation은 이미 추가됨):

```groovy
dependencies {
    // 기존 의존성 유지 ...

    // HTTP 통신 (PG API 호출용)
    implementation 'org.springframework.boot:spring-boot-starter-webflux'
    // 또는 RestTemplate만 사용하면 별도 추가 불필요 (spring-boot-starter-web에 포함)
}
```

> RestTemplate은 `spring-boot-starter-web`에 포함되어 있으므로 별도 추가 불필요.
> WebClient가 필요한 경우에만 `spring-boot-starter-webflux` 추가.

---

### 3-2. application.yaml 추가 설정

```yaml
# 기존 설정 유지...

# 토스페이먼츠
toss:
  secret-key: test_sk_여기에_시크릿키_입력
  client-key: test_ck_여기에_클라이언트키_입력

# 카카오페이
kakao:
  admin-key: 여기에_어드민키_입력
  cid: TC0ONETIME

# 네이버페이
naver:
  client-id: 여기에_클라이언트ID_입력
  client-secret: 여기에_클라이언트_시크릿_입력
  chain-id: 여기에_체인ID_입력
```

> ⚠️ 실제 키 값은 절대 Git에 커밋하지 말 것.
> 팀 공유는 카카오톡 / 노션 등으로 별도 전달.

---

## 4. DB 설계

### 4-1. payment 테이블 DDL (v1.2)

```sql
CREATE TABLE payment (
    id                BIGSERIAL       PRIMARY KEY,
    order_id          BIGINT          NOT NULL REFERENCES "order"(id),
    method            VARCHAR(30),
    provider          VARCHAR(20),                    -- TOSS / KAKAO / NAVER
    payment_status    VARCHAR(20)     NOT NULL DEFAULT 'READY',
    amount            INTEGER         NOT NULL,
    pg_tid            VARCHAR(200),                   -- 카카오/네이버 거래 ID
    payment_key       VARCHAR(200),                   -- 토스 paymentKey
    installment_months SMALLINT       DEFAULT 0,      -- 할부 개월 수
    paid_at           TIMESTAMP,
    cancel_reason     VARCHAR(300),
    failure_reason    VARCHAR(300),                   -- 실패 사유
    cancelled_at      TIMESTAMP,
    created_at        TIMESTAMP       NOT NULL DEFAULT now(),
    updated_at        TIMESTAMP
);
```

---

## 5. Domain 계층

### 5-1. PaymentStatus.java

```java
package com.bookstore.shop.readme.domain;

public enum PaymentStatus {
    READY,       // 결제 준비
    PAID,        // 결제 완료
    CANCELLED,   // 취소/환불 완료
    FAILED       // 결제 실패
}
```

### 5-2. PaymentProvider.java

```java
package com.bookstore.shop.readme.domain;

public enum PaymentProvider {
    TOSS,
    KAKAO,
    NAVER
}
```

### 5-3. Payment.java

```java
package com.bookstore.shop.readme.domain;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "payment")
@Getter @Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Payment extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "order_id", nullable = false)
    private Order order;

    @Column(length = 30)
    private String method;                      // 결제 수단 (카드, 간편결제 등)

    @Enumerated(EnumType.STRING)
    @Column(length = 20)
    private PaymentProvider provider;           // TOSS / KAKAO / NAVER

    @Enumerated(EnumType.STRING)
    @Column(name = "payment_status", length = 20, nullable = false)
    private PaymentStatus paymentStatus;

    @Column(nullable = false)
    private Integer amount;

    @Column(name = "pg_tid", length = 200)
    private String pgTid;                       // 카카오/네이버 거래 ID

    @Column(name = "payment_key", length = 200)
    private String paymentKey;                  // 토스 paymentKey

    @Column(name = "installment_months")
    private Integer installmentMonths;          // 할부 개월

    @Column(name = "paid_at")
    private LocalDateTime paidAt;

    @Column(name = "cancel_reason", length = 300)
    private String cancelReason;

    @Column(name = "failure_reason", length = 300)
    private String failureReason;               // 실패 사유

    @Column(name = "cancelled_at")
    private LocalDateTime cancelledAt;
}
```

---

## 6. DTO 계층

### 6-1. PaymentReadyRequest.java

```java
package com.bookstore.shop.readme.dto.request;

import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class PaymentReadyRequest {
    private Long orderId;
    private String provider;    // "KAKAO" or "NAVER"
    private String approvalUrl; // 성공 리다이렉트 URL
    private String cancelUrl;   // 취소 리다이렉트 URL
    private String failUrl;     // 실패 리다이렉트 URL
}
```

### 6-2. PaymentReadyResponse.java

```java
package com.bookstore.shop.readme.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PaymentReadyResponse {
    private String tid;                // 카카오 거래 고유번호
    private String redirectPcUrl;      // 카카오 PC 결제창 URL
    private String redirectMobileUrl;  // 카카오 모바일 결제창 URL
    private String paymentId;          // 네이버페이 결제 ID
    private String redirectUrl;        // 네이버페이 결제창 URL
}
```

### 6-3. PaymentConfirmRequest.java (토스)

```java
package com.bookstore.shop.readme.dto.request;

import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class PaymentConfirmRequest {
    private String paymentKey;  // 토스에서 successUrl로 전달
    private Long orderId;
    private Integer amount;
}
```

### 6-4. PaymentApproveRequest.java (카카오/네이버)

```java
package com.bookstore.shop.readme.dto.request;

import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class PaymentApproveRequest {
    private Long orderId;
    private String tid;         // 카카오 거래 ID
    private String pgToken;     // 카카오 pg_token
    private String paymentId;   // 네이버 paymentId
    private String resultCode;  // 네이버 resultCode
    private String provider;    // "KAKAO" or "NAVER"
}
```

### 6-5. PaymentFailRequest.java (토스)

```java
package com.bookstore.shop.readme.dto.request;

import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class PaymentFailRequest {
    private Long orderId;
    private String code;
    private String message;
}
```

### 6-6. PaymentCancelRequest.java (내부 DTO)

```java
package com.bookstore.shop.readme.dto.request;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class PaymentCancelRequest {
    private String paymentKey;   // 토스
    private String tid;          // 카카오
    private String paymentId;    // 네이버
    private Integer cancelAmount;
    private String cancelReason;
    private String provider;
}
```

---

## 7. Repository 계층

### 7-1. PaymentRepository.java

```java
package com.bookstore.shop.readme.repository;

import com.bookstore.shop.readme.domain.Payment;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface PaymentRepository extends JpaRepository<Payment, Long> {
    Optional<Payment> findByOrderId(Long orderId);
}
```

---

## 8. Gateway 계층

### 8-1. PaymentGateway.java (인터페이스)

```java
package com.bookstore.shop.readme.gateway;

import com.bookstore.shop.readme.dto.request.PaymentCancelRequest;
import com.bookstore.shop.readme.dto.request.PaymentConfirmRequest;
import com.bookstore.shop.readme.dto.request.PaymentReadyRequest;
import com.bookstore.shop.readme.dto.response.PaymentConfirmResponse;
import com.bookstore.shop.readme.dto.response.PaymentReadyResponse;

public interface PaymentGateway {
    PaymentReadyResponse ready(PaymentReadyRequest request);
    PaymentConfirmResponse confirm(PaymentConfirmRequest request);
    void cancel(PaymentCancelRequest request);
}
```

---

### 8-2. TossPaymentGateway.java

```java
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

import java.util.Base64;
import java.util.HashMap;
import java.util.Map;

@Component
@RequiredArgsConstructor
public class TossPaymentGateway implements PaymentGateway {

    @Value("${toss.secret-key}")
    private String secretKey;

    private final RestTemplate restTemplate;

    private static final String TOSS_CONFIRM_URL = "https://api.tosspayments.com/v1/payments/confirm";
    private static final String TOSS_CANCEL_URL  = "https://api.tosspayments.com/v1/payments/{paymentKey}/cancel";

    // 토스는 프론트 SDK에서 직접 결제창을 열므로 ready()는 사용하지 않음
    @Override
    public PaymentReadyResponse ready(PaymentReadyRequest request) {
        throw new UnsupportedOperationException("토스페이먼츠는 ready()를 사용하지 않습니다.");
    }

    @Override
    public PaymentConfirmResponse confirm(PaymentConfirmRequest request) {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.set("Authorization", buildBasicAuthHeader());

        Map<String, Object> body = new HashMap<>();
        body.put("paymentKey", request.getPaymentKey());
        body.put("orderId", String.valueOf(request.getOrderId()));
        body.put("amount", request.getAmount());

        HttpEntity<Map<String, Object>> entity = new HttpEntity<>(body, headers);

        ResponseEntity<Map> response = restTemplate.postForEntity(TOSS_CONFIRM_URL, entity, Map.class);

        Map<String, Object> result = response.getBody();
        return PaymentConfirmResponse.builder()
                .paymentKey((String) result.get("paymentKey"))
                .status((String) result.get("status"))
                .build();
    }

    @Override
    public void cancel(PaymentCancelRequest request) {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.set("Authorization", buildBasicAuthHeader());

        Map<String, Object> body = new HashMap<>();
        body.put("cancelReason", request.getCancelReason());

        HttpEntity<Map<String, Object>> entity = new HttpEntity<>(body, headers);

        String url = TOSS_CANCEL_URL.replace("{paymentKey}", request.getPaymentKey());
        restTemplate.postForEntity(url, entity, Map.class);
    }

    // secretKey 뒤에 콜론(:) 붙여서 Base64 인코딩
    private String buildBasicAuthHeader() {
        String credentials = secretKey + ":";
        String encoded = Base64.getEncoder().encodeToString(credentials.getBytes());
        return "Basic " + encoded;
    }
}
```

---

### 8-3. KakaoPaymentGateway.java

```java
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

    @Value("${kakao.admin-key}")
    private String adminKey;

    @Value("${kakao.cid}")
    private String cid;

    private final RestTemplate restTemplate;

    private static final String KAKAO_READY_URL   = "https://kapi.kakao.com/v1/payment/ready";
    private static final String KAKAO_APPROVE_URL = "https://kapi.kakao.com/v1/payment/approve";
    private static final String KAKAO_CANCEL_URL  = "https://kapi.kakao.com/v1/payment/cancel";

    @Override
    public PaymentReadyResponse ready(PaymentReadyRequest request) {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.set("Authorization", "KakaoAK " + adminKey);

        Map<String, Object> body = new HashMap<>();
        body.put("cid", cid);
        body.put("partner_order_id", String.valueOf(request.getOrderId()));
        body.put("partner_user_id", String.valueOf(request.getMemberId()));
        body.put("item_name", request.getItemName());
        body.put("quantity", 1);
        body.put("total_amount", request.getAmount());
        body.put("tax_free_amount", 0);
        body.put("approval_url", request.getApprovalUrl());
        body.put("cancel_url", request.getCancelUrl());
        body.put("fail_url", request.getFailUrl());

        HttpEntity<Map<String, Object>> entity = new HttpEntity<>(body, headers);
        ResponseEntity<Map> response = restTemplate.postForEntity(KAKAO_READY_URL, entity, Map.class);
        Map<String, Object> result = response.getBody();

        return PaymentReadyResponse.builder()
                .tid((String) result.get("tid"))
                .redirectPcUrl((String) result.get("next_redirect_pc_url"))
                .redirectMobileUrl((String) result.get("next_redirect_mobile_url"))
                .build();
    }

    // confirm() — 카카오는 approve 흐름 사용, confirm()은 내부에서 approve로 위임
    @Override
    public PaymentConfirmResponse confirm(PaymentConfirmRequest request) {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.set("Authorization", "KakaoAK " + adminKey);

        Map<String, Object> body = new HashMap<>();
        body.put("cid", cid);
        body.put("tid", request.getTid());
        body.put("partner_order_id", String.valueOf(request.getOrderId()));
        body.put("partner_user_id", String.valueOf(request.getMemberId()));
        body.put("pg_token", request.getPgToken());

        HttpEntity<Map<String, Object>> entity = new HttpEntity<>(body, headers);
        restTemplate.postForEntity(KAKAO_APPROVE_URL, entity, Map.class);

        return PaymentConfirmResponse.builder()
                .status("DONE")
                .build();
    }

    @Override
    public void cancel(PaymentCancelRequest request) {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.set("Authorization", "KakaoAK " + adminKey);

        Map<String, Object> body = new HashMap<>();
        body.put("cid", cid);
        body.put("tid", request.getTid());
        body.put("cancel_amount", request.getCancelAmount());
        body.put("cancel_tax_free_amount", 0);

        HttpEntity<Map<String, Object>> entity = new HttpEntity<>(body, headers);
        restTemplate.postForEntity(KAKAO_CANCEL_URL, entity, Map.class);
    }
}
```

---

### 8-4. NaverPaymentGateway.java

```java
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

    @Value("${naver.client-id}")
    private String clientId;

    @Value("${naver.client-secret}")
    private String clientSecret;

    @Value("${naver.chain-id}")
    private String chainId;

    private final RestTemplate restTemplate;

    private static final String NAVER_APPLY_URL  = "https://dev.apis.naver.com/naverpay-partner/naverpay/payments/v2.2/apply";
    private static final String NAVER_CANCEL_URL = "https://dev.apis.naver.com/naverpay-partner/naverpay/payments/v1/cancel";

    @Override
    public PaymentReadyResponse ready(PaymentReadyRequest request) {
        // 네이버페이는 프론트 JS SDK에서 paymentId를 받아 처리
        // 백엔드 ready()는 결제 정보를 저장하고 paymentId를 반환
        return PaymentReadyResponse.builder()
                .paymentId(String.valueOf(request.getOrderId()))  // 실제 연동 시 네이버 API 호출로 대체
                .build();
    }

    @Override
    public PaymentConfirmResponse confirm(PaymentConfirmRequest request) {
        HttpHeaders headers = new HttpHeaders();
        headers.set("X-Naver-Client-Id", clientId);
        headers.set("X-Naver-Client-Secret", clientSecret);
        headers.setContentType(MediaType.APPLICATION_JSON);

        Map<String, Object> body = new HashMap<>();
        body.put("paymentId", request.getPaymentId());

        HttpEntity<Map<String, Object>> entity = new HttpEntity<>(body, headers);
        restTemplate.postForEntity(NAVER_APPLY_URL, entity, Map.class);

        return PaymentConfirmResponse.builder()
                .status("SUCCESS")
                .build();
    }

    @Override
    public void cancel(PaymentCancelRequest request) {
        HttpHeaders headers = new HttpHeaders();
        headers.set("X-Naver-Client-Id", clientId);
        headers.set("X-Naver-Client-Secret", clientSecret);
        headers.setContentType(MediaType.APPLICATION_JSON);

        Map<String, Object> body = new HashMap<>();
        body.put("paymentId", request.getPaymentId());
        body.put("cancelAmount", request.getCancelAmount());
        body.put("cancelReason", request.getCancelReason());

        HttpEntity<Map<String, Object>> entity = new HttpEntity<>(body, headers);
        restTemplate.postForEntity(NAVER_CANCEL_URL, entity, Map.class);
    }
}
```

---

## 9. Service 계층

### 9-1. PaymentService.java

```java
package com.bookstore.shop.readme.service;

import com.bookstore.shop.readme.domain.*;
import com.bookstore.shop.readme.dto.request.*;
import com.bookstore.shop.readme.dto.response.*;
import com.bookstore.shop.readme.gateway.*;
import com.bookstore.shop.readme.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class PaymentService {

    private final PaymentRepository paymentRepository;
    private final OrderRepository orderRepository;
    private final TossPaymentGateway tossPaymentGateway;
    private final KakaoPaymentGateway kakaoPaymentGateway;
    private final NaverPaymentGateway naverPaymentGateway;

    // ─── 카카오/네이버 결제 준비 ───────────────────────────────────────────

    @Transactional
    public PaymentReadyResponse requestReady(PaymentReadyRequest request, Long memberId) {
        Order order = orderRepository.findByIdAndMemberId(request.getOrderId(), memberId)
                .orElseThrow(() -> new RuntimeException("주문을 찾을 수 없습니다."));

        PaymentGateway gateway = resolveGateway(request.getProvider());
        PaymentReadyResponse response = gateway.ready(request);

        // READY 상태로 결제 레코드 생성
        Payment payment = Payment.builder()
                .order(order)
                .provider(PaymentProvider.valueOf(request.getProvider()))
                .paymentStatus(PaymentStatus.READY)
                .amount(order.getFinalPrice())
                .pgTid(response.getTid() != null ? response.getTid() : response.getPaymentId())
                .build();
        paymentRepository.save(payment);

        return response;
    }

    // ─── 토스 결제 최종 승인 (/confirm) ──────────────────────────────────

    @Transactional
    public void confirmToss(PaymentConfirmRequest request, Long memberId) {
        Order order = orderRepository.findByIdAndMemberId(request.getOrderId(), memberId)
                .orElseThrow(() -> new RuntimeException("주문을 찾을 수 없습니다."));

        // 서버 측 금액 검증 (탬퍼링 방지)
        validateAmount(request.getOrderId(), request.getAmount());

        PaymentConfirmResponse response = tossPaymentGateway.confirm(request);

        // 결제 완료 처리
        Payment payment = paymentRepository.findByOrderId(request.getOrderId())
                .orElse(Payment.builder()
                        .order(order)
                        .provider(PaymentProvider.TOSS)
                        .amount(request.getAmount())
                        .build());

        payment.setPaymentKey(request.getPaymentKey());
        payment.setPaymentStatus(PaymentStatus.PAID);
        payment.setPaidAt(LocalDateTime.now());
        paymentRepository.save(payment);

        order.setOrderStatus(OrderStatus.PAYED);
        orderRepository.save(order);
    }

    // ─── 카카오/네이버 결제 최종 승인 (/approve) ─────────────────────────

    @Transactional
    public void approvePayment(PaymentApproveRequest request, Long memberId) {
        Order order = orderRepository.findByIdAndMemberId(request.getOrderId(), memberId)
                .orElseThrow(() -> new RuntimeException("주문을 찾을 수 없습니다."));

        Payment payment = paymentRepository.findByOrderId(request.getOrderId())
                .orElseThrow(() -> new RuntimeException("결제 정보를 찾을 수 없습니다."));

        validateAmount(request.getOrderId(), payment.getAmount());

        // provider 분기 처리
        PaymentConfirmRequest confirmRequest = PaymentConfirmRequest.builder()
                .orderId(request.getOrderId())
                .memberId(memberId)
                .tid(request.getTid())
                .pgToken(request.getPgToken())
                .paymentId(request.getPaymentId())
                .build();

        PaymentGateway gateway = resolveGateway(request.getProvider());
        gateway.confirm(confirmRequest);

        payment.setPaymentStatus(PaymentStatus.PAID);
        payment.setPaidAt(LocalDateTime.now());
        paymentRepository.save(payment);

        order.setOrderStatus(OrderStatus.PAYED);
        orderRepository.save(order);
    }

    // ─── 결제 실패 처리 (토스 failUrl) ───────────────────────────────────

    @Transactional
    public void failPayment(PaymentFailRequest request) {
        paymentRepository.findByOrderId(request.getOrderId()).ifPresent(payment -> {
            payment.setPaymentStatus(PaymentStatus.FAILED);
            payment.setFailureReason(request.getMessage());
            paymentRepository.save(payment);
        });
    }

    // ─── 결제 취소 / 환불 ─────────────────────────────────────────────────

    @Transactional
    public void cancelPayment(Long orderId, String cancelReason) {
        Payment payment = paymentRepository.findByOrderId(orderId)
                .orElseThrow(() -> new RuntimeException("결제 정보를 찾을 수 없습니다."));

        PaymentCancelRequest cancelRequest = PaymentCancelRequest.builder()
                .paymentKey(payment.getPaymentKey())
                .tid(payment.getPgTid())
                .paymentId(payment.getPgTid())
                .cancelAmount(payment.getAmount())
                .cancelReason(cancelReason)
                .provider(payment.getProvider().name())
                .build();

        PaymentGateway gateway = resolveGateway(payment.getProvider().name());
        gateway.cancel(cancelRequest);

        payment.setPaymentStatus(PaymentStatus.CANCELLED);
        payment.setCancelReason(cancelReason);
        payment.setCancelledAt(LocalDateTime.now());
        paymentRepository.save(payment);
    }

    // ─── 내부 유틸 ────────────────────────────────────────────────────────

    private void validateAmount(Long orderId, Integer amount) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new RuntimeException("주문을 찾을 수 없습니다."));
        if (!order.getFinalPrice().equals(amount)) {
            throw new RuntimeException("결제 금액이 일치하지 않습니다.");
        }
    }

    private PaymentGateway resolveGateway(String provider) {
        return switch (provider.toUpperCase()) {
            case "TOSS"  -> tossPaymentGateway;
            case "KAKAO" -> kakaoPaymentGateway;
            case "NAVER" -> naverPaymentGateway;
            default -> throw new RuntimeException("지원하지 않는 결제 수단입니다: " + provider);
        };
    }
}
```

---

## 10. Controller 계층

### 10-1. MyPaymentApiController.java

```java
package com.bookstore.shop.readme.controller;

import com.bookstore.shop.readme.domain.Member;
import com.bookstore.shop.readme.dto.request.*;
import com.bookstore.shop.readme.dto.response.*;
import com.bookstore.shop.readme.service.PaymentService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/order/payment")
public class MyPaymentApiController {

    private final PaymentService paymentService;

    // 카카오 / 네이버 결제 준비
    @PostMapping("/ready")
    public ResponseEntity<PaymentReadyResponse> readyPayment(
            @RequestBody PaymentReadyRequest request,
            @AuthenticationPrincipal Member member) {

        PaymentReadyResponse response = paymentService.requestReady(request, member.getId());
        return ResponseEntity.ok(response);
    }

    // 토스 결제 최종 승인 (successUrl 리다이렉트 후 호출)
    @PostMapping("/confirm")
    public ResponseEntity<String> confirmPayment(
            @RequestBody PaymentConfirmRequest request,
            @AuthenticationPrincipal Member member) {

        paymentService.confirmToss(request, member.getId());
        return ResponseEntity.ok("결제가 완료되었습니다.");
    }

    // 카카오 / 네이버 결제 최종 승인
    @PostMapping("/approve")
    public ResponseEntity<String> approvePayment(
            @RequestBody PaymentApproveRequest request,
            @AuthenticationPrincipal Member member) {

        paymentService.approvePayment(request, member.getId());
        return ResponseEntity.ok("결제가 완료되었습니다.");
    }

    // 토스 결제 실패 처리 (failUrl 리다이렉트 후 호출)
    @PostMapping("/fail")
    public ResponseEntity<String> failPayment(@RequestBody PaymentFailRequest request) {
        paymentService.failPayment(request);
        return ResponseEntity.ok("결제에 실패했습니다.");
    }
}
```

---

## 11. Config 계층

### 11-1. RestTemplateConfig.java

```java
package com.bookstore.shop.readme.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestTemplate;

@Configuration
public class RestTemplateConfig {

    @Bean
    public RestTemplate restTemplate() {
        return new RestTemplate();
    }
}
```

### 11-2. SecurityConfig — 결제 URL 허용 설정

```java
// SecurityConfig.java 의 authorizeHttpRequests 부분에 아래 추가

.requestMatchers("/order/payment/fail").permitAll()  // 토스 failUrl은 비인증 허용
// 나머지 /order/payment/** 는 인증 필요
```

---

## 12. 프론트엔드 구현

### 12-1. 토스페이먼츠 (Vue.js)

**index.html 또는 main.js — SDK 로드**
```html
<!-- public/index.html <head> 안에 추가 -->
<script src="https://js.tosspayments.com/v1/payment"></script>
```

**PaymentView.vue — 토스 결제 요청**
```vue
<template>
  <div>
    <button @click="payWithToss">토스페이먼츠로 결제</button>
  </div>
</template>

<script setup>
import { useOrderStore } from '@/store/order'

const orderStore = useOrderStore()

const payWithToss = () => {
  const clientKey = 'test_ck_여기에_클라이언트키_입력'
  const tossPayments = window.TossPayments(clientKey)

  tossPayments.requestPayment('카드', {
    amount: orderStore.finalPrice,
    orderId: String(orderStore.orderId),        // 토스 orderId는 String
    orderName: '교보문고 도서 주문',
    customerName: '구매자명',
    successUrl: `${window.location.origin}/payment/success`,  // 리다이렉트될 프론트 URL
    failUrl: `${window.location.origin}/payment/fail`,
  })
}
</script>
```

**PaymentSuccessView.vue — 토스 successUrl 처리**
```vue
<script setup>
import { onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { useAuthStore } from '@/store/auth'
import axios from '@/api/axios'

const route = useRoute()
const router = useRouter()
const authStore = useAuthStore()

onMounted(async () => {
  const { paymentKey, orderId, amount } = route.query

  try {
    await axios.post('/order/payment/confirm', {
      paymentKey,
      orderId: Number(orderId),
      amount: Number(amount),
    })
    router.push('/mypage/order')
  } catch (e) {
    alert('결제 승인 중 오류가 발생했습니다.')
    router.push('/cart')
  }
})
</script>
```

**PaymentFailView.vue — 토스 failUrl 처리**
```vue
<script setup>
import { onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import axios from '@/api/axios'

const route = useRoute()
const router = useRouter()

onMounted(async () => {
  const { orderId, code, message } = route.query

  await axios.post('/order/payment/fail', {
    orderId: Number(orderId),
    code,
    message,
  })

  alert(`결제 실패: ${message}`)
  router.push('/cart')
})
</script>
```

---

### 12-2. 카카오페이 (Vue.js)

**PaymentView.vue — 카카오 결제 준비 요청**
```vue
<script setup>
import { useOrderStore } from '@/store/order'
import axios from '@/api/axios'

const orderStore = useOrderStore()

const payWithKakao = async () => {
  try {
    const { data } = await axios.post('/order/payment/ready', {
      orderId: orderStore.orderId,
      provider: 'KAKAO',
      approvalUrl: `${window.location.origin}/payment/kakao/success`,
      cancelUrl: `${window.location.origin}/payment/kakao/cancel`,
      failUrl: `${window.location.origin}/payment/kakao/fail`,
    })

    // PC 환경이면 next_redirect_pc_url, 모바일이면 next_redirect_mobile_url 사용
    const isMobile = /iPhone|Android/i.test(navigator.userAgent)
    const redirectUrl = isMobile ? data.redirectMobileUrl : data.redirectPcUrl

    // 카카오 결제창으로 리다이렉트
    window.location.href = redirectUrl
  } catch (e) {
    alert('결제 준비 중 오류가 발생했습니다.')
  }
}
</script>
```

**KakaoSuccessView.vue — approval_url 처리 (pg_token 수신)**
```vue
<script setup>
import { onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import axios from '@/api/axios'

const route = useRoute()
const router = useRouter()

onMounted(async () => {
  const pgToken = route.query.pg_token
  const orderId = Number(sessionStorage.getItem('currentOrderId'))
  const tid = sessionStorage.getItem('currentTid')

  try {
    await axios.post('/order/payment/approve', {
      orderId,
      tid,
      pgToken,
      provider: 'KAKAO',
    })
    router.push('/mypage/order')
  } catch (e) {
    alert('결제 승인 중 오류가 발생했습니다.')
    router.push('/cart')
  }
})
</script>
```

> 💡 tid는 ready() 응답에서 받아서 sessionStorage 또는 Pinia store에 임시 저장해두고 approval_url 리다이렉트 후 꺼내서 사용합니다.

---

### 12-3. 네이버페이 (Vue.js)

**index.html — 네이버페이 SDK 로드**
```html
<!-- public/index.html <head> 안에 추가 -->
<script src="https://pay.naver.com/customer/js/naverPayButton.js"></script>
```

**PaymentView.vue — 네이버페이 결제**
```vue
<script setup>
import { useOrderStore } from '@/store/order'
import axios from '@/api/axios'

const orderStore = useOrderStore()

const payWithNaver = async () => {
  try {
    // 1. 백엔드에서 결제 준비
    const { data } = await axios.post('/order/payment/ready', {
      orderId: orderStore.orderId,
      provider: 'NAVER',
    })

    const { paymentId } = data

    // 2. 네이버페이 JS SDK로 결제창 오픈
    window.NaverPayButton.apply({
      BUTTON_KEY: '네이버페이에서_발급받은_버튼키',
      TYPE: 'A',
      COLOR: 1,
      COUNT: 2,
      BILLING_TYPE: 'ONE_TIME',
      ENABLE_PC_POPUP: true,
      MERCHANT_USER_ID: String(orderStore.memberId),
      MERCHANT_PAY_KEY: String(orderStore.orderId),
      PRODUCT_NAME: '교보문고 도서 주문',
      TOTAL_PAY_AMOUNT: orderStore.finalPrice,
      TAX_SCOPE_AMOUNT: orderStore.finalPrice,
      TAX_EX_SCOPE_AMOUNT: 0,
      RETURN_URL: `${window.location.origin}/payment/naver/success`,
    })
  } catch (e) {
    alert('결제 준비 중 오류가 발생했습니다.')
  }
}
</script>
```

**NaverSuccessView.vue — returnUrl 처리**
```vue
<script setup>
import { onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import axios from '@/api/axios'

const route = useRoute()
const router = useRouter()

onMounted(async () => {
  const { resultCode, paymentId } = route.query

  if (resultCode !== 'Success') {
    alert('결제에 실패했습니다.')
    router.push('/cart')
    return
  }

  const orderId = Number(sessionStorage.getItem('currentOrderId'))

  try {
    await axios.post('/order/payment/approve', {
      orderId,
      paymentId,
      resultCode,
      provider: 'NAVER',
    })
    router.push('/mypage/order')
  } catch (e) {
    alert('결제 승인 중 오류가 발생했습니다.')
    router.push('/cart')
  }
})
</script>
```

---

### 12-4. Vue Router 설정 추가

```javascript
// router/index.js 에 결제 관련 라우트 추가

{
  path: '/payment/success',
  component: () => import('@/views/payment/PaymentSuccessView.vue'),  // 토스
},
{
  path: '/payment/fail',
  component: () => import('@/views/payment/PaymentFailView.vue'),     // 토스
},
{
  path: '/payment/kakao/success',
  component: () => import('@/views/payment/KakaoSuccessView.vue'),   // 카카오
},
{
  path: '/payment/naver/success',
  component: () => import('@/views/payment/NaverSuccessView.vue'),   // 네이버
},
```

---

## 13. PG별 전체 흐름 정리

### 13-1. 토스페이먼츠 흐름

```
[프론트] 주문서 → 토스 결제 버튼 클릭
    ↓
[프론트] TossPayments SDK.requestPayment() 호출 — 서버 API 없음
    ↓
[토스 결제창] 카드 정보 입력 → 결제 진행
    ↓ 성공 시
[프론트] successUrl 리다이렉트 (?paymentKey=&orderId=&amount=)
    ↓
[프론트→백엔드] POST /order/payment/confirm { paymentKey, orderId, amount }
    ↓
[백엔드] 금액 검증 → TossPaymentGateway.confirm() → 토스 API POST /v1/payments/confirm
    ↓
[백엔드] payment_status = PAID, order_status = PAYED
    ↓
[프론트] 주문 완료 페이지 이동
```

```
    ↓ 실패 시
[프론트] failUrl 리다이렉트 (?code=&message=)
    ↓
[프론트→백엔드] POST /order/payment/fail { orderId, code, message }
    ↓
[백엔드] payment_status = FAILED, failure_reason = message
```

---

### 13-2. 카카오페이 흐름

```
[프론트] 주문서 → 카카오페이 버튼 클릭
    ↓
[프론트→백엔드] POST /order/payment/ready { orderId, provider: "KAKAO", approvalUrl, ... }
    ↓
[백엔드] KakaoPaymentGateway.ready() → 카카오 POST /v1/payment/ready
    ↓
[카카오] tid + next_redirect_pc_url 반환
    ↓
[백엔드] payment(READY, pg_tid=tid) 저장 → 프론트에 { tid, redirectPcUrl } 응답
    ↓
[프론트] tid를 sessionStorage에 저장 → 카카오 결제창 URL로 location.href 이동
    ↓
[카카오 결제창] 카카오계정 로그인 → 결제 진행
    ↓ 성공 시
[프론트] approval_url 리다이렉트 (?pg_token=)
    ↓
[프론트→백엔드] POST /order/payment/approve { orderId, tid, pgToken, provider: "KAKAO" }
    ↓
[백엔드] KakaoPaymentGateway.confirm() → 카카오 POST /v1/payment/approve
    ↓
[백엔드] payment_status = PAID, order_status = PAYED
    ↓
[프론트] 주문 완료 페이지 이동
```

---

### 13-3. 네이버페이 흐름

```
[프론트] 주문서 → 네이버페이 버튼 클릭
    ↓
[프론트→백엔드] POST /order/payment/ready { orderId, provider: "NAVER" }
    ↓
[백엔드] payment(READY) 저장 → paymentId 반환
    ↓
[프론트] NaverPayButton.apply() — JS SDK로 네이버 결제창 오픈
    ↓
[네이버 결제창] 네이버 계정 로그인 → 간편결제 진행
    ↓ 성공 시
[프론트] returnUrl 리다이렉트 (?resultCode=Success&paymentId=)
    ↓
[프론트→백엔드] POST /order/payment/approve { orderId, paymentId, resultCode, provider: "NAVER" }
    ↓
[백엔드] resultCode 검증 → NaverPaymentGateway.confirm() → 네이버 API 승인 호출
    ↓
[백엔드] payment_status = PAID, order_status = PAYED
    ↓
[프론트] 주문 완료 페이지 이동
```

---

## 14. 구현 순서 체크리스트

### STEP 1 — 기반 준비

- [ ] `application.yaml`에 toss / kakao / naver 키 설정
- [ ] `RestTemplateConfig.java` — RestTemplate 빈 등록
- [ ] `PaymentStatus.java` 열거형 작성
- [ ] `PaymentProvider.java` 열거형 작성
- [ ] `Payment.java` 엔티티 작성 (v1.2 필드 포함)
- [ ] DTO 전체 작성 (Request 5개 + Response 2개)
- [ ] `PaymentRepository.java` 작성

### STEP 2 — 토스페이먼츠 (필수)

- [ ] `PaymentGateway.java` 인터페이스 작성
- [ ] `TossPaymentGateway.java` 구현체 작성
- [ ] `PaymentService.java` — `confirmToss()`, `failPayment()`, `cancelPayment()` 구현
- [ ] `MyPaymentApiController.java` — `/confirm`, `/fail` 엔드포인트
- [ ] `SecurityConfig`에 `/order/payment/fail` permitAll 추가
- [ ] Vue.js — `PaymentSuccessView.vue`, `PaymentFailView.vue` 작성
- [ ] Vue Router에 `/payment/success`, `/payment/fail` 라우트 추가
- [ ] Postman + 브라우저로 토스 전체 플로우 테스트

### STEP 3 — 카카오페이

- [ ] `KakaoPaymentGateway.java` 구현체 작성
- [ ] `PaymentService.java` — `requestReady()`, `approvePayment()` 구현
- [ ] `MyPaymentApiController.java` — `/ready`, `/approve` 엔드포인트
- [ ] Vue.js — 카카오 ready 요청 + tid sessionStorage 저장 구현
- [ ] Vue.js — `KakaoSuccessView.vue` 작성
- [ ] Vue Router에 `/payment/kakao/success` 라우트 추가
- [ ] 카카오 전체 플로우 테스트

### STEP 4 — 네이버페이 (시간 여유 시)

- [ ] 네이버페이 샌드박스 키 발급 확인
- [ ] `NaverPaymentGateway.java` 구현체 작성
- [ ] `index.html`에 네이버페이 SDK 스크립트 추가
- [ ] Vue.js — `NaverSuccessView.vue` 작성
- [ ] Vue Router에 `/payment/naver/success` 라우트 추가
- [ ] 네이버 전체 플로우 테스트

### STEP 5 — 결제 취소/환불

- [ ] `PaymentService.cancelPayment()` 전체 구현 (provider 분기 처리)
- [ ] `MyOrderApiController` 주문 취소 API에서 `cancelPayment()` 호출 연결
- [ ] 각 PG별 취소 API 테스트

---

## ⚠️ 주의사항

| 항목 | 내용 |
| --- | --- |
| 키 보안 | secretKey, adminKey, clientSecret 은 절대 Git 커밋 금지 |
| 금액 검증 | 프론트에서 넘어온 amount를 그대로 사용하지 말고 반드시 서버에서 order.finalPrice와 비교 |
| 토스 orderId | 토스 SDK에 넘기는 orderId는 String 타입이어야 함 |
| 카카오 tid | ready() 응답의 tid는 approve() 시 반드시 필요 — sessionStorage 또는 Pinia에 보관 |
| 네이버 샌드박스 | 승인 1~2 영업일 소요 — 일정이 빠듯하면 토스/카카오만 구현 후 발표 |
| CORS | 프론트(Vue.js) 도메인을 SecurityConfig 또는 WebMvcConfigurer에 allowedOrigins 등록 필요 |
