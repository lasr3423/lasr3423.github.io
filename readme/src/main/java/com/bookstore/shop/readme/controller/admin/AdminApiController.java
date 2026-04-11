package com.bookstore.shop.readme.controller.admin;

import com.bookstore.shop.readme.domain.OrderStatus;
import com.bookstore.shop.readme.dto.request.AdminQnAAnswerRequest;
import com.bookstore.shop.readme.dto.request.AdminOrderBulkStatusRequest;
import com.bookstore.shop.readme.dto.request.CategoryCreateRequest;
import com.bookstore.shop.readme.dto.request.DeliveryUpdateRequest;
import com.bookstore.shop.readme.dto.request.NoticeCreateRequest;
import com.bookstore.shop.readme.dto.request.ProductCreateRequest;
import com.bookstore.shop.readme.dto.request.ProductUpdateRequest;
import com.bookstore.shop.readme.dto.response.*;
import com.bookstore.shop.readme.security.CustomUserDetails;
import com.bookstore.shop.readme.service.AdminService;
import com.bookstore.shop.readme.service.NoticeService;
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

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/admin")
@PreAuthorize("hasAnyRole('MANAGER', 'ADMIN')")
public class AdminApiController {

    private final AdminService adminService;
    private final NoticeService noticeService;
    private final OrderService orderService;
    private final PaymentService paymentService;
    private final QnAService qnaService;

    @GetMapping("/dashboard")
    public ResponseEntity<DashboardResponse> getDashboard() {
        return adminService.getDashboard();
    }

    @GetMapping("/members")
    public ResponseEntity<Page<MemberResponse>> getMembers(
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) String status,
            @PageableDefault(size = 20, sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable
    ) {
        return adminService.getMembers(keyword, status, pageable);
    }

    @GetMapping("/members/{memberId}")
    public ResponseEntity<MemberResponse> getMember(@PathVariable Long memberId) {
        return adminService.getMember(memberId);
    }

    @PatchMapping("/members/{memberId}/status")
    public ResponseEntity<String> updateMemberStatus(@PathVariable Long memberId, @RequestParam String status) {
        return adminService.updateMemberStatus(memberId, status);
    }

    @PatchMapping("/members/{memberId}/role")
    public ResponseEntity<String> updateMemberRole(@PathVariable Long memberId, @RequestParam String role) {
        return adminService.updateMemberRole(memberId, role);
    }

    @GetMapping("/products")
    public ResponseEntity<Page<ProductAdminResponse>> getProducts(
            @RequestParam(required = false) String status,
            @PageableDefault(size = 20, sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable
    ) {
        return adminService.getProducts(status, pageable);
    }

    @GetMapping("/products/{productId}")
    public ResponseEntity<ProductAdminResponse> getProduct(@PathVariable Long productId) {
        return adminService.getProduct(productId);
    }

    @GetMapping("/products/isbn")
    public ResponseEntity<IsbnBookLookupResponse> lookupBookByIsbn(@RequestParam String isbn) {
        return adminService.lookupBookByIsbn(isbn);
    }

    @PostMapping("/products")
    public ResponseEntity<Long> createProduct(@RequestBody ProductCreateRequest req) {
        return adminService.createProduct(req);
    }

    @PutMapping("/products/{productId}")
    public ResponseEntity<ProductAdminResponse> updateProduct(
            @PathVariable Long productId,
            @RequestBody ProductUpdateRequest req
    ) {
        return adminService.updateProduct(productId, req);
    }

    @DeleteMapping("/products/{productId}")
    public ResponseEntity<String> deleteProduct(@PathVariable Long productId) {
        return adminService.deleteProduct(productId);
    }

    @PatchMapping("/products/{productId}/stock")
    public ResponseEntity<String> updateStock(@PathVariable Long productId, @RequestParam int stock) {
        return adminService.updateStock(productId, stock);
    }

    @PatchMapping("/products/{productId}/status")
    public ResponseEntity<String> updateProductStatus(@PathVariable Long productId, @RequestParam String status) {
        return adminService.updateProductStatus(productId, status);
    }

    @GetMapping("/orders")
    public ResponseEntity<Page<OrderListResponse>> getAllOrders(
            @RequestParam(required = false) String status,
            @PageableDefault(size = 20, sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable
    ) {
        if (status != null && !status.isBlank()) {
            return orderService.getOrdersByStatus(OrderStatus.valueOf(status), pageable);
        }
        return orderService.getAllOrders(pageable);
    }

    @GetMapping("/orders/{orderId}")
    public ResponseEntity<OrderDetailResponse> getOrderDetail(@PathVariable Long orderId) {
        return orderService.getOrderDetail(orderId);
    }

    @PatchMapping("/orders/{orderId}/status")
    public ResponseEntity<String> updateOrderStatus(@PathVariable Long orderId, @RequestParam String status) {
        return orderService.updateOrderStatus(orderId, status);
    }

    @PatchMapping("/orders/status/bulk")
    public ResponseEntity<String> updateOrderStatuses(@RequestBody AdminOrderBulkStatusRequest request) {
        return orderService.updateOrderStatuses(request);
    }

    @GetMapping("/orders/pending-approval")
    public ResponseEntity<Page<OrderListResponse>> getPendingApprovalOrders(
            @PageableDefault(size = 20, sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable
    ) {
        return adminService.getOrdersByStatus("PENDING", pageable);
    }

    @GetMapping("/deliveries")
    public ResponseEntity<Page<DeliveryResponse>> getDeliveries(
            @RequestParam(required = false) String status,
            @PageableDefault(size = 20, sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable
    ) {
        return adminService.getDeliveries(status, pageable);
    }

    @PatchMapping("/deliveries/{deliveryId}")
    public ResponseEntity<DeliveryResponse> updateDelivery(
            @PathVariable Long deliveryId,
            @RequestBody DeliveryUpdateRequest req
    ) {
        return adminService.updateDelivery(deliveryId, req);
    }

    @GetMapping("/payments")
    public ResponseEntity<Page<PaymentStatusResponse>> getPayments(
            @RequestParam(required = false) String status,
            @PageableDefault(size = 20, sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable
    ) {
        return paymentService.getAllPayments(status, pageable);
    }

    @GetMapping("/payments/{paymentId}")
    public ResponseEntity<PaymentStatusResponse> getPaymentDetail(@PathVariable Long paymentId) {
        return paymentService.getPaymentDetail(paymentId);
    }

    @GetMapping("/categories")
    public ResponseEntity<List<CategoryResponse>> getAdminCategories() {
        return adminService.getAdminCategories();
    }

    @PostMapping("/categories/top")
    public ResponseEntity<Long> createCategoryTop(@RequestBody CategoryCreateRequest req) {
        return adminService.createCategoryTop(req);
    }

    @PostMapping("/categories/sub")
    public ResponseEntity<Long> createCategorySub(@RequestBody CategoryCreateRequest req) {
        return adminService.createCategorySub(req);
    }

    @PutMapping("/categories/top/{topId}")
    public ResponseEntity<String> updateCategoryTop(@PathVariable Long topId, @RequestBody CategoryCreateRequest req) {
        return adminService.updateCategoryTop(topId, req);
    }

    @PutMapping("/categories/sub/{subId}")
    public ResponseEntity<String> updateCategorySub(@PathVariable Long subId, @RequestBody CategoryCreateRequest req) {
        return adminService.updateCategorySub(subId, req);
    }

    @DeleteMapping("/categories/top/{topId}")
    public ResponseEntity<String> deleteCategoryTop(@PathVariable Long topId) {
        return adminService.deleteCategoryTop(topId);
    }

    @DeleteMapping("/categories/sub/{subId}")
    public ResponseEntity<String> deleteCategorySub(@PathVariable Long subId) {
        return adminService.deleteCategorySub(subId);
    }

    @GetMapping("/notices")
    public ResponseEntity<Page<NoticeResponse>> getAdminNotices(
            @PageableDefault(size = 20, sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable
    ) {
        return adminService.getAdminNotices(pageable);
    }

    @GetMapping("/notices/{noticeId}")
    public ResponseEntity<NoticeResponse> getAdminNoticeDetail(@PathVariable Long noticeId) {
        return adminService.getAdminNoticeDetail(noticeId);
    }

    @PostMapping("/notices")
    public ResponseEntity<Long> createAdminNotice(
            @RequestBody NoticeCreateRequest req,
            @AuthenticationPrincipal CustomUserDetails user
    ) {
        return noticeService.createNotice(req, user.getMemberId());
    }

    @PutMapping("/notices/{noticeId}")
    public ResponseEntity<NoticeResponse> updateAdminNotice(
            @PathVariable Long noticeId,
            @RequestBody NoticeCreateRequest req
    ) {
        return noticeService.updateNotice(noticeId, req);
    }

    @DeleteMapping("/notices/{noticeId}")
    public ResponseEntity<String> deleteAdminNotice(@PathVariable Long noticeId) {
        return noticeService.deleteNotice(noticeId);
    }

    @GetMapping("/qnas")
    public ResponseEntity<Page<QnAResponse>> getAdminQnAs(
            @RequestParam(required = false, defaultValue = "false") boolean unansweredOnly,
            @PageableDefault(size = 20, sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable
    ) {
        return qnaService.getAllQnAs(unansweredOnly, pageable);
    }

    @GetMapping("/qnas/{qnaId}")
    public ResponseEntity<QnAResponse> getAdminQnADetail(@PathVariable Long qnaId) {
        return qnaService.getAdminQnADetail(qnaId);
    }

    @PostMapping("/qnas/{qnaId}/answer")
    public ResponseEntity<QnAResponse> answerQnA(
            @PathVariable Long qnaId,
            @RequestBody AdminQnAAnswerRequest req,
            @AuthenticationPrincipal CustomUserDetails user
    ) {
        String title = (req.title() == null || req.title().isBlank()) ? "관리자 답변" : req.title();
        return qnaService.answerQnA(qnaId, title, req.content(), user.getMemberId());
    }

    @PatchMapping("/qnas/{qnaId}/status")
    public ResponseEntity<String> updateQnAStatus(@PathVariable Long qnaId, @RequestParam String status) {
        return qnaService.updateQnAStatus(qnaId, status);
    }

    @PutMapping("/qnas/{answerQnaId}/answer")
    public ResponseEntity<QnAResponse> updateAdminAnswer(
            @PathVariable Long answerQnaId,
            @RequestBody AdminQnAAnswerRequest req
    ) {
        return qnaService.updateAdminAnswer(answerQnaId, req.content());
    }

    @DeleteMapping("/qnas/{answerQnaId}/answer")
    public ResponseEntity<String> deleteAdminAnswer(@PathVariable Long answerQnaId) {
        return qnaService.deleteAdminAnswer(answerQnaId);
    }

    @GetMapping("/reviews")
    public ResponseEntity<Page<ReviewResponse>> getAdminReviews(
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) String searchType,
            @PageableDefault(size = 20, sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable
    ) {
        return adminService.getAdminReviews(keyword, searchType, pageable);
    }

    @GetMapping("/reviews/{reviewId}")
    public ResponseEntity<ReviewResponse> getAdminReviewDetail(@PathVariable Long reviewId) {
        return adminService.getAdminReviewDetail(reviewId);
    }

    @DeleteMapping("/reviews/{reviewId}")
    public ResponseEntity<String> deleteReview(@PathVariable Long reviewId) {
        return adminService.adminDeleteReview(reviewId);
    }
}
