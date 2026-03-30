# SEO 작업 계획서 — ReadMe 프로젝트

| 항목 | 내용 |
| --- | --- |
| 작성일 | 2026-03-29 |
| 작성자 | seo (백엔드 / 상품·주문·결제·배송 담당) |
| 제출 기한 | 2026-04-07 |
| 발표일 | 2026-04-10 |
| 프로젝트 경로 | `/readme` (Spring Boot) |

---

## ⚠️ 현재 프로젝트 상태 분석

> 코드를 새로 짜기 전에 **기존 코드의 문제점부터 수정**해야 합니다.
> 아래 항목들이 그대로면 JPA Auditing이 작동하지 않고, 빌드 후 런타임 오류가 납니다.

---

### 문제 1 — `build.gradle` JWT 의존성 누락

현재 `build.gradle`에 JWT 라이브러리가 없습니다.
`application.yaml`에 `jwt.secret`과 `jwt.expiration`이 이미 설정돼 있지만
실제로 JWT를 생성/검증할 라이브러리가 없는 상태입니다.

**추가해야 할 의존성:**
```gradle
// JWT
implementation 'io.jsonwebtoken:jjwt-api:0.12.6'
runtimeOnly 'io.jsonwebtoken:jjwt-impl:0.12.6'
runtimeOnly 'io.jsonwebtoken:jjwt-jackson:0.12.6'

// Validation (@Valid, @NotBlank 등)
implementation 'org.springframework.boot:spring-boot-starter-validation'
```

---

### 문제 2 — `BaseEntity` `@EntityListeners` 누락

현재 `BaseEntity`에 `@CreatedDate`, `@LastModifiedDate`를 사용하고 있지만
`@EntityListeners(AuditingEntityListener.class)` 어노테이션이 없어서
`createdAt`, `updatedAt`이 자동으로 채워지지 않습니다.

또한 `ReadmeApplication.java`에 `@EnableJpaAuditing`이 있는지 확인해야 합니다.

**BaseEntity에 추가해야 할 것:**
```java
@EntityListeners(AuditingEntityListener.class)  // 이 줄 추가
@MappedSuperclass
@Getter
public abstract class BaseEntity { ... }
```

**ReadmeApplication.java에 추가해야 할 것:**
```java
@EnableJpaAuditing  // 이 줄 추가
@SpringBootApplication
public class ReadmeApplication { ... }
```

---

### 문제 3 — `Member` 엔티티 필드 네이밍 오류

현재 코드:
```java
@Enumerated(EnumType.STRING)
private MemberRole MemberRole;  // ← 필드명이 대문자로 시작함 (자바 컨벤션 위반)

@Enumerated(EnumType.STRING)
private MemberStatus MemberStatus;  // ← 동일 문제
```

수정 후:
```java
@Enumerated(EnumType.STRING)
private MemberRole memberRole;

@Enumerated(EnumType.STRING)
private MemberStatus memberStatus;
```

---

### 문제 4 — `Member.deletedAt` 어노테이션 오류

현재 코드:
```java
@LastModifiedDate          // ← 잘못됨, 수정할 때마다 갱신되어 버림
@Column(name = "deleted_at")
private LocalDateTime deletedAt;
```

수정 후:
```java
@Column(name = "deleted_at")   // @LastModifiedDate 제거
private LocalDateTime deletedAt;
```

---

### 문제 5 — `Product` 엔티티 필드 없음

현재 `Product.java`는 `extends BaseEntity`만 있고 필드가 전혀 없습니다.
DB설계서 기준으로 필드를 채워야 합니다.

---

### 문제 6 — DDL 미작성 (Park 의존 불가)

DB 테이블이 아직 생성되지 않았습니다. Park을 기다리지 말고 **seo가 직접 DDL 작성**해야 합니다.
`application.yaml`의 `ddl-auto: update` 설정 덕분에 Entity만 올바르게 만들면
JPA가 테이블을 자동 생성해주므로, DDL 파일 작성보다 **Entity 작성에 집중**하는 전략이 더 빠릅니다.

---

## 📁 권장 패키지 구조

현재 패키지 구조가 정리되지 않아 팀원들이 코드를 찾기 어렵습니다.
아래 구조로 정리하는 것을 권장합니다.

```
com.bookstore.shop.readme
├── ReadmeApplication.java              ← @EnableJpaAuditing 추가 필요
│
├── config/
│   └── SecurityConfig.java             ← Spring Security 설정
│
├── controller/
│   ├── AuthApiController.java          ← 회원가입 / 로그인 / 로그아웃
│   ├── ProductApiController.java       ← 상품 목록 / 상세 / 검색
│   ├── MyCartApiController.java        ← 장바구니
│   ├── MyOrderApiController.java       ← 주문
│   ├── MyPaymentApiController.java     ← 결제
│   ├── MyDeliveryApiController.java    ← 배송 조회
│   ├── MyMemberApiController.java      ← 마이페이지
│   ├── MyReviewApiController.java      ← 리뷰
│   ├── QnaApiController.java           ← QnA
│   ├── NoticeApiController.java        ← 공지사항
│   └── admin/
│       ├── AdminMemberApiController.java
│       ├── AdminProductApiController.java
│       ├── AdminOrderApiController.java
│       ├── AdminPaymentApiController.java
│       ├── AdminDeliveryApiController.java
│       ├── AdminReviewApiController.java
│       ├── AdminQnaApiController.java
│       ├── AdminNoticeApiController.java
│       └── AdminCategoryApiController.java
│
├── domain/                             ← Entity 클래스
│   ├── BaseEntity.java                 ← @EntityListeners 추가 필요
│   ├── Member.java                     ← 필드 수정 필요
│   ├── MemberRole.java
│   ├── MemberStatus.java
│   ├── CategoryTop.java
│   ├── CategorySub.java
│   ├── Product.java                    ← 필드 없음, 작성 필요
│   ├── ProductImage.java
│   ├── Cart.java
│   ├── CartItem.java
│   ├── Order.java
│   ├── OrderItem.java
│   ├── Payment.java
│   ├── PaymentStatus.java
│   ├── PaymentProvider.java            ← 신규 (TOSS/KAKAO/NAVER)
│   ├── Delivery.java
│   ├── DeliveryStatus.java
│   ├── Notice.java
│   ├── Qna.java
│   ├── QnaStatus.java
│   ├── Review.java
│   ├── ReviewImage.java
│   └── ReviewReaction.java
│
├── dto/
│   ├── request/                        ← 클라이언트 → 서버 요청 DTO
│   │   ├── SignupRequest.java
│   │   ├── SigninRequest.java
│   │   ├── OrderRequest.java
│   │   ├── PaymentReadyRequest.java
│   │   ├── PaymentConfirmRequest.java
│   │   └── ...
│   └── response/                       ← 서버 → 클라이언트 응답 DTO
│       ├── ApiResponse.java            ← 공통 응답 형식
│       ├── TokenResponse.java
│       ├── ProductResponse.java
│       ├── OrderResponse.java
│       └── ...
│
├── exception/
│   ├── GlobalExceptionHandler.java     ← @RestControllerAdvice
│   ├── DuplicateEmailException.java
│   ├── MemberNotFoundException.java
│   ├── OutOfStockException.java
│   ├── PaymentAmountMismatchException.java
│   └── ...
│
├── repository/
│   ├── MemberRepository.java           ← 존재함
│   ├── ProductRepository.java
│   ├── CartRepository.java
│   ├── CartItemRepository.java
│   ├── OrderRepository.java
│   ├── OrderItemRepository.java
│   ├── PaymentRepository.java
│   ├── DeliveryRepository.java
│   └── ...
│
├── security/
│   ├── JwtUtil.java                    ← 토큰 생성/검증
│   ├── JwtFilter.java                  ← 요청마다 토큰 확인
│   └── CustomUserDetailsService.java   ← Spring Security 연동
│
└── service/
    ├── MemberService.java              ← 존재함
    ├── ProductService.java
    ├── CartService.java
    ├── OrderService.java
    ├── PaymentService.java
    ├── DeliveryService.java
    └── AdminService.java
```

---

## 📅 10일 작업 타임라인 (seo 기준)

> 평일: 약 5시간 (수업 중 2시간 + 귀가 후 3시간)
> 주말: 약 8~10시간
> **Park 담당 도메인은 타임라인에서 제외, 추후 흡수 여부 결정**

---

### Day 1 — 3/29 (일) 오늘 | 목표: 기반 정리 완료

| 우선순위 | 작업 | 예상 시간 |
| --- | --- | --- |
| 🔴 최우선 | `build.gradle` JWT / Validation 의존성 추가 | 15분 |
| 🔴 최우선 | `BaseEntity` `@EntityListeners` 추가 | 10분 |
| 🔴 최우선 | `ReadmeApplication` `@EnableJpaAuditing` 추가 | 5분 |
| 🔴 최우선 | `Member` 엔티티 필드 네이밍 수정 + `deletedAt` 어노테이션 수정 | 20분 |
| 🟡 중요 | 패키지 구조 정리 (dto/request, dto/response, exception, security, config 폴더 생성) | 30분 |
| 🟡 중요 | `ApiResponse<T>` 공통 응답 클래스 작성 | 20분 |
| 🟡 중요 | `SecurityConfig` 기본 틀 작성 (permitAll 우선, JWT 필터는 나중에 붙임) | 30분 |
| 🟢 가능하면 | `JwtUtil` 작성 (토큰 생성/검증/memberId 추출) | 1시간 |
| 🟢 가능하면 | `Product` 엔티티 필드 작성 | 30분 |

hwan에게 오늘 작업 완료 후 공유해야 할 것: SecurityConfig 기본 설정, ApiResponse 형식

---

### Day 2 — 3/30 (월) | 목표: JWT 인증 + 상품 Entity

| 작업 | 예상 시간 |
| --- | --- |
| `JwtFilter` + `CustomUserDetailsService` 작성 | 1.5시간 |
| `SecurityConfig`에 JWT 필터 등록 | 30분 |
| 나머지 Entity 작성 — `CategoryTop`, `CategorySub`, `ProductImage`, `Cart`, `CartItem` | 1.5시간 |
| `ProductRepository`, `CategoryTopRepository`, `CategorySubRepository` 작성 | 30분 |

> hwan이 이날부터 회원 백엔드 작성 시작 — JWT 구조 공유 필수

---

### Day 3 — 3/31 (화) | 목표: 상품 API 완성

| 작업 | 예상 시간 |
| --- | --- |
| `ProductService` — 목록 조회, 상세 조회, 검색, 조회수 증가 | 2시간 |
| `ProductApiController` — GET 엔드포인트 | 1시간 |
| `AdminProductApiController` — CRUD (관리자용) | 1.5시간 |
| `GlobalExceptionHandler` 기본 틀 작성 | 30분 |

---

### Day 4 — 4/1 (수) | 목표: 장바구니 API 완성

| 작업 | 예상 시간 |
| --- | --- |
| `Cart`, `CartItem` Entity 연관관계 설정 | 30분 |
| `CartService` — 조회, 추가, 수량변경, 삭제 | 2시간 |
| `MyCartApiController` — CRUD 엔드포인트 | 1시간 |
| hwan 회원 API와 JWT 연동 테스트 (Postman) | 1시간 |

---

### Day 5 — 4/2 (목) | 목표: 주문 API 완성

| 작업 | 예상 시간 |
| --- | --- |
| `Order`, `OrderItem` Entity 작성 | 30분 |
| `OrderService` — 주문 생성 (재고 체크, 스냅샷 저장, 장바구니 삭제) | 2시간 |
| `MyOrderApiController` — 주문 생성 / 목록 / 상세 / 취소 | 1.5시간 |
| `AdminOrderApiController` 기본 틀 | 30분 |

---

### Day 6 — 4/3 (금) | 목표: Mock 결제 + 배송 API

| 작업 | 예상 시간 |
| --- | --- |
| `Payment`, `Delivery` Entity 작성 | 30분 |
| `PaymentService` — Mock PG 방식 결제 요청/완료 | 1.5시간 |
| `MyPaymentApiController` — 결제 엔드포인트 | 1시간 |
| `DeliveryService` — 배송 상태 조회 / 관리자 배송 상태 변경 | 1시간 |
| `AdminDeliveryApiController` | 30분 |

---

### Day 7 — 4/4 (토) 주말 골든타임 | 목표: 토스페이먼츠 PG 연동

| 작업 | 예상 시간 |
| --- | --- |
| 토스페이먼츠 개발자 콘솔 가입 + 테스트 API 키 발급 | 30분 |
| `application.yaml`에 PG 키 환경변수 추가 | 15분 |
| `PaymentGateway` 인터페이스 + `TossPaymentGateway` 구현체 작성 | 2시간 |
| `PaymentService` 리팩토링 — `resolveGateway()`, `validateAmount()` 추가 | 1시간 |
| `MyPaymentApiController` — `/ready`, `/confirm`, `/fail` 엔드포인트 수정 | 1시간 |
| 토스 결제 전체 플로우 Postman 테스트 | 1시간 |
| hwan과 프론트 연동 테스트 | 1시간 |

---

### Day 8 — 4/5 (일) 주말 | 목표: 카카오페이 연동 + 공지사항 흡수

| 작업 | 예상 시간 |
| --- | --- |
| `KakaoPaymentGateway` 구현체 작성 | 2시간 |
| `/approve` 엔드포인트 추가 + 카카오 플로우 테스트 | 1시간 |
| PG사별 결제 취소/환불 (`cancelPayment`) 구현 | 1시간 |
| **공지사항 흡수** — `Notice` Entity + `NoticeService` + `NoticeApiController` + `AdminNoticeApiController` | 2시간 |
| hwan과 전체 플로우 통합 테스트 | 1시간 |

> 네이버페이는 일정상 무리. "설계 완료 / 구현 예정" 처리 권장.

---

### Day 9 — 4/6 (월) | 목표: 버그 수정 + 마무리

| 작업 | 예상 시간 |
| --- | --- |
| 전체 API 엔드포인트 Postman 재점검 | 1시간 |
| 발견된 버그 수정 | 2시간 |
| hwan 관리자 페이지 API 연동 지원 | 1시간 |
| `AdminService` 대시보드 API 완성 | 1시간 |

---

### Day 10 — 4/7 (화) 제출 당일 | 목표: 제출

| 작업 | 예상 시간 |
| --- | --- |
| 최종 통합 테스트 | 1시간 |
| README.md 작성 (실행 방법, 환경 설정) | 1시간 |
| Git 정리 + 최종 커밋 | 30분 |
| 제출 | - |

---

## 🚫 이번 일정에서 제외 (발표자료에 "설계 완료" 표기)

| 도메인 | 이유 |
| --- | --- |
| 리뷰 | 배송 완료(APPROVAL) 이후에만 작성 가능 — 데모 시연 흐름상 마지막에 위치하고, Park 담당이었으나 진행 불가 |
| QnA | Park 담당이었으나 진행 불가. 공지사항 흡수 후 시간 여유 있을 때만 진행 |
| 네이버페이 | 토스/카카오 연동 후 시간 여유 있을 때만 진행 |

---

## 🤝 hwan과의 협업 체크포인트

| 시점 | 공유 내용 |
| --- | --- |
| 오늘(3/29) | `ApiResponse<T>` 형식, `SecurityConfig` permitAll 목록, application.yaml 포트/DB 설정 |
| 3/30 | JWT 토큰 형식 (Authorization: Bearer {token}), memberId 추출 방법 |
| 3/31 | 상품 API 엔드포인트 URL 및 응답 형식 |
| 4/1 | 장바구니 API 엔드포인트 |
| 4/2 | 주문 API 엔드포인트 |
| 4/4 | 결제 PG 연동 프론트 연동 방법 (토스 SDK 사용법 포함) |

---

## 📌 application.yaml 추가 필요 항목

현재 `application.yaml`에 없는 설정으로, PG 연동 시 추가해야 합니다.

```yaml
# 토스페이먼츠
toss:
  secret-key: test_sk_xxxxxxxxxxxxxxxx
  client-key: test_ck_xxxxxxxxxxxxxxxx

# 카카오페이
kakao:
  admin-key: xxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxx
  cid: TC0ONETIME  # 테스트용 단건결제 CID
```

> 실제 키 값은 각 개발자 콘솔에서 발급. **절대 Git에 올리지 말 것** (.gitignore 또는 환경변수로 관리)

---

## ✅ 진행 상황 체크리스트

### 기반 세팅
- [ ] build.gradle JWT / Validation 의존성 추가
- [ ] BaseEntity `@EntityListeners` 추가
- [ ] ReadmeApplication `@EnableJpaAuditing` 추가
- [ ] Member 엔티티 필드 네이밍 수정
- [ ] Member.deletedAt 어노테이션 수정
- [ ] ApiResponse 공통 응답 클래스
- [ ] GlobalExceptionHandler
- [ ] SecurityConfig
- [ ] JwtUtil / JwtFilter / CustomUserDetailsService

### 도메인 (seo 담당)
- [ ] 상품 Entity + API
- [ ] 카테고리 Entity + API
- [ ] 장바구니 Entity + API
- [ ] 주문 Entity + API
- [ ] 결제 Mock PG + API
- [ ] 토스페이먼츠 PG 연동
- [ ] 카카오페이 PG 연동
- [ ] 배송 Entity + API
- [ ] 공지사항 Entity + API (Park 도메인 흡수)
- [ ] 관리자 대시보드 API

### 마무리
- [ ] 전체 플로우 통합 테스트
- [ ] README.md 작성
- [ ] 최종 제출
