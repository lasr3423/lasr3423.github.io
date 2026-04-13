package com.bookstore.shop.readme.config;

import com.bookstore.shop.readme.domain.*;
import com.bookstore.shop.readme.repository.*;
import com.bookstore.shop.readme.service.KakaoBookSearchService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.math.BigDecimal;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

@Component
@org.springframework.core.annotation.Order(2)
@ConditionalOnProperty(name = "seed.commerce.enabled", havingValue = "true")
@ConditionalOnProperty(name = "seed.product-topup.enabled", havingValue = "false", matchIfMissing = true)
@RequiredArgsConstructor
public class TestCommerceDataInitializer implements CommandLineRunner {

    private static final int SEED_ORDER_COUNT = 10;
    private static final int SEARCH_PAGE_SIZE = 50;
    private static final int SEARCH_MAX_PAGE = 50;
    private static final List<String> SEARCH_KEYWORDS = List.of(
            "소설", "에세이", "시", "경제", "경영", "자기계발", "역사", "철학", "심리학", "과학",
            "수학", "물리", "생명과학", "의학", "예술", "디자인", "건축", "요리", "여행", "인문학",
            "정치", "사회", "교육", "아동", "청소년", "IT", "프로그래밍", "자바", "스프링", "데이터",
            "인공지능", "머신러닝", "웹개발", "마케팅", "브랜딩", "리더십", "투자", "회계", "창업", "외국어",
            "문학", "만화", "파이썬", "리눅스", "게임", "사진", "건강", "취미", "문화", "예제",
            "가", "나", "다", "라", "마", "바", "사", "아", "자", "차",
            "A", "B", "C", "D", "E", "F", "G", "H"
    );

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
    private final KakaoBookSearchService kakaoBookSearchService;

    @Value("${seed.api-product-count:1000}")
    private int seedProductCount;

    @Override
    @Transactional
    public void run(String... args) {
        if (orderRepository.findByNumber("SEED-ORDER-001").isPresent()) {
            return;
        }

        Member admin = memberRepository.findByEmail("admin1@readme.test")
                .orElseThrow(() -> new IllegalStateException("Seed admin member is missing."));

        List<Member> users = loadSeedUsers();
        List<Product> products = seedProductsFromKakao();

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

    private List<Product> seedProductsFromKakao() {
        CategoryTop domestic = saveCategoryTop("국내도서", 1);
        CategoryTop foreign = saveCategoryTop("해외도서", 2);

        CategorySub novel = saveCategorySub(domestic, "소설", 1);
        CategorySub humanities = saveCategorySub(domestic, "인문", 2);
        CategorySub economy = saveCategorySub(domestic, "경제/경영", 3);
        CategorySub it = saveCategorySub(domestic, "IT/컴퓨터", 4);
        CategorySub foreignNovel = saveCategorySub(foreign, "해외소설", 1);

        List<CategorySub> categoryCycle = List.of(novel, humanities, economy, it, foreignNovel);
        List<Product> products = new ArrayList<>();
        Set<String> usedKeys = new HashSet<>();

        for (String keyword : SEARCH_KEYWORDS) {
            if (products.size() >= seedProductCount) {
                break;
            }

            for (int page = 1; page <= SEARCH_MAX_PAGE; page += 1) {
                KakaoBookSearchService.KakaoBookSearchResult result =
                        kakaoBookSearchService.searchByTitle(keyword, page, SEARCH_PAGE_SIZE);

                if (result.documents().isEmpty()) {
                    break;
                }

                for (Map<String, Object> doc : result.documents()) {
                    if (products.size() >= seedProductCount) {
                        break;
                    }

                    String title = limit(text(doc.get("title")), 300);
                    String author = limit(joinAuthors(doc.get("authors")), 200);
                    String isbn = normalizeKakaoIsbn(text(doc.get("isbn")));
                    String uniqueKey = StringUtils.hasText(isbn)
                            ? "ISBN:" + isbn
                            : "TITLE:" + title + "|" + defaultText(author, "");

                    if (!StringUtils.hasText(title) || !usedKeys.add(uniqueKey)) {
                        continue;
                    }

                    int index = products.size();
                    CategorySub categorySub = categoryCycle.get(index % categoryCycle.size());
                    CategoryTop categoryTop = categorySub.getCategoryTop();
                    int price = parsePrice(doc.get("price"), 12000 + (index * 75));
                    BigDecimal discountRate = resolveDiscountRate(doc, index);
                    int salePrice = parsePrice(doc.get("sale_price"), calcSalePrice(price, discountRate));

                    Product product = Product.builder()
                            .categoryTop(categoryTop)
                            .categorySub(categorySub)
                            .title(title)
                            .author(defaultText(author, "작자미상"))
                            .isbn(isbn)
                            .description(buildDescription(doc, isbn))
                            .price(price)
                            .discountRate(discountRate)
                            .salePrice(salePrice)
                            .stock(30 + (index % 40))
                            .thumbnail(resolveThumbnail(doc, isbn, title))
                            .viewCount(index * 5)
                            .salesCount(index * 2)
                            .productStatus(ProductStatus.ACTIVATE)
                            .build();

                    products.add(productRepository.save(product));
                }

                if (products.size() >= seedProductCount || result.isEnd()) {
                    break;
                }
            }
        }

        if (products.size() < SEED_ORDER_COUNT) {
            throw new IllegalStateException("Kakao API seed product count is too small: " + products.size());
        }

        return products;
    }

    private String resolveThumbnail(Map<String, Object> doc, String isbn, String title) {
        String thumbnail = text(doc.get("thumbnail"));
        if (StringUtils.hasText(thumbnail)) {
            return thumbnail;
        }
        return buildPlaceholderThumbnail(isbn, title);
    }

    private String buildPlaceholderThumbnail(String isbn, String title) {
        String label = StringUtils.hasText(title) ? title : defaultText(isbn, "ReadMe Book");
        String encodedLabel = URLEncoder.encode(limit(label, 32), StandardCharsets.UTF_8);
        return "https://placehold.co/320x480/F8FAFC/0F172A/png?text=" + encodedLabel;
    }

    private String buildDescription(Map<String, Object> doc, String isbn) {
        String contents = limit(text(doc.get("contents")), 700);
        String publisher = text(doc.get("publisher"));
        String datetime = text(doc.get("datetime"));
        String url = text(doc.get("url"));

        StringBuilder builder = new StringBuilder();
        if (StringUtils.hasText(contents)) {
            builder.append(contents).append("\n\n");
        } else {
            builder.append("카카오 도서 검색 API 기반 테스트용 상품 데이터입니다.\n\n");
        }
        builder.append("출판사: ").append(defaultText(publisher, "정보 없음")).append("\n");
        builder.append("ISBN: ").append(defaultText(isbn, "정보 없음")).append("\n");
        builder.append("출간일시: ").append(defaultText(datetime, "정보 없음"));
        if (StringUtils.hasText(url)) {
            builder.append("\n참고 URL: ").append(url);
        }
        return builder.toString();
    }

    private BigDecimal resolveDiscountRate(Map<String, Object> doc, int index) {
        int price = parsePrice(doc.get("price"), 0);
        int salePrice = parsePrice(doc.get("sale_price"), 0);
        if (price > 0 && salePrice > 0 && salePrice < price) {
            double rate = (1 - (salePrice / (double) price)) * 100;
            return BigDecimal.valueOf(Math.round(rate * 10) / 10.0);
        }
        return BigDecimal.valueOf((index % 4) * 5L);
    }

    private String normalizeKakaoIsbn(String rawIsbn) {
        if (!StringUtils.hasText(rawIsbn)) {
            return "";
        }

        String[] tokens = rawIsbn.split("\\s+");
        String best = "";
        for (String token : tokens) {
            String normalized = token.replaceAll("[^0-9Xx]", "");
            if (!StringUtils.hasText(normalized)) {
                continue;
            }
            if (normalized.length() == 13) {
                return normalized;
            }
            if (!StringUtils.hasText(best)) {
                best = normalized;
            }
        }
        return best;
    }

    private String joinAuthors(Object value) {
        if (value instanceof List<?> list && !list.isEmpty()) {
            return list.stream().map(String::valueOf).reduce((left, right) -> left + ", " + right).orElse(null);
        }
        return text(value);
    }

    private int parsePrice(Object priceValue, int fallback) {
        if (priceValue == null) {
            return fallback;
        }
        if (priceValue instanceof Number number) {
            int parsed = number.intValue();
            return parsed >= 0 ? parsed : fallback;
        }

        String digits = String.valueOf(priceValue).replaceAll("[^0-9]", "");
        if (!StringUtils.hasText(digits)) {
            return fallback;
        }
        try {
            return Integer.parseInt(digits);
        } catch (NumberFormatException e) {
            return fallback;
        }
    }

    private int calcSalePrice(int price, BigDecimal discountRate) {
        if (price <= 0) {
            return 0;
        }
        if (discountRate == null || discountRate.compareTo(BigDecimal.ZERO) == 0) {
            return price;
        }
        BigDecimal ratio = BigDecimal.ONE.subtract(discountRate.divide(BigDecimal.valueOf(100)));
        return Math.max(0, ratio.multiply(BigDecimal.valueOf(price)).intValue());
    }

    private String defaultText(String value, String fallback) {
        return StringUtils.hasText(value) ? value : fallback;
    }

    private String text(Object value) {
        return value == null ? null : String.valueOf(value);
    }

    private String limit(String value, int maxLength) {
        if (!StringUtils.hasText(value)) {
            return value;
        }
        return value.length() <= maxLength ? value : value.substring(0, maxLength);
    }

    private void seedOrders(List<Member> users, List<Product> products) {
        for (int i = 1; i <= SEED_ORDER_COUNT; i += 1) {
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
                    .content(String.format("관리자 공지 기능 확인용 본문 %02d", i))
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
