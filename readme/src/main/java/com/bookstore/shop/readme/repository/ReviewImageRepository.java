package com.bookstore.shop.readme.repository;

import com.bookstore.shop.readme.domain.ReviewImage;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface ReviewImageRepository extends JpaRepository<ReviewImage, Long> {

    // 특정 리뷰에 속한 모든 이미지 조회 (클래스 다이어그램 명세)
    List<ReviewImage> findByReviewId(Long reviewId);

    // 리뷰 삭제 시 관련 이미지 일괄 삭제 (시퀀스 다이어그램 명세) [cite: 1312, 1354]
    void deleteByReviewId(Long reviewId);
}