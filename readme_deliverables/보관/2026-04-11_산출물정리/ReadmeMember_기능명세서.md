# 08_기능 명세서

| 항목 | 내용 |
| --- | --- |
| 프로젝트명 | Readme Bookstore Shopping Mall |
| 개발 환경 | Java 21 / Spring Boot 3.5 / PostgreSQL / Vue.js 3 / Docker |
| 작성일 | 2026-03-22 |
| 작성자 | Readme Team (Member 파트) |
| 연관 문서 | 요구사항정의서 Member.md / ReadmeMember_유스케이스명세서.md |

---

## 📋 기능 목록

| 기능 ID | 기능명 | 연관 UC | HTTP 메서드 | 요청 URL | 액터 |
| --- | --- | --- | --- | --- | --- |
| FA-001 | 관리자 대시보드 조회 | UC-A-001 | GET | /admin | 관리자 |
| FA-002 | 회원 목록 조회 | UC-A-002 | GET | /admin/member/list | 관리자 |
| FA-003 | 회원 상세 조회 | UC-A-003 | GET | /admin/member/{id} | 관리자 |
| FA-004 | 회원 강퇴 | UC-A-004 | PATCH | /admin/member/{id}/status | 관리자(ADMIN) |
| FA-005 | 관리자 권한 변경 | UC-A-005 | PATCH | /admin/member/{id}/role | 관리자(ADMIN) |
| FA-006 | 탈퇴 회원 관리 | UC-A-006 | GET | /admin/member/list | 관리자 |
| FA-007 | 주문 목록 조회 | UC-A-007 | GET | /admin/order | 관리자 |
| FA-008 | 주문 상세 조회 | UC-A-008 | GET | /admin/order/{id} | 관리자 |
| FA-009 | 주문 상태 변경 | UC-A-009 | PATCH | /admin/order/{id}/status | 관리자 |
| FA-010 | 주문 취소 처리 | UC-A-010 | PATCH | /admin/order/{id}/cancel | 관리자 |
| FA-011 | 배송 목록 조회 | UC-A-011 | GET | /admin/delivery | 관리자 |
| FA-012 | 운송장 등록 | UC-A-012 | POST | /admin/delivery/{id} | 관리자 |
| FA-013 | 배송 상태 변경 | UC-A-013 | PATCH | /admin/delivery/{id}/status | 관리자 |
| FA-014 | 상품 목록 조회 | UC-A-014 | GET | /admin/product/list | 관리자 |
| FA-015 | 상품 등록 | UC-A-015 | POST | /admin/product/insert | 관리자 |
| FA-016 | 상품 수정 | UC-A-016 | PATCH | /admin/product/{id} | 관리자 |
| FA-017 | 상품 삭제 | UC-A-017 | DELETE | /admin/product/{id} | 관리자(ADMIN) |
| FA-018 | 카테고리 등록 | UC-A-018 | POST | /admin/category | 관리자 |
| FA-019 | 카테고리 수정 | UC-A-019 | PATCH | /admin/category/{id} | 관리자 |
| FA-020 | 카테고리 삭제 | UC-A-020 | DELETE | /admin/category/{id} | 관리자(ADMIN) |
| FA-021 | 공지사항 목록 조회 | UC-A-021 | GET | /admin/notice/list | 관리자 |
| FA-022 | 공지사항 등록 | UC-A-022 | POST | /admin/notice/insert | 관리자 |
| FA-023 | 공지사항 수정 | UC-A-023 | PATCH | /admin/notice/{id} | 관리자 |
| FA-024 | 공지사항 삭제 | UC-A-024 | DELETE | /admin/notice/{id} | 관리자 |
| FA-025 | 리뷰 목록 조회 | UC-A-025 | GET | /admin/review/list | 관리자 |
| FA-026 | 리뷰 삭제 | UC-A-026 | DELETE | /admin/review/{id} | 관리자 |
| FA-027 | QnA 질문 목록 조회 | UC-A-027 | GET | /admin/qna/list | 관리자 |
| FA-028 | QnA 질문 상세 조회 | UC-A-028 | GET | /admin/qna/{id} | 관리자 |
| FA-029 | QnA 답변 등록 | UC-A-029 | POST | /admin/qna/{id}/insert | 관리자 |
| FA-030 | QnA 답변 수정 | UC-A-030 | PATCH | /admin/qna/{id} | 관리자 |
| FA-031 | QnA 답변 삭제 | UC-A-031 | DELETE | /admin/qna/{id} | 관리자 |
| FM-001 | 회원가입 | UC-M-001 | POST | /signup | 비회원 |
| FM-002 | 로그인 | UC-M-002 | POST | /signin | 회원 |
| FM-003 | 로그아웃 | UC-M-003 | POST | /signout | 회원 |
| FM-004 | 마이페이지 주문 현황 조회 | UC-M-004 | GET | /mypage | 회원 |
| FM-005 | 마이페이지 주문/배송 조회 | UC-M-005 | GET | /mypage | 회원 |
| FM-006 | 마이페이지 리뷰 조회 | UC-M-006 | GET | /mypage | 회원 |
| FM-007 | 마이페이지 QnA 조회 | UC-M-007 | GET | /mypage | 회원 |
| FM-008 | 회원 정보 수정 | UC-M-008 | PATCH | /mypage/edit | 회원 |
| FM-009 | 비밀번호 변경 | UC-M-009 | PATCH | /mypage/password | 회원 |
| FM-010 | 회원 탈퇴 | UC-M-010 | DELETE | /mypage | 회원 |
| FM-011 | 주문 목록 조회 | UC-M-011 | GET | /mypage/order/list | 회원 |
| FM-012 | 주문 상세 조회 | UC-M-012 | GET | /mypage/order/{id} | 회원 |
| FM-013 | 주문 취소 요청 | UC-M-013 | PATCH | /mypage/order/{id}/cancel | 회원 |
| FM-014 | 배송 상태 조회 | UC-M-014 | GET | /mypage/order/{id} | 회원 |
| FM-015 | 운송장 번호 조회 | UC-M-015 | GET | /mypage/order/{id} | 회원 |
| FM-016 | 작성 리뷰 목록 조회 | UC-M-016 | GET | /mypage/review/list | 회원 |
| FM-017 | 작성 리뷰 상세 조회 | UC-M-017 | GET | /mypage/review/{id} | 회원 |
| FM-018 | 리뷰 수정 | UC-M-018 | PATCH | /mypage/review/{id} | 회원 |
| FM-019 | 리뷰 삭제 | UC-M-019 | DELETE | /mypage/review/{id} | 회원 |
| FM-020 | 작성한 질문 목록 조회 | UC-M-020 | GET | /mypage/qna/list | 회원 |
| FM-021 | 작성한 질문 상세 조회 | UC-M-021 | GET | /mypage/qna/{id} | 회원 |
| FM-022 | 질문 수정 | UC-M-022 | PATCH | /mypage/qna/{id} | 회원 |
| FM-023 | 질문 삭제 | UC-M-023 | DELETE | /mypage/qna/{id} | 회원 |

---

## 📄 기능 명세서 (Function Specification Sheet)

---

## 🔐 관리자 (Admin) 기능

### ✅ FA-001 관리자 대시보드 조회

| 기능 ID | **FA-001** |
| --- | --- |
| 기능명 | 관리자 대시보드 조회 |
| 설명 | 미승인 주문·QnA·재고부족·통계·신규 회원 등 요약 현황을 표시한다. |
| 연관 UC | UC-A-001 (관리자 대시보드 조회) |
| HTTP 메서드 | GET |
| 요청 URL | `/admin` |
| 입력값 | - (Authorization 헤더) |
| 출력값 | 대시보드 위젯 데이터 JSON (DashboardResponse) |

**처리 흐름**

1. 관리자 JWT 인증 확인 (MANAGER 또는 ADMIN 역할)
2. 미승인 주문 목록 집계 (orderStatus = PENDING, 최신 5~10건)
3. 미답변 QnA 목록 집계 (qna_status = WAITING, 최신 5~10건)
4. 재고 부족 상품 목록 집계 (stock_quantity ≤ 기준치)
5. 오늘/이번 달 매출·주문 수 집계
6. 신규 회원 수 집계 (created_at 기준)
7. DashboardResponse DTO로 조합 후 JSON 반환

---

### ✅ FA-002 회원 목록 조회

| 기능 ID | **FA-002** |
| --- | --- |
| 기능명 | 회원 목록 조회 |
| 설명 | 전체 회원 목록을 페이징·키워드 검색으로 조회한다. |
| 연관 UC | UC-A-002 (회원 목록 조회) |
| HTTP 메서드 | GET |
| 요청 URL | `/admin/member/list` |
| 입력값 | `page`, `size`, `keyword` (이름/이메일, Query Param) |
| 출력값 | `Page<MemberResponse>` |

**처리 흐름**

1. 관리자 인증 확인
2. 검색 파라미터(keyword) 파싱
3. 이름 또는 이메일 LIKE 조건으로 DB 조회
4. 페이징 처리 후 결과 반환

---

### ✅ FA-003 회원 상세 조회

| 기능 ID | **FA-003** |
| --- | --- |
| 기능명 | 회원 상세 조회 |
| 설명 | 특정 회원의 상세 정보를 조회한다. |
| 연관 UC | UC-A-003 (회원 상세 조회) |
| HTTP 메서드 | GET |
| 요청 URL | `/admin/member/{id}` |
| 입력값 | `memberId` (Path Variable) |
| 출력값 | `MemberDetailResponse` (가입일·상태·역할 포함) |

**처리 흐름**

1. 관리자 인증 확인
2. `memberId`로 member 테이블 조회
3. 존재하지 않으면 404 응답
4. MemberDetailResponse 반환

---

### ✅ FA-004 회원 강퇴

| 기능 ID | **FA-004** |
| --- | --- |
| 기능명 | 회원 강퇴 |
| 설명 | 회원 상태를 ACTIVATE / DEACTIVATE / DELETE로 변경한다. |
| 연관 UC | UC-A-004 (회원 강퇴) |
| HTTP 메서드 | PATCH |
| 요청 URL | `/admin/member/{id}/status` |
| 입력값 | `memberId` (Path), `{ memberStatus }` (Body) |
| 출력값 | 수정 완료 응답 |

**처리 흐름**

1. ADMIN 권한 확인 (MANAGER 불가)
2. 대상 회원 존재 확인
3. memberStatus ENUM 유효성 확인
4. memberStatus 변경 및 저장
5. 완료 응답 반환

---

### ✅ FA-005 관리자 권한 변경

| 기능 ID | **FA-005** |
| --- | --- |
| 기능명 | 관리자 권한 변경 |
| 설명 | 회원 역할을 USER / MANAGER / ADMIN으로 변경한다. |
| 연관 UC | UC-A-005 (관리자 권한 변경) |
| HTTP 메서드 | PATCH |
| 요청 URL | `/admin/member/{id}/role` |
| 입력값 | `memberId` (Path), `{ memberRole }` (Body) |
| 출력값 | 수정 완료 응답 |

**처리 흐름**

1. ADMIN 권한 확인 (MANAGER 불가)
2. 대상 회원 존재 확인
3. memberRole ENUM 유효성 확인
4. memberRole 변경 및 저장
5. 완료 응답 반환

---

### ✅ FA-006 탈퇴 회원 관리

| 기능 ID | **FA-006** |
| --- | --- |
| 기능명 | 탈퇴 회원 관리 |
| 설명 | `deleted_at` 기준 탈퇴 회원 목록을 조회한다. |
| 연관 UC | UC-A-006 (탈퇴 회원 관리) |
| HTTP 메서드 | GET |
| 요청 URL | `/admin/member/list` |
| 입력값 | `page`, `size`, `status=DELETE` (Query Param) |
| 출력값 | `Page<MemberResponse>` |

**처리 흐름**

1. 관리자 인증 확인
2. `deleted_at IS NOT NULL` 또는 `memberStatus = DELETE` 조건 적용
3. 페이징 처리 후 결과 반환

---

### ✅ FA-007 주문 목록 조회

| 기능 ID | **FA-007** |
| --- | --- |
| 기능명 | 주문 목록 조회 |
| 설명 | 전체 주문 목록을 페이징·주문번호/회원 검색으로 조회한다. |
| 연관 UC | UC-A-007 (주문 목록 조회) |
| HTTP 메서드 | GET |
| 요청 URL | `/admin/order` |
| 입력값 | `page`, `size`, `keyword` (주문번호/회원명, Query Param) |
| 출력값 | `Page<OrderResponse>` |

**처리 흐름**

1. 관리자 인증 확인
2. keyword 기반 주문번호 또는 회원명 LIKE 조회
3. 페이징 처리 후 결과 반환

---

### ✅ FA-008 주문 상세 조회

| 기능 ID | **FA-008** |
| --- | --- |
| 기능명 | 주문 상세 조회 |
| 설명 | 특정 주문의 상품·결제·배송 상세 정보를 조회한다. |
| 연관 UC | UC-A-008 (주문 상세 조회) |
| HTTP 메서드 | GET |
| 요청 URL | `/admin/order/{id}` |
| 입력값 | `orderId` (Path Variable) |
| 출력값 | `OrderDetailResponse` (주문 상품·결제·배송 포함) |

**처리 흐름**

1. 관리자 인증 확인
2. `orderId`로 order 테이블 조회
3. 주문 상품(order_item), 결제(payment), 배송(delivery) JOIN
4. OrderDetailResponse 반환

---

### ✅ FA-009 주문 상태 변경

| 기능 ID | **FA-009** |
| --- | --- |
| 기능명 | 주문 상태 변경 |
| 설명 | 주문 상태를 PENDING / PAYED / APPROVAL / CANCELED로 변경한다. |
| 연관 UC | UC-A-009 (주문 상태 변경) |
| HTTP 메서드 | PATCH |
| 요청 URL | `/admin/order/{id}/status` |
| 입력값 | `orderId` (Path), `{ orderStatus }` (Body) |
| 출력값 | 수정 완료 응답 |

**처리 흐름**

1. 관리자 인증 확인
2. 주문 존재 확인
3. orderStatus ENUM 유효성 확인
4. orderStatus 변경
5. 결제 상태 연동 처리 (필요 시)
6. 저장 후 응답 반환

---

### ✅ FA-010 주문 취소 처리

| 기능 ID | **FA-010** |
| --- | --- |
| 기능명 | 주문 취소 처리 |
| 설명 | 관리자 측에서 주문을 강제 취소 처리한다. |
| 연관 UC | UC-A-010 (주문 취소 처리) |
| HTTP 메서드 | PATCH |
| 요청 URL | `/admin/order/{id}/cancel` |
| 입력값 | `orderId` (Path Variable) |
| 출력값 | 취소 완료 응답 |

**처리 흐름**

1. 관리자 인증 확인
2. 주문이 PAYED 이하 상태임을 확인
3. orderStatus = CANCELED 처리
4. 결제 취소 연동
5. 저장 후 응답 반환

---

### ✅ FA-011 배송 목록 조회

| 기능 ID | **FA-011** |
| --- | --- |
| 기능명 | 배송 목록 조회 |
| 설명 | 전체 배송 목록을 상태별 필터로 조회한다. |
| 연관 UC | UC-A-011 (배송 목록 조회) |
| HTTP 메서드 | GET |
| 요청 URL | `/admin/delivery` |
| 입력값 | `page`, `size`, `status` (Query Param) |
| 출력값 | `Page<DeliveryResponse>` |

**처리 흐름**

1. 관리자 인증 확인
2. 상태 필터(deliveryStatus) 파싱
3. 조건 적용 후 페이징 결과 반환

---

### ✅ FA-012 운송장 등록

| 기능 ID | **FA-012** |
| --- | --- |
| 기능명 | 운송장 등록 |
| 설명 | 택배사명 및 운송장 번호를 입력하여 등록한다. |
| 연관 UC | UC-A-012 (운송장 등록) |
| HTTP 메서드 | POST |
| 요청 URL | `/admin/delivery/{id}` |
| 입력값 | `deliveryId` (Path), `{ courier, trackingNumber }` (Body) |
| 출력값 | 등록 완료 응답 |

**처리 흐름**

1. 관리자 인증 확인
2. 배송 상태가 READY임을 확인
3. courier·tracking_number 저장
4. delivery_status = SHIPPED 처리
5. 완료 응답 반환

---

### ✅ FA-013 배송 상태 변경

| 기능 ID | **FA-013** |
| --- | --- |
| 기능명 | 배송 상태 변경 |
| 설명 | 배송 상태를 READY / SHIPPED / IN_TRANSIT / DELIVERED / FAILED 단계로 변경한다. |
| 연관 UC | UC-A-013 (배송 상태 변경) |
| HTTP 메서드 | PATCH |
| 요청 URL | `/admin/delivery/{id}/status` |
| 입력값 | `deliveryId` (Path), `{ deliveryStatus }` (Body) |
| 출력값 | 수정 완료 응답 |

**처리 흐름**

1. 관리자 인증 확인
2. 배송 존재 확인
3. deliveryStatus ENUM 유효성 확인
4. deliveryStatus 변경 및 저장
5. 완료 응답 반환

---

### ✅ FA-014 상품 목록 조회

| 기능 ID | **FA-014** |
| --- | --- |
| 기능명 | 상품 목록 조회 |
| 설명 | 상품 정보 및 썸네일 이미지 목록을 조회한다. |
| 연관 UC | UC-A-014 (상품 목록 조회) |
| HTTP 메서드 | GET |
| 요청 URL | `/admin/product/list` |
| 입력값 | `page`, `size`, `keyword` (Query Param) |
| 출력값 | `Page<ProductResponse>` (썸네일 포함) |

**처리 흐름**

1. 관리자 인증 확인
2. keyword로 상품명·저자 LIKE 조회
3. 썸네일 포함 페이징 결과 반환

---

### ✅ FA-015 상품 등록

| 기능 ID | **FA-015** |
| --- | --- |
| 기능명 | 상품 등록 |
| 설명 | 상품 정보 및 이미지를 업로드하여 등록한다. |
| 연관 UC | UC-A-015 (상품 등록) |
| HTTP 메서드 | POST |
| 요청 URL | `/admin/product/insert` |
| 입력값 | `{ title, author, publisher, price, stockQuantity, description, categorySubId }` + 이미지 파일 (MultipartFile) |
| 출력값 | 등록 완료 응답 (productId 포함) |

**처리 흐름**

1. 관리자 인증 확인
2. 입력값 유효성 검사 (썸네일 필수 확인)
3. 이미지 파일 업로드 처리
4. product 테이블 저장
5. productId 반환

---

### ✅ FA-016 상품 수정

| 기능 ID | **FA-016** |
| --- | --- |
| 기능명 | 상품 수정 |
| 설명 | 상품 정보(재고·가격 등)를 수정한다. |
| 연관 UC | UC-A-016 (상품 수정) |
| HTTP 메서드 | PATCH |
| 요청 URL | `/admin/product/{id}` |
| 입력값 | `productId` (Path), `{ price, stockQuantity, description, ... }` (Body) |
| 출력값 | 수정 완료 응답 |

**처리 흐름**

1. 관리자 인증 확인
2. 상품 존재 확인
3. 입력값 유효성 검사
4. 변경 필드 업데이트 및 updated_at 갱신
5. 저장 후 응답 반환

---

### ✅ FA-017 상품 삭제

| 기능 ID | **FA-017** |
| --- | --- |
| 기능명 | 상품 삭제 |
| 설명 | 상품을 soft delete 방식으로 처리한다. |
| 연관 UC | UC-A-017 (상품 삭제) |
| HTTP 메서드 | DELETE |
| 요청 URL | `/admin/product/{id}` |
| 입력값 | `productId` (Path Variable) |
| 출력값 | 삭제 완료 응답 |

**처리 흐름**

1. ADMIN 권한 확인
2. 상품이 ACTIVATE 상태임을 확인
3. product_status = DELETE 처리
4. deleted_at 기록
5. 저장 후 응답 반환

---

### ✅ FA-018 카테고리 등록

| 기능 ID | **FA-018** |
| --- | --- |
| 기능명 | 카테고리 등록 |
| 설명 | 상위/하위 카테고리를 등록한다. |
| 연관 UC | UC-A-018 (카테고리 등록) |
| HTTP 메서드 | POST |
| 요청 URL | `/admin/category` |
| 입력값 | `{ type(TOP/SUB), name, sortOrder, categoryTopId(SUB 전용) }` (Body) |
| 출력값 | 등록 완료 응답 (categoryId 포함) |

**처리 흐름**

1. 관리자 인증 확인
2. type 기준 분기 처리 (TOP: category_top, SUB: category_sub)
3. SUB인 경우 categoryTopId 존재 확인
4. DB 저장
5. categoryId 반환

---

### ✅ FA-019 카테고리 수정

| 기능 ID | **FA-019** |
| --- | --- |
| 기능명 | 카테고리 수정 |
| 설명 | 카테고리 이름 및 정렬 순서를 수정한다. |
| 연관 UC | UC-A-019 (카테고리 수정) |
| HTTP 메서드 | PATCH |
| 요청 URL | `/admin/category/{id}` |
| 입력값 | `categoryId` (Path), `{ name, sortOrder }` (Body) |
| 출력값 | 수정 완료 응답 |

**처리 흐름**

1. 관리자 인증 확인
2. 카테고리 존재 확인
3. name·sortOrder 업데이트
4. 저장 후 응답 반환

---

### ✅ FA-020 카테고리 삭제

| 기능 ID | **FA-020** |
| --- | --- |
| 기능명 | 카테고리 삭제 |
| 설명 | 카테고리 상태를 DEACTIVATE/DELETE로 변경한다. |
| 연관 UC | UC-A-020 (카테고리 삭제) |
| HTTP 메서드 | DELETE |
| 요청 URL | `/admin/category/{id}` |
| 입력값 | `categoryId` (Path Variable) |
| 출력값 | 삭제 완료 응답 |

**처리 흐름**

1. ADMIN 권한 확인
2. 카테고리 존재 확인
3. category_status = DELETE 처리
4. deleted_at 기록
5. 저장 후 응답 반환

---

### ✅ FA-021 공지사항 목록 조회

| 기능 ID | **FA-021** |
| --- | --- |
| 기능명 | 공지사항 목록 조회 |
| 설명 | 공지사항 목록을 조회한다. is_fixed=true 항목은 상단 고정으로 표시한다. |
| 연관 UC | UC-A-021 (공지사항 목록 조회) |
| HTTP 메서드 | GET |
| 요청 URL | `/admin/notice/list` |
| 입력값 | `page`, `size` (Query Param) |
| 출력값 | `Page<NoticeResponse>` |

**처리 흐름**

1. 관리자 인증 확인
2. is_fixed DESC, created_at DESC 정렬 조회
3. 페이징 결과 반환

---

### ✅ FA-022 공지사항 등록

| 기능 ID | **FA-022** |
| --- | --- |
| 기능명 | 공지사항 등록 |
| 설명 | 신규 공지사항을 추가한다. |
| 연관 UC | UC-A-022 (공지사항 등록) |
| HTTP 메서드 | POST |
| 요청 URL | `/admin/notice/insert` |
| 입력값 | `{ title, content, isFixed }` (Body) |
| 출력값 | 등록 완료 응답 (noticeId 포함) |

**처리 흐름**

1. 관리자 인증 확인
2. 입력값 유효성 검사
3. notice 테이블 저장
4. noticeId 반환

---

### ✅ FA-023 공지사항 수정

| 기능 ID | **FA-023** |
| --- | --- |
| 기능명 | 공지사항 수정 |
| 설명 | 공지사항 제목·내용을 수정한다. |
| 연관 UC | UC-A-023 (공지사항 수정) |
| HTTP 메서드 | PATCH |
| 요청 URL | `/admin/notice/{id}` |
| 입력값 | `noticeId` (Path), `{ title, content, isFixed }` (Body) |
| 출력값 | 수정 완료 응답 |

**처리 흐름**

1. 관리자 인증 확인
2. 공지사항 존재 확인
3. 입력 필드 업데이트
4. updated_at 갱신 및 저장

---

### ✅ FA-024 공지사항 삭제

| 기능 ID | **FA-024** |
| --- | --- |
| 기능명 | 공지사항 삭제 |
| 설명 | 공지사항을 soft delete 방식으로 처리한다. |
| 연관 UC | UC-A-024 (공지사항 삭제) |
| HTTP 메서드 | DELETE |
| 요청 URL | `/admin/notice/{id}` |
| 입력값 | `noticeId` (Path Variable) |
| 출력값 | 삭제 완료 응답 |

**처리 흐름**

1. 관리자 인증 확인
2. 공지사항 존재 확인
3. deleted_at 기록
4. 저장 후 응답 반환

---

### ✅ FA-025 리뷰 목록 조회

| 기능 ID | **FA-025** |
| --- | --- |
| 기능명 | 리뷰 목록 조회 |
| 설명 | 리뷰 및 연관 상품 목록을 조회한다. |
| 연관 UC | UC-A-025 (리뷰 목록 조회) |
| HTTP 메서드 | GET |
| 요청 URL | `/admin/review/list` |
| 입력값 | `page`, `size`, `keyword` (Query Param) |
| 출력값 | `Page<ReviewResponse>` (상품명 포함) |

**처리 흐름**

1. 관리자 인증 확인
2. 상품명 포함하여 review 조회 (product JOIN)
3. 페이징 결과 반환

---

### ✅ FA-026 리뷰 삭제

| 기능 ID | **FA-026** |
| --- | --- |
| 기능명 | 리뷰 삭제 |
| 설명 | 리뷰를 soft delete 방식으로 처리한다. |
| 연관 UC | UC-A-026 (리뷰 삭제) |
| HTTP 메서드 | DELETE |
| 요청 URL | `/admin/review/{id}` |
| 입력값 | `reviewId` (Path Variable) |
| 출력값 | 삭제 완료 응답 |

**처리 흐름**

1. 관리자 인증 확인
2. 리뷰 존재 확인
3. deleted_at 기록
4. 저장 후 응답 반환

---

### ✅ FA-027 QnA 질문 목록 조회

| 기능 ID | **FA-027** |
| --- | --- |
| 기능명 | QnA 질문 목록 조회 |
| 설명 | QnA 질문 목록을 조회한다. 상품명을 함께 표시한다. |
| 연관 UC | UC-A-027 (질문 목록 조회) |
| HTTP 메서드 | GET |
| 요청 URL | `/admin/qna/list` |
| 입력값 | `page`, `size` (Query Param) |
| 출력값 | `Page<QnaResponse>` (상품명 포함) |

**처리 흐름**

1. 관리자 인증 확인
2. depth=0 조건(질문만 조회)으로 DB 조회
3. 상품명 JOIN
4. 페이징 결과 반환

---

### ✅ FA-028 QnA 질문 상세 조회

| 기능 ID | **FA-028** |
| --- | --- |
| 기능명 | QnA 질문 상세 조회 |
| 설명 | QnA 질문 내용 및 답변을 상세 조회한다. |
| 연관 UC | UC-A-028 (질문 상세 조회) |
| HTTP 메서드 | GET |
| 요청 URL | `/admin/qna/{id}` |
| 입력값 | `qnaId` (Path Variable) |
| 출력값 | `QnaDetailResponse` (질문·답변 포함) |

**처리 흐름**

1. 관리자 인증 확인
2. qnaId로 질문(depth=0) 조회
3. parent_id 기준 답변(depth=1) JOIN
4. QnaDetailResponse 반환

---

### ✅ FA-029 QnA 답변 등록

| 기능 ID | **FA-029** |
| --- | --- |
| 기능명 | QnA 답변 등록 |
| 설명 | QnA 질문에 대한 답변을 등록한다. |
| 연관 UC | UC-A-029 (답변 등록) |
| HTTP 메서드 | POST |
| 요청 URL | `/admin/qna/{id}/insert` |
| 입력값 | `parentId` (Path), `{ content }` (Body) |
| 출력값 | 등록 완료 응답 (qnaId 포함) |

**처리 흐름**

1. 관리자 인증 확인
2. 질문 존재 및 qna_status = WAITING/PROCESSING 확인
3. depth=1 답변 저장 (parent_id = parentId)
4. 부모 질문의 qna_status = COMPLETE 처리
5. qnaId 반환

---

### ✅ FA-030 QnA 답변 수정

| 기능 ID | **FA-030** |
| --- | --- |
| 기능명 | QnA 답변 수정 |
| 설명 | 등록한 답변 내용을 수정한다. |
| 연관 UC | UC-A-030 (답변 수정) |
| HTTP 메서드 | PATCH |
| 요청 URL | `/admin/qna/{id}` |
| 입력값 | `qnaId` (Path), `{ content }` (Body) |
| 출력값 | 수정 완료 응답 |

**처리 흐름**

1. 관리자 인증 확인
2. 답변 존재 확인 (depth=1)
3. content 업데이트
4. updated_at 갱신 및 저장

---

### ✅ FA-031 QnA 답변 삭제

| 기능 ID | **FA-031** |
| --- | --- |
| 기능명 | QnA 답변 삭제 |
| 설명 | 답변을 soft delete 방식으로 처리한다. |
| 연관 UC | UC-A-031 (답변 삭제) |
| HTTP 메서드 | DELETE |
| 요청 URL | `/admin/qna/{id}` |
| 입력값 | `qnaId` (Path Variable) |
| 출력값 | 삭제 완료 응답 |

**처리 흐름**

1. 관리자 인증 확인
2. 답변 존재 확인 (depth=1)
3. deleted_at 기록
4. 부모 질문 qna_status = WAITING으로 재조정
5. 저장 후 응답 반환

---

## 👤 회원 (Member) 기능

### ✅ FM-001 회원가입

| 기능 ID | **FM-001** |
| --- | --- |
| 기능명 | 회원가입 |
| 설명 | 이메일·비밀번호·이름·전화번호·주소 입력 후 회원으로 등록한다. |
| 연관 UC | UC-M-001 (회원가입) |
| HTTP 메서드 | POST |
| 요청 URL | `/signup` |
| 입력값 | `{ email, password, name, phone, address }` (Body) |
| 출력값 | 회원가입 완료 응답 |

**처리 흐름**

1. 이메일 중복 확인 (UNIQUE 제약)
2. 입력값 유효성 검사 (이메일 형식, 비밀번호 조건 등)
3. BCrypt 비밀번호 암호화
4. member 테이블 저장 (memberStatus = ACTIVATE, memberRole = USER)
5. 완료 응답 반환

---

### ✅ FM-002 로그인

| 기능 ID | **FM-002** |
| --- | --- |
| 기능명 | 로그인 |
| 설명 | 이메일+비밀번호 로그인 및 JWT Access/Refresh Token을 발급한다. |
| 연관 UC | UC-M-002 (로그인) |
| HTTP 메서드 | POST |
| 요청 URL | `/signin` |
| 입력값 | `{ email, password }` (Body) |
| 출력값 | `{ accessToken, refreshToken }` |

**처리 흐름**

1. 이메일로 회원 조회 (미존재 시 401)
2. BCrypt 비밀번호 검증 (불일치 시 401)
3. memberStatus = ACTIVATE 확인 (비활성/탈퇴 시 403)
4. JWT AccessToken (단기) 생성
5. JWT RefreshToken (장기) 생성 및 저장
6. 토큰 쌍 응답 반환

---

### ✅ FM-003 로그아웃

| 기능 ID | **FM-003** |
| --- | --- |
| 기능명 | 로그아웃 |
| 설명 | JWT 토큰 무효화 및 클라이언트 토큰 삭제 처리한다. |
| 연관 UC | UC-M-003 (로그아웃) |
| HTTP 메서드 | POST |
| 요청 URL | `/signout` |
| 입력값 | Authorization 헤더 (AccessToken) |
| 출력값 | 로그아웃 완료 응답 |

**처리 흐름**

1. AccessToken 파싱 및 회원 식별
2. 서버 측 RefreshToken 무효화 처리
3. 클라이언트 토큰 삭제 안내
4. 완료 응답 반환

---

### ✅ FM-004 마이페이지 주문 현황 조회

| 기능 ID | **FM-004** |
| --- | --- |
| 기능명 | 마이페이지 주문 현황 조회 |
| 설명 | 주문한 상품의 단계별 현황(6단계) 카운트를 요약 조회한다. |
| 연관 UC | UC-M-004 (마이페이지 주문 현황 조회) |
| HTTP 메서드 | GET |
| 요청 URL | `/mypage` |
| 입력값 | Authorization 헤더 |
| 출력값 | 마이페이지 요약 DTO (주문 현황·리뷰·QnA 건수) |

**처리 흐름**

1. 회원 JWT 인증 확인
2. memberId 기준 주문 상태별 COUNT 집계 (입금대기~배송완료 6단계)
3. 최신 주문/리뷰/QnA 목록 함께 조회 (5~10건)
4. MyPageResponse DTO 반환

---

### ✅ FM-005 마이페이지 주문/배송 조회

| 기능 ID | **FM-005** |
| --- | --- |
| 기능명 | 마이페이지 주문/배송 조회 |
| 설명 | 마이페이지에서 주문·배송 상태 목록을 요약 조회한다. |
| 연관 UC | UC-M-005 (마이페이지 주문/배송 조회) |
| HTTP 메서드 | GET |
| 요청 URL | `/mypage` |
| 입력값 | Authorization 헤더, `page`, `size` |
| 출력값 | `Page<OrderSummaryResponse>` |

**처리 흐름**

1. 회원 JWT 인증 확인
2. memberId 기준 주문 최신순 조회
3. 배송 상태 JOIN
4. 최신 5~10건 페이징 반환

---

### ✅ FM-006 마이페이지 리뷰 조회

| 기능 ID | **FM-006** |
| --- | --- |
| 기능명 | 마이페이지 리뷰 조회 |
| 설명 | 마이페이지에서 작성한 리뷰 목록을 요약 조회한다. |
| 연관 UC | UC-M-006 (마이페이지 리뷰 조회) |
| HTTP 메서드 | GET |
| 요청 URL | `/mypage` |
| 입력값 | Authorization 헤더, `page`, `size` |
| 출력값 | `Page<ReviewSummaryResponse>` |

**처리 흐름**

1. 회원 JWT 인증 확인
2. memberId 기준 리뷰 최신순 조회
3. 최신 5~10건 페이징 반환

---

### ✅ FM-007 마이페이지 QnA 조회

| 기능 ID | **FM-007** |
| --- | --- |
| 기능명 | 마이페이지 QnA 조회 |
| 설명 | 마이페이지에서 작성한 QnA 목록 및 답변 여부를 조회한다. |
| 연관 UC | UC-M-007 (마이페이지 QnA 조회) |
| HTTP 메서드 | GET |
| 요청 URL | `/mypage` |
| 입력값 | Authorization 헤더, `page`, `size` |
| 출력값 | `Page<QnaSummaryResponse>` (답변 여부 포함) |

**처리 흐름**

1. 회원 JWT 인증 확인
2. memberId 기준 QnA 조회 (depth=0, 질문만)
3. qna_status 기반 답변 여부 표시
4. 최신순 페이징 반환

---

### ✅ FM-008 회원 정보 수정

| 기능 ID | **FM-008** |
| --- | --- |
| 기능명 | 회원 정보 수정 |
| 설명 | 이름·전화번호·주소를 수정한다. |
| 연관 UC | UC-M-008 (회원 정보 수정) |
| HTTP 메서드 | PATCH |
| 요청 URL | `/mypage/edit` |
| 입력값 | Authorization 헤더, `{ name, phone, address }` (Body) |
| 출력값 | 수정 완료 응답 |

**처리 흐름**

1. 회원 JWT 인증 확인
2. memberStatus = ACTIVATE 확인
3. 입력값 유효성 검사
4. name·phone·address 업데이트
5. updated_at 갱신 및 저장

---

### ✅ FM-009 비밀번호 변경

| 기능 ID | **FM-009** |
| --- | --- |
| 기능명 | 비밀번호 변경 |
| 설명 | 현재 비밀번호 확인 후 새 비밀번호로 변경한다. |
| 연관 UC | UC-M-009 (비밀번호 변경) |
| HTTP 메서드 | PATCH |
| 요청 URL | `/mypage/password` |
| 입력값 | Authorization 헤더, `{ currentPassword, newPassword }` (Body) |
| 출력값 | 변경 완료 응답 |

**처리 흐름**

1. 회원 JWT 인증 확인
2. 현재 비밀번호 BCrypt 검증 (불일치 시 400)
3. 새 비밀번호 유효성 검사
4. BCrypt 재암호화
5. 저장 후 응답 반환

---

### ✅ FM-010 회원 탈퇴

| 기능 ID | **FM-010** |
| --- | --- |
| 기능명 | 회원 탈퇴 |
| 설명 | 계정을 soft delete 처리하고 즉시 로그아웃한다. |
| 연관 UC | UC-M-010 (회원 탈퇴) |
| HTTP 메서드 | DELETE |
| 요청 URL | `/mypage` |
| 입력값 | Authorization 헤더, `{ password }` (Body) |
| 출력값 | 탈퇴 완료 응답 |

**처리 흐름**

1. 회원 JWT 인증 확인
2. 비밀번호 재확인 (보안 처리)
3. memberStatus = DELETE 처리
4. deleted_at 기록
5. RefreshToken 무효화 (즉시 로그아웃)
6. 완료 응답 반환

---

### ✅ FM-011 주문 목록 조회

| 기능 ID | **FM-011** |
| --- | --- |
| 기능명 | 주문 목록 조회 |
| 설명 | 본인의 전체 주문 목록을 최신순/상태별로 조회한다. |
| 연관 UC | UC-M-011 (주문 목록 조회) |
| HTTP 메서드 | GET |
| 요청 URL | `/mypage/order/list` |
| 입력값 | Authorization 헤더, `page`, `size`, `status` (Query Param) |
| 출력값 | `Page<OrderResponse>` |

**처리 흐름**

1. 회원 JWT 인증 확인
2. memberId 기준 주문 조회
3. 상태 필터(status) 적용
4. 최신순(created_at DESC) 정렬
5. 페이징 결과 반환

---

### ✅ FM-012 주문 상세 조회

| 기능 ID | **FM-012** |
| --- | --- |
| 기능명 | 주문 상세 조회 |
| 설명 | 주문 상품·결제 금액·배송지 정보를 상세 조회한다. |
| 연관 UC | UC-M-012 (주문 상세 조회) |
| HTTP 메서드 | GET |
| 요청 URL | `/mypage/order/{id}` |
| 입력값 | `orderId` (Path Variable) |
| 출력값 | `OrderDetailResponse` (주문 상품 목록 포함) |

**처리 흐름**

1. 회원 JWT 인증 확인
2. orderId로 주문 조회
3. 본인 주문 소유 확인 (타인 접근 시 403)
4. 주문 상품(order_item)·결제(payment)·배송지 JOIN
5. OrderDetailResponse 반환

---

### ✅ FM-013 주문 취소 요청

| 기능 ID | **FM-013** |
| --- | --- |
| 기능명 | 주문 취소 요청 |
| 설명 | 결제 전/후 취소 요청을 처리한다. |
| 연관 UC | UC-M-013 (주문 취소 요청) |
| HTTP 메서드 | PATCH |
| 요청 URL | `/mypage/order/{id}/cancel` |
| 입력값 | `orderId` (Path Variable) |
| 출력값 | 취소 완료 응답 |

**처리 흐름**

1. 회원 JWT 인증 확인
2. 본인 주문 소유 확인
3. orderStatus = PENDING 상태 확인 (취소 가능 여부)
4. orderStatus = CANCELED 처리
5. 결제 상태 연동 처리
6. 저장 후 응답 반환

---

### ✅ FM-014 배송 상태 조회

| 기능 ID | **FM-014** |
| --- | --- |
| 기능명 | 배송 상태 조회 |
| 설명 | 주문별 현재 배송 상태 단계를 확인한다. |
| 연관 UC | UC-M-014 (배송 상태 조회) |
| HTTP 메서드 | GET |
| 요청 URL | `/mypage/order/{id}` |
| 입력값 | `orderId` (Path Variable) |
| 출력값 | `DeliveryStatusResponse` (READY~DELIVERED 단계) |

**처리 흐름**

1. 회원 JWT 인증 확인
2. 본인 주문 소유 확인
3. 해당 주문의 배송 상태 조회
4. 단계별 DeliveryStatusResponse 반환

---

### ✅ FM-015 운송장 번호 조회

| 기능 ID | **FM-015** |
| --- | --- |
| 기능명 | 운송장 번호 조회 |
| 설명 | 택배사명 및 운송장 번호를 확인한다. |
| 연관 UC | UC-M-015 (운송장 번호 조회) |
| HTTP 메서드 | GET |
| 요청 URL | `/mypage/order/{id}` |
| 입력값 | `orderId` (Path Variable) |
| 출력값 | `DeliveryTrackingResponse` (택배사·운송장 번호) |

**처리 흐름**

1. 회원 JWT 인증 확인
2. 본인 주문 소유 확인
3. delivery_status SHIPPED 이상 확인
4. courier·tracking_number 반환

---

### ✅ FM-016 작성 리뷰 목록 조회

| 기능 ID | **FM-016** |
| --- | --- |
| 기능명 | 작성 리뷰 목록 조회 |
| 설명 | 본인이 작성한 리뷰 전체 목록을 최신순으로 조회한다. |
| 연관 UC | UC-M-016 (작성 리뷰 목록 조회) |
| HTTP 메서드 | GET |
| 요청 URL | `/mypage/review/list` |
| 입력값 | Authorization 헤더, `page`, `size` |
| 출력값 | `Page<ReviewResponse>` |

**처리 흐름**

1. 회원 JWT 인증 확인
2. memberId 기준 리뷰 최신순 조회
3. 페이징 결과 반환

---

### ✅ FM-017 작성 리뷰 상세 조회

| 기능 ID | **FM-017** |
| --- | --- |
| 기능명 | 작성 리뷰 상세 조회 |
| 설명 | 리뷰 내용·평점·이미지를 상세 확인한다. |
| 연관 UC | UC-M-017 (작성 리뷰 상세 조회) |
| HTTP 메서드 | GET |
| 요청 URL | `/mypage/review/{id}` |
| 입력값 | `reviewId` (Path Variable) |
| 출력값 | `ReviewDetailResponse` (작성 상품 링크 포함) |

**처리 흐름**

1. 회원 JWT 인증 확인
2. reviewId로 리뷰 조회
3. 본인 리뷰 소유 확인
4. ReviewDetailResponse 반환

---

### ✅ FM-018 리뷰 수정

| 기능 ID | **FM-018** |
| --- | --- |
| 기능명 | 리뷰 수정 |
| 설명 | 작성한 리뷰 내용 및 평점을 수정한다. |
| 연관 UC | UC-M-018 (리뷰 수정) |
| HTTP 메서드 | PATCH |
| 요청 URL | `/mypage/review/{id}` |
| 입력값 | `reviewId` (Path), `{ content, rating, images }` (Body) |
| 출력값 | 수정 완료 응답 |

**처리 흐름**

1. 회원 JWT 인증 확인
2. 본인 리뷰 소유 확인
3. 입력값 유효성 검사 (rating 1~5 범위)
4. 내용·평점 업데이트
5. updated_at 갱신 및 저장

---

### ✅ FM-019 리뷰 삭제

| 기능 ID | **FM-019** |
| --- | --- |
| 기능명 | 리뷰 삭제 |
| 설명 | 작성한 리뷰를 soft delete 방식으로 처리한다. |
| 연관 UC | UC-M-019 (리뷰 삭제) |
| HTTP 메서드 | DELETE |
| 요청 URL | `/mypage/review/{id}` |
| 입력값 | `reviewId` (Path Variable) |
| 출력값 | 삭제 완료 응답 |

**처리 흐름**

1. 회원 JWT 인증 확인
2. 본인 리뷰 소유 확인
3. deleted_at 기록
4. order_item의 is_reviewed = false 연동 처리
5. 저장 후 응답 반환

---

### ✅ FM-020 작성한 질문 목록 조회

| 기능 ID | **FM-020** |
| --- | --- |
| 기능명 | 작성한 질문 목록 조회 |
| 설명 | 본인이 작성한 QnA 질문 전체 목록을 최신순으로 조회한다. |
| 연관 UC | UC-M-020 (작성한 질문 목록 조회) |
| HTTP 메서드 | GET |
| 요청 URL | `/mypage/qna/list` |
| 입력값 | Authorization 헤더, `page`, `size` |
| 출력값 | `Page<QnaResponse>` |

**처리 흐름**

1. 회원 JWT 인증 확인
2. memberId 기준 QnA 조회 (depth=0, 질문만)
3. 최신순 정렬
4. 페이징 결과 반환

---

### ✅ FM-021 작성한 질문 상세 조회

| 기능 ID | **FM-021** |
| --- | --- |
| 기능명 | 작성한 질문 상세 조회 |
| 설명 | 질문 내용 및 답변 상태·내용을 상세 확인한다. |
| 연관 UC | UC-M-021 (작성한 질문 상세 조회) |
| HTTP 메서드 | GET |
| 요청 URL | `/mypage/qna/{id}` |
| 입력값 | `qnaId` (Path Variable) |
| 출력값 | `QnaDetailResponse` (답변 포함) |

**처리 흐름**

1. 회원 JWT 인증 확인
2. qnaId로 질문(depth=0) 조회
3. 본인 질문 소유 확인
4. 답변(depth=1) JOIN
5. QnaDetailResponse 반환

---

### ✅ FM-022 질문 수정

| 기능 ID | **FM-022** |
| --- | --- |
| 기능명 | 질문 수정 |
| 설명 | 작성한 질문 내용을 수정한다. 답변 전(WAITING 상태)에만 가능하다. |
| 연관 UC | UC-M-022 (질문 수정) |
| HTTP 메서드 | PATCH |
| 요청 URL | `/mypage/qna/{id}` |
| 입력값 | `qnaId` (Path), `{ content }` (Body) |
| 출력값 | 수정 완료 응답 |

**처리 흐름**

1. 회원 JWT 인증 확인
2. 본인 질문 소유 확인
3. qna_status = WAITING 상태 확인 (답변 완료 시 수정 불가)
4. content 업데이트
5. updated_at 갱신 및 저장

---

### ✅ FM-023 질문 삭제

| 기능 ID | **FM-023** |
| --- | --- |
| 기능명 | 질문 삭제 |
| 설명 | 작성한 질문을 soft delete 방식으로 처리한다. |
| 연관 UC | UC-M-023 (질문 삭제) |
| HTTP 메서드 | DELETE |
| 요청 URL | `/mypage/qna/{id}` |
| 입력값 | `qnaId` (Path Variable) |
| 출력값 | 삭제 완료 응답 |

**처리 흐름**

1. 회원 JWT 인증 확인
2. 본인 질문 소유 확인
3. 질문 deleted_at 기록
4. 연관 답변(depth=1) 연쇄 soft delete 처리
5. 저장 후 응답 반환
