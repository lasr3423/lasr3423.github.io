package com.bookstore.shop.readme.domain;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.ColumnDefault;

@Entity
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Table(name = "order_item")
public class OrderItem extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "order_id")
    private Order order;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "product_id")
    private Product product;

    @Column(nullable = false, length = 300)
    private String productTitle;

    @Column(nullable = false, length = 200)
    private String productAuthor;

    @Column(nullable = false)
    @Builder.Default
    private int salePrice = 0;

    @Column(nullable = false)
    @Builder.Default
    private int quantity = 1;

    @Column(nullable = false)
    @Builder.Default
    private int itemTotal = 0;

    @Column(nullable = false)
    @Builder.Default
    private boolean isReviewed = false;


}
