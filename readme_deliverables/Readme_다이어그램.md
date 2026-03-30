# 11_클래스 & 시퀀스 다이어그램

| 항목 | 내용 |
| --- | --- |
| 프로젝트명 | 교보문고 쇼핑몰 시스템 (README) |
| 개발 환경 | Java 21 / Spring Boot 3.5 / PostgreSQL / Vue.js 3 |
| 작성일 | 2026-03-24 |
| 연관 문서 | DB설계서 최종.md / 유스케이스 명세서 최종.md / 기능명세서 최종.md |

---

## 📐 1. 클래스 다이어그램

### 1-1. Domain Entity 전체 구조

```mermaid
classDiagram
    direction TB

    class BaseEntity {
        <<abstract>>
        +Long id
        +LocalDateTime createdAt
        +LocalDateTime updatedAt
    }

    class Member {
        +String email
        +String password
        +String name
        +String phone
        +String address
        +MemberRole memberRole
        +MemberStatus memberStatus
        +LocalDateTime deletedAt
    }

    class CategoryTop {
        +String name
        +Integer sortOrder
        +CategoryTopStatus categoryTopStatus
    }

    class CategorySub {
        +String name
        +Integer sortOrder
        +CategorySubStatus categorySubStatus
        +CategoryTop categoryTop
    }

    class Product {
        +CategoryTop categoryTop
        +CategorySub categorySub
        +String title
        +String author
        +String description
        +Integer price
        +BigDecimal discountRate
        +Integer salePrice
        +Integer stock
        +String thumbnail
        +Integer viewCount
        +Integer salesCount
        +ProductStatus productStatus
        +LocalDateTime deletedAt
    }

    class ProductImage {
        +Product product
        +String imageUrl
        +String type
        +Integer sortOrder
    }

    class Cart {
        +Member member
    }

    class CartItem {
        +Cart cart
        +Product product
        +Integer quantity
        +Boolean isChecked
    }

    class Order {
        +Member member
        +String number
        +OrderStatus orderStatus
        +Integer totalPrice
        +Integer discountAmount
        +Integer finalPrice
        +String receiverName
        +String receiverPhone
        +String deliveryAddress
        +String deliveryMemo
        +LocalDateTime orderedAt
        +LocalDateTime cancelledAt
    }

    class OrderItem {
        +Order order
        +Product product
        +String productTitle
        +String productAuthor
        +Integer salePrice
        +Integer quantity
        +Integer itemTotal
        +Boolean isReviewed
    }

    class Payment {
        +Order order
        +String method
        +PaymentStatus paymentStatus
        +Integer amount
        +String pgTid
        +LocalDateTime paidAt
        +String cancelReason
        +LocalDateTime cancelledAt
    }

    class Delivery {
        +Order order
        +String courier
        +String trackingNumber
        +DeliveryStatus deliveryStatus
        +LocalDateTime shippedAt
        +LocalDateTime deliveredAt
    }

    class Notice {
        +Member member
        +String title
        +String content
        +Boolean isFixed
        +Integer viewCount
        +LocalDateTime deletedAt
    }

    class Qna {
        +Qna parent
        +Integer depth
        +Member member
        +String category
        +String title
        +String content
        +Integer viewCount
        +QnaStatus qnaStatus
        +Boolean isSecret
        +LocalDateTime answeredAt
        +LocalDateTime deletedAt
    }

    class Review {
        +Member member
        +Product product
        +Integer rating
        +String content
        +Integer hits
        +LocalDateTime deletedAt
    }

    class ReviewImage {
        +Review review
        +String imageUrl
    }

    class MemberRole {
        <<enumeration>>
        USER
        MANAGER
        ADMIN
    }

    class MemberStatus {
        <<enumeration>>
        ACTIVATE
        DEACTIVATE
        DELETE
    }

    class OrderStatus {
        <<enumeration>>
        PENDING
        PAYED
        APPROVAL
        CANCELED
    }

    class QnaStatus {
        <<enumeration>>
        WAITING
        PROCESSING
        COMPLETE
    }

    class ProductStatus {
        <<enumeration>>
        ACTIVATE
        DEACTIVATE
        DELETE
    }

    %% BaseEntity 상속
    BaseEntity <|-- Member
    BaseEntity <|-- CategoryTop
    BaseEntity <|-- CategorySub
    BaseEntity <|-- Product
    BaseEntity <|-- Cart
    BaseEntity <|-- CartItem
    BaseEntity <|-- Order
    BaseEntity <|-- OrderItem
    BaseEntity <|-- Payment
    BaseEntity <|-- Delivery
    BaseEntity <|-- Notice
    BaseEntity <|-- Qna
    BaseEntity <|-- Review
    BaseEntity <|-- ReviewImage

    %% 연관 관계
    Member "1" --> "1" Cart : has
    Member "1" --> "0..*" Order : places
    Member "1" --> "0..*" Notice : writes
    Member "1" --> "0..*" Qna : asks
    Member "1" --> "0..*" Review : writes

    CategoryTop "1" --> "0..*" CategorySub : contains
    CategoryTop "1" --> "0..*" Product : classifies
    CategorySub "1" --> "0..*" Product : classifies

    Product "1" --> "0..*" ProductImage : has
    Product "1" --> "0..*" CartItem : in cart
    Product "1" --> "0..*" OrderItem : ordered
    Product "1" --> "0..*" Review : reviewed

    Cart "1" --> "0..*" CartItem : contains
    Order "1" --> "0..*" OrderItem : contains
    Order "1" --> "1" Payment : paid by
    Order "1" --> "1" Delivery : shipped by

    Qna "0..1" --> "0..*" Qna : replies
    Review "1" --> "0..*" ReviewImage : has

    Member ..> MemberRole : uses
    Member ..> MemberStatus : uses
    Order ..> OrderStatus : uses
    Qna ..> QnaStatus : uses
    Product ..> ProductStatus : uses
```

---

### 1-2. 패키지 전체 구조 (com.shop)

```mermaid
classDiagram
    direction TB

    class `com.shop` {
        +ShopApplication
    }

    class `com.shop.config` {
        +SecurityConfig
        +WebMvcConfig
        +JwtConfig
    }

    class `com.shop.controller` {
        +AuthController
        +HomeController
        +MemberController
        +ProductController
        +CartController
        +OrderController
        +PaymentController
        +DeliveryController
        +ReviewController
        +QnaController
        +NoticeController
        +AdminController
        +AdminProductController
        +AdminOrderController
        +AdminPaymentController
        +AdminDeliveryController
        +AdminReviewController
        +AdminQnaController
        +AdminNoticeController
    }

    class `com.shop.service` {
        +AuthService
        +MemberService
        +ProductService
        +CartService
        +OrderService
        +PaymentService
        +DeliveryService
        +ReviewService
        +QnaService
        +NoticeService
    }

    class `com.shop.repository` {
        +MemberRepository
        +ProductRepository
        +CartRepository
        +OrderRepository
        +PaymentRepository
        +DeliveryRepository
        +ReviewRepository
        +QnaRepository
        +NoticeRepository
    }

    class `com.shop.domain` {
        +Member / MemberRole / MemberStatus
        +Product / ProductImage
        +CategoryTop / CategorySub
        +Cart / CartItem
        +Order / OrderItem
        +Payment
        +Delivery
        +Review / ReviewImage
        +Qna
        +Notice
    }

    class `com.shop.dto` {
        +MemberRequestDto / MemberResponseDto
        +ProductRequestDto / ProductResponseDto
        +CartRequestDto / CartResponseDto
        +OrderRequestDto / OrderResponseDto
        +PaymentRequestDto / PaymentResponseDto
        +DeliveryRequestDto / DeliveryResponseDto
        +ReviewRequestDto / ReviewResponseDto
        +QnaRequestDto / QnaResponseDto
        +NoticeRequestDto / NoticeResponseDto
    }

    class `com.shop.security` {
        +JwtTokenProvider
        +JwtAuthenticationFilter
        +CustomUserDetailsService
    }

    class `com.shop.util` {
        +JwtUtil
        +FileUtil
        +PageUtil
        +ResponseUtil
        +SecurityUtil
        +SliceUtil
    }

    `com.shop` --> `com.shop.config`
    `com.shop` --> `com.shop.controller`
    `com.shop.controller` --> `com.shop.service`
    `com.shop.service` --> `com.shop.repository`
    `com.shop.repository` --> `com.shop.domain`
    `com.shop.controller` --> `com.shop.dto`
    `com.shop.service` --> `com.shop.dto`
    `com.shop.config` --> `com.shop.security`
    `com.shop.controller` --> `com.shop.util`
    `com.shop.service` --> `com.shop.util`
```

---

### 1-3. 레이어 아키텍처 상세 클래스 구조

```mermaid
classDiagram
    direction LR

    %% ── Controller (회원/상품/일반) ──
    class AuthController {
        +signup(MemberRequestDto) ResponseEntity
        +signin(MemberRequestDto) ResponseEntity
        +signout(HttpServletRequest) ResponseEntity
    }
    class HomeController {
        +home() ResponseEntity
    }
    class MemberController {
        +getMyPage(Member) ResponseEntity
        +updateInfo(MemberRequestDto) ResponseEntity
        +updatePassword(MemberRequestDto) ResponseEntity
        +withdraw(Member) ResponseEntity
    }
    class ProductController {
        +getProductList(Pageable, String) ResponseEntity
        +getProductDetail(Long) ResponseEntity
        +searchProducts(String, Pageable) ResponseEntity
    }
    class CartController {
        +getCart(Member) ResponseEntity
        +addItem(CartRequestDto) ResponseEntity
        +updateQuantity(CartRequestDto) ResponseEntity
        +removeItem(Long) ResponseEntity
    }
    class OrderController {
        +createOrder(OrderRequestDto) ResponseEntity
        +getOrderList(Member, Pageable) ResponseEntity
        +getOrderDetail(Long, Member) ResponseEntity
        +cancelOrder(Long, Member) ResponseEntity
    }
    class PaymentController {
        +requestPayment(PaymentRequestDto) ResponseEntity
        +completePayment(PaymentRequestDto) ResponseEntity
        +cancelPayment(Long) ResponseEntity
    }
    class DeliveryController {
        +getDeliveryStatus(Long, Member) ResponseEntity
        +getTrackingInfo(Long, Member) ResponseEntity
    }
    class ReviewController {
        +writeReview(ReviewRequestDto) ResponseEntity
        +updateReview(Long, ReviewRequestDto) ResponseEntity
        +deleteReview(Long, Member) ResponseEntity
    }
    class QnaController {
        +getQnaList(Pageable) ResponseEntity
        +writeQuestion(QnaRequestDto) ResponseEntity
        +getQnaDetail(Long, Member) ResponseEntity
        +updateQuestion(Long, QnaRequestDto) ResponseEntity
        +deleteQuestion(Long, Member) ResponseEntity
    }
    class NoticeController {
        +getNoticeList(Pageable) ResponseEntity
        +getNoticeDetail(Long) ResponseEntity
    }

    %% ── Admin Controller (도메인별 분리) ──
    class AdminController {
        +getDashboard() ResponseEntity
    }
    class AdminProductController {
        +getProductList(Pageable) ResponseEntity
        +getProductDetail(Long) ResponseEntity
        +insertProduct(ProductRequestDto) ResponseEntity
        +updateProduct(Long, ProductRequestDto) ResponseEntity
        +deleteProduct(Long) ResponseEntity
        +getCategoryList() ResponseEntity
        +insertCategory(ProductRequestDto) ResponseEntity
        +updateCategory(Long, ProductRequestDto) ResponseEntity
        +deleteCategory(Long) ResponseEntity
    }
    class AdminOrderController {
        +getOrderList(Pageable) ResponseEntity
        +getOrderDetail(Long) ResponseEntity
        +updateOrderStatus(Long, OrderRequestDto) ResponseEntity
        +cancelOrder(Long) ResponseEntity
    }
    class AdminPaymentController {
        +cancelPayment(Long) ResponseEntity
    }
    class AdminDeliveryController {
        +getDeliveryList(Pageable) ResponseEntity
        +registerTracking(Long, DeliveryRequestDto) ResponseEntity
        +updateDeliveryStatus(Long, DeliveryRequestDto) ResponseEntity
    }
    class AdminReviewController {
        +getReviewList(Pageable) ResponseEntity
        +deleteReview(Long) ResponseEntity
    }
    class AdminQnaController {
        +getQnaList(Pageable) ResponseEntity
        +getQnaDetail(Long) ResponseEntity
        +answerQna(Long, QnaRequestDto) ResponseEntity
        +updateAnswer(Long, QnaRequestDto) ResponseEntity
        +deleteAnswer(Long) ResponseEntity
    }
    class AdminNoticeController {
        +getNoticeList(Pageable) ResponseEntity
        +insertNotice(NoticeRequestDto) ResponseEntity
        +updateNotice(Long, NoticeRequestDto) ResponseEntity
        +deleteNotice(Long) ResponseEntity
    }

    %% ── Service ──
    class AuthService {
        +signup(MemberRequestDto) void
        +signin(MemberRequestDto) MemberResponseDto
        +signout(String) void
        +validateDuplicateEmail(String) void
    }
    class MemberService {
        +getMyPage(Long) MemberResponseDto
        +updateInfo(Long, MemberRequestDto) void
        +updatePassword(Long, MemberRequestDto) void
        +withdraw(Long) void
        +loadUserByEmail(String) UserDetails
    }
    class ProductService {
        +getProductList(Pageable, String) Page
        +getProductDetail(Long) ProductResponseDto
        +insertProduct(ProductRequestDto) void
        +updateProduct(Long, ProductRequestDto) void
        +deleteProduct(Long) void
        +increaseViewCount(Long) void
    }
    class CartService {
        +getCart(Long) CartResponseDto
        +addItem(Long, CartRequestDto) void
        +updateQuantity(Long, CartRequestDto) void
        +removeItem(Long, Long) void
    }
    class OrderService {
        +createOrder(Long, OrderRequestDto) OrderResponseDto
        +getOrderList(Long, Pageable) Page
        +getOrderDetail(Long, Long) OrderResponseDto
        +cancelOrder(Long, Long) void
        +validateOwnership(Long, Long) void
    }
    class PaymentService {
        +requestPayment(PaymentRequestDto) PaymentResponseDto
        +completePayment(String, String) void
        +cancelPayment(Long) void
    }
    class DeliveryService {
        +getDeliveryStatus(Long) DeliveryResponseDto
        +registerTracking(Long, DeliveryRequestDto) void
        +updateDeliveryStatus(Long, DeliveryRequestDto) void
    }
    class ReviewService {
        +writeReview(Long, ReviewRequestDto) void
        +updateReview(Long, ReviewRequestDto, Long) void
        +deleteReview(Long, Long) void
        +validatePurchaser(Long, Long) void
    }
    class QnaService {
        +getQnaList(Pageable) Page
        +writeQuestion(Long, QnaRequestDto) void
        +answerQuestion(Long, QnaRequestDto, Long) void
        +updateQuestion(Long, QnaRequestDto, Long) void
        +deleteQuestion(Long, Long) void
    }
    class NoticeService {
        +getNoticeList(Pageable) Page
        +getNoticeDetail(Long) NoticeResponseDto
        +insertNotice(Long, NoticeRequestDto) void
        +updateNotice(Long, NoticeRequestDto) void
        +deleteNotice(Long) void
    }

    %% ── Security ──
    class JwtTokenProvider {
        +generateAccessToken(Long, String) String
        +generateRefreshToken(Long) String
        +validateToken(String) boolean
        +getMemberId(String) Long
        +getRole(String) String
    }
    class JwtAuthenticationFilter {
        +doFilterInternal(request, response, chain) void
        +resolveToken(HttpServletRequest) String
    }
    class CustomUserDetailsService {
        +loadUserByUsername(String) UserDetails
    }

    %% ── Repository ──
    class MemberRepository {
        <<interface>>
        +findByEmail(String) Optional~Member~
        +existsByEmail(String) boolean
    }
    class ProductRepository {
        <<interface>>
        +findAllWithFilter(Pageable, String) Page
        +findByIdAndProductStatus(Long, ProductStatus) Optional
    }
    class CartRepository {
        <<interface>>
        +findByMemberId(Long) Optional~Cart~
    }
    class OrderRepository {
        <<interface>>
        +findByMemberId(Long, Pageable) Page
        +findByIdAndMemberId(Long, Long) Optional
    }
    class PaymentRepository {
        <<interface>>
        +findByOrderId(Long) Optional~Payment~
    }
    class DeliveryRepository {
        <<interface>>
        +findByOrderId(Long) Optional~Delivery~
    }
    class ReviewRepository {
        <<interface>>
        +findByMemberId(Long, Pageable) Page
        +existsByMemberIdAndProductId(Long, Long) boolean
    }
    class QnaRepository {
        <<interface>>
        +findByDepthAndStatus(Integer, QnaStatus, Pageable) Page
        +findByIdAndDepth(Long, Integer) Optional
    }
    class NoticeRepository {
        <<interface>>
        +findAllByDeletedAtIsNull(Pageable) Page
        +findByIdAndDeletedAtIsNull(Long) Optional
    }

    %% ── Controller → Service 연결 ──
    AuthController --> AuthService
    MemberController --> MemberService
    ProductController --> ProductService
    CartController --> CartService
    OrderController --> OrderService
    PaymentController --> PaymentService
    DeliveryController --> DeliveryService
    ReviewController --> ReviewService
    QnaController --> QnaService
    NoticeController --> NoticeService

    AdminController --> MemberService
    AdminController --> OrderService
    AdminController --> ProductService
    AdminController --> QnaService
    AdminProductController --> ProductService
    AdminOrderController --> OrderService
    AdminPaymentController --> PaymentService
    AdminDeliveryController --> DeliveryService
    AdminReviewController --> ReviewService
    AdminQnaController --> QnaService
    AdminNoticeController --> NoticeService

    %% ── Service → Repository 연결 ──
    AuthService --> MemberRepository
    AuthService --> JwtTokenProvider
    MemberService --> MemberRepository
    MemberService --> CustomUserDetailsService
    ProductService --> ProductRepository
    CartService --> CartRepository
    OrderService --> OrderRepository
    OrderService --> ProductRepository
    OrderService --> CartRepository
    PaymentService --> PaymentRepository
    PaymentService --> OrderRepository
    DeliveryService --> DeliveryRepository
    DeliveryService --> OrderRepository
    ReviewService --> ReviewRepository
    ReviewService --> OrderRepository
    QnaService --> QnaRepository
    NoticeService --> NoticeRepository

    %% ── Security 연결 ──
    JwtAuthenticationFilter --> JwtTokenProvider
    JwtAuthenticationFilter --> CustomUserDetailsService
```

---

## 🔄 2. 시퀀스 다이어그램

---

### 2-1. 회원가입 (UC-M-001 / FM-001)

```mermaid
sequenceDiagram
    actor Client as Vue.js 클라이언트
    participant AC as AuthController
    participant AS as AuthService
    participant MR as MemberRepository
    participant DB as PostgreSQL

    Client->>AC: POST /signup
    Note over AC: MemberRequestDto { email, password, name, phone, address }
    AC->>AS: signup(MemberRequestDto)
    AS->>MR: existsByEmail(email)
    MR->>DB: SELECT COUNT(*) FROM member WHERE email = ?
    DB-->>MR: count
    alt 이메일 중복
        MR-->>AS: true
        AS-->>AC: throw DuplicateEmailException
        AC-->>Client: 409 Conflict "이미 사용 중인 이메일입니다."
    else 사용 가능
        MR-->>AS: false
        AS->>AS: BCryptPasswordEncoder.encode(password)
        AS->>MR: save(Member)
        MR->>DB: INSERT INTO member (email, password, name, phone, address, member_role='USER', member_status='ACTIVATE')
        DB-->>MR: saved Member
        MR-->>AS: Member
        AS-->>AC: void
        AC-->>Client: 201 Created "회원가입이 완료되었습니다."
    end
```

---

### 2-2. 로그인 & JWT 발급 (UC-M-002 / FM-002)

```mermaid
sequenceDiagram
    actor Client as Vue.js 클라이언트
    participant JF as JwtAuthenticationFilter
    participant AC as AuthController
    participant AS as AuthService
    participant CUDS as CustomUserDetailsService
    participant JTP as JwtTokenProvider
    participant MR as MemberRepository
    participant DB as PostgreSQL

    Client->>JF: POST /signin
    Note over JF: /signin 은 인증 제외 경로 → 필터 통과
    JF->>AC: POST /signin { MemberRequestDto }

    AC->>AS: signin(MemberRequestDto)
    AS->>MR: findByEmail(email)
    MR->>DB: SELECT * FROM member WHERE email = ?
    DB-->>MR: Member | null
    alt 회원 미존재
        MR-->>AS: Optional.empty()
        AS-->>AC: throw MemberNotFoundException
        AC-->>Client: 401 Unauthorized "이메일 또는 비밀번호가 올바르지 않습니다."
    else 회원 존재
        MR-->>AS: Optional[Member]
        AS->>CUDS: loadUserByUsername(email)
        CUDS->>MR: findByEmail(email)
        MR-->>CUDS: Member
        CUDS-->>AS: UserDetails
        AS->>AS: BCryptPasswordEncoder.matches(rawPw, encodedPw)
        alt 비밀번호 불일치
            AS-->>AC: throw InvalidPasswordException
            AC-->>Client: 401 Unauthorized
        else 비밀번호 일치
            AS->>AS: memberStatus == ACTIVATE 확인
            alt 비활성 / 탈퇴 계정
                AS-->>AC: throw InactiveMemberException
                AC-->>Client: 403 Forbidden "접근이 제한된 계정입니다."
            else 정상 계정
                AS->>JTP: generateAccessToken(memberId, role)
                JTP-->>AS: accessToken (단기)
                AS->>JTP: generateRefreshToken(memberId)
                JTP-->>AS: refreshToken (장기)
                AS-->>AC: MemberResponseDto { accessToken, refreshToken }
                AC-->>Client: 200 OK { accessToken, refreshToken }
            end
        end
    end
```

---

### 2-3. 인증 필터 처리 (JWT 보호 API 공통 흐름)

```mermaid
sequenceDiagram
    actor Client as Vue.js 클라이언트
    participant JF as JwtAuthenticationFilter
    participant JTP as JwtTokenProvider
    participant CUDS as CustomUserDetailsService
    participant Controller as XxxController

    Client->>JF: HTTP Request (Authorization: Bearer {accessToken})
    JF->>JF: resolveToken(request) → accessToken 추출
    JF->>JTP: validateToken(accessToken)
    alt 토큰 유효하지 않음 (만료 / 위변조)
        JTP-->>JF: false
        JF-->>Client: 401 Unauthorized "토큰이 유효하지 않습니다."
    else 토큰 유효
        JTP-->>JF: true
        JF->>JTP: getMemberId(accessToken)
        JTP-->>JF: memberId
        JF->>CUDS: loadUserByUsername(memberId)
        CUDS-->>JF: UserDetails
        JF->>JF: SecurityContextHolder에 Authentication 저장
        JF->>Controller: 요청 전달 (인증 완료)
        Controller-->>Client: 정상 응답
    end
```

---

### 2-4. 주문 생성 (UC-M-010 / FO-001)

```mermaid
sequenceDiagram
    actor Client as Vue.js 클라이언트
    participant OC as OrderController
    participant OS as OrderService
    participant CS as CartService
    participant PR as ProductRepository
    participant CR as CartRepository
    participant OR as OrderRepository
    participant DR as DeliveryRepository
    participant DB as PostgreSQL

    Client->>OC: POST /order
    Note over OC: OrderRequestDto { cartItemIds[], deliveryAddress, receiverName, receiverPhone }
    Note over OC: JwtAuthenticationFilter → memberId 추출 완료

    OC->>OS: createOrder(memberId, OrderRequestDto)
    OS->>CS: getCart(memberId)
    CS->>CR: findByMemberId(memberId)
    CR->>DB: SELECT * FROM cart_item WHERE cart_id = (SELECT id FROM cart WHERE member_id = ?)
    DB-->>CR: List~CartItem~
    CR-->>CS: Cart
    CS-->>OS: CartResponseDto

    loop 각 CartItem 재고 검증
        OS->>PR: findByIdAndProductStatus(productId, ACTIVATE)
        PR->>DB: SELECT * FROM product WHERE id = ? AND product_status = 'ACTIVATE'
        DB-->>PR: Product
        PR-->>OS: Product
        OS->>OS: product.stock >= quantity 확인
        alt 재고 부족
            OS-->>OC: throw OutOfStockException
            OC-->>Client: 409 Conflict "재고가 부족합니다."
        end
    end

    OS->>OS: 주문번호 생성 (ResponseUtil)
    OS->>OR: save(Order)
    OR->>DB: INSERT INTO "order" (member_id, number, order_status='PENDING', total_price, final_price, ...)
    DB-->>OR: Order

    loop 각 상품 스냅샷 저장
        OS->>OR: saveOrderItem(OrderItem)
        OR->>DB: INSERT INTO order_item (order_id, product_id, product_title, product_author, sale_price, quantity, item_total, is_reviewed=false)
        OS->>PR: decreaseStock(productId, quantity)
        PR->>DB: UPDATE product SET stock = stock - ? WHERE id = ?
    end

    OS->>DR: save(Delivery)
    DR->>DB: INSERT INTO delivery (order_id, delivery_status='READY')

    OS->>CS: clearCheckedItems(memberId, cartItemIds)
    CS->>CR: deleteCheckedItems(cartItemIds)
    CR->>DB: DELETE FROM cart_item WHERE id IN (...)

    OS-->>OC: OrderResponseDto { orderId, orderNumber }
    OC-->>Client: 201 Created { orderId, orderNumber }
```

---

### 2-5. 결제 요청 & 완료 처리 (UC-M-011 / FPAY-001~002)

```mermaid
sequenceDiagram
    actor Client as Vue.js 클라이언트
    participant PC as PaymentController
    participant PS as PaymentService
    participant OR as OrderRepository
    participant PR as PaymentRepository
    participant PG as PG 모듈(Mock)
    participant DB as PostgreSQL

    Client->>PC: POST /order/payment
    Note over PC: PaymentRequestDto { orderId, paymentMethod, amount }

    PC->>PS: requestPayment(PaymentRequestDto, memberId)
    PS->>OR: findByIdAndMemberId(orderId, memberId)
    OR->>DB: SELECT * FROM "order" WHERE id = ? AND member_id = ?
    DB-->>OR: Order
    OR-->>PS: Order

    PS->>PS: order.orderStatus == PENDING 확인
    PS->>PS: amount == order.finalPrice 금액 검증

    PS->>PR: save(Payment{paymentStatus=READY})
    PR->>DB: INSERT INTO payment (order_id, method, payment_status='READY', amount)
    DB-->>PR: Payment

    PS->>PG: requestPay(amount, orderId)
    PG-->>PS: pgTid

    PS-->>PC: PaymentResponseDto { pgTid }
    PC-->>Client: 200 OK { pgTid }
    Note over Client: PG 창에서 결제 진행

    Client->>PC: POST /order/payment/complete
    Note over PC: PaymentRequestDto { orderId, pgTid }
    PC->>PS: completePayment(orderId, pgTid)
    PS->>PG: verifyPayment(pgTid)

    alt PG 검증 실패
        PG-->>PS: FAILED
        PS->>PR: updatePaymentStatus(FAILED)
        PR->>DB: UPDATE payment SET payment_status = 'FAILED'
        PS-->>PC: throw PaymentFailedException
        PC-->>Client: 400 Bad Request "결제에 실패했습니다."
    else PG 검증 성공
        PG-->>PS: SUCCESS
        PS->>PR: updatePaymentStatus(PAID, pgTid, paidAt)
        PR->>DB: UPDATE payment SET payment_status = 'PAID', pg_tid = ?, paid_at = now()
        PS->>OR: updateOrderStatus(PAYED)
        OR->>DB: UPDATE "order" SET order_status = 'PAYED'
        PS-->>PC: void
        PC-->>Client: 200 OK "결제가 완료되었습니다."
    end
```

---

### 2-6. 관리자 배송 상태 변경 (UC-A-008 / FA-014)

```mermaid
sequenceDiagram
    actor Admin as 관리자(Vue.js)
    participant ADC as AdminDeliveryController
    participant DS as DeliveryService
    participant DR as DeliveryRepository
    participant OR as OrderRepository
    participant DB as PostgreSQL

    Admin->>ADC: PATCH /admin/delivery/{id}/status
    Note over ADC: DeliveryRequestDto { deliveryStatus }
    Note over ADC: JwtAuthenticationFilter → MANAGER/ADMIN 권한 확인

    ADC->>DS: updateDeliveryStatus(deliveryId, DeliveryRequestDto)
    DS->>DR: findByOrderId(deliveryId)
    DR->>DB: SELECT * FROM delivery WHERE id = ?
    DB-->>DR: Delivery
    DR-->>DS: Delivery

    DS->>DS: 상태 전이 순서 유효성 확인 (READY→SHIPPED→IN_TRANSIT→DELIVERED)

    alt 운송장 미등록 상태에서 SHIPPED 요청
        DS-->>ADC: throw TrackingNumberRequiredException
        ADC-->>Admin: 400 Bad Request "운송장 번호를 먼저 등록하세요."
    else 유효한 상태 전이
        DS->>DR: save(delivery.setDeliveryStatus(deliveryStatus))
        DR->>DB: UPDATE delivery SET delivery_status = ?, updated_at = now()
        alt deliveryStatus == DELIVERED
            DS->>OR: updateOrderStatus(orderId, APPROVAL)
            OR->>DB: UPDATE "order" SET order_status = 'APPROVAL'
            DS->>DR: save(delivery.setDeliveredAt(now()))
            DR->>DB: UPDATE delivery SET delivered_at = now()
        end
        DS-->>ADC: void
        ADC-->>Admin: 200 OK "배송 상태가 변경되었습니다."
    end
```

---

### 2-7. 리뷰 작성 (UC-M-015 / FR-002)

```mermaid
sequenceDiagram
    actor Client as Vue.js 클라이언트
    participant RC as ReviewController
    participant RS as ReviewService
    participant OR as OrderRepository
    participant RR as ReviewRepository
    participant FU as FileUtil
    participant DB as PostgreSQL

    Client->>RC: POST /review/write
    Note over RC: ReviewRequestDto { productId, rating, content, images[] }
    Note over RC: JwtAuthenticationFilter → memberId 추출 완료

    RC->>RS: writeReview(memberId, ReviewRequestDto)

    RS->>OR: findDeliveredItemByMemberAndProduct(memberId, productId)
    OR->>DB: SELECT oi.* FROM order_item oi JOIN "order" o ON o.id = oi.order_id\nWHERE o.member_id = ? AND oi.product_id = ?\nAND o.order_status = 'APPROVAL' AND oi.is_reviewed = false
    DB-->>OR: OrderItem | null

    alt 구매 이력 없음 / 이미 리뷰 작성
        OR-->>RS: null
        RS-->>RC: throw ReviewNotAllowedException
        RC-->>Client: 403 Forbidden "리뷰 작성 권한이 없습니다."
    else 구매 이력 확인됨
        OR-->>RS: OrderItem
        RS->>RR: existsByMemberIdAndProductId(memberId, productId)
        RR->>DB: SELECT COUNT(*) FROM review WHERE member_id = ? AND product_id = ? AND deleted_at IS NULL
        DB-->>RR: count
        alt 중복 리뷰
            RR-->>RS: true
            RS-->>RC: throw DuplicateReviewException
            RC-->>Client: 409 Conflict "이미 리뷰를 작성하셨습니다."
        else 최초 작성
            RR-->>RS: false
            RS->>RS: rating 1~5 범위 검증
            RS->>RR: save(Review)
            RR->>DB: INSERT INTO review (member_id, product_id, rating, content)
            DB-->>RR: Review

            loop 이미지 첨부 (최대 5장)
                RS->>FU: uploadFile(imageFile)
                FU-->>RS: imageUrl
                RS->>RR: saveReviewImage(ReviewImage)
                RR->>DB: INSERT INTO review_image (review_id, image_url)
            end

            RS->>OR: updateIsReviewed(orderItemId, true)
            OR->>DB: UPDATE order_item SET is_reviewed = true WHERE id = ?

            RS-->>RC: void
            RC-->>Client: 201 Created "리뷰가 등록되었습니다."
        end
    end
```

---

### 2-8. QnA 질문 등록 & 관리자 답변 (UC-M-020 / UC-A-010)

```mermaid
sequenceDiagram
    actor Member as 회원(Vue.js)
    actor Admin as 관리자(Vue.js)
    participant QC as QnaController
    participant AQC as AdminQnaController
    participant QS as QnaService
    participant QR as QnaRepository
    participant DB as PostgreSQL

    Note over Member, DB: [1단계] 회원 질문 등록
    Member->>QC: POST /qna/write
    Note over QC: QnaRequestDto { title, content, category, isSecret, productId }
    QC->>QS: writeQuestion(memberId, QnaRequestDto)
    QS->>QR: save(Qna{depth=0, qnaStatus=WAITING, parentId=null})
    QR->>DB: INSERT INTO qna (member_id, depth=0, title, content, qna_status='WAITING', is_secret, parent_id=NULL)
    DB-->>QR: Qna
    QR-->>QS: Qna
    QS-->>QC: void
    QC-->>Member: 201 Created "문의가 등록되었습니다."

    Note over Admin, DB: [2단계] 관리자 답변 등록
    Admin->>AQC: POST /admin/qna/{id}/insert
    Note over AQC: QnaRequestDto { content }
    Note over AQC: JwtAuthenticationFilter → MANAGER/ADMIN 권한 확인
    AQC->>QS: answerQuestion(parentId, QnaRequestDto, adminMemberId)

    QS->>QR: findByIdAndDepth(parentId, 0)
    QR->>DB: SELECT * FROM qna WHERE id = ? AND depth = 0 AND deleted_at IS NULL
    DB-->>QR: Qna (질문)
    QR-->>QS: Qna

    QS->>QS: qnaStatus == WAITING || PROCESSING 확인
    alt 이미 COMPLETE 상태
        QS-->>AQC: throw AlreadyAnsweredException
        AQC-->>Admin: 409 Conflict "이미 답변이 완료된 문의입니다."
    else 답변 가능
        QS->>QR: save(Qna{depth=1, parentId=parentId})
        QR->>DB: INSERT INTO qna (member_id, parent_id=?, depth=1, content, qna_status='COMPLETE', answered_at=now())
        QS->>QR: updateQnaStatus(parentId, COMPLETE)
        QR->>DB: UPDATE qna SET qna_status = 'COMPLETE', answered_at = now() WHERE id = ?
        QS-->>AQC: void
        AQC-->>Admin: 201 Created "답변이 등록되었습니다."
    end

    Note over Member: [3단계] 회원 답변 확인
    Member->>QC: GET /mypage/qna/{id}
    QC->>QS: getQnaDetail(qnaId, memberId)
    QS->>QR: findByIdWithChildren(qnaId)
    QR->>DB: SELECT * FROM qna WHERE id = ? OR parent_id = ? ORDER BY depth ASC
    DB-->>QR: List~Qna~ (질문 + 답변)
    QR-->>QS: List~Qna~
    QS->>QS: isSecret 권한 확인 (본인 또는 관리자만 열람)
    QS-->>QC: QnaResponseDto
    QC-->>Member: 200 OK { question, answer, qnaStatus: COMPLETE }
```

---

### 2-9. 관리자 대시보드 조회 (UC-A-001 / FA-001)

```mermaid
sequenceDiagram
    actor Admin as 관리자(Vue.js)
    participant ADC as AdminController
    participant OS as OrderService
    participant QS as QnaService
    participant PS as ProductService
    participant MS as MemberService
    participant OR as OrderRepository
    participant QR as QnaRepository
    participant PR as ProductRepository
    participant MR as MemberRepository
    participant DB as PostgreSQL

    Admin->>ADC: GET /admin
    Note over ADC: JwtAuthenticationFilter → MANAGER/ADMIN 권한 확인
    ADC->>OS: getPendingOrderSummary()
    ADC->>QS: getWaitingQnaSummary()
    ADC->>PS: getLowStockProducts()
    ADC->>OS: getSalesStatistics()
    ADC->>MS: getNewMemberCount()

    par 미승인 주문 집계
        OS->>OR: findTopByOrderStatus(PENDING, limit=10)
        OR->>DB: SELECT * FROM "order" WHERE order_status = 'PENDING' ORDER BY ordered_at DESC LIMIT 10
        DB-->>OR: List~Order~
        OR-->>OS: List~Order~
    and 미답변 QnA 집계
        QS->>QR: findTopByQnaStatusAndDepth(WAITING, 0, limit=10)
        QR->>DB: SELECT * FROM qna WHERE qna_status = 'WAITING' AND depth = 0 ORDER BY created_at DESC LIMIT 10
        DB-->>QR: List~Qna~
        QR-->>QS: List~Qna~
    and 재고 부족 상품 집계
        PS->>PR: findLowStockProducts(threshold=10)
        PR->>DB: SELECT * FROM product WHERE stock <= 10 AND product_status = 'ACTIVATE' ORDER BY stock ASC
        DB-->>PR: List~Product~
        PR-->>PS: List~Product~
    and 매출 집계
        OS->>OR: sumFinalPriceByPeriod(TODAY, MONTH)
        OR->>DB: SELECT SUM(final_price) FROM "order" WHERE order_status IN ('PAYED','APPROVAL') AND ordered_at >= ?
        DB-->>OR: Long
        OR-->>OS: Long
    and 신규 회원 집계
        MS->>MR: countByCreatedAtAfter(startOfToday)
        MR->>DB: SELECT COUNT(*) FROM member WHERE created_at >= ?
        DB-->>MR: Long
        MR-->>MS: Long
    end

    ADC-->>Admin: 200 OK { pendingOrders, waitingQnas, lowStockProducts, todaySales, monthSales, newMembersToday }
```
