# 상품 API 테스트

**이 문서는 현재 ReadMe 프로젝트의 상품/카테고리 조회 기능 시연을 위해 핵심 상품 API만 추려서 테스트할 수 있도록 정리한 Markdown 문서입니다.**

## ✅ API 테스트 항목

| 항목 | 설명 |
| --- | --- |
| API 이름 | 기능 또는 엔드포인트 이름 |
| Method | HTTP 요청 메서드 (GET) |
| URL | 요청 URL 경로 |
| 인증 | Authorization 헤더 필요 여부 |
| 요청 파라미터 | Query, Path 값 |
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
| 인증 필요 여부 | 공개 조회 API라 대부분 불필요 |

## 📝 상품(Product) / 카테고리(Category) 관련 API 테스트

| API 이름 | Method | URL | 인증 | 요청 파라미터 | 요청 예시 | 응답 코드 | 응답 예시 | 테스트 목적 | 테스트 결과 | 담당자 |
| --- | --- | --- | --- | --- | --- | --- | --- | --- | --- | --- |
| 상위 카테고리 전체 조회 | GET | `/api/category/top` | 불필요 | 없음 | `/api/category/top` | `200` | `[{"id":1,"name":"국내도서","subCategories":[...]}]` | 메인/사이드바 카테고리 렌더링 확인 | 정상 응답 확인 완료 | Codex |
| 특정 상위 카테고리의 하위 카테고리 조회 | GET | `/api/category/{topId}/sub` | 불필요 | Path: `topId` | `/api/category/1/sub` | `200` | `[{"id":1,"name":"한국소설"},{"id":2,"name":"에세이"}]` | 카테고리 선택 시 하위 카테고리 동적 조회 확인 | 엔드포인트 존재 확인 | Codex |
| 상품 목록 조회 | GET | `/api/product?page=0&size=12` | 불필요 | Query: `page`, `size` | `/api/product?page=0&size=12` | `200` | `{ "content": [{ "id": 2, "title": "중고생이 꼭 읽어야 할 한국단편소설 50", "salePrice": 16920 }] }` | 상품 리스트 페이지 기본 조회 확인 | 정상 응답 확인 완료 | Codex |
| 상품 검색 | GET | `/api/product?keyword=한국&page=0&size=12` | 불필요 | Query: `keyword`, `page`, `size` | `/api/product?keyword=한국&page=0&size=12` | `200` | `{ "content": [{ "id": 2, "title": "중고생이 꼭 읽어야 할 한국단편소설 50" }] }` | 검색어 기반 상품 조회 확인 | 엔드포인트 존재 확인 | Codex |
| 상위 카테고리 필터 상품 조회 | GET | `/api/product?categoryTopId=1&page=0&size=12` | 불필요 | Query: `categoryTopId`, `page`, `size` | `/api/product?categoryTopId=1&page=0&size=12` | `200` | `{ "content": [{ "id": 2, "categoryTopId": 1, "categoryTopName": "국내도서" }] }` | 상위 카테고리 필터링 확인 | 엔드포인트 존재 확인 | Codex |
| 하위 카테고리 필터 상품 조회 | GET | `/api/product?categoryTopId=1&categorySubId=1&page=0&size=12` | 불필요 | Query: `categoryTopId`, `categorySubId`, `page`, `size` | `/api/product?categoryTopId=1&categorySubId=1&page=0&size=12` | `200` | `{ "content": [{ "id": 2, "categorySubId": 1, "categorySubName": "한국소설" }] }` | 하위 카테고리까지 포함한 필터링 확인 | 엔드포인트 존재 확인 | Codex |
| 상품 상세 조회 | GET | `/api/product/detail/{productId}` | 불필요 | Path: `productId` | `/api/product/detail/2` | `200` | `{ "id": 2, "title": "중고생이 꼭 읽어야 할 한국단편소설 50", "author": "김동인", "description": "...", "images": [...] }` | 상품 상세 페이지 데이터 조회 확인 | 엔드포인트 존재 확인 | Codex |

## ✅ 상품 API 테스트용 cURL 예시

```bash
# 1. 상위 카테고리 조회
curl http://localhost:8202/api/category/top
```

```bash
# 2. 상품 목록 조회
curl "http://localhost:8202/api/product?page=0&size=12"
```

```bash
# 3. 상품 검색
curl "http://localhost:8202/api/product?keyword=한국&page=0&size=12"
```

```bash
# 4. 카테고리 필터 상품 조회
curl "http://localhost:8202/api/product?categoryTopId=1&categorySubId=1&page=0&size=12"
```

```bash
# 5. 상품 상세 조회
curl http://localhost:8202/api/product/detail/2
```

## ✅ 발표용 빠른 체크 포인트

| 순서 | 확인 항목 | 기대 결과 |
| --- | --- | --- |
| 1 | 메인/사이드바 카테고리 표시 | 상위/하위 카테고리 노출 |
| 2 | 상품 목록 진입 | 상품 카드 목록 표시 |
| 3 | 검색어 입력 | 검색 결과 갱신 |
| 4 | 카테고리 필터 선택 | 조건에 맞는 상품만 표시 |
| 5 | 상품 상세 진입 | 제목, 가격, 설명, 이미지 표시 |
