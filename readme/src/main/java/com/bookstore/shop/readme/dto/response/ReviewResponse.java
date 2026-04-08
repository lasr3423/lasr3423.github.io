package com.bookstore.shop.readme.dto.response;

import com.bookstore.shop.readme.domain.Review;
import com.bookstore.shop.readme.domain.ReviewImage;
import lombok.Getter;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 리뷰 응답 DTO — DB설계서 기준
 * hits: 조회수 (review.hits)
 * imageUrls: 리뷰 이미지 URL 목록 (review_image 테이블, 최대 5장)
 * likeCount / dislikeCount: 반응 수 (review_reaction 테이블)
 */
@Getter
public class ReviewResponse {

    private final Long   id;
    private final Long   productId;
    private final String productTitle;
    private final Long   memberId;
    private final String memberName;
    private final int    rating;
    private final String content;
    private final int    hits;          // 조회수 — DB설계서 review.hits
    private final List<String> imageUrls; // 리뷰 이미지 URL 목록
    private final long   likeCount;     // 좋아요 수 (review_reaction LIKE)
    private final long   dislikeCount;  // 싫어요 수 (review_reaction DISLIKE)
    private final LocalDateTime createdAt;
    private final LocalDateTime updatedAt;

    /**
     * 기본 생성자 — imageUrls / likeCount / dislikeCount 없이 조회할 때 사용
     * (목록 조회 등 성능 최적화 필요 시)
     */
    public ReviewResponse(Review review) {
        this.id           = review.getId();
        this.productId    = review.getProduct().getId();
        this.productTitle = review.getProduct().getTitle();
        this.memberId     = review.getMember().getId();
        this.memberName   = review.getMember().getName();
        this.rating       = review.getRating();
        this.content      = review.getContent();
        this.hits         = review.getHits();
        this.imageUrls    = List.of(); // 목록 조회 시 기본 빈 리스트 (N+1 방지)
        this.likeCount    = 0;
        this.dislikeCount = 0;
        this.createdAt    = review.getCreatedAt();
        this.updatedAt    = review.getUpdatedAt();
    }

    /**
     * 상세 생성자 — 이미지/반응 정보 포함 (상세 조회 시 사용)
     */
    public ReviewResponse(Review review, List<ReviewImage> images, long likeCount, long dislikeCount) {
        this.id           = review.getId();
        this.productId    = review.getProduct().getId();
        this.productTitle = review.getProduct().getTitle();
        this.memberId     = review.getMember().getId();
        this.memberName   = review.getMember().getName();
        this.rating       = review.getRating();
        this.content      = review.getContent();
        this.hits         = review.getHits();
        this.imageUrls    = images.stream().map(ReviewImage::getImageUrl).toList();
        this.likeCount    = likeCount;
        this.dislikeCount = dislikeCount;
        this.createdAt    = review.getCreatedAt();
        this.updatedAt    = review.getUpdatedAt();
    }
}
