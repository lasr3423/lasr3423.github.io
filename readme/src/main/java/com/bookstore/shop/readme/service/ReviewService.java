package com.bookstore.shop.readme.service;

import com.bookstore.shop.readme.domain.*;
import com.bookstore.shop.readme.dto.request.ReviewCreateRequest;
import com.bookstore.shop.readme.dto.request.ReviewUpdateRequest;
import com.bookstore.shop.readme.dto.response.ReviewResponse;
import com.bookstore.shop.readme.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Transactional
public class ReviewService {

    private final ReviewRepository         reviewRepository;
    private final MemberRepository         memberRepository;
    private final ProductRepository        productRepository;
    private final ReviewImageRepository    reviewImageRepository;
    private final ReviewReactionRepository reviewReactionRepository;
    private final OrderItemRepository      orderItemRepository;

    // ── 특정 상품 리뷰 목록 (hits 증가 없는 목록 조회) ───────────────────────
    @Transactional(readOnly = true)
    public ResponseEntity<Page<ReviewResponse>> getReviews(Long productId, Pageable pageable) {
        return ResponseEntity.ok(
                reviewRepository.findAllByProductIdAndDeletedAtIsNull(productId, pageable)
                        .map(ReviewResponse::new));
    }

    @Transactional(readOnly = true)
    public ResponseEntity<Page<ReviewResponse>> getRecentReviews(Pageable pageable) {
        return ResponseEntity.ok(
                reviewRepository.findAllByDeletedAtIsNull(pageable)
                        .map(ReviewResponse::new));
    }

    // ── 리뷰 상세 조회 (hits 자동 증가 — REQ-R-005) ──────────────────────────
    public ResponseEntity<ReviewResponse> getReviewDetail(Long reviewId) {
        Review review = findActiveReview(reviewId);
        // 조회수 증가
        review.setHits(review.getHits() + 1);
        List<ReviewImage> images = reviewImageRepository.findAllByReviewId(reviewId);
        long likeCount    = reviewReactionRepository.countByReviewIdAndReactionType(reviewId, ReactionType.LIKE);
        long dislikeCount = reviewReactionRepository.countByReviewIdAndReactionType(reviewId, ReactionType.DISLIKE);
        return ResponseEntity.ok(new ReviewResponse(review, images, likeCount, dislikeCount));
    }

    // ── 리뷰 등록 (이미지 포함) ─────────────────────────────────────────────
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

        // 이미지 저장 (최대 5장)
        if (req.imageUrls() != null && !req.imageUrls().isEmpty()) {
            List<String> urls = req.imageUrls().stream().limit(5).toList();
            for (String url : urls) {
                reviewImageRepository.save(
                        ReviewImage.builder().review(review).imageUrl(url).build());
            }
        }

        // 주문 상품 리뷰 완료 표시
        if (req.orderItemId() != null) {
            orderItemRepository.findById(req.orderItemId()).ifPresent(item -> {
                item.setReviewed(true);
            });
        }

        return ResponseEntity.status(201).body(review.getId());
    }

    // ── 리뷰 수정 (본인만, 이미지 교체 포함) — REQ-M-018 ────────────────────
    public ResponseEntity<ReviewResponse> updateReview(Long reviewId, ReviewUpdateRequest req, Long memberId) {
        Review review = findActiveReview(reviewId);
        if (!review.getMember().getId().equals(memberId))
            throw new RuntimeException("수정 권한이 없습니다.");
        if (req.rating() < 1 || req.rating() > 5)
            throw new RuntimeException("별점은 1~5 사이로 입력해주세요.");
        review.setRating(req.rating());
        review.setContent(req.content());

        // 이미지 교체 (기존 삭제 → 새 이미지 저장)
        if (req.imageUrls() != null) {
            reviewImageRepository.deleteAllByReviewId(reviewId);
            List<String> urls = req.imageUrls().stream().limit(5).toList();
            for (String url : urls) {
                reviewImageRepository.save(
                        ReviewImage.builder().review(review).imageUrl(url).build());
            }
        }
        List<ReviewImage> images = reviewImageRepository.findAllByReviewId(reviewId);
        long likeCount    = reviewReactionRepository.countByReviewIdAndReactionType(reviewId, ReactionType.LIKE);
        long dislikeCount = reviewReactionRepository.countByReviewIdAndReactionType(reviewId, ReactionType.DISLIKE);
        return ResponseEntity.ok(new ReviewResponse(review, images, likeCount, dislikeCount));
    }

    // ── 리뷰 반응 토글 (좋아요/싫어요) — REQ-R-004 ───────────────────────────
    // 동일 타입 재클릭 → 반응 취소 / 다른 타입 클릭 → 타입 변경
    public ResponseEntity<String> toggleReaction(Long reviewId, String reactionType, Long memberId) {
        Review review = findActiveReview(reviewId);
        Member member = memberRepository.findById(memberId)
                .orElseThrow(() -> new RuntimeException("회원을 찾을 수 없습니다."));
        ReactionType type = ReactionType.valueOf(reactionType.toUpperCase());

        Optional<ReviewReaction> existing =
                reviewReactionRepository.findByReviewIdAndMemberId(reviewId, memberId);

        if (existing.isPresent()) {
            ReviewReaction reaction = existing.get();
            if (reaction.getReactionType() == type) {
                // 동일 타입 → 취소 (삭제)
                reviewReactionRepository.delete(reaction);
                return ResponseEntity.ok("반응이 취소되었습니다.");
            } else {
                // 다른 타입 → 변경
                reaction.setReactionType(type);
                return ResponseEntity.ok("반응이 변경되었습니다.");
            }
        } else {
            // 신규 반응 등록
            reviewReactionRepository.save(
                    ReviewReaction.builder()
                            .review(review)
                            .member(member)
                            .reactionType(type)
                            .build());
            return ResponseEntity.ok("반응이 등록되었습니다.");
        }
    }

    // ── 리뷰 삭제 soft delete (본인만) ──────────────────────────────────────
    public ResponseEntity<String> deleteReview(Long reviewId, Long memberId) {
        Review review = findActiveReview(reviewId);
        if (!review.getMember().getId().equals(memberId))
            throw new RuntimeException("삭제 권한이 없습니다.");
        review.setDeletedAt(LocalDateTime.now());
        return ResponseEntity.ok("리뷰가 삭제되었습니다.");
    }

    // ── 마이페이지 내 리뷰 목록 — REQ-M-016 ─────────────────────────────────
    @Transactional(readOnly = true)
    public ResponseEntity<Page<ReviewResponse>> getMyReviews(Long memberId, Pageable pageable) {
        return ResponseEntity.ok(
                reviewRepository.findAllByMemberIdAndDeletedAtIsNull(memberId, pageable)
                        .map(ReviewResponse::new));
    }

    // ── 마이페이지 내 리뷰 상세 — REQ-M-017 ─────────────────────────────────
    @Transactional(readOnly = true)
    public ResponseEntity<ReviewResponse> getMyReviewDetail(Long reviewId, Long memberId) {
        Review review = findActiveReview(reviewId);
        if (!review.getMember().getId().equals(memberId))
            throw new RuntimeException("조회 권한이 없습니다.");
        List<ReviewImage> images = reviewImageRepository.findAllByReviewId(reviewId);
        long likeCount    = reviewReactionRepository.countByReviewIdAndReactionType(reviewId, ReactionType.LIKE);
        long dislikeCount = reviewReactionRepository.countByReviewIdAndReactionType(reviewId, ReactionType.DISLIKE);
        return ResponseEntity.ok(new ReviewResponse(review, images, likeCount, dislikeCount));
    }

    // ── 관리자 전체 리뷰 목록 ─────────────────────────────────────────────────
    @Transactional(readOnly = true)
    public ResponseEntity<Page<ReviewResponse>> getAllReviews(Pageable pageable) {
        return ResponseEntity.ok(
                reviewRepository.findAllByDeletedAtIsNull(pageable).map(ReviewResponse::new));
    }

    // ── 관리자 리뷰 상세 (이미지/반응 포함) ──────────────────────────────────
    @Transactional(readOnly = true)
    public ResponseEntity<ReviewResponse> getReviewDetailForAdmin(Long reviewId) {
        Review review = reviewRepository.findById(reviewId)
                .orElseThrow(() -> new RuntimeException("리뷰를 찾을 수 없습니다."));
        List<ReviewImage> images = reviewImageRepository.findAllByReviewId(reviewId);
        long likeCount    = reviewReactionRepository.countByReviewIdAndReactionType(reviewId, ReactionType.LIKE);
        long dislikeCount = reviewReactionRepository.countByReviewIdAndReactionType(reviewId, ReactionType.DISLIKE);
        return ResponseEntity.ok(new ReviewResponse(review, images, likeCount, dislikeCount));
    }

    // ── 관리자 리뷰 강제 삭제 soft delete ────────────────────────────────────
    public ResponseEntity<String> adminDeleteReview(Long reviewId) {
        Review review = reviewRepository.findById(reviewId)
                .orElseThrow(() -> new RuntimeException("리뷰를 찾을 수 없습니다."));
        review.setDeletedAt(LocalDateTime.now());
        return ResponseEntity.ok("리뷰가 삭제되었습니다.");
    }

    // ── 내부 유틸 ────────────────────────────────────────────────────────────

    private Review findActiveReview(Long reviewId) {
        Review review = reviewRepository.findById(reviewId)
                .orElseThrow(() -> new RuntimeException("리뷰를 찾을 수 없습니다."));
        if (review.getDeletedAt() != null)
            throw new RuntimeException("삭제된 리뷰입니다.");
        return review;
    }
}
