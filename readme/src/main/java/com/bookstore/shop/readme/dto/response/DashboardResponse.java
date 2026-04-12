package com.bookstore.shop.readme.dto.response;

// 관리자 대시보드 집계 응답 — REQ-A-001~007
public record DashboardResponse(
        // 회원 현황
        long totalMembers,           // 전체 회원 수
        long activeMembers,          // 활성 회원 수
        long todayNewMembers,        // REQ-A-007: 오늘 신규 가입 수
        long monthNewMembers,        // REQ-A-007: 이번 달 신규 가입 수

        // 상품 현황
        long totalProducts,          // 전체 상품 수
        long lowStockProducts,       // REQ-A-003: 재고 10개 이하 상품 수

        // 주문/매출 현황
        long totalOrders,            // 전체 주문 수
        long pendingOrders,          // PAYMENT_PENDING 상태 (결제 진행 중 주문)
        long payedOrders,            // 실결제 완료 + 승인 대기 주문 수 (PENDING/PAYED 호환)
        long todayOrders,            // REQ-A-006: 오늘 주문 수
        long todaySales,             // REQ-A-006: 오늘 매출 합계
        long monthOrders,            // REQ-A-006: 이번 달 주문 수
        long monthSales,             // REQ-A-006: 이번 달 매출 합계

        // QnA 현황
        long unansweredQnaCount,     // REQ-A-002: 미답변 QnA 수

        // 배송 현황
        long readyDeliveryCount      // REQ-A-005: 배송 준비(READY) 건수
) {}
