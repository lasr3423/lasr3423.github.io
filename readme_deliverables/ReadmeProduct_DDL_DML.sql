-- ============================================================
-- 온라인 도서 판매 쇼핑몰 - Product 도메인 DDL / DML
-- DB      : PostgreSQL
-- Schema  : public
-- 작성일  : 2026.03.17
-- 포함 테이블 :
--   category, product, product_image,
--   cart, cart_item,
--   "order", order_item, payment, delivery
--
-- ⚠️  주의 : order 는 SQL 예약어이므로 반드시 큰따옴표("order")로 감싸서 사용
-- ⚠️  테이블 생성 순서 : FK 의존성에 따라 순서를 지켜야 함
-- ============================================================


-- ============================================================
-- [DDL] ENUM 타입 정의
-- ============================================================

CREATE TYPE category_status  AS ENUM ('ACTIVATE', 'DEACTIVATE', 'DELETE');
CREATE TYPE product_type     AS ENUM ('DOMESTIC', 'FOREIGN', 'JAPAN');
CREATE TYPE product_status   AS ENUM ('ACTIVATE', 'DEACTIVATE', 'DELETE');
CREATE TYPE order_status     AS ENUM ('PENDING', 'PAYED', 'APPROVAL', 'CANCELED');
CREATE TYPE payment_status   AS ENUM ('READY', 'PAID', 'CANCELLED', 'FAILED');
CREATE TYPE delivery_status  AS ENUM ('READY', 'SHIPPED', 'IN_TRANSIT', 'DELIVERED', 'FAILED');


-- ============================================================
-- [DDL] category 테이블 (자기 참조 계층 구조)
-- ============================================================

CREATE TABLE IF NOT EXISTS category (
    id              BIGSERIAL        PRIMARY KEY,
    parent_id       BIGINT           REFERENCES category (id),
    name            VARCHAR(50)      NOT NULL,
    level           INT              NOT NULL,           -- 1=대분류, 2=중분류
    sort_order      INT              NOT NULL DEFAULT 0,
    category_status category_status  NOT NULL DEFAULT 'ACTIVATE',
    created_at      TIMESTAMP        NOT NULL DEFAULT now(),
    updated_at      TIMESTAMP
);


-- ============================================================
-- [DDL] product 테이블
-- ⚠️ review_id 는 review 도메인(review 테이블)을 참조하는 FK
-- ============================================================

CREATE TABLE IF NOT EXISTS product (
    id              BIGSERIAL        PRIMARY KEY,
    category_id     BIGINT           NOT NULL REFERENCES category (id),
    review_id       BIGINT           REFERENCES review (id),  -- review 도메인 FK
    title           VARCHAR(300)     NOT NULL,
    author          VARCHAR(200)     NOT NULL,
    description     TEXT,
    price           INT              NOT NULL DEFAULT 0,
    discount_rate   NUMERIC(5, 2)    NOT NULL DEFAULT 0,
    sale_price      INT              NOT NULL DEFAULT 0,
    stock           INT              NOT NULL DEFAULT 0,
    thumbnail       VARCHAR(500),
    type            product_type     NOT NULL DEFAULT 'DOMESTIC',
    view_count      INT              NOT NULL DEFAULT 0,
    sales_count     INT              NOT NULL DEFAULT 0,
    product_status  product_status   NOT NULL DEFAULT 'ACTIVATE',
    created_at      TIMESTAMP        NOT NULL DEFAULT now(),
    updated_at      TIMESTAMP,
    deleted_at      TIMESTAMP
);


-- ============================================================
-- [DDL] product_image 테이블
-- ============================================================

CREATE TABLE IF NOT EXISTS product_image (
    id          BIGSERIAL       PRIMARY KEY,
    book_id     BIGINT          NOT NULL REFERENCES product (id) ON DELETE CASCADE,
    url         VARCHAR(500)    NOT NULL,
    type        VARCHAR(20)     NOT NULL DEFAULT 'SUB',  -- MAIN / SUB
    sort_order  INT             NOT NULL DEFAULT 0,
    created_at  TIMESTAMP       NOT NULL DEFAULT now()
);


-- ============================================================
-- [DDL] cart 테이블 (회원 1 : 장바구니 1)
-- ============================================================

CREATE TABLE IF NOT EXISTS cart (
    id          BIGSERIAL    PRIMARY KEY,
    member_id   BIGINT       NOT NULL UNIQUE REFERENCES member (id),
    created_at  TIMESTAMP    NOT NULL DEFAULT now(),
    updated_at  TIMESTAMP
);


-- ============================================================
-- [DDL] cart_item 테이블
-- ============================================================

CREATE TABLE IF NOT EXISTS cart_item (
    id          BIGSERIAL    PRIMARY KEY,
    cart_id     BIGINT       NOT NULL REFERENCES cart (id) ON DELETE CASCADE,
    product_id  BIGINT       NOT NULL REFERENCES product (id),
    quantity    INT          NOT NULL DEFAULT 1,
    is_checked  BOOLEAN      NOT NULL DEFAULT true,
    created_at  TIMESTAMP    NOT NULL DEFAULT now(),
    updated_at  TIMESTAMP
);


-- ============================================================
-- [DDL] "order" 테이블
-- ⚠️ order 는 SQL 예약어 → 큰따옴표 필수
-- ============================================================

CREATE TABLE IF NOT EXISTS "order" (
    id                       BIGSERIAL       PRIMARY KEY,
    member_id                BIGINT          NOT NULL REFERENCES member (id),
    number                   VARCHAR(30)     NOT NULL UNIQUE,  -- 예: ORD-20260315-000001
    order_status             order_status    NOT NULL DEFAULT 'PENDING',
    total_price              INT             NOT NULL DEFAULT 0,
    discount_amount          INT             NOT NULL DEFAULT 0,
    final_price              INT             NOT NULL DEFAULT 0,
    receiver_name            VARCHAR(50)     NOT NULL,
    receiver_phone           VARCHAR(20)     NOT NULL,
    delivery_address         VARCHAR(255)    NOT NULL,
    delivery_address_detail  VARCHAR(255),
    delivery_zip_code        VARCHAR(10)     NOT NULL,
    delivery_memo            VARCHAR(300),
    ordered_at               TIMESTAMP       NOT NULL DEFAULT now(),
    created_at               TIMESTAMP       NOT NULL DEFAULT now(),
    updated_at               TIMESTAMP,
    cancelled_at             TIMESTAMP
);


-- ============================================================
-- [DDL] order_item 테이블
-- ============================================================

CREATE TABLE IF NOT EXISTS order_item (
    id              BIGSERIAL       PRIMARY KEY,
    order_id        BIGINT          NOT NULL REFERENCES "order" (id) ON DELETE CASCADE,
    product_id      BIGINT          NOT NULL REFERENCES product (id),
    product_title   VARCHAR(300)    NOT NULL,    -- 주문 당시 도서명 스냅샷
    product_author  VARCHAR(200)    NOT NULL,    -- 주문 당시 저자 스냅샷
    sale_price      INT             NOT NULL DEFAULT 0,
    quantity        INT             NOT NULL DEFAULT 1,
    item_total      INT             NOT NULL DEFAULT 0,  -- sale_price × quantity
    is_reviewed     BOOLEAN         NOT NULL DEFAULT false,
    created_at      TIMESTAMP       NOT NULL DEFAULT now()
);


-- ============================================================
-- [DDL] payment 테이블
-- ============================================================

CREATE TABLE IF NOT EXISTS payment (
    id              BIGSERIAL        PRIMARY KEY,
    order_id        BIGINT           NOT NULL REFERENCES "order" (id),
    method          VARCHAR(30)      NOT NULL,
    payment_status  payment_status   NOT NULL DEFAULT 'READY',
    amount          INT              NOT NULL DEFAULT 0,
    pg_tid          VARCHAR(100),
    paid_at         TIMESTAMP,
    cancel_reason   VARCHAR(300),
    created_at      TIMESTAMP        NOT NULL DEFAULT now(),
    updated_at      TIMESTAMP,
    cancelled_at    TIMESTAMP
);


-- ============================================================
-- [DDL] delivery 테이블 (주문 1 : 배송 1)
-- ============================================================

CREATE TABLE IF NOT EXISTS delivery (
    id               BIGSERIAL        PRIMARY KEY,
    order_id         BIGINT           NOT NULL UNIQUE REFERENCES "order" (id),
    courier          VARCHAR(50),
    tracking_number  VARCHAR(100),
    status           delivery_status  NOT NULL DEFAULT 'READY',
    created_at       TIMESTAMP        NOT NULL DEFAULT now(),
    updated_at       TIMESTAMP,
    shipped_at       TIMESTAMP,
    delivered_at     TIMESTAMP
);


-- ============================================================
-- [DDL] 인덱스 생성
-- ============================================================

-- 카테고리 계층 조회
CREATE INDEX IF NOT EXISTS idx_category_parent_id    ON category (parent_id);
CREATE INDEX IF NOT EXISTS idx_category_level        ON category (level);

-- 상품 목록 조회 (카테고리, 상태, 판매량, 조회수)
CREATE INDEX IF NOT EXISTS idx_product_category_id   ON product (category_id);
CREATE INDEX IF NOT EXISTS idx_product_status        ON product (product_status);
CREATE INDEX IF NOT EXISTS idx_product_sales_count   ON product (sales_count DESC);
CREATE INDEX IF NOT EXISTS idx_product_title         ON product (title);

-- 장바구니 항목 조회
CREATE INDEX IF NOT EXISTS idx_cart_item_cart_id     ON cart_item (cart_id);

-- 주문 목록 조회 (회원별, 상태별)
CREATE INDEX IF NOT EXISTS idx_order_member_id       ON "order" (member_id);
CREATE INDEX IF NOT EXISTS idx_order_status          ON "order" (order_status);
CREATE INDEX IF NOT EXISTS idx_order_ordered_at      ON "order" (ordered_at DESC);

-- 주문 항목 조회
CREATE INDEX IF NOT EXISTS idx_order_item_order_id   ON order_item (order_id);


-- ============================================================
-- ============================================================
-- [DML] CATEGORY
-- ============================================================
-- ============================================================


-- ============================================================
-- [DML] 대분류 카테고리 전체 조회
-- ============================================================

SELECT id, name, sort_order
FROM category
WHERE parent_id IS NULL
  AND category_status = 'ACTIVATE'
ORDER BY sort_order ASC;


-- ============================================================
-- [DML] 특정 대분류의 중분류 목록 조회
-- ============================================================

SELECT id, name, sort_order
FROM category
WHERE parent_id = 1        -- 대분류 id
  AND category_status = 'ACTIVATE'
ORDER BY sort_order ASC;


-- ============================================================
-- [DML] 전체 카테고리 계층 조회 (대분류 + 중분류 한 번에)
-- ============================================================

SELECT
    p.id   AS parent_id,
    p.name AS parent_name,
    c.id   AS child_id,
    c.name AS child_name,
    c.sort_order
FROM category p
LEFT JOIN category c
    ON c.parent_id = p.id
    AND c.category_status = 'ACTIVATE'
WHERE p.parent_id IS NULL
  AND p.category_status = 'ACTIVATE'
ORDER BY p.sort_order ASC, c.sort_order ASC;


-- ============================================================
-- ============================================================
-- [DML] PRODUCT
-- ============================================================
-- ============================================================


-- ============================================================
-- [DML] 상품 등록 (관리자)
-- ============================================================

INSERT INTO product (
    category_id, title, author, description,
    price, discount_rate, sale_price,
    stock, thumbnail, type
)
VALUES (
    1,
    '클린 코드',
    '로버트 C. 마틴',
    '좋은 코드를 작성하는 방법에 대한 가이드',
    25000, 10.00, 22500,
    100, '/images/clean-code.jpg', 'DOMESTIC'
);


-- ============================================================
-- [DML] 상품 목록 조회 - 카테고리별 (사용자)
-- ============================================================

SELECT
    p.id, p.title, p.author, p.thumbnail,
    p.price, p.discount_rate, p.sale_price,
    p.sales_count, p.view_count
FROM product p
WHERE p.category_id = 1
  AND p.product_status = 'ACTIVATE'
ORDER BY p.created_at DESC
LIMIT 20 OFFSET 0;


-- ============================================================
-- [DML] 상품 검색 - 제목 또는 저자 (사용자)
-- ============================================================

SELECT
    p.id, p.title, p.author, p.thumbnail,
    p.price, p.discount_rate, p.sale_price,
    p.sales_count
FROM product p
WHERE (p.title  ILIKE '%검색어%'
   OR  p.author ILIKE '%검색어%')
  AND p.product_status = 'ACTIVATE'
ORDER BY p.sales_count DESC
LIMIT 20 OFFSET 0;


-- ============================================================
-- [DML] 상품 상세 조회 + 조회수 1 증가 (사용자)
-- ============================================================

-- Step 1: 조회수 증가
UPDATE product
SET view_count = view_count + 1
WHERE id = 1
  AND product_status = 'ACTIVATE';

-- Step 2: 상품 상세 정보 조회
SELECT
    p.id, p.title, p.author, p.description,
    p.price, p.discount_rate, p.sale_price,
    p.stock, p.thumbnail, p.type,
    p.view_count, p.sales_count,
    c.name AS category_name
FROM product p
JOIN category c ON c.id = p.category_id
WHERE p.id = 1
  AND p.product_status = 'ACTIVATE';


-- ============================================================
-- [DML] 인기 상품 조회 - 판매량 상위 10개 (메인 화면)
-- ============================================================

SELECT id, title, author, thumbnail, sale_price, sales_count
FROM product
WHERE product_status = 'ACTIVATE'
ORDER BY sales_count DESC
LIMIT 10;


-- ============================================================
-- [DML] 상품 수정 (관리자)
-- ============================================================

UPDATE product
SET title         = '클린 코드 (개정판)',
    price         = 28000,
    discount_rate = 15.00,
    sale_price    = 23800,
    stock         = 150,
    updated_at    = now()
WHERE id = 1
  AND product_status != 'DELETE';


-- ============================================================
-- [DML] 상품 삭제 (관리자, Soft Delete)
-- ============================================================

UPDATE product
SET product_status = 'DELETE',
    deleted_at     = now(),
    updated_at     = now()
WHERE id = 1;


-- ============================================================
-- [DML] 전체 상품 목록 조회 (관리자, 페이징)
-- ============================================================

SELECT
    p.id, p.title, p.author,
    c.name AS category_name,
    p.price, p.sale_price, p.stock,
    p.product_status, p.created_at
FROM product p
JOIN category c ON c.id = p.category_id
ORDER BY p.created_at DESC
LIMIT 20 OFFSET 0;


-- ============================================================
-- ============================================================
-- [DML] PRODUCT_IMAGE
-- ============================================================
-- ============================================================


-- ============================================================
-- [DML] 상품 이미지 등록
-- ============================================================

INSERT INTO product_image (book_id, url, type, sort_order)
VALUES
    (1, '/images/clean-code-main.jpg', 'MAIN', 0),
    (1, '/images/clean-code-sub1.jpg', 'SUB',  1),
    (1, '/images/clean-code-sub2.jpg', 'SUB',  2);


-- ============================================================
-- [DML] 특정 상품 이미지 목록 조회
-- ============================================================

SELECT id, url, type, sort_order
FROM product_image
WHERE book_id = 1
ORDER BY sort_order ASC;


-- ============================================================
-- [DML] 상품 이미지 삭제
-- ============================================================

DELETE FROM product_image
WHERE id = 1;


-- ============================================================
-- ============================================================
-- [DML] CART / CART_ITEM
-- ============================================================
-- ============================================================


-- ============================================================
-- [DML] 장바구니 생성 (회원 최초 로그인 또는 가입 시)
-- ============================================================

INSERT INTO cart (member_id)
VALUES (1);


-- ============================================================
-- [DML] 장바구니에 상품 담기
-- 동일 상품이 이미 있으면 수량 증가 (UPSERT)
-- ============================================================

INSERT INTO cart_item (cart_id, product_id, quantity)
VALUES (1, 10, 1)
ON CONFLICT DO NOTHING;  -- 중복 처리는 애플리케이션 레이어에서 수량 증가로 처리

-- 이미 담긴 상품인 경우 수량 증가
UPDATE cart_item
SET quantity   = quantity + 1,
    updated_at = now()
WHERE cart_id  = 1
  AND product_id = 10;


-- ============================================================
-- [DML] 장바구니 목록 조회 (상품 정보 JOIN, 마이페이지)
-- ============================================================

SELECT
    ci.id           AS cart_item_id,
    ci.quantity,
    ci.is_checked,
    p.id            AS product_id,
    p.title,
    p.author,
    p.thumbnail,
    p.sale_price,
    (p.sale_price * ci.quantity) AS subtotal
FROM cart_item ci
JOIN cart    c  ON c.id  = ci.cart_id
JOIN product p  ON p.id  = ci.product_id
WHERE c.member_id       = 1
  AND p.product_status  = 'ACTIVATE'
ORDER BY ci.created_at DESC;


-- ============================================================
-- [DML] 장바구니 항목 수량 변경
-- ============================================================

UPDATE cart_item
SET quantity   = 3,
    updated_at = now()
WHERE id      = 1
  AND cart_id = 1;


-- ============================================================
-- [DML] 장바구니 항목 개별 삭제
-- ============================================================

DELETE FROM cart_item
WHERE id      = 1
  AND cart_id = 1;


-- ============================================================
-- [DML] 체크된 항목만 조회 (주문 진행 시)
-- ============================================================

SELECT
    ci.product_id,
    p.title, p.author,
    p.sale_price,
    ci.quantity,
    (p.sale_price * ci.quantity) AS subtotal
FROM cart_item ci
JOIN product p ON p.id = ci.product_id
WHERE ci.cart_id    = 1
  AND ci.is_checked = true
  AND p.product_status = 'ACTIVATE';


-- ============================================================
-- ============================================================
-- [DML] ORDER / ORDER_ITEM
-- ============================================================
-- ============================================================


-- ============================================================
-- [DML] 주문 생성
-- ============================================================

INSERT INTO "order" (
    member_id, number, order_status,
    total_price, discount_amount, final_price,
    receiver_name, receiver_phone,
    delivery_address, delivery_address_detail,
    delivery_zip_code, delivery_memo
)
VALUES (
    1,
    'ORD-20260317-000001',
    'PENDING',
    45000, 4500, 40500,
    '홍길동', '010-1234-5678',
    '서울시 강남구 테헤란로 123', '101호',
    '06234', '부재 시 경비실 맡겨주세요'
);


-- ============================================================
-- [DML] 주문 항목 등록 (주문 생성과 함께 처리)
-- ============================================================

INSERT INTO order_item (
    order_id, product_id,
    product_title, product_author,
    sale_price, quantity, item_total
)
VALUES
    (1, 10, '클린 코드', '로버트 C. 마틴', 22500, 1, 22500),
    (1, 11, '리팩터링', '마틴 파울러',     18000, 1, 18000);


-- ============================================================
-- [DML] 회원 주문 목록 조회 - 최근순, 마이페이지
-- ============================================================

SELECT
    o.id, o.number, o.order_status,
    o.final_price, o.ordered_at
FROM "order" o
WHERE o.member_id = 1
ORDER BY o.ordered_at DESC
LIMIT 5 OFFSET 0;


-- ============================================================
-- [DML] 주문 상세 조회 (주문 항목 포함)
-- ============================================================

SELECT
    o.id            AS order_id,
    o.number,
    o.order_status,
    o.total_price,
    o.discount_amount,
    o.final_price,
    o.receiver_name,
    o.receiver_phone,
    o.delivery_address,
    o.delivery_zip_code,
    o.ordered_at,
    oi.id           AS item_id,
    oi.product_title,
    oi.product_author,
    oi.sale_price,
    oi.quantity,
    oi.item_total,
    oi.is_reviewed
FROM "order" o
JOIN order_item oi ON oi.order_id = o.id
WHERE o.id        = 1
  AND o.member_id = 1;


-- ============================================================
-- [DML] 주문 취소 (회원)
-- order_status → CANCELED, cancelled_at 기록
-- ============================================================

UPDATE "order"
SET order_status = 'CANCELED',
    cancelled_at = now(),
    updated_at   = now()
WHERE id        = 1
  AND member_id = 1
  AND order_status = 'PENDING';  -- 결제 전 상태만 취소 가능


-- ============================================================
-- [DML] 신규 주문 목록 조회 (관리자 - 승인 대기)
-- PAYED 상태 (결제 완료, 아직 미승인)
-- ============================================================

SELECT
    o.id, o.number, o.member_id,
    o.final_price, o.ordered_at
FROM "order" o
WHERE o.order_status = 'PAYED'
ORDER BY o.ordered_at ASC;


-- ============================================================
-- [DML] 주문 승인 (관리자)
-- PAYED → APPROVAL
-- ============================================================

UPDATE "order"
SET order_status = 'APPROVAL',
    updated_at   = now()
WHERE id           = 1
  AND order_status = 'PAYED';


-- ============================================================
-- ============================================================
-- [DML] PAYMENT
-- ============================================================
-- ============================================================


-- ============================================================
-- [DML] 결제 정보 생성 (주문 생성 직후)
-- ============================================================

INSERT INTO payment (order_id, method, payment_status, amount)
VALUES (1, 'CARD', 'READY', 40500);


-- ============================================================
-- [DML] 결제 완료 처리 (PG사 콜백 수신 후)
-- READY → PAID, pg_tid 기록
-- ============================================================

UPDATE payment
SET payment_status = 'PAID',
    pg_tid         = 'PG-UUID-20260317-XXXXX',
    paid_at        = now(),
    updated_at     = now()
WHERE order_id        = 1
  AND payment_status  = 'READY';

-- 결제 완료 후 order_status 도 함께 변경 (PENDING → PAYED)
UPDATE "order"
SET order_status = 'PAYED',
    updated_at   = now()
WHERE id = 1
  AND order_status = 'PENDING';


-- ============================================================
-- [DML] 결제 취소
-- PAID → CANCELLED
-- ============================================================

UPDATE payment
SET payment_status = 'CANCELLED',
    cancel_reason  = '고객 요청 취소',
    cancelled_at   = now(),
    updated_at     = now()
WHERE order_id       = 1
  AND payment_status = 'PAID';


-- ============================================================
-- ============================================================
-- [DML] DELIVERY
-- ============================================================
-- ============================================================


-- ============================================================
-- [DML] 배송 정보 생성 (주문 승인 후)
-- ============================================================

INSERT INTO delivery (order_id, status)
VALUES (1, 'READY');


-- ============================================================
-- [DML] 배송 시작 처리 (운송장 등록)
-- READY → SHIPPED
-- ============================================================

UPDATE delivery
SET courier         = 'CJ대한통운',
    tracking_number = '123456789012',
    status          = 'SHIPPED',
    shipped_at      = now(),
    updated_at      = now()
WHERE order_id = 1
  AND status   = 'READY';


-- ============================================================
-- [DML] 배송 완료 처리
-- IN_TRANSIT → DELIVERED
-- ============================================================

UPDATE delivery
SET status       = 'DELIVERED',
    delivered_at = now(),
    updated_at   = now()
WHERE order_id = 1
  AND status   = 'IN_TRANSIT';


-- ============================================================
-- [DML] 배송 상태 조회 (주문 상세 페이지)
-- ============================================================

SELECT
    d.status,
    d.courier,
    d.tracking_number,
    d.shipped_at,
    d.delivered_at
FROM delivery d
JOIN "order" o ON o.id = d.order_id
WHERE o.id        = 1
  AND o.member_id = 1;
