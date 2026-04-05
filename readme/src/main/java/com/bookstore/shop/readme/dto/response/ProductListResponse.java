package com.bookstore.shop.readme.dto.response;

import com.bookstore.shop.readme.domain.Product;
import lombok.Getter;

import java.math.BigDecimal;

@Getter
public class ProductListResponse {

    private final Long id;
    private final String title;
    private final String author;
    private final int price;
    private final int salePrice;
    private final BigDecimal discountRate;
    private final String thumbnail;
    private final int salesCount;

    public ProductListResponse(Product product) {
        this.id = product.getId();
        this.title = product.getTitle();
        this.author = product.getAuthor();
        this.price = product.getPrice();
        this.salePrice = product.getSalePrice();
        this.discountRate = product.getDiscountRate();
        this.thumbnail = product.getThumbnail();
        this.salesCount = product.getSalesCount();
    }
}
