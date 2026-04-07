package com.bookstore.shop.readme.controller.admin;

import com.bookstore.shop.readme.dto.request.DeliveryUpdateRequest;
import com.bookstore.shop.readme.dto.request.ProductCreateRequest;
import com.bookstore.shop.readme.dto.request.ProductUpdateRequest;
import com.bookstore.shop.readme.dto.response.*;
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

    private final AdminService adminService;
    private final OrderService orderService;

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
}
