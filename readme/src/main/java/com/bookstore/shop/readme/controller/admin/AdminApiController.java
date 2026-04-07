package com.bookstore.shop.readme.controller.admin;

import com.bookstore.shop.readme.dto.request.DeliveryUpdateRequest;
import com.bookstore.shop.readme.dto.request.ProductCreateRequest;
import com.bookstore.shop.readme.dto.request.ProductUpdateRequest;
import com.bookstore.shop.readme.dto.response.*;
import com.bookstore.shop.readme.repository.*;
import com.bookstore.shop.readme.service.AdminService;
import com.bookstore.shop.readme.service.OrderService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/admin")
@PreAuthorize("hasAnyRole('MANAGER', 'ADMIN')")
public class AdminApiController {

    private final AdminService          adminService;
    private final OrderService          orderService;
    private final PaymentRepository     paymentRepository;     // [신규] 결제 목록
    private final CategoryTopRepository categoryTopRepository; // [신규] 카테고리 관리
    private final CategorySubRepository categorySubRepository; // [신규] 카테고리 관리
    private final NoticeRepository      noticeRepository;      // [신규] 공지사항 관리
    private final QnARepository         qnaRepository;         // [신규] QnA 관리
    private final ReviewRepository      reviewRepository;      // [신규] 리뷰 관리

    // ── 대시보드 ────────────────────────────────────────────────────────────

    /** 대시보드 통계 (회원 수 / 상품 수 / 주문 현황) */
    @GetMapping("/dashboard")
    public ResponseEntity<DashboardResponse> getDashboard() {
        return adminService.getDashboard();
    }

    // ── 회원 관리 ───────────────────────────────────────────────────────────

    /** 회원 목록 (전체 / 상태별 / 키워드 검색) */
    @GetMapping("/members")
    public ResponseEntity<Page<MemberResponse>> getMembers(
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) String status,
            @PageableDefault(size = 20, sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable) {
        return adminService.getMembers(keyword, status, pageable);
    }

    /** 회원 상세 조회 */
    @GetMapping("/members/{memberId}")
    public ResponseEntity<MemberResponse> getMember(@PathVariable Long memberId) {
        return adminService.getMember(memberId);
    }

    /** 회원 상태 변경 (ACTIVATE / DEACTIVATE / DELETE) */
    @PatchMapping("/members/{memberId}/status")
    public ResponseEntity<String> updateMemberStatus(
            @PathVariable Long memberId,
            @RequestParam String status) {
        return adminService.updateMemberStatus(memberId, status);
    }

    /** 회원 등급 변경 (USER / MANAGER / ADMIN) */
    @PatchMapping("/members/{memberId}/role")
    public ResponseEntity<String> updateMemberRole(
            @PathVariable Long memberId,
            @RequestParam String role) {
        return adminService.updateMemberRole(memberId, role);
    }

    // ── 상품 관리 ───────────────────────────────────────────────────────────

    /** 상품 목록 (전체 / 상태별) */
    @GetMapping("/products")
    public ResponseEntity<Page<ProductAdminResponse>> getProducts(
            @RequestParam(required = false) String status,
            @PageableDefault(size = 20, sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable) {
        return adminService.getProducts(status, pageable);
    }

    /** 상품 상세 조회 */
    @GetMapping("/products/{productId}")
    public ResponseEntity<ProductAdminResponse> getProduct(@PathVariable Long productId) {
        return adminService.getProduct(productId);
    }

    /** 상품 등록 */
    @PostMapping("/products")
    public ResponseEntity<Long> createProduct(@RequestBody ProductCreateRequest req) {
        return adminService.createProduct(req);
    }

    /** 상품 수정 */
    @PutMapping("/products/{productId}")
    public ResponseEntity<ProductAdminResponse> updateProduct(
            @PathVariable Long productId,
            @RequestBody ProductUpdateRequest req) {
        return adminService.updateProduct(productId, req);
    }

    /** 상품 삭제 (soft delete) */
    @DeleteMapping("/products/{productId}")
    public ResponseEntity<String> deleteProduct(@PathVariable Long productId) {
        return adminService.deleteProduct(productId);
    }

    // ── 주문 관리 ───────────────────────────────────────────────────────────

    /** 전체 주문 목록 */
    @GetMapping("/orders")
    public ResponseEntity<Page<OrderListResponse>> getAllOrders(
            @PageableDefault(size = 20, sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable) {
        return orderService.getAllOrders(pageable);
    }

    /** 주문 상세 */
    @GetMapping("/orders/{orderId}")
    public ResponseEntity<OrderDetailResponse> getOrderDetail(@PathVariable Long orderId) {
        return orderService.getOrderDetail(orderId);
    }

    /** 주문 상태 변경 (PENDING / PAYED / APPROVAL / CANCELED) */
    @PatchMapping("/orders/{orderId}/status")
    public ResponseEntity<String> updateOrderStatus(
            @PathVariable Long orderId,
            @RequestParam String status) {
        return orderService.updateOrderStatus(orderId, status);
    }

    // ── 배송 관리 ───────────────────────────────────────────────────────────

    /** 배송 목록 (전체 / 상태별) */
    @GetMapping("/deliveries")
    public ResponseEntity<Page<DeliveryResponse>> getDeliveries(
            @RequestParam(required = false) String status,
            @PageableDefault(size = 20, sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable) {
        return adminService.getDeliveries(status, pageable);
    }

    /** 배송 상태 및 운송장 번호 수정 */
    @PatchMapping("/deliveries/{deliveryId}")
    public ResponseEntity<DeliveryResponse> updateDelivery(
            @PathVariable Long deliveryId,
            @RequestBody DeliveryUpdateRequest req) {
        return adminService.updateDelivery(deliveryId, req);
    }

    // ── 결제 내역 관리 (/admin/payment/list) ──────────────────────────────────

    /** [신규] 전체 결제 목록 (상태 필터 선택, 최신순 페이징) */
    @GetMapping("/payments")
    public ResponseEntity<Page<PaymentStatusResponse>> getPayments(
            @RequestParam(required = false) String status,
            @PageableDefault(size = 20, sort = "createdAt",
                    direction = Sort.Direction.DESC) Pageable pageable) {
        Page<PaymentStatusResponse> result;
        if (status != null && !status.isBlank()) {
            result = paymentRepository
                    .findAllByPaymentStatus(
                            com.bookstore.shop.readme.domain.PaymentStatus.valueOf(status), pageable)
                    .map(PaymentStatusResponse::new);
        } else {
            result = paymentRepository.findAll(pageable).map(PaymentStatusResponse::new);
        }
        return ResponseEntity.ok(result);
    }

    // ── 재고 관리 (/admin/product/stock) ─────────────────────────────────────

    /** [신규] 개별 상품 재고 수량 변경 */
    @PatchMapping("/products/{productId}/stock")
    public ResponseEntity<String> updateStock(
            @PathVariable Long productId,
            @RequestParam int stock) {
        return adminService.updateStock(productId, stock);
    }

    // ── 주문 승인 처리 (/admin/order/approval) ────────────────────────────────

    /** [신규] 결제완료(PAYED) 주문 목록 조회 — 승인 처리 페이지용
     *  실제 상태 변경은 기존 PATCH /api/admin/orders/{id}/status 사용 */
    @GetMapping("/orders/pending-approval")
    public ResponseEntity<Page<OrderListResponse>> getPendingApprovalOrders(
            @PageableDefault(size = 20, sort = "createdAt",
                    direction = Sort.Direction.DESC) Pageable pageable) {
        return adminService.getOrdersByStatus("PAYED", pageable);
    }

    // ── 카테고리 관리 (/admin/category/list) ─────────────────────────────────

    /** [신규] 대분류 카테고리 목록 (소분류 포함) */
    @GetMapping("/categories")
    public ResponseEntity<java.util.List<CategoryResponse>> getAdminCategories() {
        java.util.List<CategoryResponse> result =
                categoryTopRepository.findAll().stream()
                        .map(top -> new CategoryResponse(top,
                                categorySubRepository.findByCategoryTopIdOrderBySortOrderAsc(top.getId())))
                        .collect(java.util.stream.Collectors.toList());
        return ResponseEntity.ok(result);
    }

    // ── 공지사항 관리 (/admin/notice/list) ───────────────────────────────────

    /** [신규] 전체 공지사항 목록 (삭제 포함) */
    @GetMapping("/notices")
    public ResponseEntity<Page<NoticeResponse>> getAdminNotices(
            @PageableDefault(size = 20, sort = "createdAt",
                    direction = Sort.Direction.DESC) Pageable pageable) {
        return ResponseEntity.ok(
                noticeRepository.findAll(pageable).map(NoticeResponse::new));
    }

    // ── QnA 관리 (/admin/qna/list) ───────────────────────────────────────────

    /** [신규] 전체 QnA 목록 (미답변 필터 옵션) */
    @GetMapping("/qnas")
    public ResponseEntity<Page<QnAResponse>> getAdminQnAs(
            @RequestParam(required = false, defaultValue = "false") boolean unansweredOnly,
            @PageableDefault(size = 20, sort = "createdAt",
                    direction = Sort.Direction.DESC) Pageable pageable) {
        Page<QnAResponse> result = unansweredOnly
                ? qnaRepository.findAllByAnswerIsNullAndDeletedAtIsNull(pageable).map(QnAResponse::new)
                : qnaRepository.findAllByDeletedAtIsNull(pageable).map(QnAResponse::new);
        return ResponseEntity.ok(result);
    }

    // ── 리뷰 관리 (/admin/review/list) ───────────────────────────────────────

    /** [신규] 전체 리뷰 목록 */
    @GetMapping("/reviews")
    public ResponseEntity<Page<ReviewResponse>> getAdminReviews(
            @PageableDefault(size = 20, sort = "createdAt",
                    direction = Sort.Direction.DESC) Pageable pageable) {
        return ResponseEntity.ok(
                reviewRepository.findAllByDeletedAtIsNull(pageable).map(ReviewResponse::new));
    }

    /** [신규] 리뷰 강제 삭제 (soft delete) */
    @DeleteMapping("/reviews/{reviewId}")
    public ResponseEntity<String> deleteReview(@PathVariable Long reviewId) {
        com.bookstore.shop.readme.domain.Review review = reviewRepository.findById(reviewId)
                .orElseThrow(() -> new RuntimeException("리뷰를 찾을 수 없습니다."));
        review.setDeletedAt(java.time.LocalDateTime.now());
        reviewRepository.save(review);
        return ResponseEntity.ok("리뷰가 삭제되었습니다.");
    }
}
