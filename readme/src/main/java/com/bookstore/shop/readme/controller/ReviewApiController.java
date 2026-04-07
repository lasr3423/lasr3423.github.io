package com.bookstore.shop.readme.controller;

import com.bookstore.shop.readme.domain.Member;
import com.bookstore.shop.readme.domain.Product;
import com.bookstore.shop.readme.domain.Review;
import com.bookstore.shop.readme.dto.request.ReviewCreateRequest;
import com.bookstore.shop.readme.dto.response.ReviewResponse;
import com.bookstore.shop.readme.repository.MemberRepository;
import com.bookstore.shop.readme.repository.ProductRepository;
import com.bookstore.shop.readme.repository.ReviewRepository;
import com.bookstore.shop.readme.security.CustomUserDetails;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

// [신규] 상품 리뷰 API
// - GET /api/review?productId= : 특정 상품의 리뷰 목록
// - POST /api/review            : 리뷰 등록 (로그인 필수)
// - DELETE /api/review/{id}     : 리뷰 삭제 soft delete (본인 또는 관리자)
@RestController
@RequestMapping("/api/review")
@RequiredArgsConstructor
public class ReviewApiController {

    private final ReviewRepository  reviewRepository;
    private final MemberRepository  memberRepository;
    private final ProductRepository productRepository;

    @GetMapping
    public ResponseEntity<Page<ReviewResponse>> getReviews(
            @RequestParam Long productId,
            @PageableDefault(size = 10, sort = "createdAt",
                    direction = Sort.Direction.DESC) Pageable pageable) {
        return ResponseEntity.ok(
                reviewRepository.findAllByProductIdAndDeletedAtIsNull(productId, pageable)
                        .map(ReviewResponse::new));
    }

    @PostMapping
    public ResponseEntity<Long> createReview(
            @RequestBody ReviewCreateRequest req,
            @AuthenticationPrincipal CustomUserDetails user) {
        if (req.rating() < 1 || req.rating() > 5)
            throw new RuntimeException("별점은 1~5 사이로 입력해주세요.");
        Member member = memberRepository.findById(user.getMemberId())
                .orElseThrow(() -> new RuntimeException("회원을 찾을 수 없습니다."));
        Product product = productRepository.findById(req.productId())
                .orElseThrow(() -> new RuntimeException("상품을 찾을 수 없습니다."));
        Review review = Review.builder()
                .member(member)
                .product(product)
                .rating(req.rating())
                .content(req.content())
                .build();
        reviewRepository.save(review);
        return ResponseEntity.status(201).body(review.getId());
    }

    @DeleteMapping("/{reviewId}")
    public ResponseEntity<String> deleteReview(
            @PathVariable Long reviewId,
            @AuthenticationPrincipal CustomUserDetails user) {
        Review review = reviewRepository.findById(reviewId)
                .orElseThrow(() -> new RuntimeException("리뷰를 찾을 수 없습니다."));
        // 본인 작성 리뷰 또는 관리자만 삭제 가능
        if (!review.getMember().getId().equals(user.getMemberId())) {
            throw new RuntimeException("삭제 권한이 없습니다.");
        }
        review.setDeletedAt(java.time.LocalDateTime.now()); // soft delete
        reviewRepository.save(review);
        return ResponseEntity.ok("리뷰가 삭제되었습니다.");
    }
}
