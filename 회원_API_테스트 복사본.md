# 회원 API 테스트

**이 문서는 현재 ReadMe 프로젝트의 회원 기능 시연을 위해 핵심 회원 API만 추려서 테스트할 수 있도록 정리한 Markdown 문서입니다.**

## ✅ API 테스트 항목

| 항목 | 설명 |
| --- | --- |
| API 이름 | 기능 또는 엔드포인트 이름 |
| Method | HTTP 요청 메서드 (GET, POST, PUT, DELETE) |
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

## 📝 회원(Auth/MyPage) 관련 API 테스트

| API 이름 | Method | URL | 인증 | 요청 파라미터 | 요청 예시 | 응답 코드 | 응답 예시 | 테스트 목적 | 테스트 결과 | 담당자 |
| --- | --- | --- | --- | --- | --- | --- | --- | --- | --- | --- |
| 회원가입 | POST | `/api/auth/signup` | 불필요 | Body: `email`, `password`, `name`, `phone`, `address` 등 | `{ "email": "sample@readme.test", "password": "test1234!", "name": "테스트회원" }` | `200` | `"회원가입이 완료되었습니다."` | 신규 회원 등록 확인 | 엔드포인트 존재 확인, 실데이터 추가 테스트는 미실행 | Codex |
| 로그인 | POST | `/api/auth/signin` | 불필요 | Body: `email`, `password` | `{ "email": "user1@readme.test", "password": "test1234!" }` | `200` | `{ "accessToken": "...", "refreshToken": "..." }` | 일반 회원 로그인 및 토큰 발급 확인 | 정상 응답 확인 완료 | Codex |
| 로그아웃 | POST | `/api/auth/signout` | 필요 | Header: `Authorization` | Header: `Authorization: Bearer <ACCESS_TOKEN>` | `200` | `"로그아웃 되었습니다."` | 로그아웃 처리 확인 | 엔드포인트 존재 확인 | Codex |
| 토큰 재발급 | POST | `/api/auth/refresh` | 불필요 | Body: `refreshToken` | `{ "refreshToken": "<REFRESH_TOKEN>" }` | `200` | `{ "accessToken": "...", "refreshToken": "..." }` | 액세스 토큰 갱신 확인 | 엔드포인트 존재 확인 | Codex |
| 내 정보 조회 | GET | `/api/member/me` | 필요 | 없음 | Header: `Authorization: Bearer <ACCESS_TOKEN>` | `200` | `{ "id": 3, "email": "user1@readme.test", "name": "김독자" }` | 마이페이지 기본 정보 조회 확인 | 엔드포인트 존재 확인 | Codex |
| 내 정보 수정 | PUT | `/api/member/me` | 필요 | Body: 회원 정보 수정 값 | `{ "name": "김독자수정", "phone": "010-0000-0000", "address": "서울시 ..." }` | `200` | `{ "id": 3, "name": "김독자수정" }` | 회원 정보 수정 확인 | 엔드포인트 존재 확인, 실데이터 변경 테스트는 미실행 | Codex |
| 비밀번호 변경 | PUT | `/api/member/me/password` | 필요 | Body: 기존/새 비밀번호 | `{ "currentPassword": "test1234!", "newPassword": "new1234!", "confirmPassword": "new1234!" }` | `200` | `"비밀번호가 변경되었습니다."` | 비밀번호 변경 기능 확인 | 엔드포인트 존재 확인, 실데이터 변경 테스트는 미실행 | Codex |
| 회원 탈퇴 | DELETE | `/api/member/me` | 필요 | 없음 | Header: `Authorization: Bearer <ACCESS_TOKEN>` | `200` | `"회원 탈퇴가 완료되었습니다."` | 회원 탈퇴 처리 확인 | 엔드포인트 존재 확인, 실데이터 변경 테스트는 미실행 | Codex |
| 내 주문 목록 조회 | GET | `/api/member/me/orders?page=0&size=10` | 필요 | Query: `page`, `size` | `/api/member/me/orders?page=0&size=10` | `200` | `{ "content": [{ "orderId": 4, "number": "RM20260413004" }] }` | 마이페이지 주문내역 조회 확인 | 정상 응답 확인 완료 | Codex |
| 내 주문 상세 조회 | GET | `/api/member/me/orders/{orderId}` | 필요 | Path: `orderId` | `/api/member/me/orders/4` | `200` | `{ "orderId": 4, "number": "RM20260413004", "items": [...] }` | 주문 단건 상세 조회 확인 | 엔드포인트 존재 확인 | Codex |
| 내 결제 목록 조회 | GET | `/api/member/me/payments?page=0&size=10` | 필요 | Query: `page`, `size` | `/api/member/me/payments?page=0&size=10` | `200` | `{ "content": [{ "paymentId": 3, "paymentProvider": "KAKAO", "paymentStatus": "PAID" }] }` | 마이페이지 결제내역 조회 확인 | 정상 응답 확인 완료 | Codex |
| 내 리뷰 목록 조회 | GET | `/api/member/me/reviews?page=0&size=10` | 필요 | Query: `page`, `size` | `/api/member/me/reviews?page=0&size=10` | `200` | `{ "content": [{ "id": 2, "rating": 4, "content": "..." }] }` | 내 리뷰 관리 목록 확인 | 엔드포인트 존재 확인 | Codex |
| 내 문의 목록 조회 | GET | `/api/member/me/qnas?page=0&size=10` | 필요 | Query: `page`, `size` | `/api/member/me/qnas?page=0&size=10` | `200` | `{ "content": [{ "id": 1, "title": "배송 일정이 궁금합니다" }] }` | 내 문의 관리 목록 확인 | 엔드포인트 존재 확인 | Codex |

## ✅ 회원 API 테스트용 cURL 예시

```bash
# 1. 회원 로그인
curl -X POST http://localhost:8202/api/auth/signin \
  -H "Content-Type: application/json" \
  -d '{
    "email": "user1@readme.test",
    "password": "test1234!"
  }'
```

```bash
# 2. 내 정보 조회
curl http://localhost:8202/api/member/me \
  -H "Authorization: Bearer <ACCESS_TOKEN>"
```

```bash
# 3. 내 주문 목록 조회
curl "http://localhost:8202/api/member/me/orders?page=0&size=10" \
  -H "Authorization: Bearer <ACCESS_TOKEN>"
```

```bash
# 4. 내 결제 목록 조회
curl "http://localhost:8202/api/member/me/payments?page=0&size=10" \
  -H "Authorization: Bearer <ACCESS_TOKEN>"
```

## ✅ 발표용 빠른 체크 포인트

| 순서 | 확인 항목 | 기대 결과 |
| --- | --- | --- |
| 1 | 회원 로그인 | 토큰 발급 성공 |
| 2 | 내 정보 조회 | 사용자 이름/이메일 표시 |
| 3 | 내 주문내역 | 주문 목록 표시 |
| 4 | 내 결제내역 | 결제수단/결제상태 표시 |
| 5 | 내 리뷰/문의 | 마이페이지 관리 기능 확인 |
