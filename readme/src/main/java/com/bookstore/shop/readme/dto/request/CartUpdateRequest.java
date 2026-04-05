package com.bookstore.shop.readme.dto.request;

import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class CartUpdateRequest {
    private int quantity;   // 변경할 수량 (0 이하일 땐 삭제 처리 하기)
}
