package com.bookstore.shop.readme.controller.admin;

import com.bookstore.shop.readme.dto.request.CategoryCreateRequest;
import com.bookstore.shop.readme.dto.request.DeliveryUpdateRequest;
import com.bookstore.shop.readme.dto.request.ProductCreateRequest;
import com.bookstore.shop.readme.dto.request.ProductUpdateRequest;
import com.bookstore.shop.readme.dto.response.*;
import com.bookstore.shop.readme.security.CustomUserDetails;
import com.bookstore.shop.readme.service.AdminService;
import com.bookstore.shop.readme.service.OrderService;
import com.bookstore.shop.readme.service.PaymentService;
import com.bookstore.shop.readme.service.QnAService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

// [수정] Repository 직접 주입 전부 제거 →
//       AdminService / OrderService / PaymentService 위임으로 변경
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/admin")
@PreAuthorize("hasAnyRole('MANAGER', 'ADMIN')")
public class AdminApiController {

    private final AdminService   adminService;
    private final OrderService   orderService;
    private final PaymentService paymentService;
    private final QnAService     qnaService;

    // ── 대시보드 ────────────────────────────────────────────────────────────

    @GetMapping("/dashboard")
    public ResponseEntity<DashboardResponse> getDashboard() {
        return adminService.getDashboard();
    }

    // ── 회원 관리 ───────────────────────────────────────────────────────────

    @GetMapping("/members")
    public ResponseEntity<Page<MemberResponse>> getMembers(
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) String status,
            @PageableDefault(size = 20, sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable) {
        return adminService.getMembers(keyword, status, pageable);
    }

    @GetMapping("/members/{memberId}")
    public ResponseEntity<MemberResponse> getMember(@PathVariable Long memberId) {
        return adminService.getMember(memberId);
    }

    @PatchMapping("/members/{memberId}/status")
    public ResponseEntity<String> updateMemberStatus(
            @PathVariable Long memberId,
            @RequestParam String status) {
        return adminService.updateMemberStatus(memberId, status);
    }

    @PatchMapping("/members/{memberId}/role")
    public ResponseEntity<String> updateMemberRole(
            @PathVariable Long memberId,
            @RequestParam String role) {
        return adminService.updateMemberRole(memberId, role);
    }

    // ── 상품 관리 ───────────────────────────────────────────────────────────

    @GetMapping("/products")
    public ResponseEntity<Page<ProductAdminResponse>> getProducts(
            @RequestParam(required = false) String status,
            @PageableDefault(size = 20, sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable) {
        return adminService.getProducts(status, pageable);
    }

    @GetMapping("/products/{productId}")
    public ResponseEntity<ProductAdminResponse> getProduct(@PathVariable Long productId) {
        return adminService.getProduct(productId);
    }

    @PostMapping("/products")
    public ResponseEntity<Long> createProduct(@RequestBody ProductCreateRequest req) {
        return adminService.createProduct(req);
    }

    @PutMapping("/products/{productId}")
    public ResponseEntity<ProductAdminResponse> updateProduct(
            @PathVariable Long productId,
            @RequestBody ProductUpdateRequest req) {
        return adminService.updateProduct(productId, req);
    }

    @DeleteMapping("/products/{productId}")
    public ResponseEntity<String> deleteProduct(@PathVariable Long productId) {
        return adminService.deleteProduct(productId);
    }

    // ── 재고 관리 (/admin/product/stock) ─────────────────────────────────────

    @PatchMapping("/products/{productId}/stock")
    public ResponseEntity<String> updateStock(
            @PathVariable Long productId,
            @RequestParam int stock) {
        return adminService.updateStock(productId, stock);
    }

    // ── 주문 관리 ───────────────────────────────────────────────────────────

    @GetMapping("/orders")
    public ResponseEntity<Page<OrderListResponse>> getAllOrders(
            @PageableDefault(size = 20, sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable) {
        return orderService.getAllOrders(pageable);
    }

    @GetMapping("/orders/{orderId}")
    public ResponseEntity<OrderDetailResponse> getOrderDetail(@PathVariable Long orderId) {
        return orderService.getOrderDetail(orderId);
    }

    @PatchMapping("/orders/{orderId}/status")
    public ResponseEntity<String> updateOrderStatus(
            @PathVariable Long orderId,
            @RequestParam String status) {
        return orderService.updateOrderStatus(orderId, status);
    }

    // ── 주문 승인 처리 (/admin/order/approval) ────────────────────────────────

    @GetMapping("/orders/pending-approval")
    public ResponseEntity<Page<OrderListResponse>> getPendingApprovalOrders(
            @PageableDefault(size = 20, sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable) {
        return adminService.getOrdersByStatus("PAYED", pageable);
    }

    // ── 배송 관리 ───────────────────────────────────────────────────────────

    @GetMapping("/deliveries")
    public ResponseEntity<Page<DeliveryResponse>> getDeliveries(
            @RequestParam(required = false) String status,
            @PageableDefault(size = 20, sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable) {
        return adminService.getDeliveries(status, pageable);
    }

    @PatchMapping("/deliveries/{deliveryId}")
    public ResponseEntity<DeliveryResponse> updateDelivery(
            @PathVariable Long deliveryId,
            @RequestBody DeliveryUpdateRequest req) {
        return adminService.updateDelivery(deliveryId, req);
    }

    // ── 결제 내역 관리 (/admin/payment/list) ──────────────────────────────────

    @GetMapping("/payments")
    public ResponseEntity<Page<PaymentStatusResponse>> getPayments(
            @RequestParam(required = false) String status,
            @PageableDefault(size = 20, sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable) {
        return paymentService.getAllPayments(status, pageable);
    }

    // ── 카테고리 관리 (/admin/categories) ────────────────────────────────────

    @GetMapping("/categories")
    public ResponseEntity<List<CategoryResponse>> getAdminCategories() {
        return adminService.getAdminCategories();
    }

    // 대분류 등록 — FA-027
    @PostMapping("/categories/top")
    public ResponseEntity<Long> createCategoryTop(@RequestBody CategoryCreateRequest req) {
        return adminService.createCategoryTop(req);
    }

    // 소분류 등록 — FA-027
    @PostMapping("/categories/sub")
    public ResponseEntity<Long> createCategorySub(@RequestBody CategoryCreateRequest req) {
        return adminService.createCategorySub(req);
    }

    // 대분류 수정 — FA-028
    @PutMapping("/categories/top/{topId}")
    public ResponseEntity<String> updateCategoryTop(
            @PathVariable Long topId,
            @RequestBody CategoryCreateRequest req) {
        return adminService.updateCategoryTop(topId, req);
    }

    // 소분류 수정 — FA-028
    @PutMapping("/categories/sub/{subId}")
    public ResponseEntity<String> updateCategorySub(
            @PathVariable Long subId,
            @RequestBody CategoryCreateRequest req) {
        return adminService.updateCategorySub(subId, req);
    }

    // 대분류 비활성화 — FA-029
    @DeleteMapping("/categories/top/{topId}")
    public ResponseEntity<String> deleteCategoryTop(@PathVariable Long topId) {
        return adminService.deleteCategoryTop(topId);
    }

    // 소분류 비활성화 — FA-029
    @DeleteMapping("/categories/sub/{subId}")
    public ResponseEntity<String> deleteCategorySub(@PathVariable Long subId) {
        return adminService.deleteCategorySub(subId);
    }

    // ── 공지사항 관리 (/admin/notices) ───────────────────────────────────────

    @GetMapping("/notices")
    public ResponseEntity<Page<NoticeResponse>> getAdminNotices(
            @PageableDefault(size = 20, sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable) {
        return adminService.getAdminNotices(pageable);
    }

    // 공지사항 상세 조회 — FA-031
    @GetMapping("/notices/{noticeId}")
    public ResponseEntity<NoticeResponse> getAdminNoticeDetail(@PathVariable Long noticeId) {
        return adminService.getAdminNoticeDetail(noticeId);
    }

    // ── QnA 관리 (/admin/qnas) ───────────────────────────────────────────────

    // depth=0 질문 목록 (unansweredOnly=true → WAITING 상태만)
    @GetMapping("/qnas")
    public ResponseEntity<Page<QnAResponse>> getAdminQnAs(
            @RequestParam(required = false, defaultValue = "false") boolean unansweredOnly,
            @PageableDefault(size = 20, sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable) {
        return qnaService.getAllQnAs(unansweredOnly, pageable);
    }

    // QnA 상세 조회 — FA-039
    @GetMapping("/qnas/{qnaId}")
    public ResponseEntity<QnAResponse> getAdminQnADetail(@PathVariable Long qnaId) {
        return qnaService.getAdminQnADetail(qnaId);
    }

    // 답변 등록 — qnaId=질문 ID, 관리자 memberId로 자식 QnA(depth+1) 생성
    @PostMapping("/qnas/{qnaId}/answer")
    public ResponseEntity<QnAResponse> answerQnA(
            @PathVariable Long qnaId,
            @RequestBody java.util.Map<String, String> body,
            @AuthenticationPrincipal CustomUserDetails user) {
        return qnaService.answerQnA(qnaId, body.get("title"), body.get("content"), user.getMemberId());
    }

    // QnA 상태 변경 — FA-040 (WAITING / PROCESSING / COMPLETE)
    @PatchMapping("/qnas/{qnaId}/status")
    public ResponseEntity<String> updateQnAStatus(
            @PathVariable Long qnaId,
            @RequestParam String status) {
        return qnaService.updateQnAStatus(qnaId, status);
    }

    // 답변 수정 — FA-042 (answerQnaId = 답변 QnA 레코드의 ID)
    @PutMapping("/qnas/{answerQnaId}/answer")
    public ResponseEntity<QnAResponse> updateAdminAnswer(
            @PathVariable Long answerQnaId,
            @RequestBody java.util.Map<String, String> body) {
        return qnaService.updateAdminAnswer(answerQnaId, body.get("content"));
    }

    // 답변 삭제 — FA-043 (answerQnaId = 답변 QnA 레코드의 ID)
    @DeleteMapping("/qnas/{answerQnaId}/answer")
    public ResponseEntity<String> deleteAdminAnswer(@PathVariable Long answerQnaId) {
        return qnaService.deleteAdminAnswer(answerQnaId);
    }

    // ── 리뷰 관리 (/admin/reviews) ───────────────────────────────────────────

    @GetMapping("/reviews")
    public ResponseEntity<Page<ReviewResponse>> getAdminReviews(
            @PageableDefault(size = 20, sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable) {
        return adminService.getAdminReviews(pageable);
    }

    // 리뷰 상세 조회 — FA-036
    @GetMapping("/reviews/{reviewId}")
    public ResponseEntity<ReviewResponse> getAdminReviewDetail(@PathVariable Long reviewId) {
        return adminService.getAdminReviewDetail(reviewId);
    }

    @DeleteMapping("/reviews/{reviewId}")
    public ResponseEntity<String> deleteReview(@PathVariable Long reviewId) {
        return adminService.adminDeleteReview(reviewId);
    }
}
