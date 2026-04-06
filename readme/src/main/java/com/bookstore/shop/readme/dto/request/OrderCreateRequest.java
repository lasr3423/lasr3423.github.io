package com.bookstore.shop.readme.dto.request;

import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;

@Getter
@NoArgsConstructor
public class OrderCreateRequest {

    private List<Long> cartItemIds;         // 체크된 장바구니 상품 ID 목록 - 이 항목들로 OrderItem 생성 됨

    private String deliveryAddress;         // 기본 주소 (필수)
    private String deliveryAddressDetail;   // 상세 주소 (선택)
}
