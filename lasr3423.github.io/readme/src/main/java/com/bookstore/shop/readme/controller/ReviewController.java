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

    @PostMapping("/write")
    public ResponseEntity<String> writeReview(@RequestBody ReviewRequest request) {
        Long memberId = 1L; // 추후 인증 정보에서 추출

        reviewService.writeReview(
                memberId,
                request.getProductId(), // 수정됨: productId 전달 추가
                request.getContent(),
                request.getRating(),
                request.getImageUrls()
        );

        return ResponseEntity.status(HttpStatus.CREATED).body("리뷰가 등록되었습니다.");
    }
}