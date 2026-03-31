package com.bookstore.shop.readme.controller;

import com.bookstore.shop.readme.dto.ReviewRequest;
import com.bookstore.shop.readme.service.ReviewService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/review")
@RequiredArgsConstructor
public class ReviewController {

    private final ReviewService reviewService;

    /**
     * [설계서 14, 15번 테이블 정의서 준수]
     * UC-M-015: 리뷰 등록
     * 반영: member_id(FK), product_id(FK), rating(INT), content(TEXT)
     */
    @PostMapping("/write")
    public ResponseEntity<String> writeReview(@RequestBody ReviewRequest request) {
        // v1.3 설계서 member.id 참조 (인증 연동 전 고정값 사용)
        Long memberId = 1L;

        // 서비스 호출 (내부적으로 rating BETWEEN 1 AND 5 제약조건 검증 포함)
        reviewService.writeReview(
                memberId,
                request.getProductId(), // product_id FK 매핑
                request.getContent(),   // TEXT content 매핑
                request.getRating(),    // INTEGER rating 매핑
                request.getImageUrls()  // 15번 테이블 review_image 연동
        );

        return ResponseEntity.status(HttpStatus.CREATED).body("리뷰가 등록되었습니다.");
    }
}