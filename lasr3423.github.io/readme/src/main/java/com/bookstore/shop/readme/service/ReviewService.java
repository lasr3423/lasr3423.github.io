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
     * [설계서 2-6] 리뷰 작성 로직
     * 제약조건: 중복 작성 방지, 이미지 최대 5장, Soft Delete 고려
     */
    @Transactional
    public void writeReview(Long memberId, Long productId, String content, Integer rating, List<String> imageUrls) {
        Member member = memberRepository.findById(memberId).orElseThrow();
        Product product = productRepository.findById(productId).orElseThrow();

        // 중복 체크 (Soft Delete된 리뷰 제외)
        if (reviewRepository.existsByMemberIdAndProductIdAndDeletedAtIsNull(memberId, productId)) {
            throw new RuntimeException("이미 리뷰를 작성하셨습니다.");
        }

        Review review = new Review();
        review.setMember(member);
        review.setProduct(product);
        review.setContent(content);
        review.setRating(rating);

        // 이미지 첨부 (최대 5장 제한)
        if (imageUrls != null && !imageUrls.isEmpty()) {
            imageUrls.stream()
                    .limit(5)
                    .forEach(url -> {
                        ReviewImage img = new ReviewImage();
                        img.setReview(review);
                        img.setImageUrl(url);
                        review.getImages().add(img);
                    });
        }
        reviewRepository.save(review);
    }

    // [관리자] 전체 리뷰 목록 조회
    public Page<Review> getAllReviewsForAdmin(Pageable pageable) {
        return reviewRepository.findAll(pageable);
    }

    // [관리자/사용자] 특정 리뷰 상세 조회
    public Review getReviewDetail(Long id) {
        return reviewRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("리뷰를 찾을 수 없습니다."));
    }

    // [관리자] 리뷰 수정
    @Transactional
    public void updateReviewByAdmin(Long id, String content, Integer rating) {
        Review review = getReviewDetail(id);
        review.setContent(content);
        review.setRating(rating);
    }

    // [관리자] 리뷰 삭제 (Soft Delete 제약조건)
    @Transactional
    public void deleteReviewByAdmin(Long id) {
        Review review = getReviewDetail(id);
        review.setDeletedAt(LocalDateTime.now());
    }
}