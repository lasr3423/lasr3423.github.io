package com.bookstore.shop.readme.controller;

import com.bookstore.shop.readme.domain.CategorySub;
import com.bookstore.shop.readme.domain.CategoryTop;
import com.bookstore.shop.readme.dto.response.CategoryResponse;
import com.bookstore.shop.readme.repository.CategorySubRepository;
import com.bookstore.shop.readme.repository.CategoryTopRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

// [신규] 카테고리 조회 API — SecurityConfig에서 GET /api/category/** 이미 permitAll()
@RestController
@RequestMapping("/api/category")
@RequiredArgsConstructor
public class CategoryApiController {

    private final CategoryTopRepository  categoryTopRepository;
    private final CategorySubRepository  categorySubRepository;

    /** 대분류 카테고리 전체 목록 (소분류 포함) — 사이드바/네비게이션 메뉴 렌더링용 */
    @GetMapping("/top")
    public ResponseEntity<List<CategoryResponse>> getCategoryList() {
        List<CategoryTop> tops = categoryTopRepository.findAll();
        List<CategoryResponse> result = tops.stream()
                .map(top -> {
                    List<CategorySub> subs =
                            categorySubRepository.findByCategoryTopIdOrderBySortOrderAsc(top.getId());
                    return new CategoryResponse(top, subs);
                })
                .collect(Collectors.toList());
        return ResponseEntity.ok(result);
    }

    /** 특정 대분류의 소분류 목록만 조회 */
    @GetMapping("/{topId}/sub")
    public ResponseEntity<List<CategoryResponse.SubCategoryDto>> getSubCategories(
            @PathVariable Long topId) {
        List<CategorySub> subs =
                categorySubRepository.findByCategoryTopIdOrderBySortOrderAsc(topId);
        List<CategoryResponse.SubCategoryDto> result = subs.stream()
                .map(CategoryResponse.SubCategoryDto::new)
                .collect(Collectors.toList());
        return ResponseEntity.ok(result);
    }
}
