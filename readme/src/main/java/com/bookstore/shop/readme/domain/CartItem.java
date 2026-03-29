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
@Table(name = "cart_item")
public class CartItem extends BaseEntity {

    @ManyToOne
    @JoinColumn(name = "cart_id")
    private Cart cart;

    @ManyToOne
    @JoinColumn(name = "product_id")
    private Product product;

    @Column(nullable = false)
    @ColumnDefault("1")
    private int quantity = 1;

    @Column(nullable = false)
    @ColumnDefault("true")
    private boolean isChecked = true;

}
