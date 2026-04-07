package com.bookstore.shop.readme.service;

import com.bookstore.shop.readme.domain.*;
import com.bookstore.shop.readme.dto.request.DeliveryUpdateRequest;
import com.bookstore.shop.readme.dto.request.ProductCreateRequest;
import com.bookstore.shop.readme.dto.request.ProductUpdateRequest;
import com.bookstore.shop.readme.dto.response.*;
import com.bookstore.shop.readme.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.stream.Collectors;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
@Transactional
public class AdminService {

    private final MemberRepository    memberRepository;
    private final ProductRepository   productRepository;
    private final OrderRepository     orderRepository;
    private final DeliveryRepository  deliveryRepository;
    private final CategoryTopRepository  categoryTopRepository;
    private final CategorySubRepository  categorySubRepository;

    // ── 대시보드 ────────────────────────────────────────────────────────────

    @Transactional(readOnly = true)
    public ResponseEntity<DashboardResponse> getDashboard() {
        long totalMembers   = memberRepository.count();
        long activeMembers  = memberRepository.countByMemberStatus(MemberStatus.ACTIVATE);
        long totalProducts  = productRepository.count();
        long totalOrders    = orderRepository.count();
        long pendingOrders  = orderRepository.countByOrderStatus(OrderStatus.PENDING);

        return ResponseEntity.ok(new DashboardResponse(
                totalMembers, activeMembers, totalProducts, totalOrders, pendingOrders));
    }

    // ── 회원 관리 ───────────────────────────────────────────────────────────

    /** 회원 목록 (전체 / 상태별 / 키워드 검색) */
    @Transactional(readOnly = true)
    public ResponseEntity<Page<MemberResponse>> getMembers(
            String keyword, String status, Pageable pageable) {

        Page<MemberResponse> result;

        if (keyword != null && !keyword.isBlank()) {
            result = memberRepository
                    .findByNameContainingOrEmailContaining(keyword, keyword, pageable)
                    .map(MemberResponse::new);
        } else if (status != null && !status.isBlank()) {
            result = memberRepository
                    .findAllByMemberStatus(MemberStatus.valueOf(status), pageable)
                    .map(MemberResponse::new);
        } else {
            result = memberRepository.findAll(pageable).map(MemberResponse::new);
        }
        return ResponseEntity.ok(result);
    }

    /** 회원 상세 조회 */
    @Transactional(readOnly = true)
    public ResponseEntity<MemberResponse> getMember(Long memberId) {
        Member member = memberRepository.findById(memberId)
                .orElseThrow(() -> new RuntimeException("회원을 찾을 수 없습니다."));
        return ResponseEntity.ok(new MemberResponse(member));
    }

    /** 회원 상태 변경 (ACTIVATE / DEACTIVATE / DELETE) */
    public ResponseEntity<String> updateMemberStatus(Long memberId, String status) {
        Member member = memberRepository.findById(memberId)
                .orElseThrow(() -> new RuntimeException("회원을 찾을 수 없습니다."));
        member.setMemberStatus(MemberStatus.valueOf(status));
        if (MemberStatus.valueOf(status) == MemberStatus.DELETE) {
            member.setDeletedAt(LocalDateTime.now());
        }
        return ResponseEntity.ok("회원 상태가 변경되었습니다.");
    }

    /** 회원 등급 변경 (USER / MANAGER / ADMIN) */
    public ResponseEntity<String> updateMemberRole(Long memberId, String role) {
        Member member = memberRepository.findById(memberId)
                .orElseThrow(() -> new RuntimeException("회원을 찾을 수 없습니다."));
        member.setMemberRole(MemberRole.valueOf(role));
        return ResponseEntity.ok("회원 등급이 변경되었습니다.");
    }

    // ── 상품 관리 ───────────────────────────────────────────────────────────

    /** 상품 목록 (전체 / 상태별) */
    @Transactional(readOnly = true)
    public ResponseEntity<Page<ProductAdminResponse>> getProducts(String status, Pageable pageable) {
        Page<ProductAdminResponse> result;
        if (status != null && !status.isBlank()) {
            result = productRepository
                    .findAllByProductStatus(ProductStatus.valueOf(status), pageable)
                    .map(ProductAdminResponse::new);
        } else {
            result = productRepository.findAll(pageable).map(ProductAdminResponse::new);
        }
        return ResponseEntity.ok(result);
    }

    /** 상품 상세 조회 */
    @Transactional(readOnly = true)
    public ResponseEntity<ProductAdminResponse> getProduct(Long productId) {
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new RuntimeException("상품을 찾을 수 없습니다."));
        return ResponseEntity.ok(new ProductAdminResponse(product));
    }

    /** 상품 등록 */
    public ResponseEntity<Long> createProduct(ProductCreateRequest req) {
        CategoryTop categoryTop = categoryTopRepository.findById(req.categoryTopId())
                .orElseThrow(() -> new RuntimeException("대분류 카테고리를 찾을 수 없습니다."));
        CategorySub categorySub = categorySubRepository.findById(req.categorySubId())
                .orElseThrow(() -> new RuntimeException("소분류 카테고리를 찾을 수 없습니다."));

        int salePrice = calcSalePrice(req.price(), req.discountRate());

        Product product = Product.builder()
                .categoryTop(categoryTop)
                .categorySub(categorySub)
                .title(req.title())
                .author(req.author())
                .description(req.description())
                .price(req.price())
                .discountRate(req.discountRate())
                .salePrice(salePrice)
                .stock(req.stock())
                .thumbnail(req.thumbnail())
                .productStatus(ProductStatus.ACTIVATE)
                .build();
        productRepository.save(product);
        return ResponseEntity.status(201).body(product.getId());
    }

    /** 상품 수정 */
    public ResponseEntity<ProductAdminResponse> updateProduct(Long productId, ProductUpdateRequest req) {
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new RuntimeException("상품을 찾을 수 없습니다."));

        product.setTitle(req.title());
        product.setAuthor(req.author());
        product.setDescription(req.description());
        product.setPrice(req.price());
        product.setDiscountRate(req.discountRate());
        product.setSalePrice(calcSalePrice(req.price(), req.discountRate()));
        product.setStock(req.stock());
        product.setThumbnail(req.thumbnail());
        if (req.productStatus() != null && !req.productStatus().isBlank()) {
            product.setProductStatus(ProductStatus.valueOf(req.productStatus()));
        }
        return ResponseEntity.ok(new ProductAdminResponse(product));
    }

    /** 상품 삭제 (soft delete) */
    public ResponseEntity<String> deleteProduct(Long productId) {
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new RuntimeException("상품을 찾을 수 없습니다."));
        product.setProductStatus(ProductStatus.DELETE);
        product.setDeletedAt(LocalDateTime.now());
        return ResponseEntity.ok("상품이 삭제되었습니다.");
    }

    // ── 배송 관리 ───────────────────────────────────────────────────────────

    /** 배송 목록 (상태별) */
    @Transactional(readOnly = true)
    public ResponseEntity<Page<DeliveryResponse>> getDeliveries(String status, Pageable pageable) {
        Page<DeliveryResponse> result;
        if (status != null && !status.isBlank()) {
            result = deliveryRepository
                    .findAllByDeliveryStatus(DeliveryStatus.valueOf(status), pageable)
                    .map(DeliveryResponse::new);
        } else {
            result = deliveryRepository.findAll(pageable).map(DeliveryResponse::new);
        }
        return ResponseEntity.ok(result);
    }

    /** 배송 정보 수정 (운송장 번호 / 상태 업데이트) */
    public ResponseEntity<DeliveryResponse> updateDelivery(Long deliveryId, DeliveryUpdateRequest req) {
        Delivery delivery = deliveryRepository.findById(deliveryId)
                .orElseThrow(() -> new RuntimeException("배송 정보를 찾을 수 없습니다."));

        if (req.courier() != null)        { delivery.setCourier(req.courier()); }
        if (req.trackingNumber() != null) { delivery.setTrackingNumber(req.trackingNumber()); }
        if (req.deliveryStatus() != null) {
            DeliveryStatus ds = DeliveryStatus.valueOf(req.deliveryStatus());
            delivery.setDeliveryStatus(ds);
            if (ds == DeliveryStatus.SHIPPED && delivery.getShippedAt() == null) {
                delivery.setShippedAt(LocalDateTime.now());
            }
            if (ds == DeliveryStatus.DELIVERED && delivery.getDeliveredAt() == null) {
                delivery.setDeliveredAt(LocalDateTime.now());
            }
        }
        return ResponseEntity.ok(new DeliveryResponse(delivery));
    }

    // ── 재고 관리 (/admin/product/stock) ─────────────────────────────────────

    /** [신규] 상품 재고 수량 직접 변경 */
    public ResponseEntity<String> updateStock(Long productId, int stock) {
        if (stock < 0) throw new RuntimeException("재고는 0 이상이어야 합니다.");
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new RuntimeException("상품을 찾을 수 없습니다."));
        product.setStock(stock);
        // 재고 0이면 비활성화, 재고 복구 시 활성화
        if (stock == 0 && product.getProductStatus() == ProductStatus.ACTIVATE) {
            product.setProductStatus(ProductStatus.DEACTIVATE);
        } else if (stock > 0 && product.getProductStatus() == ProductStatus.DEACTIVATE) {
            product.setProductStatus(ProductStatus.ACTIVATE);
        }
        return ResponseEntity.ok("재고가 " + stock + "개로 변경되었습니다.");
    }

    // ── 주문 상태 필터 조회 ───────────────────────────────────────────────────

    /** [신규] 특정 상태 주문만 조회 — 승인 대기(PAYED) 목록 페이지에서 사용 */
    @Transactional(readOnly = true)
    public ResponseEntity<Page<OrderListResponse>> getOrdersByStatus(
            String status, Pageable pageable) {
        OrderStatus orderStatus = OrderStatus.valueOf(status);
        return ResponseEntity.ok(
                orderRepository.findAllByOrderStatus(orderStatus, pageable)
                        .map(OrderListResponse::new)
        );
    }

    // ── 내부 유틸 ───────────────────────────────────────────────────────────

    private int calcSalePrice(int price, BigDecimal discountRate) {
        if (discountRate == null || discountRate.compareTo(BigDecimal.ZERO) == 0) {
            return price;
        }
        BigDecimal ratio = BigDecimal.ONE.subtract(discountRate.divide(BigDecimal.valueOf(100)));
        return ratio.multiply(BigDecimal.valueOf(price)).intValue();
    }
}
