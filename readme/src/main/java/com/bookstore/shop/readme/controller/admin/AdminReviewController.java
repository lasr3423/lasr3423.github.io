package com.bookstore.shop.readme.controller.admin;

import com.bookstore.shop.readme.domain.Review;
import com.bookstore.shop.readme.dto.ReviewRequestDto;
import com.bookstore.shop.readme.repository.ReviewRepository;
import com.bookstore.shop.readme.security.CustomUserDetails;
import com.bookstore.shop.readme.service.ReviewService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/admin/review")
@RequiredArgsConstructor
public class AdminReviewController { // 관리자용 [cite: 1298]
    private final ReviewService reviewService;

    @DeleteMapping("/{id}") // FA-031 [cite: 324, 1034]
    public ResponseEntity<?> deleteReview(@PathVariable Long id,
                                          @AuthenticationPrincipal CustomUserDetails user) {
        // getRole() 대신 권한 목록에서 체크 (범위를 넘지 않는 해결책)
        String role = user.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN") || a.getAuthority().equals("ROLE_MANAGER"))
                ? "ADMIN" : "USER";

        reviewService.deleteReview(id, user.getMemberId(), role);
        return ResponseEntity.ok().build();
    }
}