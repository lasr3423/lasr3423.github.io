package com.bookstore.shop.readme.dto.response;

import com.bookstore.shop.readme.domain.Product;
import lombok.Getter;

import java.math.BigDecimal;

@Getter
public class ProductDetailResponse {

    private final Long id;
    private final String title;
    private final String author;
    private final String description;
    private final int price;
    private final int salePrice;
    private final BigDecimal discountRate;
    private final int stock;
    private final String thumbnail;
    private final int viewCount;
    private final int salesCount;


    public ProductDetailResponse(Product product) {
        this.id = product.getId();
        this.title = product.getTitle();
        this.author = product.getAuthor();
        this.description = product.getDescription();
        this.price = product.getPrice();
        this.salePrice = normalizeSalePrice(product);
        this.discountRate = product.getDiscountRate();
        this.stock = product.getStock();
        this.thumbnail = product.getThumbnail();
        this.viewCount = product.getViewCount();
        this.salesCount = product.getSalesCount();
    }

    private int normalizeSalePrice(Product product) {
        if (product.getSalePrice() >= 0) {
            return product.getSalePrice();
        }
        if (product.getPrice() <= 0) {
            return 0;
        }
        BigDecimal rate = product.getDiscountRate() == null ? BigDecimal.ZERO : product.getDiscountRate();
        if (rate.compareTo(BigDecimal.ZERO) <= 0) {
            return product.getPrice();
        }
        BigDecimal ratio = BigDecimal.ONE.subtract(rate.divide(BigDecimal.valueOf(100)));
        return Math.max(0, ratio.multiply(BigDecimal.valueOf(product.getPrice())).intValue());
    }
}
