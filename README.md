# 📚 ReadMe — 온라인 서점 쇼핑몰

> Java 21 + Spring Boot 3 + Vue 3 기반의 풀스택 온라인 서점 프로젝트

---

## 팀 소개

| 항목 | 내용 |
|------|------|
| **팀명** | ReadMe |
| **프로젝트 기간** | 2026.03.13 ~ 2026.04.12 (약 4주) |
| **팀 구성** | 3인 |

| 이름 | 역할 | 담당 |
|------|------|------|
| 유환희 | Frontend | Vue 3 전체 화면 구현, 레이아웃 설계, Pinia 상태 관리, 결제 흐름, 라우터 가드 |
| 백경서(팀장) | Backend | Spring Boot REST API, JPA 엔티티 설계, Spring Security + JWT, OAuth2 소셜 로그인, 결제 연동 |
| 박주환 | DB | DB 설계 |

---

## 기술 스택

### Backend

| 분류 | 기술 |
|------|------|
| 언어 / 런타임 | Java 21 |
| 프레임워크 | Spring Boot 3.x |
| ORM | Spring Data JPA (Hibernate) |
| 데이터베이스 | PostgreSQL 16 |
| 인증 | Spring Security, JWT (Access Token + Refresh Token Rotation) |
| 소셜 로그인 | OAuth2 (Google, Kakao) |
| 파일 업로드 | Spring Multipart (로컬 볼륨) |
| 외부 API | Kakao Book Search API |
| 결제 | Toss Payments, Kakao Pay, Naver Pay |

### Frontend

| 분류 | 기술 |
|------|------|
| 프레임워크 | Vue 3 (Composition API, `<script setup>`) |
| 상태 관리 | Pinia |
| 라우팅 | Vue Router 4 |
| HTTP 클라이언트 | Axios |
| 스타일링 | Tailwind CSS v4 |
| 빌드 도구 | Vite |

### 인프라 / DevOps

| 분류 | 기술 |
|------|------|
| 컨테이너 | Docker, Docker Compose |
| 웹서버 | Nginx (프론트 서빙 + API 리버스 프록시) |
| CI/CD | GitHub Actions |
| 이미지 레지스트리 | Docker Hub |

---

## 페이지 구성

### 공통 (비회원 포함)

| 경로 | 페이지 | 설명 |
|------|--------|------|
| `/` | 메인 홈 | 히어로 배너, 카테고리 바로가기, 추천 도서 |
| `/product` | 상품 목록 | 카테고리 필터, 검색, 페이지네이션 |
| `/product/:id` | 상품 상세 | 도서 정보, 리뷰 목록, 장바구니 담기 |
| `/notice` | 공지사항 목록 | - |
| `/notice/:id` | 공지사항 상세 | - |
| `/event` | 이벤트 | - |
| `/qna` | Q&A 목록 | - |
| `/review` | 리뷰 목록 | - |
| `/review/:id` | 리뷰 상세 | - |

### 인증

| 경로 | 페이지 | 설명 |
|------|--------|------|
| `/signin` | 로그인 | 일반 로그인 + Google / Kakao 소셜 로그인 |
| `/signup` | 회원가입 | - |
| `/oauth/callback` | OAuth 콜백 | 소셜 로그인 처리 후 토큰 저장 |

### 쇼핑 (로그인 필요)

| 경로 | 페이지 | 설명 |
|------|--------|------|
| `/cart` | 장바구니 | 수량 조절, 선택 삭제, 선택 결제 |
| `/order` | 주문서 | 배송지 입력, 주문 상품 확인 |
| `/payment` | 결제 | Toss / Kakao Pay / Naver Pay 선택 |
| `/payment/success` | 결제 성공 | 결제 승인 처리 후 주문 완료 |
| `/payment/fail` | 결제 실패 | 실패 안내 및 장바구니 복귀 |
| `/payment/complete` | 결제 완료 | 최종 완료 안내 |

### 마이페이지 (로그인 필요)

| 경로 | 페이지 | 설명 |
|------|--------|------|
| `/mypage` | 마이페이지 홈 | 회원 정보 요약 |
| `/mypage/order` | 주문 내역 | 주문 목록, 주문 취소 |
| `/mypage/order/:id` | 주문 상세 | 상품 / 배송 / 금액 상세 |
| `/mypage/payment` | 결제 내역 | - |
| `/mypage/edit` | 회원정보 수정 | - |
| `/mypage/password` | 비밀번호 변경 | - |
| `/mypage/qna` | 내 Q&A 목록 | - |
| `/mypage/qna/detail/:id` | 내 Q&A 상세 | - |
| `/mypage/review` | 내 리뷰 목록 | - |
| `/mypage/review/detail/:id` | 내 리뷰 상세 | - |
| `/mypage/withdraw` | 회원 탈퇴 | Soft Delete 처리 |

### 관리자 (MANAGER / ADMIN 권한)

| 경로 | 페이지 | 설명 |
|------|--------|------|
| `/admin` | 대시보드 | 통계 요약 |
| `/admin/product/list` | 상품 목록 | - |
| `/admin/product/insert` | 상품 등록 | Kakao Book API 검색 연동 |
| `/admin/product/edit/:id` | 상품 수정 | - |
| `/admin/product/stock` | 재고 관리 | - |
| `/admin/category/list` | 카테고리 관리 | - |
| `/admin/order/list` | 주문 목록 | - |
| `/admin/order/approval` | 주문 승인 | - |
| `/admin/delivery/list` | 배송 관리 | - |
| `/admin/payment/list` | 결제 내역 | - |
| `/admin/notice/list` | 공지사항 관리 | - |
| `/admin/qna/list` | Q&A 관리 | - |
| `/admin/review/list` | 리뷰 관리 | - |
| `/admin/member/list` | 회원 목록 | - |
| `/admin/member/role` | 권한 관리 | - |

---

## 구현 기능

### 회원

- 이메일 기반 회원가입 / 로그인
- Google, Kakao 소셜 로그인 (OAuth2)
- JWT 인증: Access Token 메모리 보관 + Refresh Token 로테이션
- 회원정보 수정, 비밀번호 변경, 회원 탈퇴 (Soft Delete)
- 라우터 가드: 비로그인 시 `/signin` 자동 리다이렉트

### 상품

- 카테고리별 상품 목록 조회 및 검색
- 상품 상세 페이지 (도서 정보, 리뷰)
- 관리자 상품 등록 시 Kakao Book Search API 자동 정보 조회
- 재고 관리

### 장바구니 / 주문 / 결제

- 장바구니 담기, 수량 변경, 선택 삭제
- 주문서 작성 (배송지 직접 입력)
- Toss Payments, Kakao Pay, Naver Pay 결제 연동
- 결제 성공/실패 처리 및 주문 상태 업데이트
- 주문 취소

### 게시판

- 공지사항, 이벤트, Q&A, 리뷰 조회
- 마이페이지에서 내 Q&A / 리뷰 관리

### 관리자

- 상품 등록 / 수정 / 재고 관리
- 주문 목록 조회 및 승인 처리
- 배송 / 결제 현황 조회
- 회원 목록 및 권한(ROLE) 변경
- 공지사항 / Q&A / 리뷰 관리

---

## 비기능 요구사항

### 보안

- 민감 정보(DB 비밀번호, JWT Secret, API 키 등)는 `.env` 파일로 분리 관리하며 Git에 포함하지 않음
- Entity를 API 응답으로 직접 반환하지 않고 반드시 DTO로 변환
- Soft Delete 적용 — 실제 DB 데이터 삭제 금지 (`deleted_at` 컬럼 관리)

### 인증 / 인가

- Access Token은 메모리(Pinia store)에만 보관하여 XSS 탈취 방지
- Refresh Token은 localStorage에 보관하고 재발급 시 Rotation 적용
- 라우터 레벨 가드로 미인증 접근 차단, 관리자 페이지는 역할 기반 접근 제어 (ROLE_MANAGER, ROLE_ADMIN)

### 코드 품질

- Vue 3 Composition API + `<script setup>` 통일
- 비동기 처리 `async/await` + `try/catch` 필수 적용
- 스타일 CSS 변수 / Tailwind 토큰만 사용, 하드코딩 금지
- 새 View 추가 시 `router.meta.layout` 명시 필수

### 성능 / 안정성

- Spring Boot Healthcheck 기반 DB 연결 확인 후 백엔드 기동 (`depends_on: condition: service_healthy`)
- 파일 업로드 용량 제한 (단건 10MB, 요청 20MB)
- Docker 재시작 정책 `unless-stopped` 적용

---

## 배포

### 아키텍처

```
[사용자] → Nginx (포트 80)
              ├── 정적 파일 (Vue 빌드 결과) 직접 서빙
              └── /api/* → Spring Boot (내부 포트 8202)
                              └── PostgreSQL 16 (내부 포트 5432)
```

### Docker Hub 이미지

| 이미지 | 태그 |
|--------|------|
| `olgksgml/readme-backend` | `latest` |
| `olgksgml/readme-frontend` | `latest` |

### 실행 방법

**1. 환경변수 파일 준비**

```bash
# .env 파일을 docker-compose.yml과 같은 위치에 생성
cp .env.example .env
# 이후 도메인, DB 비밀번호, API 키 값 수정
```

**2. 이미지 Pull 후 실행**

```bash
docker compose pull
docker compose up -d
```

**3. 로그 확인**

```bash
docker compose logs -f
```

**4. 중지**

```bash
docker compose down
```

### 주요 포트

| 서비스 | 호스트 포트 | 컨테이너 포트 |
|--------|-------------|---------------|
| Frontend (Nginx) | 80 | 80 |
| Backend (Spring Boot) | 8202 | 8202 |
| PostgreSQL | 5420 | 5432 |

---

## 프로젝트 구조

```
.
├── readme/             # Spring Boot 백엔드
├── readme_vue/         # Vue 3 프론트엔드
├── docker-compose.yml  # 통합 실행
├── .env                # 환경변수 (Git 제외)
└── scripts/
    ├── docker-publish.sh   # 빌드 & 푸시 스크립트 (macOS/Linux)
    └── docker-publish.ps1  # 빌드 & 푸시 스크립트 (Windows)
```

---

## 환경변수 주요 항목

| 변수 | 설명 |
|------|------|
| `DB_USERNAME` / `DB_PASSWORD` | PostgreSQL 접속 정보 |
| `JWT_SECRET` | JWT 서명 키 |
| `GOOGLE_CLIENT_ID` / `SECRET` | Google OAuth2 |
| `KAKAO_CLIENT_ID` / `SECRET` | Kakao OAuth2 |
| `TOSS_CLIENT_KEY` / `SECRET_KEY` | Toss Payments |
| `KAKAO_SECRET_KEY` | Kakao Pay |
| `NAVER_PAY_CLIENT_ID` 등 | Naver Pay |
| `APP_BASE_URL` | 백엔드 서버 URL |
| `CORS_ALLOWED_ORIGINS` | CORS 허용 도메인 |
