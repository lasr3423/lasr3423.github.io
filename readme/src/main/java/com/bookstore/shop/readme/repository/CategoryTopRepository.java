package com.bookstore.shop.readme.repository;

import com.bookstore.shop.readme.domain.CategoryStatus;
import com.bookstore.shop.readme.domain.CategoryTop;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface CategoryTopRepository extends JpaRepository<CategoryTop, Long> {
    List<CategoryTop> findByCategoryStatusOrderBySortOrderAsc(CategoryStatus status);
}
