package com.bookstore.shop.readme.service;

import com.bookstore.shop.readme.domain.*;
import com.bookstore.shop.readme.repository.*;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@Transactional
class OrderServiceIntegrationTest {

    @Autowired
    private OrderService orderService;

    @Autowired
    private MemberRepository memberRepository;

    @Autowired
    private CategoryTopRepository categoryTopRepository;

    @Autowired
    private CategorySubRepository categorySubRepository;

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private OrderRepository orderRepository;

    @Autowired
    private DeliveryRepository deliveryRepository;

    @Test
    void updateOrderStatusCreatesDeliveryWhenApproved() {
        Member member = new Member();
        member.setEmail("integration-order@test.local");
        member.setPassword("encoded-password");
        member.setName("Integration User");
        member.setPhone("010-9999-9999");
        member.setAddress("Seoul Test Address");
        member.setMemberRole(MemberRole.USER);
        member.setMemberStatus(MemberStatus.ACTIVATE);
        member.setProvider(AuthProvider.LOCAL);
        member.setMarketingAgreed(Boolean.TRUE);
        member = memberRepository.save(member);

        CategoryTop top = CategoryTop.builder()
                .name("테스트 상위 카테고리")
                .sortOrder(99)
                .categoryStatus(CategoryStatus.ACTIVATE)
                .build();
        top = categoryTopRepository.save(top);

        CategorySub sub = CategorySub.builder()
                .categoryTop(top)
                .name("테스트 하위 카테고리")
                .sortOrder(99)
                .categoryStatus(CategoryStatus.ACTIVATE)
                .build();
        sub = categorySubRepository.save(sub);

        Product product = Product.builder()
                .categoryTop(top)
                .categorySub(sub)
                .title("통합 테스트 도서")
                .author("테스트 저자")
                .description("주문 승인 테스트용 도서")
                .price(20000)
                .discountRate(java.math.BigDecimal.ZERO)
                .salePrice(18000)
                .stock(100)
                .productStatus(ProductStatus.ACTIVATE)
                .build();
        product = productRepository.save(product);

        Order order = Order.builder()
                .member(member)
                .number("INTEGRATION-ORDER-001")
                .orderStatus(OrderStatus.PAYED)
                .totalPrice(product.getSalePrice())
                .discountAmount(0)
                .finalPrice(product.getSalePrice())
                .receiverName(member.getName())
                .receiverPhone(member.getPhone())
                .deliveryAddress(member.getAddress())
                .deliveryAddressDetail("상세 주소")
                .deliveryZipCode("00000")
                .deliveryMemo("테스트 메모")
                .build();
        order = orderRepository.save(order);

        assertThat(deliveryRepository.findByOrderId(order.getId())).isEmpty();

        orderService.updateOrderStatus(order.getId(), OrderStatus.APPROVAL.name());

        Delivery delivery = deliveryRepository.findByOrderId(order.getId()).orElse(null);
        assertThat(delivery).isNotNull();
        assertThat(delivery.getDeliveryStatus()).isEqualTo(DeliveryStatus.READY);
    }
}
