package com.bookstore.shop.readme.repository;

import com.bookstore.shop.readme.domain.CategorySub;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CategorySubRepository extends JpaRepository<CategorySub, Long> {
    // [신규] 대분류 ID로 소분류 목록 조회
    java.util.List<CategorySub> findByCategoryTopIdOrderBySortOrderAsc(Long categoryTopId);
}
