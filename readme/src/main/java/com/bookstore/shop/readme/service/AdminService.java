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

    private final MemberRepository memberRepository;
    private final ProductRepository productRepository;
    private final OrderRepository orderRepository;
    private final DeliveryRepository deliveryRepository;
    private final CategoryTopRepository categoryTopRepository;
    private final CategorySubRepository categorySubRepository;
    private final NoticeRepository noticeRepository;
    private final QnARepository qnaRepository;
    private final ReviewRepository reviewRepository;
    private final ReviewService reviewService;
    private final KakaoBookSearchService kakaoBookSearchService;
    private final OrderService orderService;

    @Transactional(readOnly = true)
    public ResponseEntity<DashboardResponse> getDashboard() {
        LocalDateTime todayStart = LocalDate.now().atStartOfDay();
        LocalDateTime todayEnd = todayStart.plusDays(1);
        LocalDateTime monthStart = LocalDate.now().withDayOfMonth(1).atStartOfDay();

        long totalMembers = memberRepository.count();
        long activeMembers = memberRepository.countByMemberStatus(MemberStatus.ACTIVATE);
        long todayNewMembers = memberRepository.countByCreatedAtBetween(todayStart, todayEnd);
        long monthNewMembers = memberRepository.countByCreatedAtBetween(monthStart, todayEnd);

        long totalProducts = productRepository.count();
        long lowStockProducts = productRepository.countByStockLessThanAndProductStatus(10, ProductStatus.ACTIVATE);

        long totalOrders = orderRepository.count();
        long pendingOrders = orderRepository.countByOrderStatus(OrderStatus.PENDING);
        long payedOrders = orderRepository.countByOrderStatus(OrderStatus.PAYED);
        long todayOrders = orderRepository.countByCreatedAtBetween(todayStart, todayEnd);
        long todaySales = orderRepository.sumFinalPriceBetween(todayStart, todayEnd);
        long monthOrders = orderRepository.countByCreatedAtBetween(monthStart, todayEnd);
        long monthSales = orderRepository.sumFinalPriceBetween(monthStart, todayEnd);

        long unansweredQnaCount = qnaRepository.countByDepthAndQnaStatusAndDeletedAtIsNull(0, QnaStatus.WAITING);
        long readyDeliveryCount = deliveryRepository.countByDeliveryStatus(DeliveryStatus.READY);

        return ResponseEntity.ok(new DashboardResponse(
                totalMembers, activeMembers, todayNewMembers, monthNewMembers,
                totalProducts, lowStockProducts,
                totalOrders, pendingOrders, payedOrders,
                todayOrders, todaySales, monthOrders, monthSales,
                unansweredQnaCount, readyDeliveryCount
        ));
    }

    @Transactional(readOnly = true)
    public ResponseEntity<Page<MemberResponse>> getMembers(String keyword, String status, Pageable pageable) {
        String normalizedKeyword = (keyword == null || keyword.isBlank()) ? null : keyword.trim();
        MemberStatus memberStatus = (status == null || status.isBlank()) ? null : MemberStatus.valueOf(status);

        return ResponseEntity.ok(
                memberRepository.searchByKeywordAndStatus(normalizedKeyword, memberStatus, pageable)
                        .map(MemberResponse::new)
        );
    }

    @Transactional(readOnly = true)
    public ResponseEntity<MemberResponse> getMember(Long memberId) {
        Member member = memberRepository.findById(memberId)
                .orElseThrow(() -> new RuntimeException("회원을 찾을 수 없습니다."));
        return ResponseEntity.ok(new MemberResponse(member));
    }

    public ResponseEntity<String> updateMemberStatus(Long memberId, String status) {
        Member member = memberRepository.findById(memberId)
                .orElseThrow(() -> new RuntimeException("회원을 찾을 수 없습니다."));
        member.setMemberStatus(MemberStatus.valueOf(status));
        if (MemberStatus.valueOf(status) == MemberStatus.DELETE) {
            member.setDeletedAt(LocalDateTime.now());
        }
        return ResponseEntity.ok("회원 상태가 변경되었습니다.");
    }

    public ResponseEntity<String> updateMemberRole(Long memberId, String role) {
        Member member = memberRepository.findById(memberId)
                .orElseThrow(() -> new RuntimeException("회원을 찾을 수 없습니다."));
        member.setMemberRole(MemberRole.valueOf(role));
        return ResponseEntity.ok("회원 등급이 변경되었습니다.");
    }

    @Transactional(readOnly = true)
    public ResponseEntity<Page<ProductAdminResponse>> getProducts(String status, Pageable pageable) {
        Page<ProductAdminResponse> result;
        if (status != null && !status.isBlank()) {
            result = productRepository.findAllByProductStatus(ProductStatus.valueOf(status), pageable)
                    .map(ProductAdminResponse::new);
        } else {
            result = productRepository.findAll(pageable).map(ProductAdminResponse::new);
        }
        return ResponseEntity.ok(result);
    }

    @Transactional(readOnly = true)
    public ResponseEntity<ProductAdminResponse> getProduct(Long productId) {
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new RuntimeException("상품을 찾을 수 없습니다."));
        return ResponseEntity.ok(new ProductAdminResponse(product));
    }

    @Transactional(readOnly = true)
    public ResponseEntity<IsbnBookLookupResponse> lookupBookByIsbn(String isbn) {
        return ResponseEntity.ok(kakaoBookSearchService.lookupByIsbn(isbn));
    }

    public ResponseEntity<Long> createProduct(ProductCreateRequest req) {
        CategoryTop categoryTop = categoryTopRepository.findById(req.categoryTopId())
                .orElseThrow(() -> new RuntimeException("상위 카테고리를 찾을 수 없습니다."));
        CategorySub categorySub = categorySubRepository.findById(req.categorySubId())
                .orElseThrow(() -> new RuntimeException("하위 카테고리를 찾을 수 없습니다."));

        int salePrice = calcSalePrice(req.price(), req.discountRate());

        Product product = Product.builder()
                .categoryTop(categoryTop)
                .categorySub(categorySub)
                .title(req.title())
                .author(req.author())
                .isbn(req.isbn())
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

    public ResponseEntity<ProductAdminResponse> updateProduct(Long productId, ProductUpdateRequest req) {
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new RuntimeException("상품을 찾을 수 없습니다."));

        product.setTitle(req.title());
        product.setAuthor(req.author());
        product.setIsbn(req.isbn());
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

    public ResponseEntity<String> deleteProduct(Long productId) {
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new RuntimeException("상품을 찾을 수 없습니다."));
        product.setProductStatus(ProductStatus.DELETE);
        product.setDeletedAt(LocalDateTime.now());
        return ResponseEntity.ok("상품이 삭제되었습니다.");
    }

    @Transactional(readOnly = true)
    public ResponseEntity<Page<DeliveryResponse>> getDeliveries(String status, Pageable pageable) {
        Page<DeliveryResponse> result;
        if (status != null && !status.isBlank()) {
            result = deliveryRepository.findAllByDeliveryStatus(DeliveryStatus.valueOf(status), pageable)
                    .map(DeliveryResponse::new);
        } else {
            result = deliveryRepository.findAll(pageable).map(DeliveryResponse::new);
        }
        return ResponseEntity.ok(result);
    }

    public ResponseEntity<DeliveryResponse> updateDelivery(Long deliveryId, DeliveryUpdateRequest req) {
        Delivery delivery = deliveryRepository.findById(deliveryId)
                .orElseThrow(() -> new RuntimeException("배송 정보를 찾을 수 없습니다."));

        if (req.courier() != null) {
            delivery.setCourier(req.courier());
        }
        if (req.trackingNumber() != null) {
            delivery.setTrackingNumber(req.trackingNumber());
        }
        if (req.deliveryStatus() != null) {
            DeliveryStatus deliveryStatus = DeliveryStatus.valueOf(req.deliveryStatus());
            delivery.setDeliveryStatus(deliveryStatus);
            if (deliveryStatus == DeliveryStatus.SHIPPED && delivery.getShippedAt() == null) {
                delivery.setShippedAt(LocalDateTime.now());
            }
            if (deliveryStatus == DeliveryStatus.DELIVERED && delivery.getDeliveredAt() == null) {
                delivery.setDeliveredAt(LocalDateTime.now());
            }
        }
        return ResponseEntity.ok(new DeliveryResponse(delivery));
    }

    public ResponseEntity<String> updateStock(Long productId, int stock) {
        if (stock < 0) {
            throw new RuntimeException("재고는 0 이상이어야 합니다.");
        }
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new RuntimeException("상품을 찾을 수 없습니다."));
        product.setStock(stock);
        if (stock == 0 && product.getProductStatus() == ProductStatus.ACTIVATE) {
            product.setProductStatus(ProductStatus.DEACTIVATE);
        } else if (stock > 0 && product.getProductStatus() == ProductStatus.DEACTIVATE) {
            product.setProductStatus(ProductStatus.ACTIVATE);
        }
        return ResponseEntity.ok("재고가 " + stock + "개로 변경되었습니다.");
    }

    @Transactional
    public ResponseEntity<String> updateProductStatus(Long productId, String status) {
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new RuntimeException("상품을 찾을 수 없습니다."));
        ProductStatus next = ProductStatus.valueOf(status);
        product.setProductStatus(next);
        return ResponseEntity.ok("상품 상태가 변경되었습니다.");
    }

    @Transactional(readOnly = true)
    public ResponseEntity<Page<OrderListResponse>> getOrdersByStatus(String status, Pageable pageable) {
        OrderStatus orderStatus = OrderStatus.valueOf(status);
        return orderService.getOrdersByStatus(orderStatus, pageable);
    }

    @Transactional(readOnly = true)
    public ResponseEntity<List<CategoryResponse>> getAdminCategories() {
        List<CategoryResponse> result = categoryTopRepository.findAll().stream()
                .map(top -> new CategoryResponse(
                        top,
                        categorySubRepository.findByCategoryTopIdOrderBySortOrderAsc(top.getId())
                ))
                .collect(Collectors.toList());
        return ResponseEntity.ok(result);
    }

    public ResponseEntity<Long> createCategoryTop(CategoryCreateRequest req) {
        CategoryTop top = CategoryTop.builder()
                .name(req.name())
                .sortOrder(req.sortOrder() != null ? req.sortOrder() : 0)
                .categoryStatus(CategoryStatus.ACTIVATE)
                .build();
        categoryTopRepository.save(top);
        return ResponseEntity.status(201).body(top.getId());
    }

    public ResponseEntity<Long> createCategorySub(CategoryCreateRequest req) {
        CategoryTop top = categoryTopRepository.findById(req.categoryTopId())
                .orElseThrow(() -> new RuntimeException("상위 카테고리를 찾을 수 없습니다."));
        CategorySub sub = CategorySub.builder()
                .categoryTop(top)
                .name(req.name())
                .sortOrder(req.sortOrder() != null ? req.sortOrder() : 0)
                .categoryStatus(CategoryStatus.ACTIVATE)
                .build();
        categorySubRepository.save(sub);
        return ResponseEntity.status(201).body(sub.getId());
    }

    public ResponseEntity<String> updateCategoryTop(Long topId, CategoryCreateRequest req) {
        CategoryTop top = categoryTopRepository.findById(topId)
                .orElseThrow(() -> new RuntimeException("상위 카테고리를 찾을 수 없습니다."));
        if (req.name() != null) {
            top.setName(req.name());
        }
        if (req.sortOrder() != null) {
            top.setSortOrder(req.sortOrder());
        }
        return ResponseEntity.ok("카테고리가 수정되었습니다.");
    }

    public ResponseEntity<String> updateCategorySub(Long subId, CategoryCreateRequest req) {
        CategorySub sub = categorySubRepository.findById(subId)
                .orElseThrow(() -> new RuntimeException("하위 카테고리를 찾을 수 없습니다."));
        if (req.name() != null) {
            sub.setName(req.name());
        }
        if (req.sortOrder() != null) {
            sub.setSortOrder(req.sortOrder());
        }
        return ResponseEntity.ok("카테고리가 수정되었습니다.");
    }

    public ResponseEntity<String> deleteCategoryTop(Long topId) {
        CategoryTop top = categoryTopRepository.findById(topId)
                .orElseThrow(() -> new RuntimeException("상위 카테고리를 찾을 수 없습니다."));
        top.setCategoryStatus(CategoryStatus.DEACTIVATE);
        return ResponseEntity.ok("카테고리가 비활성화되었습니다.");
    }

    public ResponseEntity<String> deleteCategorySub(Long subId) {
        CategorySub sub = categorySubRepository.findById(subId)
                .orElseThrow(() -> new RuntimeException("하위 카테고리를 찾을 수 없습니다."));
        sub.setCategoryStatus(CategoryStatus.DEACTIVATE);
        return ResponseEntity.ok("카테고리가 비활성화되었습니다.");
    }

    @Transactional(readOnly = true)
    public ResponseEntity<Page<NoticeResponse>> getAdminNotices(Pageable pageable) {
        return ResponseEntity.ok(noticeRepository.findAllByDeletedAtIsNull(pageable).map(NoticeResponse::new));
    }

    @Transactional(readOnly = true)
    public ResponseEntity<NoticeResponse> getAdminNoticeDetail(Long noticeId) {
        Notice notice = noticeRepository.findById(noticeId)
                .orElseThrow(() -> new RuntimeException("공지사항을 찾을 수 없습니다."));
        return ResponseEntity.ok(new NoticeResponse(notice));
    }

    @Transactional(readOnly = true)
    public ResponseEntity<Page<ReviewResponse>> getAdminReviews(String keyword, String searchType, Pageable pageable) {
        String normalizedKeyword = (keyword == null || keyword.isBlank()) ? null : keyword.trim();
        String normalizedSearchType = (searchType == null || searchType.isBlank()) ? "reviewTitle" : searchType.trim();

        Page<Review> reviewPage;

        if (normalizedKeyword == null) {
            reviewPage = reviewRepository.findAllByDeletedAtIsNull(pageable);
        } else {
            reviewPage = switch (normalizedSearchType) {
                case "productTitle" ->
                        reviewRepository.findAllByDeletedAtIsNullAndProduct_TitleContainingIgnoreCase(normalizedKeyword, pageable);
                case "memberName" ->
                        reviewRepository.findAllByDeletedAtIsNullAndMember_NameContainingIgnoreCase(normalizedKeyword, pageable);
                case "reviewTitle" ->
                        reviewRepository.findAllByDeletedAtIsNullAndContentContainingIgnoreCase(normalizedKeyword, pageable);
                default ->
                        reviewRepository.findAllByDeletedAtIsNullAndContentContainingIgnoreCase(normalizedKeyword, pageable);
            };
        }

        return ResponseEntity.ok(reviewPage.map(ReviewResponse::new));
    }

    @Transactional(readOnly = true)
    public ResponseEntity<ReviewResponse> getAdminReviewDetail(Long reviewId) {
        return reviewService.getReviewDetailForAdmin(reviewId);
    }

    public ResponseEntity<String> adminDeleteReview(Long reviewId) {
        return reviewService.adminDeleteReview(reviewId);
    }

    private int calcSalePrice(int price, BigDecimal discountRate) {
        if (discountRate == null || discountRate.compareTo(BigDecimal.ZERO) == 0) {
            return price;
        }
        BigDecimal ratio = BigDecimal.ONE.subtract(discountRate.divide(BigDecimal.valueOf(100)));
        return ratio.multiply(BigDecimal.valueOf(price)).intValue();
    }
}
