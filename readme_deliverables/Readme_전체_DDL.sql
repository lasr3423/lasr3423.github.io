-- ============================================================
-- 온라인 도서 판매 쇼핑몰 (README 팀) - 전체 DDL
-- DB      : PostgreSQL
-- Schema  : public
-- 작성일  : 2026.03.22
-- 기준문서 : 최종/DB설계서 최종.md
--
-- ⚠️  실행 순서 주의: FK 의존성에 따라 순서를 반드시 지켜야 함
-- ⚠️  order 는 SQL 예약어 → 큰따옴표("order") 필수
-- ⚠️  기존 ReadmeMember_DDL_DML.sql, ReadmeProduct_DDL_DML.sql과
--      ENUM 명칭이 다를 수 있음 → 본 파일 기준(DB설계서 최종)으로 통일
-- ============================================================


-- ============================================================
-- [STEP 0] 기존 ENUM 타입 및 테이블 정리 (재실행 시)
-- 개발 환경에서만 사용 / 운영 환경에서는 절대 실행 금지
-- ============================================================

-- DROP TABLE IF EXISTS review_reaction, review_image, review, qna, notice CASCADE;
-- DROP TABLE IF EXISTS delivery, payment, order_item, "order" CASCADE;
-- DROP TABLE IF EXISTS cart_item, cart CASCADE;
-- DROP TABLE IF EXISTS product_image, product CASCADE;
-- DROP TABLE IF EXISTS category_sub, category_top CASCADE;
-- DROP TABLE IF EXISTS member CASCADE;
-- DROP TYPE IF EXISTS member_role, member_status CASCADE;
-- DROP TYPE IF EXISTS category_top_status, category_sub_status CASCADE;
-- DROP TYPE IF EXISTS product_status CASCADE;
-- DROP TYPE IF EXISTS order_status, payment_status, delivery_status CASCADE;
-- DROP TYPE IF EXISTS qna_status CASCADE;


-- ============================================================
-- [STEP 1] ENUM 타입 정의
-- ============================================================

-- 회원 권한 (USER: 일반회원, MANAGER: 운영자, ADMIN: 최고관리자)
CREATE TYPE member_role AS ENUM ('USER', 'MANAGER', 'ADMIN');

-- 회원 상태 (ACTIVATE: 활성, DEACTIVATE: 강퇴, DELETE: 탈퇴)
CREATE TYPE member_status AS ENUM ('ACTIVATE', 'DEACTIVATE', 'DELETE');

-- 대분류 카테고리 상태
CREATE TYPE category_top_status AS ENUM ('ACTIVATE', 'DEACTIVATE', 'DELETE');

-- 소분류 카테고리 상태
CREATE TYPE category_sub_status AS ENUM ('ACTIVATE', 'DEACTIVATE', 'DELETE');

-- 상품 상태 (ACTIVATE: 판매중, DEACTIVATE: 판매중지, DELETE: 삭제)
CREATE TYPE product_status AS ENUM ('ACTIVATE', 'DEACTIVATE', 'DELETE');

-- 주문 상태 (PENDING: 결제대기, PAYED: 결제완료, APPROVAL: 승인, CANCELED: 취소)
CREATE TYPE order_status AS ENUM ('PENDING', 'PAYED', 'APPROVAL', 'CANCELED');

-- 결제 상태
CREATE TYPE payment_status AS ENUM ('READY', 'PAID', 'CANCELLED', 'FAILED');

-- 배송 상태
CREATE TYPE delivery_status AS ENUM ('READY', 'SHIPPED', 'IN_TRANSIT', 'DELIVERED', 'FAILED');

-- QnA 답변 상태
CREATE TYPE qna_status AS ENUM ('WAITING', 'PROCESSING', 'COMPLETE');


-- ============================================================
-- [STEP 2] 테이블 생성
-- ============================================================


-- ============================================================
-- 도메인: 회원 (Member)
-- ============================================================

CREATE TABLE IF NOT EXISTS member (
    id            BIGSERIAL      PRIMARY KEY,
    email         VARCHAR(100)   NOT NULL UNIQUE,
    password      VARCHAR(255)   NOT NULL,
    name          VARCHAR(50)    NOT NULL,
    phone         VARCHAR(20)    NOT NULL,
    address       VARCHAR(100)   NOT NULL,
    member_role   member_role    NOT NULL DEFAULT 'USER',
    member_status member_status  NOT NULL DEFAULT 'ACTIVATE',
    created_at    TIMESTAMP      NOT NULL DEFAULT now(),
    updated_at    TIMESTAMP,
    deleted_at    TIMESTAMP
);

COMMENT ON TABLE  member               IS '회원 정보';
COMMENT ON COLUMN member.id            IS '회원 고유 식별자';
COMMENT ON COLUMN member.email         IS '이메일 주소 (로그인 ID, UNIQUE)';
COMMENT ON COLUMN member.password      IS '비밀번호 (BCrypt 암호화)';
COMMENT ON COLUMN member.name          IS '이름';
COMMENT ON COLUMN member.phone         IS '전화번호';
COMMENT ON COLUMN member.address       IS '주소';
COMMENT ON COLUMN member.member_role   IS '권한: USER | MANAGER | ADMIN';
COMMENT ON COLUMN member.member_status IS '상태: ACTIVATE | DEACTIVATE | DELETE';
COMMENT ON COLUMN member.created_at    IS '가입일';
COMMENT ON COLUMN member.updated_at    IS '수정일';
COMMENT ON COLUMN member.deleted_at    IS '탈퇴 처리일 (Soft Delete)';


-- ============================================================
-- 도메인: 카테고리 (Category)
-- ============================================================

-- 대분류 (국내도서 / 해외도서 / 일본도서)
CREATE TABLE IF NOT EXISTS category_top (
    id                  BIGSERIAL           PRIMARY KEY,
    name                VARCHAR(50)         NOT NULL,
    sort_order          INT                 NOT NULL DEFAULT 0,
    category_top_status category_top_status NOT NULL DEFAULT 'ACTIVATE',
    created_at          TIMESTAMP           NOT NULL DEFAULT now(),
    updated_at          TIMESTAMP
);

COMMENT ON TABLE  category_top                    IS '도서 대분류 (국내 / 해외 / 일본)';
COMMENT ON COLUMN category_top.id                 IS '대분류 고유 ID';
COMMENT ON COLUMN category_top.name               IS '대분류명';
COMMENT ON COLUMN category_top.sort_order         IS '화면 정렬 순서';
COMMENT ON COLUMN category_top.category_top_status IS '상태: ACTIVATE | DEACTIVATE | DELETE';
COMMENT ON COLUMN category_top.created_at         IS '생성 시간';
COMMENT ON COLUMN category_top.updated_at         IS '수정 시간';

-- 소분류 (장르: 소설, IT/컴퓨터, 외국어 등)
CREATE TABLE IF NOT EXISTS category_sub (
    id                  BIGSERIAL           PRIMARY KEY,
    category_top_id     BIGINT              NOT NULL REFERENCES category_top (id),
    name                VARCHAR(50)         NOT NULL,
    sort_order          INT                 NOT NULL DEFAULT 0,
    category_sub_status category_sub_status NOT NULL DEFAULT 'ACTIVATE',
    created_at          TIMESTAMP           NOT NULL DEFAULT now(),
    updated_at          TIMESTAMP
);

COMMENT ON TABLE  category_sub                    IS '도서 소분류 (장르)';
COMMENT ON COLUMN category_sub.id                 IS '소분류 고유 ID';
COMMENT ON COLUMN category_sub.category_top_id    IS '상위 대분류 ID (FK)';
COMMENT ON COLUMN category_sub.name               IS '소분류명 (장르명)';
COMMENT ON COLUMN category_sub.sort_order         IS '화면 정렬 순서';
COMMENT ON COLUMN category_sub.category_sub_status IS '상태: ACTIVATE | DEACTIVATE | DELETE';
COMMENT ON COLUMN category_sub.created_at         IS '생성 시간';
COMMENT ON COLUMN category_sub.updated_at         IS '수정 시간';


-- ============================================================
-- 도메인: 상품 (Product)
-- ============================================================

CREATE TABLE IF NOT EXISTS product (
    id              BIGSERIAL       PRIMARY KEY,
    category_top_id BIGINT          NOT NULL REFERENCES category_top (id),
    category_sub_id BIGINT          NOT NULL REFERENCES category_sub (id),
    title           VARCHAR(300)    NOT NULL,
    author          VARCHAR(200)    NOT NULL,
    description     TEXT,
    price           INT             NOT NULL DEFAULT 0,
    discount_rate   NUMERIC(5, 2)   NOT NULL DEFAULT 0,
    sale_price      INT             NOT NULL DEFAULT 0,
    stock           INT             NOT NULL DEFAULT 0,
    thumbnail       VARCHAR(500),
    view_count      INT             NOT NULL DEFAULT 0,
    sales_count     INT             NOT NULL DEFAULT 0,
    product_status  product_status  NOT NULL DEFAULT 'ACTIVATE',
    created_at      TIMESTAMP       NOT NULL DEFAULT now(),
    updated_at      TIMESTAMP,
    deleted_at      TIMESTAMP
);

COMMENT ON TABLE  product                 IS '도서 상품 정보';
COMMENT ON COLUMN product.id              IS '도서 고유 ID';
COMMENT ON COLUMN product.category_top_id IS '대분류 ID (FK)';
COMMENT ON COLUMN product.category_sub_id IS '소분류 ID (FK)';
COMMENT ON COLUMN product.title           IS '도서명';
COMMENT ON COLUMN product.author          IS '저자';
COMMENT ON COLUMN product.description     IS '도서 설명';
COMMENT ON COLUMN product.price           IS '정가';
COMMENT ON COLUMN product.discount_rate   IS '할인율 (%)';
COMMENT ON COLUMN product.sale_price      IS '판매가 (정가 × (1 - 할인율/100), 앱에서 계산 후 저장)';
COMMENT ON COLUMN product.stock           IS '재고 수량';
COMMENT ON COLUMN product.thumbnail       IS '대표 이미지 경로';
COMMENT ON COLUMN product.view_count      IS '조회수';
COMMENT ON COLUMN product.sales_count     IS '판매량';
COMMENT ON COLUMN product.product_status  IS '상태: ACTIVATE | DEACTIVATE | DELETE';
COMMENT ON COLUMN product.created_at      IS '등록 시간';
COMMENT ON COLUMN product.updated_at      IS '수정 시간';
COMMENT ON COLUMN product.deleted_at      IS '삭제 시간 (Soft Delete)';

-- 도서 상세 이미지
CREATE TABLE IF NOT EXISTS product_image (
    id          BIGSERIAL       PRIMARY KEY,
    product_id  BIGINT          NOT NULL REFERENCES product (id) ON DELETE CASCADE,
    image_url   VARCHAR(500)    NOT NULL,
    type        VARCHAR(20)     NOT NULL DEFAULT 'SUB',
    sort_order  INT             NOT NULL DEFAULT 0,
    created_at  TIMESTAMP       NOT NULL DEFAULT now()
);

COMMENT ON TABLE  product_image            IS '도서 이미지';
COMMENT ON COLUMN product_image.id         IS '이미지 고유 ID';
COMMENT ON COLUMN product_image.product_id IS '도서 ID (FK, CASCADE 삭제)';
COMMENT ON COLUMN product_image.image_url  IS '이미지 경로 또는 URL';
COMMENT ON COLUMN product_image.type       IS '이미지 유형: MAIN(대표) | SUB(추가)';
COMMENT ON COLUMN product_image.sort_order IS '이미지 정렬 순서';
COMMENT ON COLUMN product_image.created_at IS '등록 시간';


-- ============================================================
-- 도메인: 장바구니 (Cart)
-- ============================================================

-- 장바구니 (회원 1 : 장바구니 1)
CREATE TABLE IF NOT EXISTS cart (
    id          BIGSERIAL    PRIMARY KEY,
    member_id   BIGINT       NOT NULL UNIQUE REFERENCES member (id),
    created_at  TIMESTAMP    NOT NULL DEFAULT now(),
    updated_at  TIMESTAMP
);

COMMENT ON TABLE  cart            IS '장바구니 (회원당 1개)';
COMMENT ON COLUMN cart.id         IS '장바구니 고유 ID';
COMMENT ON COLUMN cart.member_id  IS '회원 ID (FK, UNIQUE - 1:1 관계)';
COMMENT ON COLUMN cart.created_at IS '생성 시간';
COMMENT ON COLUMN cart.updated_at IS '수정 시간';

-- 장바구니 항목
CREATE TABLE IF NOT EXISTS cart_item (
    id          BIGSERIAL    PRIMARY KEY,
    cart_id     BIGINT       NOT NULL REFERENCES cart (id) ON DELETE CASCADE,
    product_id  BIGINT       NOT NULL REFERENCES product (id),
    quantity    INT          NOT NULL DEFAULT 1 CHECK (quantity >= 1),
    is_checked  BOOLEAN      NOT NULL DEFAULT true,
    created_at  TIMESTAMP    NOT NULL DEFAULT now(),
    updated_at  TIMESTAMP
);

COMMENT ON TABLE  cart_item            IS '장바구니 항목';
COMMENT ON COLUMN cart_item.id         IS '장바구니 항목 ID';
COMMENT ON COLUMN cart_item.cart_id    IS '장바구니 ID (FK, CASCADE 삭제)';
COMMENT ON COLUMN cart_item.product_id IS '도서 ID (FK)';
COMMENT ON COLUMN cart_item.quantity   IS '수량 (1 이상)';
COMMENT ON COLUMN cart_item.is_checked IS '주문 시 선택 여부';
COMMENT ON COLUMN cart_item.created_at IS '생성 시간';
COMMENT ON COLUMN cart_item.updated_at IS '수정 시간';


-- ============================================================
-- 도메인: 주문 (Order)
-- ⚠️ order 는 SQL 예약어 → 반드시 큰따옴표("order") 사용
-- ============================================================

CREATE TABLE IF NOT EXISTS "order" (
    id                       BIGSERIAL      PRIMARY KEY,
    member_id                BIGINT         NOT NULL REFERENCES member (id),
    number                   VARCHAR(30)    NOT NULL UNIQUE,
    order_status             order_status   NOT NULL DEFAULT 'PENDING',
    total_price              INT            NOT NULL DEFAULT 0,
    discount_amount          INT            NOT NULL DEFAULT 0,
    final_price              INT            NOT NULL DEFAULT 0,
    receiver_name            VARCHAR(50)    NOT NULL,
    receiver_phone           VARCHAR(20)    NOT NULL,
    delivery_address         VARCHAR(255)   NOT NULL,
    delivery_address_detail  VARCHAR(255),
    delivery_zip_code        VARCHAR(10)    NOT NULL,
    delivery_memo            VARCHAR(300),
    ordered_at               TIMESTAMP      NOT NULL DEFAULT now(),
    created_at               TIMESTAMP      NOT NULL DEFAULT now(),
    updated_at               TIMESTAMP,
    cancelled_at             TIMESTAMP
);

COMMENT ON TABLE  "order"                          IS '주문';
COMMENT ON COLUMN "order".id                       IS '주문 고유 ID';
COMMENT ON COLUMN "order".member_id                IS '회원 ID (FK)';
COMMENT ON COLUMN "order".number                   IS '주문번호 UNIQUE (예: ORD-20260315-000001)';
COMMENT ON COLUMN "order".order_status             IS '상태: PENDING | PAYED | APPROVAL | CANCELED';
COMMENT ON COLUMN "order".total_price              IS '총 상품금액';
COMMENT ON COLUMN "order".discount_amount          IS '할인금액';
COMMENT ON COLUMN "order".final_price              IS '최종 결제금액';
COMMENT ON COLUMN "order".receiver_name            IS '수령인 이름';
COMMENT ON COLUMN "order".receiver_phone           IS '수령인 연락처';
COMMENT ON COLUMN "order".delivery_address         IS '배송지 주소';
COMMENT ON COLUMN "order".delivery_address_detail  IS '배송지 상세 주소';
COMMENT ON COLUMN "order".delivery_zip_code        IS '우편번호';
COMMENT ON COLUMN "order".delivery_memo            IS '배송 메모';
COMMENT ON COLUMN "order".ordered_at               IS '주문 시간';
COMMENT ON COLUMN "order".created_at               IS '생성 시간';
COMMENT ON COLUMN "order".updated_at               IS '수정 시간';
COMMENT ON COLUMN "order".cancelled_at             IS '취소 시각';

-- 주문 항목
CREATE TABLE IF NOT EXISTS order_item (
    id              BIGSERIAL       PRIMARY KEY,
    order_id        BIGINT          NOT NULL REFERENCES "order" (id) ON DELETE CASCADE,
    product_id      BIGINT          NOT NULL REFERENCES product (id),
    product_title   VARCHAR(300)    NOT NULL,
    product_author  VARCHAR(200)    NOT NULL,
    sale_price      INT             NOT NULL DEFAULT 0,
    quantity        INT             NOT NULL DEFAULT 1 CHECK (quantity >= 1),
    item_total      INT             NOT NULL DEFAULT 0,
    is_reviewed     BOOLEAN         NOT NULL DEFAULT false,
    created_at      TIMESTAMP       NOT NULL DEFAULT now()
);

COMMENT ON TABLE  order_item                IS '주문 항목';
COMMENT ON COLUMN order_item.id             IS '주문 항목 ID';
COMMENT ON COLUMN order_item.order_id       IS '주문 ID (FK, CASCADE 삭제)';
COMMENT ON COLUMN order_item.product_id     IS '도서 ID (FK)';
COMMENT ON COLUMN order_item.product_title  IS '도서명 스냅샷 (주문 당시)';
COMMENT ON COLUMN order_item.product_author IS '저자 스냅샷 (주문 당시)';
COMMENT ON COLUMN order_item.sale_price     IS '주문 당시 판매가 스냅샷';
COMMENT ON COLUMN order_item.quantity       IS '주문 수량 (1 이상)';
COMMENT ON COLUMN order_item.item_total     IS '소계 (sale_price × quantity)';
COMMENT ON COLUMN order_item.is_reviewed    IS '리뷰 작성 여부';
COMMENT ON COLUMN order_item.created_at     IS '생성 시간';


-- ============================================================
-- 도메인: 결제 (Payment)
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

COMMENT ON TABLE  payment                IS '결제';
COMMENT ON COLUMN payment.id             IS '결제 고유 ID';
COMMENT ON COLUMN payment.order_id       IS '주문 ID (FK)';
COMMENT ON COLUMN payment.method         IS '결제 수단 (CARD, BANK 등)';
COMMENT ON COLUMN payment.payment_status IS '상태: READY | PAID | CANCELLED | FAILED';
COMMENT ON COLUMN payment.amount         IS '결제 금액';
COMMENT ON COLUMN payment.pg_tid         IS 'PG 트랜잭션 ID (Mock: UUID)';
COMMENT ON COLUMN payment.paid_at        IS '결제 완료 시각';
COMMENT ON COLUMN payment.cancel_reason  IS '취소 사유';
COMMENT ON COLUMN payment.created_at     IS '생성 시간';
COMMENT ON COLUMN payment.updated_at     IS '수정 시간';
COMMENT ON COLUMN payment.cancelled_at   IS '취소 시각';


-- ============================================================
-- 도메인: 배송 (Delivery)
-- ============================================================

CREATE TABLE IF NOT EXISTS delivery (
    id               BIGSERIAL        PRIMARY KEY,
    order_id         BIGINT           NOT NULL UNIQUE REFERENCES "order" (id),
    courier          VARCHAR(50),
    tracking_number  VARCHAR(100),
    delivery_status  delivery_status  NOT NULL DEFAULT 'READY',
    created_at       TIMESTAMP        NOT NULL DEFAULT now(),
    updated_at       TIMESTAMP,
    shipped_at       TIMESTAMP,
    delivered_at     TIMESTAMP
);

COMMENT ON TABLE  delivery                 IS '배송';
COMMENT ON COLUMN delivery.id              IS '배송 고유 ID';
COMMENT ON COLUMN delivery.order_id        IS '주문 ID (FK, UNIQUE - 1:1 관계)';
COMMENT ON COLUMN delivery.courier         IS '택배사명';
COMMENT ON COLUMN delivery.tracking_number IS '운송장 번호';
COMMENT ON COLUMN delivery.delivery_status IS '상태: READY | SHIPPED | IN_TRANSIT | DELIVERED | FAILED';
COMMENT ON COLUMN delivery.created_at      IS '생성 시간';
COMMENT ON COLUMN delivery.updated_at      IS '수정 시간';
COMMENT ON COLUMN delivery.shipped_at      IS '발송 시각';
COMMENT ON COLUMN delivery.delivered_at    IS '배송 완료 시각';


-- ============================================================
-- 도메인: 게시판 (Board) - 공지사항, QnA, 리뷰
-- ============================================================

-- 공지사항
CREATE TABLE IF NOT EXISTS notice (
    id          BIGSERIAL     PRIMARY KEY,
    member_id   BIGINT        NOT NULL REFERENCES member (id),
    title       VARCHAR(255)  NOT NULL,
    content     TEXT          NOT NULL,
    is_fixed    BOOLEAN       NOT NULL DEFAULT false,
    view_count  INTEGER       NOT NULL DEFAULT 0,
    created_at  TIMESTAMP     NOT NULL DEFAULT now(),
    updated_at  TIMESTAMP,
    deleted_at  TIMESTAMP
);

COMMENT ON TABLE  notice            IS '공지사항';
COMMENT ON COLUMN notice.id         IS '공지사항 고유 식별자';
COMMENT ON COLUMN notice.member_id  IS '작성 관리자 ID (FK)';
COMMENT ON COLUMN notice.title      IS '공지사항 제목';
COMMENT ON COLUMN notice.content    IS '공지사항 본문';
COMMENT ON COLUMN notice.is_fixed   IS '상단 고정 여부';
COMMENT ON COLUMN notice.view_count IS '조회수';
COMMENT ON COLUMN notice.created_at IS '등록일';
COMMENT ON COLUMN notice.updated_at IS '수정일';
COMMENT ON COLUMN notice.deleted_at IS '삭제일 (Soft Delete)';

-- 1:1 문의 (자기 참조 계층 구조)
CREATE TABLE IF NOT EXISTS qna (
    id          BIGSERIAL   PRIMARY KEY,
    parent_id   BIGINT      REFERENCES qna (id),
    depth       INT         NOT NULL DEFAULT 0,
    member_id   BIGINT      NOT NULL REFERENCES member (id),
    category    VARCHAR(50) NOT NULL,
    title       VARCHAR(255) NOT NULL,
    content     TEXT        NOT NULL,
    view_count  INTEGER     NOT NULL DEFAULT 0,
    qna_status  qna_status  NOT NULL DEFAULT 'WAITING',
    is_secret   BOOLEAN     NOT NULL DEFAULT false,
    answered_at TIMESTAMP,
    created_at  TIMESTAMP   NOT NULL DEFAULT now(),
    deleted_at  TIMESTAMP
);

COMMENT ON TABLE  qna             IS '1:1 문의';
COMMENT ON COLUMN qna.id          IS '고유 식별자';
COMMENT ON COLUMN qna.parent_id   IS '부모 ID (NULL이면 최상위 질문, 자기참조 FK)';
COMMENT ON COLUMN qna.depth       IS '깊이 (0: 질문, 1: 답변, 최대 4)';
COMMENT ON COLUMN qna.member_id   IS '작성자 ID (FK)';
COMMENT ON COLUMN qna.category    IS '문의 유형 (배송, 환불 등)';
COMMENT ON COLUMN qna.title       IS '제목';
COMMENT ON COLUMN qna.content     IS '내용';
COMMENT ON COLUMN qna.view_count  IS '조회수';
COMMENT ON COLUMN qna.qna_status  IS '답변 상태: WAITING | PROCESSING | COMPLETE';
COMMENT ON COLUMN qna.is_secret   IS '비밀글 여부';
COMMENT ON COLUMN qna.answered_at IS '답변 완료 시각';
COMMENT ON COLUMN qna.created_at  IS '등록 일시';
COMMENT ON COLUMN qna.deleted_at  IS '삭제 일시 (Soft Delete)';

-- 상품 리뷰
CREATE TABLE IF NOT EXISTS review (
    id          BIGSERIAL   PRIMARY KEY,
    member_id   BIGINT      NOT NULL REFERENCES member (id),
    product_id  BIGINT      NOT NULL REFERENCES product (id),
    rating      INTEGER     NOT NULL CHECK (rating BETWEEN 1 AND 5),
    content     TEXT        NOT NULL,
    hits        INTEGER     NOT NULL DEFAULT 0,
    created_at  TIMESTAMP   NOT NULL DEFAULT now(),
    updated_at  TIMESTAMP,
    deleted_at  TIMESTAMP
);

COMMENT ON TABLE  review            IS '상품 리뷰';
COMMENT ON COLUMN review.id         IS '리뷰 고유 식별자';
COMMENT ON COLUMN review.member_id  IS '작성자 ID (FK)';
COMMENT ON COLUMN review.product_id IS '상품 ID (FK)';
COMMENT ON COLUMN review.rating     IS '평점 (1~5)';
COMMENT ON COLUMN review.content    IS '리뷰 상세 내용';
COMMENT ON COLUMN review.hits       IS '조회수';
COMMENT ON COLUMN review.created_at IS '작성일';
COMMENT ON COLUMN review.updated_at IS '수정일';
COMMENT ON COLUMN review.deleted_at IS '삭제일 (Soft Delete)';

-- 리뷰 이미지
CREATE TABLE IF NOT EXISTS review_image (
    id          BIGSERIAL     PRIMARY KEY,
    review_id   BIGINT        NOT NULL REFERENCES review (id) ON DELETE CASCADE,
    image_url   VARCHAR(512)  NOT NULL,
    created_at  TIMESTAMP     NOT NULL DEFAULT now()
);

COMMENT ON TABLE  review_image           IS '리뷰 이미지';
COMMENT ON COLUMN review_image.id        IS '이미지 고유 식별자';
COMMENT ON COLUMN review_image.review_id IS '리뷰 ID (FK, CASCADE 삭제)';
COMMENT ON COLUMN review_image.image_url IS '이미지 경로/URL';
COMMENT ON COLUMN review_image.created_at IS '등록일';

-- 리뷰 반응 (좋아요 / 싫어요)
CREATE TABLE IF NOT EXISTS review_reaction (
    id             BIGSERIAL    PRIMARY KEY,
    review_id      BIGINT       NOT NULL REFERENCES review (id) ON DELETE CASCADE,
    member_id      BIGINT       NOT NULL REFERENCES member (id),
    reaction_type  VARCHAR(10)  NOT NULL CHECK (reaction_type IN ('LIKE', 'DISLIKE')),
    created_at     TIMESTAMP    NOT NULL DEFAULT now(),
    UNIQUE (review_id, member_id)   -- 한 회원은 리뷰당 하나의 반응만 가능
);

COMMENT ON TABLE  review_reaction               IS '리뷰 반응 (좋아요/싫어요)';
COMMENT ON COLUMN review_reaction.id            IS '반응 고유 식별자';
COMMENT ON COLUMN review_reaction.review_id     IS '리뷰 ID (FK, CASCADE 삭제)';
COMMENT ON COLUMN review_reaction.member_id     IS '반응한 회원 ID (FK)';
COMMENT ON COLUMN review_reaction.reaction_type IS '반응 유형: LIKE | DISLIKE';
COMMENT ON COLUMN review_reaction.created_at    IS '반응 일시';


-- ============================================================
-- [STEP 3] 인덱스 생성
-- ============================================================

-- -------------------------------------------------------
-- member 인덱스
-- -------------------------------------------------------
-- 이메일 단건 조회 (로그인, 중복 체크) - UNIQUE 제약으로 자동 생성되나 명시적 선언
CREATE UNIQUE INDEX IF NOT EXISTS idx_member_email
    ON member (email);

-- 관리자 회원 목록 정렬 성능
CREATE INDEX IF NOT EXISTS idx_member_created_at
    ON member (created_at DESC);

-- 회원 상태 필터 (강퇴/탈퇴 목록 조회)
CREATE INDEX IF NOT EXISTS idx_member_status
    ON member (member_status);

-- -------------------------------------------------------
-- category_top / category_sub 인덱스
-- -------------------------------------------------------
-- 활성 대분류 정렬 조회
CREATE INDEX IF NOT EXISTS idx_category_top_status_sort
    ON category_top (category_top_status, sort_order);

-- 소분류 → 대분류 조회 (FK 탐색)
CREATE INDEX IF NOT EXISTS idx_category_sub_top_id
    ON category_sub (category_top_id);

-- 활성 소분류 정렬 조회
CREATE INDEX IF NOT EXISTS idx_category_sub_status_sort
    ON category_sub (category_sub_status, sort_order);

-- -------------------------------------------------------
-- product 인덱스
-- -------------------------------------------------------
-- 카테고리별 상품 목록 (가장 빈번한 조회)
CREATE INDEX IF NOT EXISTS idx_product_category_top_id
    ON product (category_top_id);

CREATE INDEX IF NOT EXISTS idx_product_category_sub_id
    ON product (category_sub_id);

-- 상품 상태 필터
CREATE INDEX IF NOT EXISTS idx_product_status
    ON product (product_status);

-- 베스트셀러 정렬 (판매량 내림차순)
CREATE INDEX IF NOT EXISTS idx_product_sales_count
    ON product (sales_count DESC);

-- 신상품 정렬 (등록일 내림차순)
CREATE INDEX IF NOT EXISTS idx_product_created_at
    ON product (created_at DESC);

-- 제목/저자 검색 (ILIKE 전방 탐색용)
CREATE INDEX IF NOT EXISTS idx_product_title
    ON product (title);

CREATE INDEX IF NOT EXISTS idx_product_author
    ON product (author);

-- -------------------------------------------------------
-- product_image 인덱스
-- -------------------------------------------------------
-- 상품별 이미지 목록 + 정렬
CREATE INDEX IF NOT EXISTS idx_product_image_product_id
    ON product_image (product_id, sort_order);

-- -------------------------------------------------------
-- cart / cart_item 인덱스
-- -------------------------------------------------------
-- 회원 장바구니 조회 (UNIQUE로 자동 생성되나 명시적 선언)
CREATE UNIQUE INDEX IF NOT EXISTS idx_cart_member_id
    ON cart (member_id);

-- 장바구니 항목 목록 조회
CREATE INDEX IF NOT EXISTS idx_cart_item_cart_id
    ON cart_item (cart_id);

-- 특정 상품이 담긴 장바구니 역조회
CREATE INDEX IF NOT EXISTS idx_cart_item_product_id
    ON cart_item (product_id);

-- -------------------------------------------------------
-- order 인덱스
-- -------------------------------------------------------
-- 회원별 주문 목록 조회 (마이페이지)
CREATE INDEX IF NOT EXISTS idx_order_member_id
    ON "order" (member_id);

-- 주문 상태별 필터 (관리자 페이지)
CREATE INDEX IF NOT EXISTS idx_order_status
    ON "order" (order_status);

-- 주문 최신순 정렬
CREATE INDEX IF NOT EXISTS idx_order_ordered_at
    ON "order" (ordered_at DESC);

-- -------------------------------------------------------
-- order_item 인덱스
-- -------------------------------------------------------
-- 주문별 항목 조회
CREATE INDEX IF NOT EXISTS idx_order_item_order_id
    ON order_item (order_id);

-- 상품별 주문 이력 조회 (관리자 통계)
CREATE INDEX IF NOT EXISTS idx_order_item_product_id
    ON order_item (product_id);

-- 리뷰 미작성 항목 조회 (마이페이지)
CREATE INDEX IF NOT EXISTS idx_order_item_is_reviewed
    ON order_item (is_reviewed)
    WHERE is_reviewed = false;

-- -------------------------------------------------------
-- payment 인덱스
-- -------------------------------------------------------
-- 주문별 결제 조회
CREATE INDEX IF NOT EXISTS idx_payment_order_id
    ON payment (order_id);

-- 결제 상태별 필터 (관리자)
CREATE INDEX IF NOT EXISTS idx_payment_status
    ON payment (payment_status);

-- -------------------------------------------------------
-- delivery 인덱스
-- -------------------------------------------------------
-- 주문별 배송 조회 (UNIQUE로 자동 생성되나 명시적 선언)
CREATE UNIQUE INDEX IF NOT EXISTS idx_delivery_order_id
    ON delivery (order_id);

-- 배송 상태별 필터 (관리자 배송 대시보드)
CREATE INDEX IF NOT EXISTS idx_delivery_status
    ON delivery (delivery_status);

-- -------------------------------------------------------
-- notice 인덱스
-- -------------------------------------------------------
-- 상단 고정 공지 우선 정렬
CREATE INDEX IF NOT EXISTS idx_notice_is_fixed_created_at
    ON notice (is_fixed DESC, created_at DESC);

-- Soft Delete 제외 조회
CREATE INDEX IF NOT EXISTS idx_notice_deleted_at
    ON notice (deleted_at)
    WHERE deleted_at IS NULL;

-- -------------------------------------------------------
-- qna 인덱스
-- -------------------------------------------------------
-- 회원별 문의 목록 조회 (마이페이지)
CREATE INDEX IF NOT EXISTS idx_qna_member_id
    ON qna (member_id);

-- 부모-자식 계층 조회 (답변 스레드)
CREATE INDEX IF NOT EXISTS idx_qna_parent_id
    ON qna (parent_id);

-- 답변 상태별 필터 (관리자)
CREATE INDEX IF NOT EXISTS idx_qna_status
    ON qna (qna_status);

-- Soft Delete 제외 조회
CREATE INDEX IF NOT EXISTS idx_qna_deleted_at
    ON qna (deleted_at)
    WHERE deleted_at IS NULL;

-- -------------------------------------------------------
-- review 인덱스
-- -------------------------------------------------------
-- 상품별 리뷰 목록 조회
CREATE INDEX IF NOT EXISTS idx_review_product_id
    ON review (product_id);

-- 회원별 리뷰 목록 조회 (마이페이지)
CREATE INDEX IF NOT EXISTS idx_review_member_id
    ON review (member_id);

-- 최신순 정렬
CREATE INDEX IF NOT EXISTS idx_review_created_at
    ON review (created_at DESC);

-- Soft Delete 제외 조회
CREATE INDEX IF NOT EXISTS idx_review_deleted_at
    ON review (deleted_at)
    WHERE deleted_at IS NULL;

-- -------------------------------------------------------
-- review_image 인덱스
-- -------------------------------------------------------
-- 리뷰별 이미지 조회
CREATE INDEX IF NOT EXISTS idx_review_image_review_id
    ON review_image (review_id);

-- -------------------------------------------------------
-- review_reaction 인덱스
-- -------------------------------------------------------
-- 리뷰별 반응 집계 (좋아요 수 등)
CREATE INDEX IF NOT EXISTS idx_review_reaction_review_id
    ON review_reaction (review_id);

-- 회원별 반응 조회 (중복 반응 방지용 - UNIQUE로 자동 생성)
CREATE UNIQUE INDEX IF NOT EXISTS idx_review_reaction_review_member
    ON review_reaction (review_id, member_id);
