package com.bookstore.shop.readme.controller;

import com.bookstore.shop.readme.dto.request.ReviewCreateRequest;
import com.bookstore.shop.readme.dto.request.ReviewUpdateRequest;
import com.bookstore.shop.readme.dto.response.ReviewResponse;
import com.bookstore.shop.readme.security.CustomUserDetails;
import com.bookstore.shop.readme.service.ReviewService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/review")
@RequiredArgsConstructor
public class ReviewApiController {

    private final ReviewService reviewService;

    // 상품 페이지 리뷰 목록 — REQ-R-001
    @GetMapping
    public ResponseEntity<Page<ReviewResponse>> getReviews(
            @RequestParam Long productId,
            @PageableDefault(size = 10, sort = "createdAt",
                    direction = Sort.Direction.DESC) Pageable pageable) {
        return reviewService.getReviews(productId, pageable);
    }

    // 리뷰 상세 조회 (hits 자동 증가) — REQ-R-005
    @GetMapping("/{reviewId}")
    public ResponseEntity<ReviewResponse> getReviewDetail(@PathVariable Long reviewId) {
        return reviewService.getReviewDetail(reviewId);
    }

    // 리뷰 등록 — REQ-R-002
    @PostMapping
    public ResponseEntity<Long> createReview(
            @RequestBody ReviewCreateRequest req,
            @AuthenticationPrincipal CustomUserDetails user) {
        return reviewService.createReview(req, user.getMemberId());
    }

    // 리뷰 수정 — REQ-M-018
    @PutMapping("/{reviewId}")
    public ResponseEntity<ReviewResponse> updateReview(
            @PathVariable Long reviewId,
            @RequestBody ReviewUpdateRequest req,
            @AuthenticationPrincipal CustomUserDetails user) {
        return reviewService.updateReview(reviewId, req, user.getMemberId());
    }

    // 리뷰 삭제 — REQ-M-019
    @DeleteMapping("/{reviewId}")
    public ResponseEntity<String> deleteReview(
            @PathVariable Long reviewId,
            @AuthenticationPrincipal CustomUserDetails user) {
        return reviewService.deleteReview(reviewId, user.getMemberId());
    }

    // 리뷰 반응 토글 (좋아요/싫어요) — REQ-R-004
    // body: { "reactionType": "LIKE" } 또는 { "reactionType": "DISLIKE" }
    @PostMapping("/{reviewId}/reaction")
    public ResponseEntity<String> toggleReaction(
            @PathVariable Long reviewId,
            @RequestBody Map<String, String> body,
            @AuthenticationPrincipal CustomUserDetails user) {
        return reviewService.toggleReaction(reviewId, body.get("reactionType"), user.getMemberId());
    }
}
