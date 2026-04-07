package com.bookstore.shop.readme.dto.request;

import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;

@Getter
@NoArgsConstructor
public class OrderCreateRequest {

    // 체크된 장바구니 상품 ID 목록 - 이 항목들로 OrderItem 생성 됨
    private List<Long> cartItemIds;

    // 수신자 정보 — Order 엔티티의 receiverName, receiverPhone 에 저장됨
    private String receiverName;    // 수령인 이름
    private String receiverPhone;   // 수령인 연락처

    // 배송지 정보 — Order 엔티티의 deliveryAddress 등에 저장됨
    private String deliveryAddress;         // 기본 주소 (필수)
    private String deliveryAddressDetail;   // 상세 주소 (선택)
    private String deliveryZipCode;         // 우편 번호 (필수)
    private String deliveryMemo;            // 배송 메모 (선택)


}
