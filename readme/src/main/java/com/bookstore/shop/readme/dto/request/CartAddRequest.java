package com.bookstore.shop.readme.dto.request;

import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class CartAddRequest {
    private Long productId;     // cart에 담을 상품 Id
    private int quantity;       // 담을 수량
}
