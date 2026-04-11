package com.bookstore.shop.readme.repository;

import com.bookstore.shop.readme.domain.OrderItem;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface OrderItemRepository extends JpaRepository<OrderItem, Long> {
    // 특정 주문에 있는 상품 조회
    List<OrderItem> findByOrderId(Long orderId);

    List<OrderItem> findByOrderIdIn(List<Long> orderIds);
}
