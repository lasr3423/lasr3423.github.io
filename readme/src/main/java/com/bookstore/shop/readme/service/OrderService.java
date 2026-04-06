package com.bookstore.shop.readme.service;

import com.bookstore.shop.readme.repository.CartItemRepository;
import com.bookstore.shop.readme.repository.CartRepository;
import com.bookstore.shop.readme.repository.OrderItemRepository;
import com.bookstore.shop.readme.repository.OrderRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class OrderService {
    private final CartRepository cartRepository;
    private final CartItemRepository cartItemRepository;
    private final OrderRepository orderRepository;
    private final OrderItemRepository orderItemRepository;

    /*
     * 장바구니 -> 주문 생성
     * 체크된 CartItem 목록을 받아서 Order + OrderItem 레코드 생성
     * 재고 확인 (PRoductStatus.ACTIVE + 수량)
     * totalPrice, discountAmount, finalPrice 계산 후 Order 저장
     * 성공 시 orderId 반환 -> 프론트에서 orderStore.setOrder() 호출에 사용
     */

}
