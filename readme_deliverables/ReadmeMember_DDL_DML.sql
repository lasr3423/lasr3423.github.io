-- ============================================================
-- 온라인 도서 판매 쇼핑몰 - Member 도메인 DDL / DML
-- DB      : PostgreSQL
-- Schema  : public
-- 작성일  : 2026.03.17
-- 작성자 : 유환희
-- 주의    : Spring Boot JPA (ddl-auto: update) 사용 환경
--           → SpringPhysicalNamingStrategy 적용으로
--             Java 필드명(camelCase)이 DB 컬럼명(snake_case)으로 자동 변환됨
--             예) memberRole → member_role, createdAt → created_at
-- ============================================================


-- ============================================================
-- [DDL] ENUM 타입 정의
-- ※ JPA @Enumerated(EnumType.STRING) 사용 시 아래 타입 생성 불필요
--   (VARCHAR 컬럼으로 관리됨)
-- ※ PostgreSQL 네이티브 ENUM 사용 시 아래 구문을 먼저 실행할 것
-- ============================================================

CREATE TYPE member_role AS ENUM ('USER', 'MANAGER', 'ADMIN');
CREATE TYPE member_status AS ENUM ('ACTIVATED', 'DEACTIVATED', 'DELETED');


-- ============================================================
-- [DDL] member 테이블 생성
-- ※ JPA ddl-auto: update 사용 시 자동 생성되므로 참조용 스크립트
-- ============================================================

CREATE TABLE IF NOT EXISTS member (
    id             BIGSERIAL        PRIMARY KEY,
    email          VARCHAR(100)     NOT NULL UNIQUE,
    password       VARCHAR(255)     NOT NULL,
    name           VARCHAR(50)      NOT NULL,
    phone          VARCHAR(20)      NOT NULL,
    address        VARCHAR(100)     NOT NULL,
    member_role    member_role      NOT NULL DEFAULT 'USER',
    member_status  member_status    NOT NULL DEFAULT 'ACTIVATED',
    created_at     TIMESTAMP        NOT NULL DEFAULT now(),
    updated_at     TIMESTAMP        NOT NULL DEFAULT now(),
    deleted_at     TIMESTAMP
);


-- ============================================================
-- [DDL] 인덱스 생성
-- ============================================================

-- 이메일 단건 조회 (로그인, 중복 체크) 성능 향상
CREATE UNIQUE INDEX IF NOT EXISTS idx_member_email
    ON member (email);

-- 관리자 회원 목록 조회 시 정렬 성능 향상
CREATE INDEX IF NOT EXISTS idx_member_created_at
    ON member (created_at DESC);

-- 관리자 회원 상태 필터 성능 향상
CREATE INDEX IF NOT EXISTS idx_member_status
    ON member (member_status);


-- ============================================================
-- [DML] FR-M-002 | 회원가입
-- 필수 입력: 이메일, 비밀번호(BCrypt), 이름, 전화번호, 주소
-- ============================================================

INSERT INTO member (email, password, name, phone, address)
VALUES (
    'user@example.com',
    '$2a$10$BCryptHashedPasswordHere',
    '홍길동',
    '010-1234-5678',
    '서울시 강남구 테헤란로 123'
);


-- ============================================================
-- [DML] FR-M-002 | 회원가입 전 이메일 중복 체크
-- 결과가 1 이상이면 이미 가입된 이메일
-- ============================================================

SELECT COUNT(*)
FROM member
WHERE email = 'user@example.com';


-- ============================================================
-- [DML] FR-M-001 | 로그인
-- 이메일로 회원 조회 후 BCrypt 비밀번호 매칭은 애플리케이션 레이어에서 처리
-- DEACTIVATED(강퇴) / DELETED(탈퇴) 상태 회원은 로그인 불가
-- ============================================================

SELECT id, email, password, name, member_role, member_status
FROM member
WHERE email = 'user@example.com'
  AND member_status = 'ACTIVATED';


-- ============================================================
-- [DML] FR-M-003 | 마이페이지 - 개인정보 조회
-- 탈퇴 회원(DELETED) 제외
-- ============================================================

SELECT id, email, name, phone, address, member_role, created_at
FROM member
WHERE id = 1
  AND member_status != 'DELETED';


-- ============================================================
-- [DML] FR-M-004 | 마이페이지 - 개인정보 수정 (이름 / 전화번호 / 주소)
-- ============================================================

UPDATE member
SET name       = '홍길동',
    phone      = '010-9876-5432',
    address    = '서울시 서초구 반포대로 100',
    updated_at = now()
WHERE id = 1
  AND member_status = 'ACTIVATED';


-- ============================================================
-- [DML] FR-M-004 | 마이페이지 - 비밀번호 변경
-- 현재 비밀번호 검증은 애플리케이션 레이어에서 처리 후 실행
-- ============================================================

UPDATE member
SET password   = '$2a$10$NewBCryptHashedPasswordHere',
    updated_at = now()
WHERE id = 1
  AND member_status = 'ACTIVATED';


-- ============================================================
-- [DML] FR-M-005 | 마이페이지 - 회원 탈퇴 (Soft Delete)
-- memberStatus → DELETED, deleted_at 기록
-- ============================================================

UPDATE member
SET member_status = 'DELETED',
    deleted_at    = now(),
    updated_at    = now()
WHERE id = 1
  AND member_status = 'ACTIVATED';


-- ============================================================
-- [DML] FR-A-002 | 관리자 - 전체 회원 목록 조회 (페이징)
-- 최근 가입 순(created_at DESC) 정렬
-- LIMIT : 페이지 크기, OFFSET : (현재 페이지 - 1) * 페이지 크기
-- ============================================================

SELECT id, email, name, phone, member_role, member_status, created_at
FROM member
ORDER BY created_at DESC
LIMIT 10 OFFSET 0;


-- ============================================================
-- [DML] FR-A-003 | 관리자 - 회원 검색 (전체 키워드)
-- 이메일 또는 이름에 검색어 포함 여부 조회 (대소문자 무시: ILIKE)
-- ============================================================

SELECT id, email, name, phone, member_role, member_status, created_at
FROM member
WHERE (email ILIKE '%검색어%'
   OR  name  ILIKE '%검색어%')
ORDER BY created_at DESC
LIMIT 10 OFFSET 0;


-- ============================================================
-- [DML] FR-A-003 | 관리자 - 회원 검색 (이메일 기준)
-- ============================================================

SELECT id, email, name, phone, member_role, member_status, created_at
FROM member
WHERE email ILIKE '%검색어%'
ORDER BY created_at DESC
LIMIT 10 OFFSET 0;


-- ============================================================
-- [DML] FR-A-003 | 관리자 - 회원 검색 (이름 기준)
-- ============================================================

SELECT id, email, name, phone, member_role, member_status, created_at
FROM member
WHERE name ILIKE '%검색어%'
ORDER BY created_at DESC
LIMIT 10 OFFSET 0;


-- ============================================================
-- [DML] FR-A-004 | 관리자 - 특정 회원 상세 조회
-- ============================================================

SELECT id, email, name, phone, address,
       member_role, member_status,
       created_at, updated_at, deleted_at
FROM member
WHERE id = 1;


-- ============================================================
-- [DML] FR-A-005 | 관리자 - 특정 회원 강퇴
-- memberStatus → DEACTIVATED (로그인 불가 처리)
-- ============================================================

UPDATE member
SET member_status = 'DEACTIVATED',
    updated_at    = now()
WHERE id = 1
  AND member_status = 'ACTIVATED';


-- ============================================================
-- [DML] FR-A-007 | 관리자 - 회원 등급 변경
-- USER → MANAGER → ADMIN 단계 변경
-- ADMIN 권한자만 실행 가능 (애플리케이션 레이어에서 권한 검증)
-- ============================================================

-- USER → MANAGER 승급
UPDATE member
SET member_role = 'MANAGER',
    updated_at  = now()
WHERE id = 1
  AND member_role = 'USER';

-- MANAGER → ADMIN 승급
UPDATE member
SET member_role = 'ADMIN',
    updated_at  = now()
WHERE id = 1
  AND member_role = 'MANAGER';

-- ADMIN → MANAGER 강등 (권한 축소)
UPDATE member
SET member_role = 'MANAGER',
    updated_at  = now()
WHERE id = 1
  AND member_role = 'ADMIN';
