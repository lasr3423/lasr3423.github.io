# ISBN 조회 가이드 (카카오 도서 API 기준)

## 1. 문서 목적

- 현재 프로젝트의 ISBN 조회 기능이 어떤 외부 API를 사용하는지 실제 코드 기준으로 정리한다.
- 관리자 상품 등록 화면에서 ISBN 조회가 어떤 백엔드 API를 통해 동작하는지 설명한다.
- 더 이상 사용하지 않는 국립중앙도서관 ISBN Open API 문서와 구분하기 위해 최신 기준을 명확히 남긴다.

## 2. 현재 기준 결론

- 이 프로젝트는 현재 **국립중앙도서관 ISBN Open API를 사용하지 않는다.**
- 실제 구현은 **카카오 도서 검색 API**를 사용한다.
- ISBN 조회는 백엔드에서 카카오 API를 호출한 뒤, 필요한 값만 정리해서 프론트에 반환하는 방식이다.

## 3. 실제 프로젝트 연동 구조

### 3.1 프론트 → 백엔드

- 관리자 화면에서 ISBN 조회 요청
- 호출 엔드포인트

```http
GET /api/admin/products/isbn?isbn={isbn}
```

### 3.2 백엔드 → 카카오 API

- 백엔드 서비스: `KakaoBookSearchService`
- 내부 호출 대상: `https://dapi.kakao.com/v3/search/book`
- 조회 방식: `target=isbn`

### 3.3 응답 흐름

1. 프론트가 ISBN으로 관리자 API 호출
2. 백엔드가 ISBN 문자열을 정규화
3. 카카오 도서 API에 ISBN 검색 요청
4. 카카오 응답에서 제목, 저자, 출판사, 썸네일을 추출
5. 프로젝트 DTO 형식으로 프론트에 반환

## 4. 실제 코드 기준 관련 위치

### 4.1 관리자 API

- 파일: `readme/src/main/java/com/bookstore/shop/readme/controller/admin/AdminApiController.java`
- 메서드: `GET /api/admin/products/isbn`

### 4.2 서비스 로직

- 파일: `readme/src/main/java/com/bookstore/shop/readme/service/KakaoBookSearchService.java`
- 주요 메서드
- `lookupByIsbn(String rawIsbn)`
- `searchByTitle(String query, int page, int size)`

### 4.3 관리자 서비스 연결

- 파일: `readme/src/main/java/com/bookstore/shop/readme/service/AdminService.java`
- 메서드: `lookupBookByIsbn(String isbn)`

## 5. 카카오 API 요청 방식

### 5.1 기본 URL

```text
https://dapi.kakao.com/v3/search/book
```

### 5.2 헤더

```http
Authorization: KakaoAK {REST_API_KEY}
```

### 5.3 ISBN 조회 요청 예시

```http
GET https://dapi.kakao.com/v3/search/book?query=9791162243077&target=isbn&page=1&size=10
Authorization: KakaoAK {REST_API_KEY}
```

### 5.4 제목 검색 요청 예시

```http
GET https://dapi.kakao.com/v3/search/book?query=스프링&target=title&page=1&size=50
Authorization: KakaoAK {REST_API_KEY}
```

## 6. 프로젝트 환경 변수

현재 프로젝트는 아래 설정을 사용한다.

```env
KAKAO_BOOK_API_BASE_URL=https://dapi.kakao.com/v3/search/book
KAKAO_BOOK_REST_API_KEY=...
```

`application.yml` 기준 설정 키:

```yaml
kakao:
  book:
    base-url: ${KAKAO_BOOK_API_BASE_URL:https://dapi.kakao.com/v3/search/book}
    rest-api-key: ${KAKAO_BOOK_REST_API_KEY:${KAKAO_CLIENT_ID:}}
```

## 7. 프로젝트 응답 형식

관리자 ISBN 조회 API는 아래 DTO 형태로 반환한다.

```json
{
  "isbn": "9791162243077",
  "title": "도서 제목",
  "author": "저자명",
  "publisher": "출판사명",
  "thumbnail": "https://..."
}
```

프로젝트 내부 DTO:

- `IsbnBookLookupResponse`

## 8. ISBN 정규화 규칙

- 입력값에서 숫자와 `X/x` 이외 문자는 제거한다.
- 카카오 응답의 `isbn` 값이 공백으로 여러 개 들어오면 우선 13자리 ISBN을 선택한다.
- 13자리가 없으면 첫 번째 유효값을 사용한다.

예시:

```text
입력: 979-11-6224-307-7
정규화: 9791162243077
```

## 9. 예외 처리 규칙

### 9.1 입력값이 비어 있을 때

```text
ISBN을 입력해 주세요.
```

### 9.2 카카오 API 키가 없을 때

```text
카카오 도서 검색 REST API 키가 설정되지 않았습니다.
```

### 9.3 검색 결과가 없을 때

```text
해당 ISBN으로 도서를 찾지 못했습니다.
```

### 9.4 응답 본문이 비어 있을 때

```text
카카오 도서 검색 응답이 비어 있습니다.
```

## 10. 더미 데이터 생성과의 관계

- 카카오 도서 API는 관리자 ISBN 조회뿐 아니라 더미 상품 생성에도 사용되고 있다.
- 현재 프로젝트에는 카카오 응답을 기반으로 상품을 자동 생성하는 시드/보충 로직이 존재한다.
- 따라서 이 문서는 단순 ISBN 조회 문서가 아니라, 실제 상품 메타데이터 수집 기준 문서로도 사용 가능하다.

관련 파일:

- `readme/src/main/java/com/bookstore/shop/readme/config/TestCommerceDataInitializer.java`
- `readme/src/main/java/com/bookstore/shop/readme/config/KakaoProductTopUpRunner.java`

## 11. 기존 문서와의 차이

- 기존 `ISBN_open_api_guide.md` 내용은 국립중앙도서관 Open API 설명 문서였다.
- 현재 프로젝트는 그 문서의 요청 URL, 파라미터, 인증 방식, 응답 형식을 사용하지 않는다.
- 따라서 기존 문서는 **참고용 과거 자료**로만 의미가 있고, 현재 구현 기준 문서로는 부적합하다.

## 12. 최종 정리

- 현재 ISBN 조회 기준 API는 **카카오 도서 검색 API**다.
- 관리자 상품 등록 ISBN 조회는 **`GET /api/admin/products/isbn`** 으로 처리한다.
- 백엔드가 카카오 API를 호출해 ISBN, 제목, 저자, 출판사, 썸네일만 정리해서 반환한다.
- 앞으로 ISBN 관련 산출물은 이 문서를 기준으로 유지한다.
