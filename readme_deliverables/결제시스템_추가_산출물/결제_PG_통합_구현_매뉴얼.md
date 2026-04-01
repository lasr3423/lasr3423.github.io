# 결제 PG 통합 구현 매뉴얼

> 프로젝트: README 온라인 도서 쇼핑몰
> 기술 스택: Java 21 / Spring Boot 3.5 / PostgreSQL / Vue.js 3
> 결제 방식: 토스페이먼츠(필수) / 카카오페이 / 네이버페이
> 패턴: Strategy Pattern (PaymentGateway 인터페이스 + 구현체 3개)
> 코드 주석 기준: 모든 코드 블록에 어노테이션/필드/메서드별 한국어 주석 포함

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
│   │   ├── PaymentReadyRequest.java    ← 카카오/네이버 결제 준비 요청 DTO
│   │   ├── PaymentConfirmRequest.java  ← 토스 결제 승인 요청 DTO
│   │   ├── PaymentApproveRequest.java  ← 카카오/네이버 결제 승인 요청 DTO
│   │   ├── PaymentFailRequest.java     ← 토스 결제 실패 처리 DTO
│   │   └── PaymentCancelRequest.java   ← 결제 취소/환불 DTO (내부용)
│   └── response/
│       ├── PaymentReadyResponse.java   ← ready() 응답 (redirectUrl, tid 등)
│       └── PaymentConfirmResponse.java ← PG API 응답 결과 DTO
├── gateway/                            ← 전략 패턴 핵심
│   ├── PaymentGateway.java             ← 인터페이스 (추상화)
│   ├── TossPaymentGateway.java         ← 토스 구현체
│   ├── KakaoPaymentGateway.java        ← 카카오 구현체
│   └── NaverPaymentGateway.java        ← 네이버 구현체
├── repository/
│   └── PaymentRepository.java          ← JPA Repository
└── service/
    └── PaymentService.java             ← 비즈니스 로직 (금액 검증, 상태 업데이트)
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
클라이언트 키 (clientKey)  → test_ck_로 시작  → 프론트엔드에서 사용 (소스코드 노출 가능)
시크릿 키 (secretKey)     → test_sk_로 시작  → 백엔드에서만 사용 (절대 노출 금지)
                                               application.yaml에 환경변수로 관리
```

4. `결제창` → `successUrl`, `failUrl` 은 코드에서 직접 지정 (콘솔 등록 불필요)

> ⚠️ 실서비스 전환 시 `live_` 키로 교체하고 사업자 인증 필요

---

### 2-2. 카카오페이 개발자 센터

1. [developers.kakao.com](https://developers.kakao.com) 접속 → 로그인
2. `내 애플리케이션` → `애플리케이션 추가`
3. **Admin 키** 복사 (REST API 키가 아닌 Admin 키 — 혼동 주의)
4. `제품 설정` → `카카오페이` → 활성화
5. 테스트용 CID: `TC0ONETIME` (단건결제), `TCSUBSCRIP` (정기결제) — 별도 등록 불필요

```
Admin 키    → 백엔드 HTTP 헤더 "Authorization: KakaoAK {adminKey}" 에 사용
cid         → TC0ONETIME (테스트), 실서비스 시 카카오 심사 후 발급
```

> ⚠️ 실서비스 시 카카오 비즈니스 채널 연결 및 검수 필요

---

### 2-3. 네이버페이 개발자 센터

1. [developer.pay.naver.com](https://developer.pay.naver.com) 접속
2. 샌드박스 신청 (사업자 등록 전 테스트 가능)
3. 발급되는 키:

```
clientId      → 가맹점 식별자 (HTTP 헤더에 사용)
clientSecret  → 서명 생성용 시크릿 (HTTP 헤더에 사용)
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
    // RestTemplate은 spring-boot-starter-web에 포함 → 별도 추가 불필요
    // WebClient가 필요한 경우에만 아래 추가
    implementation 'org.springframework.boot:spring-boot-starter-webflux'
}
```

> RestTemplate은 `spring-boot-starter-web`에 포함되어 있으므로 별도 추가 불필요.
> WebClient가 필요한 경우에만 `spring-boot-starter-webflux` 추가.

---

### 3-2. application.yaml 추가 설정

```yaml
# 기존 설정 유지...

# ─── 토스페이먼츠 설정 ─────────────────────────────────────────────────────
# @Value("${toss.secret-key}") 로 TossPaymentGateway.java에서 주입받아 사용
toss:
  secret-key: test_sk_여기에_시크릿키_입력   # 절대 Git 커밋 금지
  client-key: test_ck_여기에_클라이언트키_입력  # 프론트 index.html에서 사용

# ─── 카카오페이 설정 ────────────────────────────────────────────────────────
# @Value("${kakao.admin-key}") 로 KakaoPaymentGateway.java에서 주입받아 사용
kakao:
  admin-key: 여기에_어드민키_입력  # 절대 Git 커밋 금지
  cid: TC0ONETIME                 # 테스트용 단건 CID (변경 불필요)

# ─── 네이버페이 설정 ────────────────────────────────────────────────────────
naver:
  client-id: 여기에_클라이언트ID_입력
  client-secret: 여기에_클라이언트_시크릿_입력  # 절대 Git 커밋 금지
  chain-id: 여기에_체인ID_입력
```

> ⚠️ 실제 키 값은 절대 Git에 커밋하지 말 것.
> 팀 공유는 카카오톡 / 노션 등으로 별도 전달.

---

## 4. DB 설계

### 4-1. payment 테이블 DDL (v1.2)

```sql
-- payment 테이블: 결제 정보 저장
-- JPA ddl-auto: update 사용 시 자동 생성되므로 직접 실행 불필요
CREATE TABLE payment (
    id                BIGSERIAL       PRIMARY KEY,           -- 자동 증가 PK
    order_id          BIGINT          NOT NULL REFERENCES "order"(id),  -- 주문 FK
    method            VARCHAR(30),                            -- 결제 수단 (카드, 간편결제 등)
    provider          VARCHAR(20),                            -- TOSS / KAKAO / NAVER
    payment_status    VARCHAR(20)     NOT NULL DEFAULT 'READY', -- 결제 상태
    amount            INTEGER         NOT NULL,               -- 결제 금액
    pg_tid            VARCHAR(200),                           -- 카카오/네이버 거래 ID
    payment_key       VARCHAR(200),                           -- 토스 paymentKey
    installment_months SMALLINT       DEFAULT 0,             -- 할부 개월 수
    paid_at           TIMESTAMP,                              -- 결제 완료 일시
    cancel_reason     VARCHAR(300),                           -- 취소 사유
    failure_reason    VARCHAR(300),                           -- 실패 사유
    cancelled_at      TIMESTAMP,                              -- 취소 완료 일시
    created_at        TIMESTAMP       NOT NULL DEFAULT now(), -- 레코드 생성 일시
    updated_at        TIMESTAMP                               -- 마지막 수정 일시
);
```

---

## 5. Domain 계층

### 5-1. PaymentStatus.java

```java
package com.bookstore.shop.readme.domain;

// 결제 상태 흐름: READY → PAID (정상) / READY → FAILED (실패)
//                PAID → CANCELLED (취소/환불)
public enum PaymentStatus {
    READY,      // 결제 준비 중 — Payment 레코드 생성 직후 기본값
    PAID,       // 결제 완료 — PG사 confirm/approve API 응답 성공 후 이 상태로 변경
    CANCELLED,  // 취소/환불 완료 — cancelPayment() 호출 후 이 상태로 변경
    FAILED      // 결제 실패 — 카드 오류, 잔액 부족 등 PG 실패 응답 시
}
```

### 5-2. PaymentProvider.java

```java
package com.bookstore.shop.readme.domain;

// 어떤 PG사를 통해 결제했는지 구분
// PaymentService.resolveGateway()에서 switch문으로 구현체 선택에 활용
public enum PaymentProvider {
    TOSS,   // 토스페이먼츠 — paymentKey 기반 confirm 방식
    KAKAO,  // 카카오페이 — tid + pg_token 기반 approve 방식
    NAVER   // 네이버페이 — paymentId + resultCode 기반 approve 방식
}
```

### 5-3. Payment.java

```java
package com.bookstore.shop.readme.domain;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

// @Entity : JPA가 이 클래스를 DB 테이블과 매핑
// @Builder : Payment.builder()...build() 패턴으로 생성 가능
@Entity
@Table(name = "payment")
@Getter @Setter
@NoArgsConstructor   // JPA 필수 기본 생성자
@AllArgsConstructor  // @Builder와 함께 사용
@Builder
public class Payment extends BaseEntity {
    // BaseEntity 상속: id(PK), createdAt, updatedAt 자동 관리

    // 어떤 주문의 결제인지 — Order와 1:1 관계
    // LAZY: Payment 조회 시 Order를 즉시 로딩하지 않음 (N+1 방지)
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "order_id", nullable = false)
    private Order order;

    // 결제 수단 — PG사 응답에서 받은 method 값 저장 (예: "카드", "간편결제")
    // nullable: 결제 완료 전 READY 상태에서는 아직 모름
    @Column(length = 30)
    private String method;

    // 어떤 PG사를 사용했는지 — @Enumerated(STRING) 으로 DB에 문자열 저장
    @Enumerated(EnumType.STRING)
    @Column(length = 20)
    private PaymentProvider provider;

    // 결제 현재 상태 — READY → PAID / FAILED / CANCELLED
    @Enumerated(EnumType.STRING)
    @Column(name = "payment_status", length = 20, nullable = false)
    private PaymentStatus paymentStatus;

    // 결제 금액 — Order.finalPrice와 서비스에서 반드시 비교 검증
    @Column(nullable = false)
    private Integer amount;

    // 카카오/네이버 거래 ID — approve(취소) API 호출 시 tid/paymentId로 사용
    // 토스는 이 필드 대신 paymentKey 사용
    @Column(name = "pg_tid", length = 200)
    private String pgTid;

    // 토스 paymentKey — 취소 API URL: /v1/payments/{paymentKey}/cancel
    @Column(name = "payment_key", length = 200)
    private String paymentKey;

    // 할부 개월 수
    // ⚠️ int(primitive) 대신 Integer(wrapper): DB nullable 컬럼 → null 저장 가능해야 함
    //    int 사용 시 null을 받으면 NullPointerException 발생
    @Column(name = "installment_months")
    private Integer installmentMonths;

    // 결제 완료 일시 — PaymentStatus.PAID 변경 시점에 LocalDateTime.now() 저장
    @Column(name = "paid_at")
    private LocalDateTime paidAt;

    // 취소 사유 — cancelPayment() 호출 시 사용자 입력값 저장
    @Column(name = "cancel_reason", length = 300)
    private String cancelReason;

    // 결제 실패 사유 — 토스 failUrl의 ?message= 쿼리파라미터 값 저장
    @Column(name = "failure_reason", length = 300)
    private String failureReason;

    // 취소 완료 일시 — PaymentStatus.CANCELLED 변경 시점에 저장
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

// 카카오/네이버 결제 준비 요청 시 프론트에서 전송하는 데이터
// 토스는 /ready 호출 없이 프론트 SDK에서 직접 처리하므로 이 DTO 사용 안 함
@Getter
@NoArgsConstructor // Jackson JSON 역직렬화 시 기본 생성자 필요
public class PaymentReadyRequest {

    private Long orderId;        // 결제할 주문의 DB ID
    private String provider;     // 결제 수단: "KAKAO" 또는 "NAVER"
    private String itemName;     // 상품명 — 카카오 API required 필드 (예: "교보문고 도서 외 2권")
    private Integer amount;      // 결제 금액 — 서버에서 Order.finalPrice와 비교 검증
    private Long memberId;       // 카카오 partner_user_id에 전달 (서버에서 SecurityContext로 채움)
    private String approvalUrl;  // 결제 성공 → 이 URL로 리다이렉트 (pg_token 포함됨)
    private String cancelUrl;    // 결제창에서 취소 버튼 → 이 URL로 리다이렉트
    private String failUrl;      // 결제 실패 → 이 URL로 리다이렉트
}
```

### 6-2. PaymentReadyResponse.java

```java
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

    // 카카오: 거래 고유번호 (프론트에서 sessionStorage에 저장 → approve 시 재전송 필요)
    private String tid;

    // 카카오: PC용 결제창 URL — 프론트에서 PC 감지 시 이 URL로 location.href 이동
    private String redirectPcUrl;

    // 카카오: 모바일용 결제창 URL — 프론트에서 모바일 감지 시 이 URL로 이동
    private String redirectMobileUrl;

    // 네이버페이: 결제 ID — approve 시 다시 서버로 전송해야 함
    private String paymentId;

    // 네이버페이: 결제창 URL (카카오의 redirectPcUrl과 동일한 역할)
    private String redirectUrl;
}
```

### 6-3. PaymentConfirmRequest.java (토스)

```java
package com.bookstore.shop.readme.dto.request;

import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

// 토스 결제 최종 승인 요청 DTO
// 토스 successUrl 리다이렉트 시 쿼리파라미터로 전달된 값들을 담아 /confirm API 호출
// 카카오/네이버 approve 흐름에서도 재사용 가능하도록 추가 필드 포함
@Getter
@Builder  // PaymentService 내부에서 approve 흐름에 재사용할 때 빌더로 생성
@NoArgsConstructor
@AllArgsConstructor
public class PaymentConfirmRequest {

    // 토스: successUrl 쿼리파라미터 ?paymentKey= 로 전달받은 고유 결제 키
    //       취소 API URL에도 사용: POST /v1/payments/{paymentKey}/cancel
    private String paymentKey;

    // 어떤 주문의 결제인지 식별 — Order.id와 매핑
    private Long orderId;

    // 클라이언트에서 전달받은 금액 — 반드시 Order.finalPrice와 서버에서 비교 검증
    // 다르면 탬퍼링(금액 조작) 시도로 판단하고 RuntimeException 발생
    private Integer amount;

    // 카카오/네이버 approve에서 재사용 시 필요한 추가 필드
    private String tid;       // 카카오 거래 ID (ready 응답에서 받은 값)
    private String pgToken;   // 카카오: approval_url 쿼리파라미터 ?pg_token= 값
    private String paymentId; // 네이버: returnUrl 쿼리파라미터 ?paymentId= 값
    private Long memberId;    // 카카오 approve API의 partner_user_id 필드에 사용
}
```

### 6-4. PaymentApproveRequest.java (카카오/네이버)

```java
package com.bookstore.shop.readme.dto.request;

import lombok.Getter;
import lombok.NoArgsConstructor;

// 카카오/네이버 결제 최종 승인 요청 DTO
// 프론트에서 approval_url(카카오) 또는 returnUrl(네이버) 처리 후 서버로 전송
@Getter
@NoArgsConstructor
public class PaymentApproveRequest {

    private Long orderId;       // 어떤 주문의 결제인지 식별
    private String tid;         // 카카오: ready 응답에서 받아 sessionStorage에 저장한 거래 ID
    private String pgToken;     // 카카오: approval_url 리다이렉트 시 ?pg_token= 쿼리파라미터
    private String paymentId;   // 네이버: returnUrl 리다이렉트 시 ?paymentId= 쿼리파라미터
    private String resultCode;  // 네이버: "Success" 이외의 값이면 결제 실패로 처리
    private String provider;    // "KAKAO" 또는 "NAVER" — 어떤 Gateway 구현체 사용할지 결정
}
```

### 6-5. PaymentFailRequest.java (토스)

```java
package com.bookstore.shop.readme.dto.request;

import lombok.Getter;
import lombok.NoArgsConstructor;

// 토스 결제 실패 처리 DTO
// 토스 failUrl 리다이렉트 시 ?code=&message= 쿼리파라미터로 전달받은 값
@Getter
@NoArgsConstructor
public class PaymentFailRequest {

    private Long orderId;      // 어떤 주문의 결제가 실패했는지 식별
    private String code;       // 토스 에러 코드 (예: "USER_CANCEL", "INVALID_CARD_EXPIRY_DATE")
    private String message;    // 에러 메시지 — Payment.failureReason 필드에 저장
}
```

### 6-6. PaymentCancelRequest.java (내부 DTO)

```java
package com.bookstore.shop.readme.dto.request;

import lombok.Builder;
import lombok.Getter;

// 결제 취소/환불 요청 DTO — 프론트에서 직접 받지 않고 PaymentService 내부에서 빌드
// 각 PG사 취소 API에 전달할 파라미터를 하나의 DTO로 통합
@Getter
@Builder  // PaymentService.cancelPayment()에서 .builder()로 생성
public class PaymentCancelRequest {

    private String paymentKey;    // 토스: 취소 API URL 경로 /v1/payments/{paymentKey}/cancel
    private String tid;           // 카카오: 취소 API body의 "tid" 필드
    private String paymentId;     // 네이버: 취소 API body의 "paymentId" 필드
    private Integer cancelAmount; // 부분취소 금액 (null이면 전액 취소)
    private String cancelReason;  // 취소 사유 (필수) — 사용자 입력값
    private String provider;      // "TOSS" / "KAKAO" / "NAVER" — resolveGateway() 분기에 사용
}
```

### 6-7. PaymentConfirmResponse.java

```java
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
```

---

## 7. Repository 계층

### 7-1. PaymentRepository.java

```java
package com.bookstore.shop.readme.repository;

import com.bookstore.shop.readme.domain.Payment;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

// JpaRepository<Payment, Long> : Payment 엔티티, PK 타입 Long
// → save, findById, delete 등 기본 CRUD 자동 제공
public interface PaymentRepository extends JpaRepository<Payment, Long> {

    // 주문 ID로 결제 정보 단건 조회
    // confirm/approve/cancel 처리 시 주문 ID만 있을 때 결제 정보 조회에 사용
    // Payment ↔ Order = 1:1 → Optional(0 또는 1개)
    // → SELECT * FROM payment WHERE order_id = ?
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

    // 카카오 Admin 키 — Authorization 헤더: "KakaoAK {adminKey}"
    // ⚠️ REST API 키가 아닌 Admin 키임에 주의
    @Value("${kakao.admin-key}")
    private String adminKey;

    // 결제 수단 식별자 — 테스트: TC0ONETIME, 실서비스: 카카오 심사 후 발급
    @Value("${kakao.cid}")
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

// @Service : 비즈니스 로직 계층 빈 등록
// @RequiredArgsConstructor : final 필드 생성자 주입 자동 생성
@Service
@RequiredArgsConstructor
public class PaymentService {

    // Repository: DB 접근 계층
    private final PaymentRepository paymentRepository;
    private final OrderRepository orderRepository;

    // Gateway: 각 PG사 API 호출 계층 (전략 패턴 구현체)
    private final TossPaymentGateway tossPaymentGateway;
    private final KakaoPaymentGateway kakaoPaymentGateway;
    private final NaverPaymentGateway naverPaymentGateway;

    // ─── 카카오/네이버 결제 준비 ───────────────────────────────────────────

    // @Transactional : 이 메서드 실행 중 예외 발생 시 DB 변경사항 자동 롤백
    @Transactional
    public PaymentReadyResponse requestReady(PaymentReadyRequest request, Long memberId) {

        // 1. 주문 소유권 확인 — memberId 검증으로 타인의 주문에 결제 시도 방지
        Order order = orderRepository.findByIdAndMemberId(request.getOrderId(), memberId)
                .orElseThrow(() -> new RuntimeException("주문을 찾을 수 없습니다."));

        // 2. provider 문자열로 해당 Gateway 구현체 선택 (전략 패턴 실행부)
        PaymentGateway gateway = resolveGateway(request.getProvider());

        // 3. PG사에 결제 준비 API 호출 → tid 또는 paymentId + 결제창 URL 발급
        PaymentReadyResponse response = gateway.ready(request);

        // 4. READY 상태의 Payment 레코드 생성 및 저장
        //    — confirm/approve 시 이 레코드를 찾아서 상태 업데이트
        Payment payment = Payment.builder()
                .order(order)
                .provider(PaymentProvider.valueOf(request.getProvider())) // "KAKAO" → KAKAO ENUM
                .paymentStatus(PaymentStatus.READY)                       // 초기 상태
                .amount(order.getFinalPrice())                             // ⚠️ 서버에서 금액 직접 세팅
                // 카카오: tid 저장 / 네이버: paymentId 저장 (둘 중 하나만 값 있음)
                .pgTid(response.getTid() != null ? response.getTid() : response.getPaymentId())
                .build();
        paymentRepository.save(payment);

        // 5. 프론트에 결제창 URL 응답 (프론트는 이 URL로 리다이렉트)
        return response;
    }

    // ─── 토스 결제 최종 승인 (/confirm) ──────────────────────────────────

    @Transactional
    public void confirmToss(PaymentConfirmRequest request, Long memberId) {

        // 1. 주문 소유권 확인
        Order order = orderRepository.findByIdAndMemberId(request.getOrderId(), memberId)
                .orElseThrow(() -> new RuntimeException("주문을 찾을 수 없습니다."));

        // 2. ⚠️ 금액 검증 필수 — 탬퍼링 방지
        //    프론트에서 amount를 임의로 수정해서 보낼 수 있음
        //    서버에서 order.getFinalPrice()와 비교해야 함
        validateAmount(request.getOrderId(), request.getAmount());

        // 3. 토스 confirm API 호출 → 실제 결제 확정
        PaymentConfirmResponse response = tossPaymentGateway.confirm(request);

        // 4. 결제 레코드 조회 또는 신규 생성 (ready() 없이 바로 confirm 오는 경우 대비)
        Payment payment = paymentRepository.findByOrderId(request.getOrderId())
                .orElse(Payment.builder()
                        .order(order)
                        .provider(PaymentProvider.TOSS)
                        .amount(request.getAmount())
                        .build());

        // 5. 결제 완료 상태로 업데이트
        payment.setPaymentKey(request.getPaymentKey()); // 취소 시 필요
        payment.setPaymentStatus(PaymentStatus.PAID);   // READY → PAID
        payment.setPaidAt(LocalDateTime.now());          // 결제 완료 시각 기록
        paymentRepository.save(payment);

        // 6. 주문 상태도 PAYED로 업데이트
        order.setOrderStatus(OrderStatus.PAYED);
        orderRepository.save(order);
    }

    // ─── 카카오/네이버 결제 최종 승인 (/approve) ─────────────────────────

    @Transactional
    public void approvePayment(PaymentApproveRequest request, Long memberId) {

        // 1. 주문 소유권 확인
        Order order = orderRepository.findByIdAndMemberId(request.getOrderId(), memberId)
                .orElseThrow(() -> new RuntimeException("주문을 찾을 수 없습니다."));

        // 2. ready() 시 저장한 Payment 레코드 조회 (pgTid, amount 가져오기 위해)
        Payment payment = paymentRepository.findByOrderId(request.getOrderId())
                .orElseThrow(() -> new RuntimeException("결제 정보를 찾을 수 없습니다."));

        // 3. DB에 저장된 amount로 검증 (프론트에서 amount 안 보냄 → DB값 사용)
        validateAmount(request.getOrderId(), payment.getAmount());

        // 4. approve API에 전달할 파라미터 빌드
        //    (카카오: tid + pgToken 필요 / 네이버: paymentId 필요)
        PaymentConfirmRequest confirmRequest = PaymentConfirmRequest.builder()
                .orderId(request.getOrderId())
                .memberId(memberId)
                .tid(request.getTid())           // 카카오 tid
                .pgToken(request.getPgToken())   // 카카오 pg_token
                .paymentId(request.getPaymentId()) // 네이버 paymentId
                .build();

        // 5. provider로 Gateway 구현체 선택 후 confirm() 호출
        PaymentGateway gateway = resolveGateway(request.getProvider());
        gateway.confirm(confirmRequest);

        // 6. 결제 완료 상태로 업데이트
        payment.setPaymentStatus(PaymentStatus.PAID);
        payment.setPaidAt(LocalDateTime.now());
        paymentRepository.save(payment);

        // 7. 주문 상태도 PAYED로 업데이트
        order.setOrderStatus(OrderStatus.PAYED);
        orderRepository.save(order);
    }

    // ─── 결제 실패 처리 (토스 failUrl) ───────────────────────────────────

    @Transactional
    public void failPayment(PaymentFailRequest request) {
        // Payment 레코드가 있으면 실패 상태로 업데이트
        // ready() 없이 실패하는 경우엔 레코드가 없을 수 있으므로 ifPresent 사용
        paymentRepository.findByOrderId(request.getOrderId()).ifPresent(payment -> {
            payment.setPaymentStatus(PaymentStatus.FAILED);
            payment.setFailureReason(request.getMessage()); // 토스 에러 메시지 저장
            paymentRepository.save(payment);
        });
    }

    // ─── 결제 취소 / 환불 ─────────────────────────────────────────────────

    @Transactional
    public void cancelPayment(Long orderId, String cancelReason) {

        // 1. Payment 레코드 조회 — provider, paymentKey/pgTid 가져오기
        Payment payment = paymentRepository.findByOrderId(orderId)
                .orElseThrow(() -> new RuntimeException("결제 정보를 찾을 수 없습니다."));

        // 2. PG사별 취소 파라미터 빌드 (모든 PG사 정보를 하나의 DTO에 담음)
        PaymentCancelRequest cancelRequest = PaymentCancelRequest.builder()
                .paymentKey(payment.getPaymentKey()) // 토스 취소 API URL path variable
                .tid(payment.getPgTid())             // 카카오 취소 body "tid"
                .paymentId(payment.getPgTid())       // 네이버 취소 body "paymentId"
                .cancelAmount(payment.getAmount())   // 전액 취소
                .cancelReason(cancelReason)          // 사용자 입력 취소 사유
                .provider(payment.getProvider().name()) // "TOSS" / "KAKAO" / "NAVER"
                .build();

        // 3. provider로 Gateway 구현체 선택 후 cancel() 호출
        PaymentGateway gateway = resolveGateway(payment.getProvider().name());
        gateway.cancel(cancelRequest);

        // 4. 취소 완료 상태로 업데이트
        payment.setPaymentStatus(PaymentStatus.CANCELLED);
        payment.setCancelReason(cancelReason);
        payment.setCancelledAt(LocalDateTime.now()); // 취소 완료 시각 기록
        paymentRepository.save(payment);
    }

    // ─── 내부 유틸 메서드 ─────────────────────────────────────────────────

    // 금액 검증 — 탬퍼링 방지 핵심 메서드
    // 클라이언트에서 보낸 amount를 절대 신뢰하지 말 것
    // DB의 order.getFinalPrice()와 반드시 비교
    private void validateAmount(Long orderId, Integer amount) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new RuntimeException("주문을 찾을 수 없습니다."));
        if (!order.getFinalPrice().equals(amount)) {
            // 금액 불일치 → 결제 탬퍼링 시도로 판단, 강제 예외 발생
            throw new RuntimeException("결제 금액이 일치하지 않습니다.");
        }
    }

    // provider 문자열로 Gateway 구현체 선택
    // Java 14+ switch expression 사용
    private PaymentGateway resolveGateway(String provider) {
        return switch (provider.toUpperCase()) {
            case "TOSS"  -> tossPaymentGateway;   // 토스 구현체 반환
            case "KAKAO" -> kakaoPaymentGateway;  // 카카오 구현체 반환
            case "NAVER" -> naverPaymentGateway;  // 네이버 구현체 반환
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

// @RestController : @Controller + @ResponseBody 합친 어노테이션
//                   모든 메서드 반환값을 자동으로 JSON으로 직렬화하여 HTTP 응답
@RestController
@RequiredArgsConstructor
// "/order/payment" prefix를 이 컨트롤러의 모든 엔드포인트에 적용
@RequestMapping("/order/payment")
public class MyPaymentApiController {

    private final PaymentService paymentService;

    // ── POST /order/payment/ready ─────────────────────────────────────────
    // 카카오/네이버 결제 준비: PG사 API에서 tid + 결제창 URL 발급
    // 프론트에서 이 응답의 redirectUrl로 location.href 이동
    @PostMapping("/ready")
    public ResponseEntity<PaymentReadyResponse> readyPayment(
            @RequestBody PaymentReadyRequest request,
            // @AuthenticationPrincipal : Spring Security가 JWT 토큰을 파싱해서
            //                            SecurityContext에 저장한 Member 객체를 자동 주입
            @AuthenticationPrincipal Member member) {

        PaymentReadyResponse response = paymentService.requestReady(request, member.getId());
        return ResponseEntity.ok(response); // HTTP 200 + JSON 응답
    }

    // ── POST /order/payment/confirm ───────────────────────────────────────
    // 토스 결제 최종 승인
    // 토스 successUrl 리다이렉트 이후 프론트에서 paymentKey, orderId, amount 전송
    @PostMapping("/confirm")
    public ResponseEntity<String> confirmPayment(
            @RequestBody PaymentConfirmRequest request,
            @AuthenticationPrincipal Member member) {

        paymentService.confirmToss(request, member.getId());
        return ResponseEntity.ok("결제가 완료되었습니다.");
    }

    // ── POST /order/payment/approve ───────────────────────────────────────
    // 카카오/네이버 결제 최종 승인
    // approval_url(카카오)/returnUrl(네이버) 리다이렉트 이후 pg_token/paymentId 전송
    @PostMapping("/approve")
    public ResponseEntity<String> approvePayment(
            @RequestBody PaymentApproveRequest request,
            @AuthenticationPrincipal Member member) {

        paymentService.approvePayment(request, member.getId());
        return ResponseEntity.ok("결제가 완료되었습니다.");
    }

    // ── POST /order/payment/fail ──────────────────────────────────────────
    // 토스 결제 실패 처리: failUrl 리다이렉트 이후 code, message 전송
    // ⚠️ 인증 없이 호출 가능해야 함 → SecurityConfig에서 permitAll() 추가 필요
    //    (결제 실패 시 비로그인 상태가 될 수 있음)
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

// @Configuration : 스프링이 이 클래스를 설정 파일로 인식
//                  @Bean 어노테이션이 붙은 메서드들을 스프링 빈으로 등록
@Configuration
public class RestTemplateConfig {

    // @Bean : restTemplate 반환값을 스프링 컨테이너에 빈으로 등록
    // TossPaymentGateway, KakaoPaymentGateway, NaverPaymentGateway에서
    // @RequiredArgsConstructor로 주입받아 HTTP 요청에 사용
    @Bean
    public RestTemplate restTemplate() {
        return new RestTemplate(); // PG사 API 호출용 HTTP 클라이언트
    }
}
```

### 11-2. SecurityConfig — 결제 URL 허용 설정

```java
// SecurityConfig.java 의 authorizeHttpRequests 부분에 아래 추가
// 토스 failUrl은 결제 실패 후 비로그인 상태에서도 호출될 수 있으므로 인증 없이 허용
.requestMatchers("/order/payment/fail").permitAll()  // 토스 failUrl: 비인증 허용
// 나머지 /order/payment/** 는 인증 필요 (JWT 토큰 필수)
// .requestMatchers("/order/payment/**").authenticated() ← 기본적으로 인증 필요
```

---

## 12. 프론트엔드 구현

### 12-1. 토스페이먼츠 (Vue.js)

**index.html 또는 main.js — SDK 로드**
```html
<!-- public/index.html <head> 안에 추가 -->
<!-- 토스 JS SDK: TossPayments 객체를 window에 등록 -->
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

const orderStore = useOrderStore()  // Pinia store에서 주문 정보 가져오기

const payWithToss = () => {
  // 1. clientKey로 TossPayments 인스턴스 생성 (공개 키 — 노출 가능)
  const clientKey = 'test_ck_여기에_클라이언트키_입력'
  const tossPayments = window.TossPayments(clientKey)

  // 2. 결제 요청 — 토스 결제창 오픈 (서버 API 호출 없음)
  tossPayments.requestPayment('카드', {
    amount: orderStore.finalPrice,            // 결제 금액 — 서버의 order.finalPrice와 일치해야 함
    orderId: String(orderStore.orderId),      // ⚠️ 토스 orderId는 String 타입 (DB ID와 다른 개념)
    orderName: '교보문고 도서 주문',            // 결제창에 표시될 주문명
    customerName: '구매자명',                  // 구매자 이름
    successUrl: `${window.location.origin}/payment/success`,  // 결제 성공 시 이동할 프론트 URL
    failUrl: `${window.location.origin}/payment/fail`,        // 결제 실패 시 이동할 프론트 URL
  })
}
</script>
```

**PaymentSuccessView.vue — 토스 successUrl 처리**
```vue
<script setup>
import { onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import axios from '@/api/axios'

const route = useRoute()  // 현재 URL의 쿼리파라미터 접근
const router = useRouter() // 프로그래밍 방식 페이지 이동

onMounted(async () => {
  // 토스가 successUrl에 쿼리파라미터로 paymentKey, orderId, amount를 전달
  const { paymentKey, orderId, amount } = route.query

  try {
    // 서버의 /confirm 엔드포인트에 POST 요청 → 백엔드에서 토스 API 최종 승인 처리
    await axios.post('/order/payment/confirm', {
      paymentKey,
      orderId: Number(orderId),  // 쿼리파라미터는 String → Number 변환 필요
      amount: Number(amount),    // 쿼리파라미터는 String → Number 변환 필요
    })
    router.push('/mypage/order')  // 결제 완료 → 주문 내역 페이지 이동
  } catch (e) {
    alert('결제 승인 중 오류가 발생했습니다.')
    router.push('/cart')  // 오류 → 장바구니로 돌아가기
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
  // 토스가 failUrl에 code, message를 쿼리파라미터로 전달
  const { orderId, code, message } = route.query

  // 서버에 실패 사실 알림 → Payment.failureReason 저장
  await axios.post('/order/payment/fail', {
    orderId: Number(orderId),
    code,     // 토스 에러 코드 (예: "USER_CANCEL")
    message,  // 에러 메시지 (DB failure_reason 컬럼에 저장)
  })

  alert(`결제 실패: ${message}`)
  router.push('/cart')  // 장바구니로 돌아가기
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
    // 1. 서버 /ready 호출 → 카카오 API에서 tid + 결제창 URL 발급
    const { data } = await axios.post('/order/payment/ready', {
      orderId: orderStore.orderId,
      provider: 'KAKAO',
      approvalUrl: `${window.location.origin}/payment/kakao/success`,  // pg_token 포함해서 리다이렉트
      cancelUrl:   `${window.location.origin}/payment/kakao/cancel`,
      failUrl:     `${window.location.origin}/payment/kakao/fail`,
    })

    // 2. tid는 sessionStorage에 저장 → approval_url 처리 시 꺼내서 서버로 전송
    sessionStorage.setItem('currentTid', data.tid)
    sessionStorage.setItem('currentOrderId', String(orderStore.orderId))

    // 3. PC/모바일 환경 감지 후 해당 URL로 리다이렉트 (카카오 결제창 오픈)
    const isMobile = /iPhone|Android/i.test(navigator.userAgent)
    const redirectUrl = isMobile ? data.redirectMobileUrl : data.redirectPcUrl
    window.location.href = redirectUrl  // 카카오 결제창으로 페이지 이동
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
  // 카카오가 approval_url에 pg_token을 쿼리파라미터로 전달
  const pgToken = route.query.pg_token

  // ready() 시 저장해둔 tid와 orderId 꺼내기
  const orderId = Number(sessionStorage.getItem('currentOrderId'))
  const tid     = sessionStorage.getItem('currentTid')

  try {
    // 서버 /approve 호출 → 카카오 최종 승인 처리
    await axios.post('/order/payment/approve', {
      orderId,
      tid,       // ready 시 발급받은 tid — 반드시 함께 전송해야 함
      pgToken,   // 카카오가 전달한 pg_token
      provider: 'KAKAO',
    })
    router.push('/mypage/order')  // 결제 완료 → 주문 내역 이동
  } catch (e) {
    alert('결제 승인 중 오류가 발생했습니다.')
    router.push('/cart')
  }
})
</script>
```

> 💡 tid는 ready() 응답에서 받아서 sessionStorage 또는 Pinia store에 임시 저장해두고
> approval_url 리다이렉트 후 꺼내서 사용합니다.

---

### 12-3. 네이버페이 (Vue.js)

**index.html — 네이버페이 SDK 로드**
```html
<!-- public/index.html <head> 안에 추가 -->
<!-- 네이버페이 버튼 JS SDK — NaverPayButton 객체를 window에 등록 -->
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
    // 1. 서버 /ready 호출 → paymentId 발급
    const { data } = await axios.post('/order/payment/ready', {
      orderId: orderStore.orderId,
      provider: 'NAVER',
    })

    const { paymentId } = data
    // 네이버도 orderId sessionStorage에 저장
    sessionStorage.setItem('currentOrderId', String(orderStore.orderId))

    // 2. 네이버페이 JS SDK로 결제창 오픈 (토스처럼 SDK 방식)
    window.NaverPayButton.apply({
      BUTTON_KEY: '네이버페이에서_발급받은_버튼키', // 네이버페이 개발자 콘솔에서 발급
      TYPE: 'A',                                    // 버튼 타입
      COLOR: 1,                                     // 버튼 색상
      COUNT: 2,
      BILLING_TYPE: 'ONE_TIME',                     // 단건 결제
      ENABLE_PC_POPUP: true,                        // PC에서 팝업 방식으로 결제창 오픈
      MERCHANT_USER_ID: String(orderStore.memberId),// 가맹점 회원 ID
      MERCHANT_PAY_KEY: String(orderStore.orderId), // 가맹점 주문 ID
      PRODUCT_NAME: '교보문고 도서 주문',              // 상품명
      TOTAL_PAY_AMOUNT: orderStore.finalPrice,      // 결제 금액
      TAX_SCOPE_AMOUNT: orderStore.finalPrice,      // 과세 금액 (도서는 면세라면 0으로 변경)
      TAX_EX_SCOPE_AMOUNT: 0,                       // 비과세 금액
      RETURN_URL: `${window.location.origin}/payment/naver/success`, // 결제 성공 리다이렉트 URL
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
  // 네이버가 returnUrl에 resultCode, paymentId를 쿼리파라미터로 전달
  const { resultCode, paymentId } = route.query

  // resultCode가 "Success"가 아니면 결제 실패 처리
  if (resultCode !== 'Success') {
    alert('결제에 실패했습니다.')
    router.push('/cart')
    return
  }

  // ready() 시 저장해둔 orderId 꺼내기
  const orderId = Number(sessionStorage.getItem('currentOrderId'))

  try {
    // 서버 /approve 호출 → 네이버 최종 승인 처리
    await axios.post('/order/payment/approve', {
      orderId,
      paymentId,   // 네이버가 전달한 paymentId
      resultCode,
      provider: 'NAVER',
    })
    router.push('/mypage/order')  // 결제 완료 → 주문 내역 이동
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
// 각 URL은 PG사 콘솔에 등록한 successUrl / failUrl / approval_url / returnUrl과 일치해야 함

{
  path: '/payment/success',
  component: () => import('@/views/payment/PaymentSuccessView.vue'),  // 토스 successUrl 처리
},
{
  path: '/payment/fail',
  component: () => import('@/views/payment/PaymentFailView.vue'),     // 토스 failUrl 처리
},
{
  path: '/payment/kakao/success',
  component: () => import('@/views/payment/KakaoSuccessView.vue'),   // 카카오 approval_url 처리
},
{
  path: '/payment/naver/success',
  component: () => import('@/views/payment/NaverSuccessView.vue'),   // 네이버 returnUrl 처리
},
```

---

## 13. PG별 전체 흐름 정리

### 13-1. 토스페이먼츠 흐름

```
[프론트] 주문서 → 토스 결제 버튼 클릭
    ↓
[프론트] TossPayments SDK.requestPayment() 호출 — 서버 API 없음 (SDK가 직접 토스 통신)
    ↓
[토스 결제창] 카드 정보 입력 → 결제 진행
    ↓ 성공 시
[프론트] successUrl 리다이렉트 (?paymentKey=&orderId=&amount=)
    ↓
[프론트→백엔드] POST /order/payment/confirm { paymentKey, orderId, amount }
    ↓
[백엔드] 금액 검증 (order.finalPrice == amount) → TossPaymentGateway.confirm()
    ↓
[토스 API] POST /v1/payments/confirm → "status": "DONE" 응답
    ↓
[백엔드] Payment: paymentStatus = PAID, paidAt = now()
         Order: orderStatus = PAYED
    ↓
[프론트] 주문 완료 페이지 (/mypage/order) 이동
```

```
    ↓ 실패 시
[프론트] failUrl 리다이렉트 (?code=USER_CANCEL&message=사용자가 결제를 취소했습니다)
    ↓
[프론트→백엔드] POST /order/payment/fail { orderId, code, message }
    ↓
[백엔드] Payment: paymentStatus = FAILED, failureReason = message
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
[백엔드] Payment(READY, pg_tid=tid) 저장 → 프론트에 { tid, redirectPcUrl } 응답
    ↓
[프론트] tid를 sessionStorage에 저장 → 카카오 결제창 URL로 location.href 이동
    ↓
[카카오 결제창] 카카오계정 로그인 → 결제 진행
    ↓ 성공 시
[프론트] approval_url 리다이렉트 (?pg_token=xxxxxxx)
    ↓
[프론트→백엔드] POST /order/payment/approve { orderId, tid, pgToken, provider: "KAKAO" }
    ↓
[백엔드] KakaoPaymentGateway.confirm() → 카카오 POST /v1/payment/approve
    ↓
[백엔드] Payment: paymentStatus = PAID, paidAt = now()
         Order: orderStatus = PAYED
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
[백엔드] Payment(READY) 저장 → paymentId 반환 (현재는 orderId 활용)
    ↓
[프론트] NaverPayButton.apply() — JS SDK로 네이버 결제창 팝업 오픈
    ↓
[네이버 결제창] 네이버 계정 로그인 → 간편결제 진행
    ↓ 성공 시
[프론트] returnUrl 리다이렉트 (?resultCode=Success&paymentId=xxxxx)
    ↓
[프론트] resultCode !== "Success" 면 실패 처리 후 종료
    ↓
[프론트→백엔드] POST /order/payment/approve { orderId, paymentId, resultCode, provider: "NAVER" }
    ↓
[백엔드] NaverPaymentGateway.confirm() → 네이버 POST apply API 호출
    ↓
[백엔드] Payment: paymentStatus = PAID, paidAt = now()
         Order: orderStatus = PAYED
    ↓
[프론트] 주문 완료 페이지 이동
```

---

## 14. 구현 순서 체크리스트

### STEP 1 — 기반 준비

- [ ] `application.yaml`에 toss / kakao / naver 키 설정 (환경변수 방식 권장)
- [ ] `RestTemplateConfig.java` — RestTemplate 빈 등록
- [ ] `PaymentStatus.java` 열거형 작성
- [ ] `PaymentProvider.java` 열거형 작성
- [ ] `Payment.java` 엔티티 작성 (v1.2 필드 포함)
- [ ] DTO 전체 작성 (Request 5개 + Response 2개)
- [ ] `PaymentRepository.java` 작성

### STEP 2 — 토스페이먼츠 (필수)

- [ ] `PaymentGateway.java` 인터페이스 작성
- [ ] `TossPaymentGateway.java` 구현체 작성
- [ ] `PaymentService.java` 작성 (confirmToss, failPayment, cancelPayment 먼저)
- [ ] `MyPaymentApiController.java` 작성 (/confirm, /fail 먼저)
- [ ] 프론트: `PaymentView.vue` 토스 버튼 + `PaymentSuccessView.vue` + `PaymentFailView.vue`
- [ ] 토스 전체 플로우 Postman / 브라우저 테스트

### STEP 3 — 카카오페이 (선택)

- [ ] `KakaoPaymentGateway.java` 구현체 작성
- [ ] `PaymentService.requestReady()`, `approvePayment()` 작성
- [ ] `MyPaymentApiController.java` /ready, /approve 추가
- [ ] 프론트: `KakaoSuccessView.vue` + 카카오 버튼 추가
- [ ] 카카오 전체 플로우 테스트

### STEP 4 — 네이버페이 (일정 여유 시)

- [ ] `NaverPaymentGateway.java` 구현체 작성
- [ ] 프론트: `NaverSuccessView.vue` + 네이버 버튼 추가
- [ ] 네이버 전체 플로우 테스트

### STEP 5 — hwan JWT 완성 후

- [ ] `MyPaymentApiController.java` — JWT 플레이스홀더 → `@AuthenticationPrincipal` 교체
- [ ] SecurityConfig — `/order/payment/fail` permitAll() 추가
- [ ] 인증 포함한 전체 결제 플로우 통합 테스트
