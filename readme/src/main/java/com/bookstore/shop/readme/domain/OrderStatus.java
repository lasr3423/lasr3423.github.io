package com.bookstore.shop.readme.domain;

public enum OrderStatus {
    PAYMENT_PENDING, // 주문 생성됨, 결제 진행 중 (결제 미완료 — 30분 초과 시 자동 취소)
    PENDING,         // 결제 완료, 관리자 승인 대기
    PAYED,           // (레거시 — 이전 코드 하위 호환용, 신규 코드는 PENDING 사용)
    APPROVAL,        // 관리자 승인 완료, 배송 준비 중
    DELIVERING,      // 배송 중
    DELIVERED,       // 배송 완료
    CANCELED         // 취소 (결제 시간 초과 자동 취소 포함)
}
