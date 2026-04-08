package com.bookstore.shop.readme.domain;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Builder
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Table(name = "product")
public class Product extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "category_top_id", nullable = false)
    private CategoryTop categoryTop;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "category_sub_id", nullable = false)
    private CategorySub categorySub;

    @Column(nullable = false, length = 300)
    private String title;

    @Column(nullable = false, length = 200)
    private String author;

    @Column(length = 20)
    private String isbn;

    @Column(columnDefinition = "TEXT")
    private String description;

    @Column(nullable = false)
    @Builder.Default
    private int price = 0;

    @Column(nullable = false, precision = 5, scale = 2)
    @Builder.Default
    private BigDecimal discountRate = BigDecimal.ZERO;

    @Column(nullable = false)
    @Builder.Default
    private int salePrice = 0;

    @Column(nullable = false)
    @Builder.Default
    private int stock = 0;

    @Column(length = 500)
    private String thumbnail;

    @Column(nullable = false)
    @Builder.Default
    private int viewCount = 0;

    @Column(nullable = false)
    @Builder.Default
    private int salesCount = 0;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    @Builder.Default
    private ProductStatus productStatus = ProductStatus.ACTIVATE;

    @Column(name = "deleted_at")
    private LocalDateTime deletedAt;
}
