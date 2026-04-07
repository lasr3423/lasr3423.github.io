package com.bookstore.shop.readme.controller.admin;

import com.bookstore.shop.readme.dto.request.DeliveryUpdateRequest;
import com.bookstore.shop.readme.dto.request.ProductCreateRequest;
import com.bookstore.shop.readme.dto.request.ProductUpdateRequest;
import com.bookstore.shop.readme.dto.response.*;
import com.bookstore.shop.readme.service.AdminService;
import com.bookstore.shop.readme.service.OrderService;
import com.bookstore.shop.readme.service.PaymentService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
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

    // ── 카테고리 관리 (/admin/category/list) ─────────────────────────────────

    @GetMapping("/categories")
    public ResponseEntity<List<CategoryResponse>> getAdminCategories() {
        return adminService.getAdminCategories();
    }

    // ── 공지사항 관리 (/admin/notice/list) ───────────────────────────────────

    @GetMapping("/notices")
    public ResponseEntity<Page<NoticeResponse>> getAdminNotices(
            @PageableDefault(size = 20, sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable) {
        return adminService.getAdminNotices(pageable);
    }

    // ── QnA 관리 (/admin/qna/list) ───────────────────────────────────────────

    @GetMapping("/qnas")
    public ResponseEntity<Page<QnAResponse>> getAdminQnAs(
            @RequestParam(required = false, defaultValue = "false") boolean unansweredOnly,
            @PageableDefault(size = 20, sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable) {
        return adminService.getAdminQnAs(unansweredOnly, pageable);
    }

    // ── 리뷰 관리 (/admin/review/list) ───────────────────────────────────────

    @GetMapping("/reviews")
    public ResponseEntity<Page<ReviewResponse>> getAdminReviews(
            @PageableDefault(size = 20, sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable) {
        return adminService.getAdminReviews(pageable);
    }

    @DeleteMapping("/reviews/{reviewId}")
    public ResponseEntity<String> deleteReview(@PathVariable Long reviewId) {
        return adminService.adminDeleteReview(reviewId);
    }
}
