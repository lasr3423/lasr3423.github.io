package com.bookstore.shop.readme.domain;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.ColumnDefault;

@Entity
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Table(name = "order_item")
public class OrderItem extends BaseEntity {

    @ManyToOne
    @JoinColumn(name = "order_id")
    private Order order;

    @ManyToOne
    @JoinColumn(name = "product_id")
    private Product product;

    @Column(nullable = false, length = 300)
    private String productTitle;

    @Column(nullable = false, length = 200)
    private String productAuthor;

    @Column(nullable = false)
    @ColumnDefault("0")
    private int salePrice = 0;

    @Column(nullable = false)
    @ColumnDefault("1")
    private int quantity = 1;

    @Column(nullable = false)
    @ColumnDefault("0")
    private int itemTotal = 0;

    @Column(nullable = false)
    @ColumnDefault("false")
    private boolean isReviewed = false;


}
