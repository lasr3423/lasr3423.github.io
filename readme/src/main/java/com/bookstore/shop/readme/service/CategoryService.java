package com.bookstore.shop.readme.service;

import com.bookstore.shop.readme.domain.CategorySub;
import com.bookstore.shop.readme.domain.CategoryTop;
import com.bookstore.shop.readme.dto.response.CategoryResponse;
import com.bookstore.shop.readme.repository.CategorySubRepository;
import com.bookstore.shop.readme.repository.CategoryTopRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class CategoryService {

    private final CategoryTopRepository categoryTopRepository;
    private final CategorySubRepository categorySubRepository;

    /** 대분류 카테고리 전체 목록 (소분류 포함) — 사이드바/네비게이션용 */
    public ResponseEntity<List<CategoryResponse>> getCategoryList() {
        List<CategoryTop> tops = categoryTopRepository.findAll();
        List<CategoryResponse> result = tops.stream()
                .map(top -> new CategoryResponse(top,
                        categorySubRepository.findByCategoryTopIdOrderBySortOrderAsc(top.getId())))
                .collect(Collectors.toList());
        return ResponseEntity.ok(result);
    }

    /** 특정 대분류의 소분류 목록만 조회 */
    public ResponseEntity<List<CategoryResponse.SubCategoryDto>> getSubCategories(Long topId) {
        List<CategorySub> subs =
                categorySubRepository.findByCategoryTopIdOrderBySortOrderAsc(topId);
        List<CategoryResponse.SubCategoryDto> result = subs.stream()
                .map(CategoryResponse.SubCategoryDto::new)
                .collect(Collectors.toList());
        return ResponseEntity.ok(result);
    }
}
