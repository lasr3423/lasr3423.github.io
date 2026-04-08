package com.bookstore.shop.readme.service;

import com.bookstore.shop.readme.domain.*;
import com.bookstore.shop.readme.dto.request.CategoryCreateRequest;
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

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional
public class AdminService {

    private final MemberRepository       memberRepository;
    private final ProductRepository      productRepository;
    private final OrderRepository        orderRepository;
    private final DeliveryRepository     deliveryRepository;
    private final CategoryTopRepository  categoryTopRepository;
    private final CategorySubRepository  categorySubRepository;
    private final NoticeRepository       noticeRepository;
    private final QnARepository          qnaRepository;
    private final ReviewRepository       reviewRepository;
    private final ReviewService          reviewService;   // 리뷰 상세/삭제 위임

    // ── 대시보드 — REQ-A-001~007 ────────────────────────────────────────────

    @Transactional(readOnly = true)
    public ResponseEntity<DashboardResponse> getDashboard() {
        LocalDateTime todayStart  = LocalDate.now().atStartOfDay();
        LocalDateTime todayEnd    = todayStart.plusDays(1);
        LocalDateTime monthStart  = LocalDate.now().withDayOfMonth(1).atStartOfDay();

        // 회원
        long totalMembers    = memberRepository.count();
        long activeMembers   = memberRepository.countByMemberStatus(MemberStatus.ACTIVATE);
        long todayNewMembers = memberRepository.countByCreatedAtBetween(todayStart, todayEnd);
        long monthNewMembers = memberRepository.countByCreatedAtBetween(monthStart, todayEnd);

        // 상품 (재고 10개 이하 = 부족)
        long totalProducts    = productRepository.count();
        long lowStockProducts = productRepository
                .countByStockLessThanAndProductStatus(10, ProductStatus.ACTIVATE);

        // 주문
        long totalOrders   = orderRepository.count();
        long pendingOrders = orderRepository.countByOrderStatus(OrderStatus.PENDING);
        long payedOrders   = orderRepository.countByOrderStatus(OrderStatus.PAYED);
        long todayOrders   = orderRepository.countByCreatedAtBetween(todayStart, todayEnd);
        long todaySales    = orderRepository.sumFinalPriceBetween(todayStart, todayEnd);
        long monthOrders   = orderRepository.countByCreatedAtBetween(monthStart, todayEnd);
        long monthSales    = orderRepository.sumFinalPriceBetween(monthStart, todayEnd);

        // QnA 미답변 (depth=0 질문 중 WAITING 상태)
        long unansweredQnaCount = qnaRepository.countByDepthAndQnaStatusAndDeletedAtIsNull(0, QnaStatus.WAITING);

        // 배송 READY
        long readyDeliveryCount = deliveryRepository.countByDeliveryStatus(DeliveryStatus.READY);

        return ResponseEntity.ok(new DashboardResponse(
                totalMembers, activeMembers, todayNewMembers, monthNewMembers,
                totalProducts, lowStockProducts,
                totalOrders, pendingOrders, payedOrders,
                todayOrders, todaySales, monthOrders, monthSales,
                unansweredQnaCount, readyDeliveryCount));
    }

    // ── 회원 관리 ───────────────────────────────────────────────────────────

    /** 회원 목록 (전체 / 상태별 / 키워드 검색) */
    @Transactional(readOnly = true)
    public ResponseEntity<Page<MemberResponse>> getMembers(
            String keyword, String status, Pageable pageable) {
        String normalizedKeyword = (keyword == null || keyword.isBlank()) ? null : keyword.trim();
        MemberStatus memberStatus = (status == null || status.isBlank()) ? null : MemberStatus.valueOf(status);

        return ResponseEntity.ok(
                memberRepository.searchByKeywordAndStatus(normalizedKeyword, memberStatus, pageable)
                        .map(MemberResponse::new)
        );
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

    // ── 카테고리 관리 (/admin/category) ──────────────────────────────────────

    /** 대분류 카테고리 목록 (소분류 포함) */
    @Transactional(readOnly = true)
    public ResponseEntity<List<CategoryResponse>> getAdminCategories() {
        List<CategoryResponse> result = categoryTopRepository.findAll().stream()
                .map(top -> new CategoryResponse(top,
                        categorySubRepository.findByCategoryTopIdOrderBySortOrderAsc(top.getId())))
                .collect(Collectors.toList());
        return ResponseEntity.ok(result);
    }

    /** 대분류 카테고리 등록 — FA-027 */
    public ResponseEntity<Long> createCategoryTop(CategoryCreateRequest req) {
        CategoryTop top = CategoryTop.builder()
                .name(req.name())
                .sortOrder(req.sortOrder() != null ? req.sortOrder() : 0)
                .categoryStatus(CategoryStatus.ACTIVATE)
                .build();
        categoryTopRepository.save(top);
        return ResponseEntity.status(201).body(top.getId());
    }

    /** 소분류 카테고리 등록 — FA-027 */
    public ResponseEntity<Long> createCategorySub(CategoryCreateRequest req) {
        CategoryTop top = categoryTopRepository.findById(req.categoryTopId())
                .orElseThrow(() -> new RuntimeException("대분류 카테고리를 찾을 수 없습니다."));
        CategorySub sub = CategorySub.builder()
                .categoryTop(top)
                .name(req.name())
                .sortOrder(req.sortOrder() != null ? req.sortOrder() : 0)
                .categoryStatus(CategoryStatus.ACTIVATE)
                .build();
        categorySubRepository.save(sub);
        return ResponseEntity.status(201).body(sub.getId());
    }

    /** 대분류 카테고리 수정 — FA-028 */
    public ResponseEntity<String> updateCategoryTop(Long topId, CategoryCreateRequest req) {
        CategoryTop top = categoryTopRepository.findById(topId)
                .orElseThrow(() -> new RuntimeException("대분류 카테고리를 찾을 수 없습니다."));
        if (req.name() != null) top.setName(req.name());
        if (req.sortOrder() != null) top.setSortOrder(req.sortOrder());
        return ResponseEntity.ok("카테고리가 수정되었습니다.");
    }

    /** 소분류 카테고리 수정 — FA-028 */
    public ResponseEntity<String> updateCategorySub(Long subId, CategoryCreateRequest req) {
        CategorySub sub = categorySubRepository.findById(subId)
                .orElseThrow(() -> new RuntimeException("소분류 카테고리를 찾을 수 없습니다."));
        if (req.name() != null) sub.setName(req.name());
        if (req.sortOrder() != null) sub.setSortOrder(req.sortOrder());
        return ResponseEntity.ok("카테고리가 수정되었습니다.");
    }

    /** 대분류 카테고리 삭제 (DEACTIVATE) — FA-029 */
    public ResponseEntity<String> deleteCategoryTop(Long topId) {
        CategoryTop top = categoryTopRepository.findById(topId)
                .orElseThrow(() -> new RuntimeException("대분류 카테고리를 찾을 수 없습니다."));
        top.setCategoryStatus(CategoryStatus.DEACTIVATE);
        return ResponseEntity.ok("카테고리가 비활성화되었습니다.");
    }

    /** 소분류 카테고리 삭제 (DEACTIVATE) — FA-029 */
    public ResponseEntity<String> deleteCategorySub(Long subId) {
        CategorySub sub = categorySubRepository.findById(subId)
                .orElseThrow(() -> new RuntimeException("소분류 카테고리를 찾을 수 없습니다."));
        sub.setCategoryStatus(CategoryStatus.DEACTIVATE);
        return ResponseEntity.ok("카테고리가 비활성화되었습니다.");
    }

    // ── 공지사항 관리 (/admin/notice) ────────────────────────────────────────

    /** 관리자 전체 공지사항 목록 */
    @Transactional(readOnly = true)
    public ResponseEntity<Page<NoticeResponse>> getAdminNotices(Pageable pageable) {
        return ResponseEntity.ok(
                noticeRepository.findAllByDeletedAtIsNull(pageable).map(NoticeResponse::new));
    }

    /** 관리자 공지사항 상세 조회 — FA-031 */
    @Transactional(readOnly = true)
    public ResponseEntity<NoticeResponse> getAdminNoticeDetail(Long noticeId) {
        Notice notice = noticeRepository.findById(noticeId)
                .orElseThrow(() -> new RuntimeException("공지사항을 찾을 수 없습니다."));
        return ResponseEntity.ok(new NoticeResponse(notice));
    }

    // ── QnA 관리 (/admin/qna) ────────────────────────────────────────────────

    // getAdminQnAs — AdminApiController에서 QnAService.getAllQnAs()로 직접 위임하므로 제거됨

    // ── 리뷰 관리 (/admin/review) ─────────────────────────────────────────────

    /** 관리자 전체 리뷰 목록 */
    @Transactional(readOnly = true)
    public ResponseEntity<Page<ReviewResponse>> getAdminReviews(Pageable pageable) {
        return ResponseEntity.ok(
                reviewRepository.findAllByDeletedAtIsNull(pageable).map(ReviewResponse::new));
    }

    /** 관리자 리뷰 상세 조회 — FA-036 (이미지/반응 포함 — ReviewService 위임) */
    @Transactional(readOnly = true)
    public ResponseEntity<ReviewResponse> getAdminReviewDetail(Long reviewId) {
        return reviewService.getReviewDetailForAdmin(reviewId);
    }

    /** 관리자 리뷰 강제 삭제 (soft delete — ReviewService 위임) */
    public ResponseEntity<String> adminDeleteReview(Long reviewId) {
        return reviewService.adminDeleteReview(reviewId);
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
