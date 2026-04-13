# 관리자 API 테스트

**이 문서는 현재 ReadMe 프로젝트의 관리자 기능 시연을 위해 핵심 관리자 API만 추려서 테스트할 수 있도록 정리한 Markdown 문서입니다.**

## ✅ API 테스트 항목

| 항목 | 설명 |
| --- | --- |
| API 이름 | 기능 또는 엔드포인트 이름 |
| Method | HTTP 요청 메서드 (GET, POST, PUT, PATCH, DELETE) |
| URL | 요청 URL 경로 |
| 인증 | Authorization 헤더 필요 여부 |
| 요청 파라미터 | Query, Path, Body 값 |
| 요청 예시 | 실제 요청 예시 |
| 응답 코드 | 기대 응답 코드 |
| 응답 예시 | 성공 응답 예시 |
| 테스트 목적 | 발표/시연에서 확인할 포인트 |
| 테스트 결과 | 현재 로컬 환경 확인 결과 |
| 담당자 | 구현 또는 문서화 담당자 |

## ✅ 공통 테스트 정보

| 항목 | 값 |
| --- | --- |
| Front Base URL | `http://localhost:5173` |
| API Base URL | `http://localhost:8202` |
| 관리자 계정 | `admin@readme.test` |
| 관리자 비밀번호 | `test1234!` |
| 인증 방식 | `Authorization: Bearer <ACCESS_TOKEN>` |

## 📝 관리자 인증 토큰 발급 API

| API 이름 | Method | URL | 인증 | 요청 파라미터 | 요청 예시 | 응답 코드 | 응답 예시 | 테스트 목적 | 테스트 결과 | 담당자 |
| --- | --- | --- | --- | --- | --- | --- | --- | --- | --- | --- |
| 관리자 로그인 | POST | `/api/auth/signin` | 불필요 | Body: `email`, `password` | `{ "email": "admin@readme.test", "password": "test1234!" }` | `200` | `{ "accessToken": "...", "refreshToken": "..." }` | 관리자 API 테스트용 JWT 발급 확인 | 정상 응답 확인 완료 | Codex |

## 📝 관리자(Admin) 관련 API 테스트

| API 이름 | Method | URL | 인증 | 요청 파라미터 | 요청 예시 | 응답 코드 | 응답 예시 | 테스트 목적 | 테스트 결과 | 담당자 |
| --- | --- | --- | --- | --- | --- | --- | --- | --- | --- | --- |
| 대시보드 조회 | GET | `/api/admin/dashboard` | 필요 | 없음 | Header: `Authorization: Bearer <ACCESS_TOKEN>` | `200` | `{ "totalMembers": 17, "totalProducts": 880, "totalOrders": 6, "monthSales": 62820, "unansweredQnaCount": 1 }` | 관리자 첫 화면 요약 정보 노출 여부 확인 | 정상 응답 확인 완료 | Codex |
| 회원 목록 조회 | GET | `/api/admin/members?page=0&size=5` | 필요 | Query: `page`, `size`, `keyword`, `status` | `/api/admin/members?page=0&size=5` | `200` | `{ "content": [{ "id": 3, "email": "user1@readme.test", "memberRole": "USER" }] }` | 회원 관리 목록 조회 확인 | 정상 응답 확인 완료 | Codex |
| 회원 권한 변경 | PATCH | `/api/admin/members/{memberId}/role?role=MANAGER` | 필요 | Path: `memberId`, Query: `role` | `/api/admin/members/3/role?role=MANAGER` | `200` | `"회원 권한이 변경되었습니다."` | 회원 권한 수정 기능 확인 | 엔드포인트 존재 확인, 실데이터 변경 테스트는 미실행 | Codex |
| 회원 상태 변경 | PATCH | `/api/admin/members/{memberId}/status?status=DEACTIVATE` | 필요 | Path: `memberId`, Query: `status` | `/api/admin/members/3/status?status=DEACTIVATE` | `200` | `"회원 상태가 변경되었습니다."` | 회원 활성/비활성 전환 확인 | 엔드포인트 존재 확인, 실데이터 변경 테스트는 미실행 | Codex |
| 상품 목록 조회 | GET | `/api/admin/products?page=0&size=3` | 필요 | Query: `page`, `size`, `status` | `/api/admin/products?page=0&size=3` | `200` | `{ "content": [{ "id": 2, "title": "중고생이 꼭 읽어야 할 한국단편소설 50", "stock": 10 }] }` | 관리자 상품 목록 조회 확인 | 정상 응답 확인 완료 | Codex |
| 상품 상세 조회 | GET | `/api/admin/products/{productId}` | 필요 | Path: `productId` | `/api/admin/products/2` | `200` | `{ "id": 2, "title": "...", "isbn": "9788965823148" }` | 관리자 상품 상세 정보 확인 | 엔드포인트 존재 확인, 목록 조회 기반으로 시연 가능 | Codex |
| 주문 목록 조회 | GET | `/api/admin/orders?page=0&size=5` | 필요 | Query: `page`, `size`, `status` | `/api/admin/orders?page=0&size=5` | `200` | `{ "content": [{ "orderId": 4, "number": "RM20260413004", "orderStatus": "DELIVERING" }] }` | 주문 관리 목록 조회 확인 | 정상 응답 확인 완료 | Codex |
| 주문 상세 조회 | GET | `/api/admin/orders/{orderId}` | 필요 | Path: `orderId` | `/api/admin/orders/4` | `200` | `{ "orderId": 4, "number": "RM20260413004", "items": [...] }` | 주문 단건 상세 확인 | 엔드포인트 존재 확인 | Codex |
| 결제 목록 조회 | GET | `/api/admin/payments?page=0&size=5` | 필요 | Query: `page`, `size`, `status` | `/api/admin/payments?page=0&size=5` | `200` | `{ "content": [{ "paymentId": 3, "paymentProvider": "KAKAO", "paymentStatus": "PAID" }] }` | 결제 관리 목록 조회 확인 | 정상 응답 확인 완료 | Codex |
| 결제 상세 조회 | GET | `/api/admin/payments/{paymentId}` | 필요 | Path: `paymentId` | `/api/admin/payments/3` | `200` | `{ "paymentId": 3, "orderNumber": "RM20260413004", "amount": 21600 }` | 결제 상세 정보 확인 | 엔드포인트 존재 확인 | Codex |
| 카테고리 전체 조회 | GET | `/api/admin/categories` | 필요 | 없음 | Header: `Authorization: Bearer <ACCESS_TOKEN>` | `200` | `[ { "id": 1, "name": "국내도서", "subCategories": [...] } ]` | 카테고리 관리 화면 데이터 확인 | 정상 응답 확인 완료 | Codex |
| 공지 목록 조회 | GET | `/api/admin/notices?page=0&size=10` | 필요 | Query: `page`, `size` | `/api/admin/notices?page=0&size=10` | `200` | `{ "content": [{ "id": 1, "title": "4월 배송 일정 안내" }] }` | 공지사항 관리 목록 확인 | 엔드포인트 존재 확인 | Codex |
| 문의 목록 조회 | GET | `/api/admin/qnas?page=0&size=10` | 필요 | Query: `page`, `size`, `unansweredOnly` | `/api/admin/qnas?page=0&size=10&unansweredOnly=false` | `200` | `{ "content": [{ "id": 1, "title": "배송 일정이 궁금합니다" }] }` | 문의 관리 목록 확인 | 엔드포인트 존재 확인 | Codex |
| 리뷰 목록 조회 | GET | `/api/admin/reviews?page=0&size=10` | 필요 | Query: `page`, `size`, `keyword`, `searchType` | `/api/admin/reviews?page=0&size=10` | `200` | `{ "content": [{ "id": 1, "rating": 5, "content": "배송이 빠르고..." }] }` | 리뷰 관리 목록 확인 | 엔드포인트 존재 확인 | Codex |

## ✅ 관리자 API 테스트용 cURL 예시

```bash
# 1. 관리자 로그인
curl -X POST http://localhost:8202/api/auth/signin \
  -H "Content-Type: application/json" \
  -d '{
    "email": "admin@readme.test",
    "password": "test1234!"
  }'
```

```bash
# 2. 대시보드 조회
curl http://localhost:8202/api/admin/dashboard \
  -H "Authorization: Bearer <ACCESS_TOKEN>"
```

```bash
# 3. 회원 목록 조회
curl "http://localhost:8202/api/admin/members?page=0&size=5" \
  -H "Authorization: Bearer <ACCESS_TOKEN>"
```

```bash
# 4. 상품 목록 조회
curl "http://localhost:8202/api/admin/products?page=0&size=3" \
  -H "Authorization: Bearer <ACCESS_TOKEN>"
```

```bash
# 5. 주문 목록 조회
curl "http://localhost:8202/api/admin/orders?page=0&size=5" \
  -H "Authorization: Bearer <ACCESS_TOKEN>"
```

```bash
# 6. 결제 목록 조회
curl "http://localhost:8202/api/admin/payments?page=0&size=5" \
  -H "Authorization: Bearer <ACCESS_TOKEN>"
```

```bash
# 7. 카테고리 조회
curl http://localhost:8202/api/admin/categories \
  -H "Authorization: Bearer <ACCESS_TOKEN>"
```

## ✅ 발표용 빠른 체크 포인트

| 순서 | 확인 항목 | 기대 결과 |
| --- | --- | --- |
| 1 | 관리자 로그인 | 토큰 발급 성공 |
| 2 | 관리자 대시보드 | 회원/상품/주문/매출 수치 표시 |
| 3 | 회원 관리 | 회원 목록 조회 가능 |
| 4 | 상품 관리 | 상품 목록 조회 가능 |
| 5 | 주문 관리 | 주문 상태별 목록 확인 가능 |
| 6 | 결제 관리 | 결제 목록 및 결제수단 표시 |
| 7 | 카테고리 관리 | 상위/하위 카테고리 구조 표시 |
