package com.bookstore.shop.readme.repository;

import com.bookstore.shop.readme.domain.Review;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ReviewRepository extends JpaRepository<Review, Long> {

    // 특정 상품의 리뷰 목록
    Page<Review> findAllByProductIdAndDeletedAtIsNull(Long productId, Pageable pageable);

    // 특정 회원의 리뷰 목록
    Page<Review> findAllByMemberIdAndDeletedAtIsNull(Long memberId, Pageable pageable);

    // 관리자 전체 리뷰 목록 (삭제 제외)
    Page<Review> findAllByDeletedAtIsNull(Pageable pageable);
}
