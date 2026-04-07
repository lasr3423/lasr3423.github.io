package com.bookstore.shop.readme.dto.response;

import com.bookstore.shop.readme.domain.CategorySub;
import com.bookstore.shop.readme.domain.CategoryTop;
import lombok.Getter;

import java.util.List;
import java.util.stream.Collectors;

// [신규] 카테고리 응답 DTO — /api/category/top, /api/admin/categories 에서 사용
@Getter
public class CategoryResponse {

    private final Long id;
    private final String name;
    private final String status;
    private final List<SubCategoryDto> subCategories;

    public CategoryResponse(CategoryTop top, List<CategorySub> subs) {
        this.id     = top.getId();
        this.name   = top.getName();
        this.status = top.getCategoryStatus() != null ? top.getCategoryStatus().name() : null;
        this.subCategories = subs.stream()
                .map(SubCategoryDto::new)
                .collect(Collectors.toList());
    }

    @Getter
    public static class SubCategoryDto {
        private final Long id;
        private final String name;
        private final String status;

        public SubCategoryDto(CategorySub sub) {
            this.id     = sub.getId();
            this.name   = sub.getName();
            this.status = sub.getCategoryStatus() != null ? sub.getCategoryStatus().name() : null;
        }
    }
}
