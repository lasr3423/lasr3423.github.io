package com.bookstore.shop.readme.dto.response;

import com.bookstore.shop.readme.domain.Review;
import com.bookstore.shop.readme.domain.ReviewImage;
import lombok.Getter;

import java.time.LocalDateTime;
import java.util.List;

@Getter
public class ReviewResponse {

    private final Long id;
    private final Long reviewId;
    private final Long productId;
    private final String productTitle;
    private final Long memberId;
    private final String memberName;
    private final int rating;
    private final String content;
    private final int hits;
    private final List<String> imageUrls;
    private final long likeCount;
    private final long dislikeCount;
    private final LocalDateTime createdAt;
    private final LocalDateTime updatedAt;

    public ReviewResponse(Review review) {
        this.id = review.getId();
        this.reviewId = review.getId();
        this.productId = review.getProduct().getId();
        this.productTitle = review.getProduct().getTitle();
        this.memberId = review.getMember().getId();
        this.memberName = review.getMember().getName();
        this.rating = review.getRating();
        this.content = review.getContent();
        this.hits = review.getHits();
        this.imageUrls = List.of();
        this.likeCount = 0;
        this.dislikeCount = 0;
        this.createdAt = review.getCreatedAt();
        this.updatedAt = review.getUpdatedAt();
    }

    public ReviewResponse(Review review, List<ReviewImage> images, long likeCount, long dislikeCount) {
        this.id = review.getId();
        this.reviewId = review.getId();
        this.productId = review.getProduct().getId();
        this.productTitle = review.getProduct().getTitle();
        this.memberId = review.getMember().getId();
        this.memberName = review.getMember().getName();
        this.rating = review.getRating();
        this.content = review.getContent();
        this.hits = review.getHits();
        this.imageUrls = images.stream().map(ReviewImage::getImageUrl).toList();
        this.likeCount = likeCount;
        this.dislikeCount = dislikeCount;
        this.createdAt = review.getCreatedAt();
        this.updatedAt = review.getUpdatedAt();
    }
}
