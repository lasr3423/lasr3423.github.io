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
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/api/review")
@RequiredArgsConstructor
public class ReviewApiController {

    private final ReviewService reviewService;

    @GetMapping("/recent")
    public ResponseEntity<Page<ReviewResponse>> getRecentReviews(
            @PageableDefault(size = 5, sort = "createdAt", direction = Sort.Direction.DESC)
            Pageable pageable
    ) {
        return reviewService.getRecentReviews(pageable);
    }

    @GetMapping
    public ResponseEntity<Page<ReviewResponse>> getReviews(
            @RequestParam Long productId,
            @PageableDefault(size = 10, sort = "createdAt", direction = Sort.Direction.DESC)
            Pageable pageable
    ) {
        return reviewService.getReviews(productId, pageable);
    }

    @GetMapping("/{reviewId}")
    public ResponseEntity<ReviewResponse> getReviewDetail(@PathVariable Long reviewId) {
        return reviewService.getReviewDetail(reviewId);
    }

    @PostMapping
    public ResponseEntity<Long> createReview(
            @RequestBody ReviewCreateRequest request,
            @AuthenticationPrincipal CustomUserDetails user
    ) {
        return reviewService.createReview(request, user.getMemberId());
    }

    @PutMapping("/{reviewId}")
    public ResponseEntity<ReviewResponse> updateReview(
            @PathVariable Long reviewId,
            @RequestBody ReviewUpdateRequest request,
            @AuthenticationPrincipal CustomUserDetails user
    ) {
        return reviewService.updateReview(reviewId, request, user.getMemberId());
    }

    @DeleteMapping("/{reviewId}")
    public ResponseEntity<String> deleteReview(
            @PathVariable Long reviewId,
            @AuthenticationPrincipal CustomUserDetails user
    ) {
        return reviewService.deleteReview(reviewId, user.getMemberId());
    }

    @PostMapping("/{reviewId}/reaction")
    public ResponseEntity<String> toggleReaction(
            @PathVariable Long reviewId,
            @RequestBody Map<String, String> body,
            @AuthenticationPrincipal CustomUserDetails user
    ) {
        return reviewService.toggleReaction(reviewId, body.get("reactionType"), user.getMemberId());
    }
}
