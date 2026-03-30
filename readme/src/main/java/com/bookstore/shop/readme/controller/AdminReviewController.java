package com.bookstore.shop.readme.controller;

import com.bookstore.shop.readme.dto.ReviewRequest;
import com.bookstore.shop.readme.service.ReviewService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/admin/review")
@RequiredArgsConstructor
public class AdminReviewController {

    private final ReviewService reviewService;

    @GetMapping
    public ResponseEntity<?> getReviewList(Pageable pageable) {
        return ResponseEntity.ok(reviewService.getAllReviewsForAdmin(pageable));
    }

    @GetMapping("/{id}")
    public ResponseEntity<?> getReviewDetail(@PathVariable Long id) {
        return ResponseEntity.ok(reviewService.getReviewDetail(id));
    }

    @PatchMapping("/{id}")
    public ResponseEntity<String> updateReview(@PathVariable Long id, @RequestBody ReviewRequest request) {
        reviewService.updateReviewByAdmin(id, request.getContent(), request.getRating());
        return ResponseEntity.ok("관리자 권한으로 리뷰가 수정되었습니다.");
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<String> deleteReview(@PathVariable Long id) {
        reviewService.deleteReviewByAdmin(id);
        return ResponseEntity.ok("관리자 권한으로 리뷰가 삭제되었습니다.");
    }
}