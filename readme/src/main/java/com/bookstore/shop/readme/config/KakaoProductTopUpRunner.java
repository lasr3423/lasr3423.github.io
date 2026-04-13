package com.bookstore.shop.readme.config;

import com.bookstore.shop.readme.domain.CategoryStatus;
import com.bookstore.shop.readme.domain.CategorySub;
import com.bookstore.shop.readme.domain.CategoryTop;
import com.bookstore.shop.readme.domain.Product;
import com.bookstore.shop.readme.domain.ProductStatus;
import com.bookstore.shop.readme.repository.CategorySubRepository;
import com.bookstore.shop.readme.repository.CategoryTopRepository;
import com.bookstore.shop.readme.repository.ProductRepository;
import com.bookstore.shop.readme.service.KakaoBookSearchService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.math.BigDecimal;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

@Slf4j
@Component
@Order(2)
@ConditionalOnProperty(name = "seed.product-topup.enabled", havingValue = "true")
@RequiredArgsConstructor
public class KakaoProductTopUpRunner implements CommandLineRunner {

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

    private final CategoryTopRepository categoryTopRepository;
    private final CategorySubRepository categorySubRepository;
    private final ProductRepository productRepository;
    private final KakaoBookSearchService kakaoBookSearchService;

    @Value("${seed.product-topup.add-count:200}")
    private int addCount;

    @Override
    @Transactional
    public void run(String... args) {
        if (addCount <= 0) {
            log.info("Kakao product top-up skipped because add-count is {}", addCount);
            return;
        }

        long initialCount = productRepository.count();
        int inserted = addProducts();
        long finalCount = productRepository.count();

        log.info("Kakao product top-up completed. inserted={}, initialCount={}, finalCount={}",
                inserted, initialCount, finalCount);

        if (inserted < addCount) {
            throw new IllegalStateException("Requested " + addCount + " products but inserted only " + inserted);
        }
    }

    private int addProducts() {
        long baseCount = productRepository.count();
        CategoryTop domestic = getOrCreateCategoryTop("국내도서", 1);
        CategoryTop foreign = getOrCreateCategoryTop("해외도서", 2);

        CategorySub novel = getOrCreateCategorySub(domestic, "소설", 1);
        CategorySub humanities = getOrCreateCategorySub(domestic, "인문", 2);
        CategorySub economy = getOrCreateCategorySub(domestic, "경제/경영", 3);
        CategorySub it = getOrCreateCategorySub(domestic, "IT/컴퓨터", 4);
        CategorySub foreignNovel = getOrCreateCategorySub(foreign, "해외소설", 1);

        List<CategorySub> categoryCycle = List.of(novel, humanities, economy, it, foreignNovel);
        Set<String> usedKeys = loadExistingKeys();
        int inserted = 0;

        for (String keyword : SEARCH_KEYWORDS) {
            if (inserted >= addCount) {
                break;
            }

            for (int page = 1; page <= SEARCH_MAX_PAGE; page += 1) {
                KakaoBookSearchService.KakaoBookSearchResult result =
                        kakaoBookSearchService.searchByTitle(keyword, page, SEARCH_PAGE_SIZE);

                if (result.documents().isEmpty()) {
                    break;
                }

                for (Map<String, Object> doc : result.documents()) {
                    if (inserted >= addCount) {
                        break;
                    }

                    String title = limit(text(doc.get("title")), 300);
                    String author = limit(joinAuthors(doc.get("authors")), 200);
                    String isbn = normalizeKakaoIsbn(text(doc.get("isbn")));
                    String uniqueKey = buildUniqueKey(title, author, isbn);

                    if (!StringUtils.hasText(title) || !usedKeys.add(uniqueKey)) {
                        continue;
                    }

                    int index = inserted;
                    CategorySub categorySub = categoryCycle.get(index % categoryCycle.size());
                    CategoryTop categoryTop = categorySub.getCategoryTop();
                    int price = parsePrice(doc.get("price"), 12000 + (int) ((baseCount + index) * 75L));
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
                            .stock(30 + ((int) ((baseCount + index) % 40)))
                            .thumbnail(resolveThumbnail(doc, isbn, title))
                            .viewCount(0)
                            .salesCount(0)
                            .productStatus(ProductStatus.ACTIVATE)
                            .build();

                    productRepository.save(product);
                    inserted += 1;
                }

                if (inserted >= addCount || result.isEnd()) {
                    break;
                }
            }
        }

        return inserted;
    }

    private Set<String> loadExistingKeys() {
        Set<String> usedKeys = new HashSet<>();
        for (Product product : productRepository.findAll()) {
            usedKeys.add(buildUniqueKey(product.getTitle(), product.getAuthor(), product.getIsbn()));
        }
        return usedKeys;
    }

    private String buildUniqueKey(String title, String author, String isbn) {
        if (StringUtils.hasText(isbn)) {
            return "ISBN:" + isbn;
        }
        return "TITLE:" + defaultText(title, "") + "|" + defaultText(author, "");
    }

    private CategoryTop getOrCreateCategoryTop(String name, int sortOrder) {
        for (CategoryTop categoryTop : categoryTopRepository.findAll()) {
            if (name.equals(categoryTop.getName())) {
                if (categoryTop.getCategoryStatus() == null) {
                    categoryTop.setCategoryStatus(CategoryStatus.ACTIVATE);
                }
                return categoryTop;
            }
        }

        CategoryTop categoryTop = CategoryTop.builder()
                .name(name)
                .sortOrder(sortOrder)
                .categoryStatus(CategoryStatus.ACTIVATE)
                .build();
        return categoryTopRepository.save(categoryTop);
    }

    private CategorySub getOrCreateCategorySub(CategoryTop categoryTop, String name, int sortOrder) {
        for (CategorySub categorySub : categorySubRepository.findAll()) {
            if (categoryTop.getId().equals(categorySub.getCategoryTop().getId()) && name.equals(categorySub.getName())) {
                if (categorySub.getCategoryStatus() == null) {
                    categorySub.setCategoryStatus(CategoryStatus.ACTIVATE);
                }
                return categorySub;
            }
        }

        CategorySub categorySub = CategorySub.builder()
                .categoryTop(categoryTop)
                .name(name)
                .sortOrder(sortOrder)
                .categoryStatus(CategoryStatus.ACTIVATE)
                .build();
        return categorySubRepository.save(categorySub);
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
            builder.append("카카오 도서 검색 API 기반 더미 상품 데이터입니다.\n\n");
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
}
