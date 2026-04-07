package com.bookstore.shop.readme.controller;

import com.bookstore.shop.readme.dto.request.ReviewCreateRequest;
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

// [수정] Repository 직접 주입 제거 → ReviewService 위임으로 변경
@RestController
@RequestMapping("/api/review")
@RequiredArgsConstructor
public class ReviewApiController {

    private final ReviewService reviewService;

    @GetMapping
    public ResponseEntity<Page<ReviewResponse>> getReviews(
            @RequestParam Long productId,
            @PageableDefault(size = 10, sort = "createdAt",
                    direction = Sort.Direction.DESC) Pageable pageable) {
        return reviewService.getReviews(productId, pageable);
    }

    @PostMapping
    public ResponseEntity<Long> createReview(
            @RequestBody ReviewCreateRequest req,
            @AuthenticationPrincipal CustomUserDetails user) {
        return reviewService.createReview(req, user.getMemberId());
    }

    @DeleteMapping("/{reviewId}")
    public ResponseEntity<String> deleteReview(
            @PathVariable Long reviewId,
            @AuthenticationPrincipal CustomUserDetails user) {
        return reviewService.deleteReview(reviewId, user.getMemberId());
    }
}
