package com.bookstore.shop.readme.dto.response;

import com.bookstore.shop.readme.domain.Product;
import lombok.Getter;

import java.math.BigDecimal;
import java.time.LocalDateTime;

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
    private final int stock;
    private final LocalDateTime createdAt;
    private final Long categoryTopId;
    private final String categoryTopName;
    private final Long categorySubId;
    private final String categorySubName;


    public ProductListResponse(Product product) {
        this.id = product.getId();
        this.title = product.getTitle();
        this.author = product.getAuthor();
        this.price = product.getPrice();
        this.salePrice = product.getSalePrice();
        this.discountRate = product.getDiscountRate();
        this.thumbnail = product.getThumbnail();
        this.salesCount = product.getSalesCount();
        this.stock = product.getStock();
        this.createdAt = product.getCreatedAt();
        this.categoryTopId = product.getCategoryTop().getId();
        this.categoryTopName = product.getCategoryTop().getName();
        this.categorySubId = product.getCategorySub().getId();
        this.categorySubName = product.getCategorySub().getName();
    }
}
