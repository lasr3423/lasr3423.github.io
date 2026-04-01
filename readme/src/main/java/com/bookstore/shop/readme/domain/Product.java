package com.bookstore.shop.readme.domain;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.ColumnDefault;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity @Builder
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Table(name = "product")
public class Product extends BaseEntity {

    @ManyToOne
    @JoinColumn(name = "category_top_id", nullable = false)
    private CategoryTop categoryTop;    // 카테고리 대분류

    @ManyToOne
    @JoinColumn(name = "category_sub_id", nullable = false)
    private CategorySub categorySub;    // 카테고리 소분류

    @Column(nullable = false, length = 300)
    private String title;   // 도서명(상품명)

    @Column(nullable = false, length = 200)
    private String author;  // 저자(글쓴이)

    @Column(columnDefinition = "TEXT")
    private String description; // 도서 설명

    @Column(nullable = false)
    @ColumnDefault("0")
    @Builder.Default
    private int price = 0;  // 가격(정가)

    @Column(nullable = false, precision = 5, scale = 2)
    @ColumnDefault("0.00")
    @Builder.Default
    private BigDecimal discountRate = BigDecimal.ZERO;  // 할인율(%)

    @Column(nullable = false)
    @ColumnDefault("0")
    @Builder.Default
    private int salePrice = 0;  // 판매가

    @Column(nullable = false)
    @ColumnDefault("0")
    @Builder.Default
    private int stock = 0;  // 재고 수량

    @Column(length = 500)
    private String thumbnail;   // 대표 이미지 경로

    @Column(nullable = false)
    @ColumnDefault("0")
    @Builder.Default
    private int viewCount = 0;  // 조회수

    @Column(nullable = false)
    @ColumnDefault("0")
    @Builder.Default
    private int salesCount = 0; // 판매량

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    @ColumnDefault("'ACTIVATE'")
    @Builder.Default
    private ProductStatus productStatus = ProductStatus.ACTIVATE;   // 책 상태?

    @Column(name = "deleted_at")
    private LocalDateTime deletedAt;

}