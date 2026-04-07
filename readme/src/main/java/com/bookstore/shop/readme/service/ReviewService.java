package com.bookstore.shop.readme.service;

import com.bookstore.shop.readme.domain.Member;
import com.bookstore.shop.readme.domain.Product;
import com.bookstore.shop.readme.domain.Review;
import com.bookstore.shop.readme.dto.request.ReviewCreateRequest;
import com.bookstore.shop.readme.dto.response.ReviewResponse;
import com.bookstore.shop.readme.repository.MemberRepository;
import com.bookstore.shop.readme.repository.ProductRepository;
import com.bookstore.shop.readme.repository.ReviewRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
@Transactional
public class ReviewService {

    private final ReviewRepository  reviewRepository;
    private final MemberRepository  memberRepository;
    private final ProductRepository productRepository;

    // ── 특정 상품 리뷰 목록 (삭제되지 않은 것만) ────────────────────────────
    @Transactional(readOnly = true)
    public ResponseEntity<Page<ReviewResponse>> getReviews(Long productId, Pageable pageable) {
        return ResponseEntity.ok(
                reviewRepository.findAllByProductIdAndDeletedAtIsNull(productId, pageable)
                        .map(ReviewResponse::new));
    }

    // ── 리뷰 등록 ────────────────────────────────────────────────────────────
    public ResponseEntity<Long> createReview(ReviewCreateRequest req, Long memberId) {
        if (req.rating() < 1 || req.rating() > 5)
            throw new RuntimeException("별점은 1~5 사이로 입력해주세요.");
        Member member = memberRepository.findById(memberId)
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

    // ── 리뷰 삭제 soft delete (본인만) ──────────────────────────────────────
    public ResponseEntity<String> deleteReview(Long reviewId, Long memberId) {
        Review review = reviewRepository.findById(reviewId)
                .orElseThrow(() -> new RuntimeException("리뷰를 찾을 수 없습니다."));
        if (!review.getMember().getId().equals(memberId))
            throw new RuntimeException("삭제 권한이 없습니다.");
        review.setDeletedAt(LocalDateTime.now());
        return ResponseEntity.ok("리뷰가 삭제되었습니다.");
    }

    // ── 관리자 전체 리뷰 목록 ─────────────────────────────────────────────────
    @Transactional(readOnly = true)
    public ResponseEntity<Page<ReviewResponse>> getAllReviews(Pageable pageable) {
        return ResponseEntity.ok(
                reviewRepository.findAllByDeletedAtIsNull(pageable).map(ReviewResponse::new));
    }

    // ── 관리자 리뷰 강제 삭제 soft delete ────────────────────────────────────
    public ResponseEntity<String> adminDeleteReview(Long reviewId) {
        Review review = reviewRepository.findById(reviewId)
                .orElseThrow(() -> new RuntimeException("리뷰를 찾을 수 없습니다."));
        review.setDeletedAt(LocalDateTime.now());
        return ResponseEntity.ok("리뷰가 삭제되었습니다.");
    }
}
