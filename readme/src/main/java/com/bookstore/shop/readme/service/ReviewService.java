package com.bookstore.shop.readme.service;

import com.bookstore.shop.readme.domain.Member;
import com.bookstore.shop.readme.domain.Product;
import com.bookstore.shop.readme.domain.Review;
import com.bookstore.shop.readme.domain.ReviewImage;
import com.bookstore.shop.readme.dto.ReviewRequestDto;
import com.bookstore.shop.readme.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ReviewService {
    private final ReviewRepository reviewRepository;
    private final ReviewImageRepository reviewImageRepository; // 프로젝트 구조도 반영
    private final OrderItemRepository orderItemRepository;
    private final MemberRepository memberRepository;
    private final ProductRepository productRepository;

    @Transactional // UC-M-015: 상품 리뷰 작성 [cite: 531, 1353]
    public void writeReview(ReviewRequestDto dto, Long memberId, List<String> imageUrls) {
        // 1. 구매 확정 여부 확인 (order_item JOIN order) [cite: 1348, 1349]
        orderItemRepository.findDeliveredItemByMemberAndProduct(memberId, dto.productId())
                .orElseThrow(() -> new RuntimeException("리뷰 작성 권한이 없습니다."));

        // 2. 중복 리뷰 확인 [cite: 1352]
        if (reviewRepository.existsByMemberIdAndProductIdAndDeletedAtIsNull(memberId, dto.productId())) {
            throw new RuntimeException("이미 리뷰를 작성한 상품입니다.");
        }

        // 3. 리뷰 저장 (상세 로직 생략된 매핑 부분 처리) [cite: 1353]
        Member member = memberRepository.findById(memberId).orElseThrow();
        Product product = productRepository.findById(dto.productId()).orElseThrow();

        Review review = new Review();
        review.setMember(member);
        review.setProduct(product);
        review.setRating(dto.rating());
        review.setContent(dto.content());
        reviewRepository.save(review);

        // 4. 다중 이미지 저장 (UC-M-016: 최대 5장) [cite: 233, 505, 1354]
        if (imageUrls != null && !imageUrls.isEmpty()) {
            imageUrls.stream()
                    .limit(5)
                    .map(url -> new ReviewImage(review, url))
                    .forEach(reviewImageRepository::save);
        }

        // 5. 리뷰 작성 여부 플래그 업데이트 (is_reviewed = true) [cite: 437, 1355]
        orderItemRepository.updateIsReviewed(memberId, dto.productId(), true);
    }

    @Transactional // UC-A-012: 관리자 리뷰 삭제 [cite: 520, 1034]
    public void deleteReview(Long reviewId, Long memberId, String role) {
        Review review = reviewRepository.findById(reviewId)
                .orElseThrow(() -> new RuntimeException("리뷰를 찾을 수 없습니다."));

        // 권한 확인: 본인 또는 관리자(MANAGER/ADMIN) [cite: 1298, 1304]
        if (!review.getMember().getId().equals(memberId) && role.equals("USER")) {
            throw new RuntimeException("권한이 없습니다.");
        }

        review.setDeletedAt(LocalDateTime.now()); // Soft Delete (deleted_at 기록) [cite: 715, 1304]
    }
}