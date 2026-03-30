package com.bookstore.shop.readme.repository;

import com.bookstore.shop.readme.domain.Review;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface ReviewRepository extends JpaRepository<Review, Long> {
    // 설계서: 상품별 리뷰 최신순 조회
    Page<Review> findByProductIdAndDeletedAtIsNull(Long productId, Pageable pageable);
}