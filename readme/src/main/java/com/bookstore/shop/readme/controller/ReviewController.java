package com.bookstore.shop.readme.controller;

import com.bookstore.shop.readme.domain.Review;
import com.bookstore.shop.readme.dto.ReviewRequestDto;
import com.bookstore.shop.readme.security.CustomUserDetails;
import com.bookstore.shop.readme.service.ReviewService;
import com.bookstore.shop.readme.util.SecurityUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/review")
@RequiredArgsConstructor
public class ReviewController {
    private final ReviewService reviewService;

    @PostMapping("/write") // FR-002: 리뷰 작성 [cite: 502]
    public ResponseEntity<?> writeReview(@RequestBody ReviewRequestDto dto,
                                         @AuthenticationPrincipal CustomUserDetails user) {
        reviewService.writeReview(dto, user.getMemberId(), null);
        return ResponseEntity.status(HttpStatus.CREATED).body("리뷰가 등록되었습니다.");
    }
}