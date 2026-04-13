package com.bookstore.shop.readme.controller;

import com.bookstore.shop.readme.dto.response.CategoryResponse;
import com.bookstore.shop.readme.service.CategoryService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

// [신규] 카테고리 조회 API — SecurityConfig에서 GET /api/category/** 이미 permitAll()
@RestController
@RequestMapping("/api/category")
@RequiredArgsConstructor
public class CategoryApiController {

    private final CategoryService categoryService;

    /** 대분류 카테고리 전체 목록 (소분류 포함) — 사이드바/네비게이션 메뉴 렌더링용 */
    @GetMapping("/top")
    public ResponseEntity<List<CategoryResponse>> getCategoryList() {
        return categoryService.getCategoryList();
    }

    /** 특정 대분류의 소분류 목록만 조회 */
    @GetMapping("/{topId}/sub")
    public ResponseEntity<List<CategoryResponse.SubCategoryDto>> getSubCategories(
            @PathVariable Long topId) {
        return categoryService.getSubCategories(topId);
    }
}
