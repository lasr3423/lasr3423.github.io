package com.bookstore.shop.readme.repository;

import com.bookstore.shop.readme.domain.ReviewImage;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ReviewImageRepository extends JpaRepository<ReviewImage, Long> {

    /** 특정 리뷰의 이미지 목록 (등록 순) */
    List<ReviewImage> findAllByReviewId(Long reviewId);

    /** 특정 리뷰의 이미지 전체 삭제 (리뷰 수정 시 기존 이미지 교체용) */
    void deleteAllByReviewId(Long reviewId);
}
