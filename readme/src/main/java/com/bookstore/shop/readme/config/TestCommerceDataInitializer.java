package com.bookstore.shop.readme.config;

import com.bookstore.shop.readme.domain.*;
import com.bookstore.shop.readme.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Component
@org.springframework.core.annotation.Order(2)
@RequiredArgsConstructor
public class TestCommerceDataInitializer implements CommandLineRunner {

    private static final int SEED_PRODUCT_COUNT = 10;

    private final MemberRepository memberRepository;
    private final CategoryTopRepository categoryTopRepository;
    private final CategorySubRepository categorySubRepository;
    private final ProductRepository productRepository;
    private final OrderRepository orderRepository;
    private final OrderItemRepository orderItemRepository;
    private final PaymentRepository paymentRepository;
    private final DeliveryRepository deliveryRepository;
    private final NoticeRepository noticeRepository;
    private final QnARepository qnaRepository;
    private final ReviewRepository reviewRepository;

    @Override
    @Transactional
    public void run(String... args) {
        if (orderRepository.findByNumber("SEED-ORDER-001").isPresent()) {
            return;
        }

        Member admin = memberRepository.findByEmail("admin1@readme.test")
                .orElseThrow(() -> new IllegalStateException("Seed admin member is missing."));

        List<Member> users = loadSeedUsers();
        List<Product> products = seedProducts();

        seedOrders(users, products);
        seedNotices(admin);
        seedQnas(users, admin);
        seedReviews(users, products);
    }

    private List<Member> loadSeedUsers() {
        List<Member> users = new ArrayList<>();
        for (int i = 1; i <= 5; i += 1) {
            String email = "user" + i + "@readme.test";
            Member member = memberRepository.findByEmail(email)
                    .orElseThrow(() -> new IllegalStateException("Seed user is missing: " + email));
            users.add(member);
        }
        return users;
    }

    private List<Product> seedProducts() {
        CategoryTop korean = saveCategoryTop("국내도서", 1);
        CategoryTop foreign = saveCategoryTop("외국도서", 2);

        CategorySub novel = saveCategorySub(korean, "소설", 1);
        CategorySub it = saveCategorySub(korean, "IT/컴퓨터", 2);
        CategorySub selfHelp = saveCategorySub(korean, "자기계발", 3);
        CategorySub business = saveCategorySub(foreign, "경제경영", 1);

        List<Product> products = new ArrayList<>();
        for (int i = 1; i <= SEED_PRODUCT_COUNT; i += 1) {
            CategoryTop categoryTop = i <= 7 ? korean : foreign;
            CategorySub categorySub = switch (i % 4) {
                case 1 -> novel;
                case 2 -> it;
                case 3 -> selfHelp;
                default -> business;
            };

            Product product = Product.builder()
                    .categoryTop(categoryTop)
                    .categorySub(categorySub)
                    .title(String.format("테스트 도서 %02d", i))
                    .author(String.format("테스트 저자 %02d", i))
                    .description(String.format("관리자 기능 테스트용 도서 데이터 %02d", i))
                    .price(15000 + (i * 1000))
                    .discountRate(java.math.BigDecimal.valueOf((i % 3) * 5L))
                    .salePrice(14000 + (i * 900))
                    .stock(20 + i)
                    .thumbnail("/uploads/test-book-" + i + ".png")
                    .viewCount(i * 10)
                    .salesCount(i * 3)
                    .productStatus(ProductStatus.ACTIVATE)
                    .build();
            products.add(productRepository.save(product));
        }
        return products;
    }

    private void seedOrders(List<Member> users, List<Product> products) {
        for (int i = 1; i <= 10; i += 1) {
            Member member = users.get((i - 1) % users.size());
            Product product = products.get(i - 1);
            OrderStatus orderStatus = resolveOrderStatus(i);

            int quantity = (i % 3) + 1;
            int totalAmount = product.getSalePrice() * quantity;

            Order order = Order.builder()
                    .member(member)
                    .number(String.format("SEED-ORDER-%03d", i))
                    .orderStatus(orderStatus)
                    .totalPrice(totalAmount)
                    .discountAmount(0)
                    .finalPrice(totalAmount)
                    .receiverName(member.getName())
                    .receiverPhone(member.getPhone())
                    .deliveryAddress(member.getAddress())
                    .deliveryAddressDetail("테스트 상세주소 " + i)
                    .deliveryZipCode(String.format("060%02d", i))
                    .deliveryMemo("테스트 주문 메모 " + i)
                    .orderAt(LocalDateTime.now().minusDays(11L - i))
                    .build();
            order = orderRepository.save(order);

            OrderItem orderItem = OrderItem.builder()
                    .order(order)
                    .product(product)
                    .productTitle(product.getTitle())
                    .productAuthor(product.getAuthor())
                    .salePrice(product.getSalePrice())
                    .quantity(quantity)
                    .itemTotal(totalAmount)
                    .isReviewed(i <= 5)
                    .build();
            orderItemRepository.save(orderItem);

            if (orderStatus != OrderStatus.PENDING) {
                Payment payment = Payment.builder()
                        .order(order)
                        .paymentProvider(i % 2 == 0 ? PaymentProvider.TOSS : PaymentProvider.KAKAO)
                        .method(i % 2 == 0 ? "CARD" : "EASY_PAY")
                        .paymentStatus(orderStatus == OrderStatus.CANCELED ? PaymentStatus.CANCELLED : PaymentStatus.PAID)
                        .amount(totalAmount)
                        .pgTid("SEED-TID-" + i)
                        .paymentKey("SEED-PAYMENT-KEY-" + i)
                        .installmentMonths(i % 4)
                        .paidAt(LocalDateTime.now().minusDays(11L - i))
                        .cancelReason(orderStatus == OrderStatus.CANCELED ? "테스트 취소 주문" : null)
                        .cancelledAt(orderStatus == OrderStatus.CANCELED ? LocalDateTime.now().minusDays(10L - i) : null)
                        .build();
                paymentRepository.save(payment);
            }

            if (orderStatus == OrderStatus.PAYED || orderStatus == OrderStatus.APPROVAL || orderStatus == OrderStatus.CANCELED) {
                Delivery delivery = new Delivery();
                delivery.setOrder(order);
                delivery.setCourier("CJ대한통운");
                delivery.setTrackingNumber(String.format("TEST-TRACK-%04d", i));
                delivery.setDeliveryStatus(resolveDeliveryStatus(orderStatus, i));
                if (orderStatus == OrderStatus.APPROVAL) {
                    delivery.setShippedAt(LocalDateTime.now().minusDays(10L - i));
                    if (i % 2 == 0) {
                        delivery.setDeliveredAt(LocalDateTime.now().minusDays(9L - i));
                    }
                }
                deliveryRepository.save(delivery);
            }
        }
    }

    private void seedNotices(Member admin) {
        if (noticeRepository.count() > 0) {
            return;
        }

        for (int i = 1; i <= 5; i += 1) {
            Notice notice = Notice.builder()
                    .author(admin)
                    .title(String.format("테스트 공지사항 %02d", i))
                    .content(String.format("관리자 공지 기능 점검용 본문 %02d", i))
                    .isFixed(i <= 2)
                    .viewCount(i * 11)
                    .build();
            noticeRepository.save(notice);
        }
    }

    private void seedQnas(List<Member> users, Member admin) {
        if (qnaRepository.count() > 0) {
            return;
        }

        for (int i = 1; i <= 5; i += 1) {
            Member member = users.get((i - 1) % users.size());
            QnA question = QnA.builder()
                    .member(member)
                    .category(i % 2 == 0 ? "배송" : "결제")
                    .title(String.format("테스트 문의 %02d", i))
                    .content(String.format("테스트 문의 내용 %02d", i))
                    .qnaStatus(i <= 3 ? QnaStatus.COMPLETE : QnaStatus.WAITING)
                    .isSecret(i % 2 == 0)
                    .viewCount(i * 2)
                    .answeredAt(i <= 3 ? LocalDateTime.now().minusDays(i) : null)
                    .build();
            question = qnaRepository.save(question);

            if (i <= 3) {
                QnA answer = QnA.builder()
                        .parent(question)
                        .depth(1)
                        .member(admin)
                        .category(question.getCategory())
                        .title("[답변] " + question.getTitle())
                        .content(String.format("테스트 답변 내용 %02d", i))
                        .qnaStatus(QnaStatus.COMPLETE)
                        .build();
                qnaRepository.save(answer);
            }
        }
    }

    private void seedReviews(List<Member> users, List<Product> products) {
        if (reviewRepository.count() > 0) {
            return;
        }

        for (int i = 1; i <= 5; i += 1) {
            Review review = Review.builder()
                    .member(users.get((i - 1) % users.size()))
                    .product(products.get(i - 1))
                    .rating((i % 5) + 1)
                    .content(String.format("테스트 리뷰 내용 %02d", i))
                    .hits(i * 3)
                    .build();
            reviewRepository.save(review);
        }
    }

    private CategoryTop saveCategoryTop(String name, int sortOrder) {
        CategoryTop categoryTop = CategoryTop.builder()
                .name(name)
                .sortOrder(sortOrder)
                .categoryStatus(CategoryStatus.ACTIVATE)
                .build();
        return categoryTopRepository.save(categoryTop);
    }

    private CategorySub saveCategorySub(CategoryTop categoryTop, String name, int sortOrder) {
        CategorySub categorySub = CategorySub.builder()
                .categoryTop(categoryTop)
                .name(name)
                .sortOrder(sortOrder)
                .categoryStatus(CategoryStatus.ACTIVATE)
                .build();
        return categorySubRepository.save(categorySub);
    }

    private OrderStatus resolveOrderStatus(int index) {
        if (index <= 3) {
            return OrderStatus.PENDING;
        }
        if (index <= 6) {
            return OrderStatus.PAYED;
        }
        if (index <= 9) {
            return OrderStatus.APPROVAL;
        }
        return OrderStatus.CANCELED;
    }

    private DeliveryStatus resolveDeliveryStatus(OrderStatus orderStatus, int index) {
        if (orderStatus == OrderStatus.PAYED) {
            return DeliveryStatus.READY;
        }
        if (orderStatus == OrderStatus.CANCELED) {
            return DeliveryStatus.FAILED;
        }
        return index % 2 == 0 ? DeliveryStatus.DELIVERED : DeliveryStatus.SHIPPED;
    }
}
