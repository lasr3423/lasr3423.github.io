# 주문_결제 API 테스트

**이 문서는 현재 ReadMe 프로젝트의 주문/결제 기능 시연을 위해 핵심 주문 및 결제 API만 추려서 테스트할 수 있도록 정리한 Markdown 문서입니다.**

## ✅ API 테스트 항목

| 항목 | 설명 |
| --- | --- |
| API 이름 | 기능 또는 엔드포인트 이름 |
| Method | HTTP 요청 메서드 (GET, POST, DELETE) |
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
| 테스트 회원 계정 | `user1@readme.test` |
| 테스트 회원 비밀번호 | `test1234!` |
| 인증 방식 | `Authorization: Bearer <ACCESS_TOKEN>` |

## 📝 주문(Order) 관련 API 테스트

| API 이름 | Method | URL | 인증 | 요청 파라미터 | 요청 예시 | 응답 코드 | 응답 예시 | 테스트 목적 | 테스트 결과 | 담당자 |
| --- | --- | --- | --- | --- | --- | --- | --- | --- | --- | --- |
| 주문 생성 | POST | `/api/order` | 필요 | Body: 주문 상품/배송 정보 | `{ "items": [...], "receiverName": "김독자", "receiverPhone": "010-7000-1001", "deliveryAddress": "서울시 ..." }` | `200` | `{ "orderId": 7, "finalPrice": 15120, "itemName": "한국단편소설 40" }` | 장바구니 → 주문서 → 결제 흐름 시작점 확인 | 엔드포인트 존재 확인, 실주문 생성 테스트는 미실행 | Codex |
| 주문 목록 조회 | GET | `/api/order?page=0&size=10` | 필요 | Query: `page`, `size` | `/api/order?page=0&size=10` | `200` | `{ "content": [{ "orderId": 4, "number": "RM20260413004" }] }` | 주문 목록 API 응답 확인 | 엔드포인트 존재 확인 | Codex |
| 주문 상세 조회 | GET | `/api/order/{orderId}` | 필요 | Path: `orderId` | `/api/order/4` | `200` | `{ "orderId": 4, "number": "RM20260413004", "items": [...] }` | 주문 상세 확인 | 엔드포인트 존재 확인 | Codex |
| 주문 취소 | DELETE | `/api/order/{orderId}` | 필요 | Path: `orderId`, Body: `cancelReason` | `{ "cancelReason": "고객 요청" }` | `200` | `"주문이 취소되었습니다."` | 주문 취소 및 환불 처리 확인 | 엔드포인트 존재 확인, 실데이터 변경 테스트는 미실행 | Codex |

## 📝 결제(Payment) 관련 API 테스트

| API 이름 | Method | URL | 인증 | 요청 파라미터 | 요청 예시 | 응답 코드 | 응답 예시 | 테스트 목적 | 테스트 결과 | 담당자 |
| --- | --- | --- | --- | --- | --- | --- | --- | --- | --- | --- |
| 카카오/네이버 결제 준비 | POST | `/api/order/payment/ready` | 필요 | Body: `orderId`, `provider`, `successUrl`, `failUrl` | `{ "orderId": 4, "provider": "KAKAO", "successUrl": "http://localhost:5173/payment/success", "failUrl": "http://localhost:5173/payment/fail" }` | `200` | `{ "redirectUrl": "...", "tid": "..." }` | 외부 PG 결제창 이동 전 준비 단계 확인 | 엔드포인트 존재 확인 | Codex |
| 계좌이체 주문 접수 | POST | `/api/order/payment/bank-transfer` | 필요 | Body: 계좌이체 정보 | `{ "orderId": 4, "depositorName": "김독자" }` | `200` | `"계좌이체 주문이 접수되었습니다."` | 무통장입금 흐름 확인 | 엔드포인트 존재 확인 | Codex |
| 토스 결제 승인 | POST | `/api/order/payment/confirm` | 필요 | Body: `paymentKey`, `orderId`, `amount`, `method` | `{ "paymentKey": "test_key", "orderId": 4, "amount": 21600, "method": "CARD" }` | `200` | `"결제가 완료되었습니다."` | 토스 결제 승인 처리 확인 | 엔드포인트 존재 확인 | Codex |
| 카카오/네이버 결제 승인 | POST | `/api/order/payment/approve` | 필요 | Body: `orderId`, `provider`, `tid`, `pgToken`, `paymentId` | `{ "orderId": 4, "provider": "KAKAO", "tid": "KAKAO-TID-0004", "pgToken": "sample_pg_token" }` | `200` | `"결제가 완료되었습니다."` | 카카오/네이버 승인 후 처리 확인 | 엔드포인트 존재 확인 | Codex |
| 결제 실패 처리 | POST | `/api/order/payment/fail` | 불필요 | Body: `orderId`, `code`, `message` | `{ "orderId": 4, "code": "PAY_FAIL", "message": "사용자 결제 취소" }` | `200` | `"결제에 실패했습니다."` | 결제 실패 후 상태 반영 확인 | 엔드포인트 존재 확인 | Codex |
| 내 결제 목록 조회 | GET | `/api/member/me/payments?page=0&size=10` | 필요 | Query: `page`, `size` | `/api/member/me/payments?page=0&size=10` | `200` | `{ "content": [{ "paymentId": 3, "paymentProvider": "KAKAO", "paymentStatus": "PAID" }] }` | 결제 결과가 마이페이지에 반영되는지 확인 | 정상 응답 확인 완료 | Codex |

## ✅ 주문/결제 API 테스트용 cURL 예시

```bash
# 1. 사용자 로그인
curl -X POST http://localhost:8202/api/auth/signin \
  -H "Content-Type: application/json" \
  -d '{
    "email": "user1@readme.test",
    "password": "test1234!"
  }'
```

```bash
# 2. 주문 목록 조회
curl "http://localhost:8202/api/order?page=0&size=10" \
  -H "Authorization: Bearer <ACCESS_TOKEN>"
```

```bash
# 3. 결제 준비 (카카오페이 예시)
curl -X POST http://localhost:8202/api/order/payment/ready \
  -H "Authorization: Bearer <ACCESS_TOKEN>" \
  -H "Content-Type: application/json" \
  -d '{
    "orderId": 4,
    "provider": "KAKAO",
    "successUrl": "http://localhost:5173/payment/success",
    "failUrl": "http://localhost:5173/payment/fail"
  }'
```

```bash
# 4. 내 결제 목록 조회
curl "http://localhost:8202/api/member/me/payments?page=0&size=10" \
  -H "Authorization: Bearer <ACCESS_TOKEN>"
```

## ✅ 발표용 빠른 체크 포인트

| 순서 | 확인 항목 | 기대 결과 |
| --- | --- | --- |
| 1 | 주문 목록 조회 | 주문번호/상태/금액 표시 |
| 2 | 주문 상세 조회 | 주문 상품/배송지/결제 정보 표시 |
| 3 | 결제 수단 선택 화면 | 카카오/토스/네이버/계좌이체 선택 가능 |
| 4 | 결제 준비 API | PG 결제 준비 응답 반환 |
| 5 | 마이페이지 결제내역 | 결제수단/상태 확인 가능 |
