package com.bookstore.shop.readme.service;

import com.bookstore.shop.readme.domain.Member;
import com.bookstore.shop.readme.domain.Product;
import com.bookstore.shop.readme.domain.Review;
import com.bookstore.shop.readme.domain.ReviewImage;
import com.bookstore.shop.readme.repository.MemberRepository;
import com.bookstore.shop.readme.repository.ProductRepository;
import com.bookstore.shop.readme.repository.ReviewRepository;
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
    private final MemberRepository memberRepository;
    private final ProductRepository productRepository;

    /**
     * [설계서 14, 15번 테이블 준수] 리뷰 등록
     */
    @Transactional
    public void writeReview(Long memberId, Long productId, String content, Integer rating, List<String> imageUrls) {
        Member member = memberRepository.findById(memberId).orElseThrow();
        Product product = productRepository.findById(productId).orElseThrow();

        // 설계서 v1.3 중복 체크 (Soft Delete 제외)
        if (reviewRepository.existsByMemberIdAndProductIdAndDeletedAtIsNull(memberId, productId)) {
            throw new RuntimeException("이미 리뷰를 작성한 상품입니다.");
        }

        // 설계서 14번 CHECK (1~5) 제약 준수
        if (rating < 1 || rating > 5) {
            throw new IllegalArgumentException("평점은 1~5점 사이여야 합니다.");
        }

        Review review = new Review();
        review.setMember(member);
        review.setProduct(product);
        review.setContent(content);
        review.setRating(rating);
        review.setHits(0); // 설계서 기본값 0

        if (imageUrls != null) {
            imageUrls.stream().limit(5).forEach(url -> {
                ReviewImage img = new ReviewImage();
                img.setReview(review);
                img.setImageUrl(url);
                review.getImages().add(img);
            });
        }
        reviewRepository.save(review);
    }

    /**
     * [설계서 v1.3 제약준수] 리뷰 상세 조회 (Soft Delete 체크 + hits 증가)
     */
    @Transactional
    public Review getReviewDetail(Long id) {
        Review review = reviewRepository.findByIdAndDeletedAtIsNull(id)
                .orElseThrow(() -> new RuntimeException("리뷰를 찾을 수 없습니다."));

        review.setHits(review.getHits() + 1); // 설계서 hits(조회수) 증가
        return review;
    }

    /**
     * [에러 해결] AdminReviewController에서 호출하는 관리자용 목록 조회 메서드
     * 설계서 v1.3 인덱스 전략에 맞춰 페이징 반환
     */
    public Page<Review> getAllReviewsForAdmin(Pageable pageable) {
        return reviewRepository.findAll(pageable);
    }

    /**
     * [설계서 14번 테이블 명세 준수] 관리자 리뷰 수정
     */
    @Transactional
    public void updateReviewByAdmin(Long id, String content, Integer rating) {
        Review review = reviewRepository.findByIdAndDeletedAtIsNull(id)
                .orElseThrow(() -> new RuntimeException("리뷰를 찾을 수 없습니다."));

        if (rating != null && (rating < 1 || rating > 5)) {
            throw new IllegalArgumentException("평점은 1에서 5 사이여야 합니다.");
        }

        review.setContent(content);
        review.setRating(rating);
    }

    /**
     * [설계서 Soft Delete 준수] 관리자 리뷰 삭제
     */
    @Transactional
    public void deleteReviewByAdmin(Long id) {
        Review review = reviewRepository.findByIdAndDeletedAtIsNull(id)
                .orElseThrow(() -> new RuntimeException("이미 삭제되었거나 존재하지 않는 리뷰입니다."));

        review.setDeletedAt(LocalDateTime.now());
    }
}