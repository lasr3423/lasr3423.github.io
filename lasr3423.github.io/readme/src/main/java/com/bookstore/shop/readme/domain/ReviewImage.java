package com.bookstore.shop.readme.domain;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;


@Getter
@Setter
@Table(name = "review_image")
public class ReviewImage extends BaseEntity {
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "review_id")
    private Review review;

    @Column(name = "image_url")
    private String imageUrl;
}