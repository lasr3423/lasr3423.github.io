package com.bookstore.shop.readme.controller.admin;

import com.bookstore.shop.readme.domain.Review;
import com.bookstore.shop.readme.dto.ReviewRequest;
import com.bookstore.shop.readme.service.ReviewService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/admin/review")
@RequiredArgsConstructor
public class AdminReviewController {

    private final ReviewService reviewService;

    // [설계서 1-2] 관리자용 전체 조회 (Pageable 정렬 포함)
    @GetMapping
    public ResponseEntity<Page<Review>> getReviewList(Pageable pageable) {
        return ResponseEntity.ok(reviewService.getAllReviewsForAdmin(pageable));
    }

    // [설계서 v1.3] 특정 리뷰 상세 (조회수 hits 증가 로직 포함 여부는 선택)
    @GetMapping("/{id}")
    public ResponseEntity<Review> getReviewDetail(@PathVariable Long id) {
        return ResponseEntity.ok(reviewService.getReviewDetail(id));
    }

    // [설계서 14번 테이블 제약 준수] rating 범위(1~5) 검증 포함
    @PatchMapping("/{id}")
    public ResponseEntity<String> updateReview(@PathVariable Long id, @RequestBody ReviewRequest request) {
        reviewService.updateReviewByAdmin(id, request.getContent(), request.getRating());
        return ResponseEntity.ok("관리자 권한으로 리뷰가 수정되었습니다.");
    }
}