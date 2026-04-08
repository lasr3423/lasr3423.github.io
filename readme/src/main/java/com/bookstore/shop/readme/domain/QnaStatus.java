package com.bookstore.shop.readme.domain;

// QnA 답변 상태 ENUM — DB설계서 ENUM 타입 정의 기준
// WAITING   : 답변 대기 (초기 상태)
// PROCESSING: 처리 중 (관리자 확인 완료)
// COMPLETE  : 답변 완료
public enum QnaStatus {
    WAITING,
    PROCESSING,
    COMPLETE
}
