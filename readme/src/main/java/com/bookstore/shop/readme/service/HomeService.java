package com.bookstore.shop.readme.service;

import com.bookstore.shop.readme.domain.ProductStatus;
import com.bookstore.shop.readme.dto.response.NoticeResponse;
import com.bookstore.shop.readme.dto.response.ProductListResponse;
import com.bookstore.shop.readme.repository.NoticeRepository;
import com.bookstore.shop.readme.repository.ProductRepository;
import lombok.RequiredArgsConstructor;

import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class HomeService {

//    private final ProductRepository productRepository;
//    private final NoticeRepository  noticeRepository;
//
//    /**
//     * 신상품 목록 — 최신 등록순 상위 N개
//     * GET /api/home/new-products?size=8
//     */
//    public ResponseEntity<List<ProductListResponse>> getNewProducts(int size) {
//        Pageable pageable = PageRequest.of(0, size, Sort.by(Sort.Direction.DESC, "createdAt"));
//        List<ProductListResponse> result = productRepository
//                .findAllByProductStatusOrderByCreatedAtDesc(ProductStatus.ACTIVATE, pageable)
//                .stream()
//                .map(ProductListResponse::new)
//                .collect(Collectors.toList());
//        return ResponseEntity.ok(result);
//    }
//
//    /**
//     * 판매 인기 상품 — salesCount 기준 상위 N개
//     * GET /api/home/popular-products?size=8
//     */
//    public ResponseEntity<List<ProductListResponse>> getPopularProducts(int size) {
//        Pageable pageable = PageRequest.of(0, size, Sort.by(Sort.Direction.DESC, "salesCount"));
//        List<ProductListResponse> result = productRepository
//                .findAllByProductStatusOrderBySalesCountDesc(ProductStatus.ACTIVATE, pageable)
//                .stream()
//                .map(ProductListResponse::new)
//                .collect(Collectors.toList());
//        return ResponseEntity.ok(result);
//    }
//
//    /**
//     * 조회수 인기 상품 — viewCount 기준 상위 N개
//     * GET /api/home/viewed-products?size=8
//     */
//    public ResponseEntity<List<ProductListResponse>> getMostViewedProducts(int size) {
//        Pageable pageable = PageRequest.of(0, size, Sort.by(Sort.Direction.DESC, "viewCount"));
//        List<ProductListResponse> result = productRepository
//                .findAllByProductStatusOrderByViewCountDesc(ProductStatus.ACTIVATE, pageable)
//                .stream()
//                .map(ProductListResponse::new)
//                .collect(Collectors.toList());
//        return ResponseEntity.ok(result);
//    }
//
//    /**
//     * 홈화면 핀 공지사항 — pinned=true인 전체 목록 (size 제한 없이 반환, 보통 d5개 이하)
//     * GET /api/home/notices
//     */
//    public ResponseEntity<List<NoticeResponse>> getPinnedNotices() {
//        List<NoticeResponse> result = noticeRepository
//                .findAllByPinnedTrueAndDeletedAtIsNull()
//                .stream()
//                .map(NoticeResponse::new)
//                .collect(Collectors.toList());
//        return ResponseEntity.ok(result);
//    }
}
