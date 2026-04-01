package com.bookstore.shop.readme.repository;

import com.bookstore.shop.readme.domain.Review;
import com.bookstore.shop.readme.domain.ReviewImage;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface ReviewRepository extends JpaRepository<Review, Long> {
    // 본인 작성 리뷰 목록 조회 (마이페이지용) [cite: 383, 1309]
    Page<Review> findByMemberIdAndDeletedAtIsNull(Long memberId, Pageable pageable);

    // 중복 리뷰 확인 [cite: 503, 1309]
    boolean existsByMemberIdAndProductIdAndDeletedAtIsNull(Long memberId, Long productId);

    // 관리자용: 전체 리뷰 및 연관 상품명 목록 조회 [cite: 320, 321]
    @Query("SELECT r FROM Review r JOIN FETCH r.product p WHERE r.deletedAt IS NULL")
    Page<Review> findAllWithProduct(Pageable pageable);
}