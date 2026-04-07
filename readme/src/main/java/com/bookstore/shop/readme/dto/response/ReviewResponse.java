package com.bookstore.shop.readme.dto.response;

import com.bookstore.shop.readme.domain.Review;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
public class ReviewResponse {

    private final Long id;
    private final Long productId;
    private final String productTitle;
    private final Long memberId;
    private final String memberName;
    private final int rating;
    private final String content;
    private final LocalDateTime createdAt;

    public ReviewResponse(Review review) {
        this.id           = review.getId();
        this.productId    = review.getProduct().getId();
        this.productTitle = review.getProduct().getTitle();
        this.memberId     = review.getMember().getId();
        this.memberName   = review.getMember().getName();
        this.rating       = review.getRating();
        this.content      = review.getContent();
        this.createdAt    = review.getCreatedAt();
    }
}
