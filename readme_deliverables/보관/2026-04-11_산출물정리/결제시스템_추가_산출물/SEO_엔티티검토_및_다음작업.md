# SEO 담당 - 엔티티 최종 검토 및 다음 작업 순서

> 작성일: 2026-03-30
> 담당자: seo
> 기준: JWT 작업은 hwan 담당 (별도 진행 중)
> 코드 주석 기준: 모든 코드 블록에 어노테이션/필드/메서드별 한국어 주석 포함

---

## 1. 엔티티 최종 검토 결과

### ✅ 수정 완료 (정상)

| 파일 | 수정 내용 |
|------|-----------|
| `Delivery.java` | `extends BaseEntity` 추가, `@OneToOne` 수정, `delivered_at` 오타 수정 |
| `Order.java` | `@Table(name = "\"order\"")` 수정, `discountAmount` 필드명 수정, `deliveryMemo` nullable 제거 |
| `Cart.java` | `@ManyToOne` → `@OneToOne` 수정, `@Builder` 추가 |
| `Payment.java` | `int installmentMonths` → `Integer` 수정 |
| `Member.java` | hwan이 `AuthProvider`, `providerId`, `marketingAgreed` 추가 및 팩토리 메서드 작성 |

---

### ⚠️ 아직 수정 안 된 것 (2개)

#### ① BaseEntity.java — `updatedAt` 컬럼 어노테이션 버그

**현재 코드 (문제있음):**
```java
@LastModifiedDate
// ❌ updatable = false : UPDATE 쿼리 실행 시 이 컬럼을 DB에 쓰지 않음
// ❌ insertable = false : INSERT 쿼리 실행 시에도 이 컬럼을 DB에 쓰지 않음
// → @LastModifiedDate가 값을 세팅해도 DB에 전혀 반영이 안 되는 심각한 버그
@Column(name = "updated_at", updatable = false, insertable = false)
private LocalDateTime updatedAt;
```

**수정 후:**
```java
@LastModifiedDate
// ✅ 별도 속성 없이 기본값 사용 — updatable = true (기본), insertable = true (기본)
// → INSERT, UPDATE 모두 DB에 정상 반영됨
@Column(name = "updated_at")
private LocalDateTime updatedAt;
```

**수정 방법:** `updatable = false, insertable = false` 두 속성 모두 제거.

---

#### ② CategoryTop.java — `columnDefinition = "0"` 잘못된 문법

**현재 코드 (문제있음):**
```java
// ❌ columnDefinition은 DDL 타입 정의용 (예: "INTEGER DEFAULT 0")
// ❌ 숫자 "0" 하나만 넣으면 Hibernate DDL 자동 생성 시 오류 발생 가능
@Column(nullable = false, columnDefinition = "0")
private Integer sortOrder;
```

**수정 후 (방법 A — 권장):**
```java
// ✅ @ColumnDefault : DB DDL의 DEFAULT 절에 "0" 삽입
// ✅ @Builder.Default : @Builder 사용 시 기본값 0을 보장
@Column(nullable = false)
@ColumnDefault("0")              // import org.hibernate.annotations.ColumnDefault; 필요
@Builder.Default
private Integer sortOrder = 0;  // Java 객체 생성 시 기본값도 0으로 설정
```

---

## 2. 다음 작업 순서

> JWT가 없는 지금 상태에서 할 수 있는 작업 순서.
> 결제 관련 상세 구현은 `결제_구현_작업범위_및_순서.md`, `결제_PG_통합_구현_매뉴얼.md` 참고.

---

### STEP 1. Repository 작성 (7개)

`src/main/java/com/bookstore/shop/readme/repository/` 폴더 생성 후 작성.
모두 `JpaRepository<엔티티, Long>` 상속 — 기본 CRUD는 자동 제공됨.

```java
// ─── ProductRepository.java ───────────────────────────────────────────────
package com.bookstore.shop.readme.repository;

import com.bookstore.shop.readme.domain.Product;
import com.bookstore.shop.readme.domain.ProductStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

// JpaRepository<Product, Long> : Product 엔티티, PK 타입 Long
// → findById, save, delete 등 기본 CRUD 메서드 자동 생성
public interface ProductRepository extends JpaRepository<Product, Long> {

    // 재고 조회 시 삭제된 상품 제외: status = ACTIVE 인 상품만 반환
    // Spring Data JPA가 메서드명으로 자동 쿼리 생성
    // → SELECT * FROM product WHERE id = ? AND product_status = ?
    Optional<Product> findByIdAndProductStatus(Long id, ProductStatus status);
}
```

```java
// ─── CartRepository.java ──────────────────────────────────────────────────
package com.bookstore.shop.readme.repository;

import com.bookstore.shop.readme.domain.Cart;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface CartRepository extends JpaRepository<Cart, Long> {

    // 로그인한 사용자의 장바구니 조회
    // Cart ↔ Member = @OneToOne 관계이므로 결과가 항상 0개 또는 1개
    // → SELECT * FROM cart WHERE member_id = ?
    Optional<Cart> findByMemberId(Long memberId);
}
```

```java
// ─── CartItemRepository.java ──────────────────────────────────────────────
package com.bookstore.shop.readme.repository;

import com.bookstore.shop.readme.domain.CartItem;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface CartItemRepository extends JpaRepository<CartItem, Long> {

    // 특정 장바구니의 모든 상품 조회
    // → SELECT * FROM cart_item WHERE cart_id = ?
    List<CartItem> findByCartId(Long cartId);

    // 선택 삭제: 체크된 항목 ID 목록을 넘겨서 일괄 삭제
    // → DELETE FROM cart_item WHERE id IN (?, ?, ...)
    void deleteAllByIdIn(List<Long> ids);
}
```

```java
// ─── OrderRepository.java ─────────────────────────────────────────────────
package com.bookstore.shop.readme.repository;

import com.bookstore.shop.readme.domain.Order;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface OrderRepository extends JpaRepository<Order, Long> {

    // 본인 주문인지 확인하며 조회 — memberId를 같이 검증해서 타인 주문 접근 방지
    // → SELECT * FROM "order" WHERE id = ? AND member_id = ?
    Optional<Order> findByIdAndMemberId(Long id, Long memberId);

    // 마이페이지 주문 목록 — Pageable로 페이징 처리
    // → SELECT * FROM "order" WHERE member_id = ? ORDER BY ... LIMIT ? OFFSET ?
    Page<Order> findByMemberId(Long memberId, Pageable pageable);
}
```

```java
// ─── OrderItemRepository.java ─────────────────────────────────────────────
package com.bookstore.shop.readme.repository;

import com.bookstore.shop.readme.domain.OrderItem;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface OrderItemRepository extends JpaRepository<OrderItem, Long> {

    // 주문 상세 조회: 특정 주문의 모든 상품 목록 반환
    // → SELECT * FROM order_item WHERE order_id = ?
    List<OrderItem> findByOrderId(Long orderId);
}
```

```java
// ─── PaymentRepository.java ───────────────────────────────────────────────
package com.bookstore.shop.readme.repository;

import com.bookstore.shop.readme.domain.Payment;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface PaymentRepository extends JpaRepository<Payment, Long> {

    // 주문 ID로 결제 정보 조회 — Payment ↔ Order = 1:1 관계
    // → SELECT * FROM payment WHERE order_id = ?
    Optional<Payment> findByOrderId(Long orderId);

    // 토스 paymentKey로 결제 정보 조회 — 취소/환불 시 사용
    // → SELECT * FROM payment WHERE payment_key = ?
    Optional<Payment> findByPaymentKey(String paymentKey);
}
```

```java
// ─── DeliveryRepository.java ──────────────────────────────────────────────
package com.bookstore.shop.readme.repository;

import com.bookstore.shop.readme.domain.Delivery;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface DeliveryRepository extends JpaRepository<Delivery, Long> {

    // 주문 ID로 배송 정보 조회 — Delivery ↔ Order = 1:1 관계
    // → SELECT * FROM delivery WHERE order_id = ?
    Optional<Delivery> findByOrderId(Long orderId);
}
```

---

### STEP 2. DTO 클래스 작성 (결제 관련 7개)

`src/main/java/com/bookstore/shop/readme/dto/` 폴더 아래 `request/`, `response/` 분리.

```java
// ─── request/PaymentReadyRequest.java ─────────────────────────────────────
package com.bookstore.shop.readme.dto.request;

import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter           // Getter 자동 생성 (Jackson 역직렬화 시 필요)
@NoArgsConstructor // Jackson이 JSON → 객체 변환 시 기본 생성자 필요
public class PaymentReadyRequest {

    private Long orderId;        // 결제할 주문의 DB ID
    private String provider;     // 결제 수단: "KAKAO" 또는 "NAVER"
    private String itemName;     // 상품명 (카카오 required 필드)
    private Integer amount;      // 결제 금액 (서버에서 order.finalPrice와 비교 검증)
    private String approvalUrl;  // 결제 성공 시 리다이렉트할 프론트엔드 URL
    private String cancelUrl;    // 사용자가 결제 취소 시 리다이렉트할 URL
    private String failUrl;      // 결제 실패 시 리다이렉트할 URL
}
```

```java
// ─── response/PaymentReadyResponse.java ───────────────────────────────────
package com.bookstore.shop.readme.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Builder           // PaymentService에서 .builder() 패턴으로 생성
@NoArgsConstructor
@AllArgsConstructor
public class PaymentReadyResponse {

    private String tid;                // 카카오: 거래 고유번호 (approve 시 다시 전송해야 함)
    private String redirectPcUrl;      // 카카오: PC 결제창 URL
    private String redirectMobileUrl;  // 카카오: 모바일 결제창 URL
    private String paymentId;          // 네이버페이: 결제 ID
    private String redirectUrl;        // 네이버페이: 결제창 URL
}
```

```java
// ─── request/PaymentConfirmRequest.java ───────────────────────────────────
package com.bookstore.shop.readme.dto.request;

import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PaymentConfirmRequest {

    private String paymentKey;  // 토스: successUrl 쿼리파라미터로 전달받은 값
    private Long orderId;       // 어떤 주문의 결제인지 식별
    private Integer amount;     // 클라이언트에서 받은 금액 (서버에서 검증 필수)

    // 카카오/네이버 approve 흐름에서 재사용 시 추가 필드
    private String tid;         // 카카오 거래 ID (ready 시 저장해둔 값)
    private String pgToken;     // 카카오: approval_url 쿼리파라미터로 전달받은 값
    private String paymentId;   // 네이버: returnUrl 쿼리파라미터로 전달받은 값
    private Long memberId;      // 카카오 approve API에 partner_user_id로 전달
}
```

```java
// ─── request/PaymentCancelRequest.java ────────────────────────────────────
package com.bookstore.shop.readme.dto.request;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder  // PaymentService.cancelPayment() 내부에서 빌더로 생성
public class PaymentCancelRequest {

    private String paymentKey;    // 토스: 취소 API URL에 path variable로 사용
    private String tid;           // 카카오: 취소 API body에 포함
    private String paymentId;     // 네이버: 취소 API body에 포함
    private Integer cancelAmount; // 부분취소 가능 — null이면 전액 취소
    private String cancelReason;  // 취소 사유 (필수)
    private String provider;      // "TOSS" / "KAKAO" / "NAVER" — 어떤 게이트웨이 호출할지 결정
}
```

---

### STEP 3. 설정 클래스 작성

`src/main/java/com/bookstore/shop/readme/config/` 폴더에 추가.

```java
// ─── config/RestTemplateConfig.java ───────────────────────────────────────
package com.bookstore.shop.readme.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestTemplate;

// @Configuration : 이 클래스가 스프링 Bean 설정 파일임을 선언
// → @Bean 메서드들이 스프링 컨테이너에 등록됨
@Configuration
public class RestTemplateConfig {

    // @Bean : restTemplate() 반환값을 스프링 Bean으로 등록
    // → PaymentGateway 구현체들에서 @RequiredArgsConstructor로 주입받아 사용
    @Bean
    public RestTemplate restTemplate() {
        return new RestTemplate();  // PG API(토스/카카오/네이버) HTTP 호출에 사용
    }
}
```

**application.yml 추가 설정** (`src/main/resources/application.yml`)

```yaml
# ─── PG 연동 키 설정 ────────────────────────────────────────────────────────
# ⚠️ 실제 키값은 절대 Git에 올리지 말 것
# ⚠️ 팀 공유: 카카오톡/노션으로 별도 전달

payment:
  toss:
    secret-key: ${TOSS_SECRET_KEY}      # 환경변수로 관리 (test_sk_로 시작하는 테스트 키)
    confirm-url: https://api.tosspayments.com/v1/payments/confirm
    cancel-url: https://api.tosspayments.com/v1/payments/{paymentKey}/cancel

  kakao:
    admin-key: ${KAKAO_ADMIN_KEY}       # 카카오 Admin 키 (REST API 키 아님)
    cid: ${KAKAO_CID}                   # 테스트: TC0ONETIME / 실서비스: 카카오 심사 후 발급
    ready-url: https://kapi.kakao.com/v1/payment/ready
    approve-url: https://kapi.kakao.com/v1/payment/approve
    cancel-url: https://kapi.kakao.com/v1/payment/cancel

  naver:
    client-id: ${NAVER_CLIENT_ID}       # 네이버페이 샌드박스 신청 후 발급
    client-secret: ${NAVER_CLIENT_SECRET}
    chain-id: ${NAVER_CHAIN_ID}
```

---

### STEP 4. Gateway 인터페이스 + 구현체 작성

`src/main/java/com/bookstore/shop/readme/gateway/` 폴더 신규 생성.

```java
// ─── gateway/PaymentGateway.java (인터페이스) ──────────────────────────────
package com.bookstore.shop.readme.gateway;

import com.bookstore.shop.readme.dto.request.PaymentCancelRequest;
import com.bookstore.shop.readme.dto.request.PaymentConfirmRequest;
import com.bookstore.shop.readme.dto.request.PaymentReadyRequest;
import com.bookstore.shop.readme.dto.response.PaymentConfirmResponse;
import com.bookstore.shop.readme.dto.response.PaymentReadyResponse;

// 전략 패턴(Strategy Pattern) 인터페이스
// → Toss / Kakao / Naver 구현체를 이 타입으로 교체 가능
// → PaymentService는 이 인터페이스에만 의존 (구현체 직접 참조 X)
public interface PaymentGateway {

    // 결제 준비: 카카오/네이버는 서버에서 먼저 준비 API 호출 필요
    // 토스는 프론트 SDK에서 직접 처리 → UnsupportedOperationException
    PaymentReadyResponse ready(PaymentReadyRequest request);

    // 결제 최종 승인: PG사에 결제 확정 요청 전송
    PaymentConfirmResponse confirm(PaymentConfirmRequest request);

    // 결제 취소/환불: PG사에 취소 요청 전송
    void cancel(PaymentCancelRequest request);
}
```

우선순위: **Toss 먼저 완성 → Kakao → Naver (일정 부족 시 생략 가능)**

상세 구현 코드: `결제_PG_통합_구현_매뉴얼.md` 8번 섹션 참조

---

### STEP 5. PaymentService 작성

`src/main/java/com/bookstore/shop/readme/service/PaymentService.java`

주요 메서드와 역할:

```java
// ─── service/PaymentService.java 메서드 목록 ──────────────────────────────

// 카카오/네이버 결제 준비
// - 주문 소유권 확인 (memberId 검증)
// - PG사에 ready API 호출 → tid 또는 paymentId 발급
// - Payment 레코드를 READY 상태로 DB 저장
// - 프론트에 redirectUrl 응답
public PaymentReadyResponse requestReady(PaymentReadyRequest request, Long memberId);

// 토스 결제 최종 승인 (/confirm 엔드포인트에서 호출)
// - 서버 측 금액 검증 (order.finalPrice == request.amount 비교 — 탬퍼링 방지)
// - TossPaymentGateway.confirm() → 토스 API POST /v1/payments/confirm
// - Payment 상태 PAID, Order 상태 PAYED로 업데이트
public void confirmToss(PaymentConfirmRequest request, Long memberId);

// 카카오/네이버 결제 최종 승인 (/approve 엔드포인트에서 호출)
// - Payment 레코드 조회 (ready 시 저장된 tid/paymentId 필요)
// - provider 분기 → 해당 Gateway.confirm() 호출
// - Payment 상태 PAID, Order 상태 PAYED로 업데이트
public void approvePayment(PaymentApproveRequest request, Long memberId);

// 토스 결제 실패 처리 (/fail 엔드포인트에서 호출)
// - Payment 상태 FAILED, failureReason 저장
public void failPayment(PaymentFailRequest request);

// 결제 취소/환불 (마이페이지 주문 취소 버튼에서 호출)
// - Payment에서 provider, paymentKey/tid/paymentId 꺼내서 취소 요청 빌드
// - 해당 Gateway.cancel() 호출
// - Payment 상태 CANCELLED, cancelledAt 저장
public void cancelPayment(Long orderId, String cancelReason);
```

상세 구현 코드: `결제_PG_통합_구현_매뉴얼.md` 9번 섹션 참조

---

### STEP 6. PaymentApiController 작성 (JWT 플레이스홀더 사용)

`src/main/java/com/bookstore/shop/readme/controller/MyPaymentApiController.java`

```java
// ─── controller/MyPaymentApiController.java ───────────────────────────────
package com.bookstore.shop.readme.controller;

import com.bookstore.shop.readme.dto.request.*;
import com.bookstore.shop.readme.dto.response.*;
import com.bookstore.shop.readme.service.PaymentService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

// @RestController = @Controller + @ResponseBody
// → 모든 메서드의 반환값이 JSON으로 직렬화되어 응답
@RestController
// @RequestMapping : 이 컨트롤러의 모든 엔드포인트에 "/order/payment" prefix 적용
@RequestMapping("/order/payment")
// @RequiredArgsConstructor : final 필드들을 생성자 주입으로 자동 처리
@RequiredArgsConstructor
public class MyPaymentApiController {

    private final PaymentService paymentService;  // 결제 비즈니스 로직 담당

    // ── JWT 미완성 임시 처리 ─────────────────────────────────────────────
    // hwan의 JWT 작업이 완성되면 아래 메서드를 삭제하고
    // 각 엔드포인트 파라미터에 @AuthenticationPrincipal Member member 추가
    private Long getCurrentMemberId() {
        return 1L; // TODO: JWT 연동 후 → SecurityContext에서 memberId 추출로 교체
    }

    // ── POST /order/payment/ready ─────────────────────────────────────────
    // 카카오/네이버 결제 준비: PG사에 tid 또는 paymentId 발급 요청
    // @RequestBody : HTTP 요청 바디(JSON)를 PaymentReadyRequest 객체로 역직렬화
    @PostMapping("/ready")
    public ResponseEntity<PaymentReadyResponse> readyPayment(
            @RequestBody PaymentReadyRequest request) {

        Long memberId = getCurrentMemberId(); // TODO: JWT 연동 후 교체
        PaymentReadyResponse response = paymentService.requestReady(request, memberId);
        return ResponseEntity.ok(response);  // HTTP 200 + JSON 응답
    }

    // ── POST /order/payment/confirm ───────────────────────────────────────
    // 토스 결제 최종 승인: 프론트의 successUrl 리다이렉트 이후 호출
    @PostMapping("/confirm")
    public ResponseEntity<String> confirmPayment(
            @RequestBody PaymentConfirmRequest request) {

        Long memberId = getCurrentMemberId(); // TODO: JWT 연동 후 교체
        paymentService.confirmToss(request, memberId);
        return ResponseEntity.ok("결제가 완료되었습니다.");
    }

    // ── POST /order/payment/approve ───────────────────────────────────────
    // 카카오/네이버 결제 최종 승인: approval_url / returnUrl 이후 호출
    @PostMapping("/approve")
    public ResponseEntity<String> approvePayment(
            @RequestBody PaymentApproveRequest request) {

        Long memberId = getCurrentMemberId(); // TODO: JWT 연동 후 교체
        paymentService.approvePayment(request, memberId);
        return ResponseEntity.ok("결제가 완료되었습니다.");
    }

    // ── POST /order/payment/fail ──────────────────────────────────────────
    // 토스 결제 실패 처리: failUrl 리다이렉트 이후 호출
    // 인증 없이 호출 가능해야 함 → SecurityConfig에서 permitAll() 처리 필요
    @PostMapping("/fail")
    public ResponseEntity<String> failPayment(
            @RequestBody PaymentFailRequest request) {

        paymentService.failPayment(request);
        return ResponseEntity.ok("결제에 실패했습니다.");
    }
}
```

---

## 3. 작업 우선순위 요약

```
즉시 수정 (코드 버그)
├── BaseEntity.java — updatedAt @Column 수정 (updatable=false, insertable=false 제거)
└── CategoryTop.java — columnDefinition → @ColumnDefault 수정

오늘 ~ 내일
├── STEP 1: Repository 7개 (30분)
├── STEP 2: DTO 7개 (1시간)
└── STEP 3: RestTemplateConfig + application.yml (30분)

이번 주말
├── STEP 4: PaymentGateway interface + TossPaymentGateway 구현체 (2시간)
├── STEP 5: PaymentService (2시간)
└── STEP 6: PaymentApiController 스켈레톤 (1시간)

hwan JWT 완료 후 (1시간 미만)
└── PaymentApiController — getCurrentMemberId() 플레이스홀더
    → @AuthenticationPrincipal Member member 로 교체
```

---

## 4. hwan 작업과의 연동 포인트

| hwan 작업 | seo 연동 시점 | 수정 내용 |
|-----------|---------------|-----------|
| JWT 토큰 발급/검증 | Controller 완성 후 | `getCurrentMemberId()` → `@AuthenticationPrincipal` 교체 |
| Security Filter Chain | JWT 완성 후 | `/order/payment/fail` → `permitAll()` 추가 확인 |
| Member 팩토리 메서드 | 즉시 확인 | `Member.getId()` 등 메서드 존재 여부 확인 |

> hwan JWT 완료 전까지 `return 1L;` 하드코딩으로 개발 진행해도 무방.
> Toss 실제 연동 테스트 때는 JWT 완성 후 교체.
