
---

## 📋 데이터베이스 엔티티 상세 설계서

### 1. notice (공지사항)
| 테이블명 | 컬럼명 | 자료형 | PK | FK | NULL | 기본값 | 설명 |
| --- | --- | --- | --- | --- | --- | --- | --- |
| notice | id | BIGSERIAL | ✅ |  | ❌ | | 공지사항 고유 식별자 |
| notice | member_id | BIGINT |  | ✅ | ❌ | | 작성 관리자 ID |
| notice | title | VARCHAR(255) |  |  | ❌ | | 공지사항 제목 |
| notice | content | TEXT |  |  | ❌ | | 공지사항 본문 |
| notice | is_fixed | BOOLEAN |  |  | ❌ | false | 상단 고정 여부 |
| notice | view_count | INTEGER |  |  | ❌ | 0 | 조회수 |
| notice | created_at | TIMESTAMP |  |  | ❌ | now() | 등록일 |
| notice | updated_at | TIMESTAMP |  |  | ✅ | NULL | 수정일 |
| notice | deleted_at | TIMESTAMP |  |  | ✅ | NULL | 삭제일 (소프트 삭제용) |

---

### 2. qna (1:1 문의)

| 테이블명 | 컬럼명 | 자료형 | PK | FK | NULL | 기본값 | 설명 |
| --- | --- | --- | --- | --- | --- | --- | --- |
| qna | id | BIGSERIAL | ✅ | | ❌ | | 고유 식별자 |
| qna | parent_id | BIGINT | | | ✅ | | 부모 ID (0 또는 NULL이면 질문) |
| qna | depth | INT | | | ❌ | 0 | 0: 질문, 1: 답변, 2: 재문의 등 4까지 규정 |
| qna | member_id | BIGINT | | ✅ | ❌ | | 작성자 ID (members 참조) |
| qna | category | VARCHAR(50) | | | ❌ | | 문의 유형 (배송, 환불 등) |
| qna | title | VARCHAR(255) | | | ❌ | | 제목 (답변 시 'RE:' 등 자동생성 가능) |
| qna | content | TEXT | | | ❌ | | 내용 |
| qna | view_count | INTEGER | | | ❌ | 0 | 조회수 (추가) |
| qna | qna_status | enum | | | ❌ | 'WAITING' | WAITING, PROCESSING, COMPLETE |
| qna | is_secret | BOOLEAN | | | ❌ | false | 비밀글 여부 |
| qna | answered_at | TIMESTAMP | | | ✅ | | 답변 완료 시각 |
| qna | created_at | TIMESTAMP | | | ❌ | now() | 등록 일시 |
| qna | deleted_at | TIMESTAMP | | | ✅ | | 삭제 일시 (Soft Delete) |

---

#### 3. review (상품 리뷰)
| 테이블명 | 컬럼명 | 자료형 | PK | FK | NULL | 기본값 | 설명 |
| --- | --- | --- | --- | --- | --- | --- | --- |
| review | id | BIGSERIAL | ✅ |  | ❌ | | 리뷰 고유 식별자 |
| review | member_id | BIGINT |  | ✅ | ❌ | | 작성자 ID (members.id 참조) |
| review | product_id | BIGINT |  | ✅ | ❌ | | 상품 ID (products.id 참조) |
| review | rating | INTEGER |  |  | ❌ | | 평점 (1~5) |
| review | content | TEXT |  |  | ❌ | | 리뷰 상세 내용 |
| review | hits | INTEGER |  |  | ❌ | 0 | 조회수 |
| review | created_at | TIMESTAMP |  |  | ❌ | now() | 작성일 |
| review | updated_at | TIMESTAMP |  |  | ✅ | | 수정일 |
| review | deleted_at | TIMESTAMP |  |  | ✅ | NULL | 삭제일 |

---

### 4. review_image (리뷰 이미지)
| 테이블명 | 컬럼명 | 자료형 | PK | FK | NULL | 기본값 | 설명 |
| --- | --- | --- | --- | --- | --- | --- | --- |
| review_image | id | BIGSERIAL | ✅ |  | ❌ | | 이미지 고유 식별자 |
| review_image | review_id | BIGINT |  | ✅ | ❌ | | 해당 리뷰 ID (reviews.id 참조) |
| review_image | image_url | VARCHAR(512) |  |  | ❌ | | 이미지 경로/URL |
| review_image | created_at | TIMESTAMP |  |  | ❌ | now() | 등록일 |

---

#### 5. review_reaction (리뷰 반응)
| 테이블명 | 컬럼명 | 자료형 | PK | FK | NULL | 기본값 | 설명 |
| --- | --- | --- | --- | --- | --- | --- | --- |
| review_reaction | id | BIGSERIAL | ✅ |  | ❌ | | 반응 고유 식별자 |
| review_reaction | review_id | BIGINT |  | ✅ | ❌ | | 해당 리뷰 ID (reviews.id 참조) |
| review_reaction | member_id | BIGINT |  | ✅ | ❌ | | 반응한 회원 ID (members.id 참조) |
| review_reaction | reaction_type | VARCHAR(10) |  |  | ❌ | | LIKE (좋아요) / DISLIKE (싫어요) |
| review_reaction | created_at | TIMESTAMP |  |  | ❌ | now() | 반응 일시 |

---