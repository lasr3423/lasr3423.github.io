package com.bookstore.shop.readme.controller;

import com.bookstore.shop.readme.domain.Notice;
import com.bookstore.shop.readme.service.NoticeService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/notice")
@RequiredArgsConstructor
public class NoticeController {

    private final NoticeService noticeService;

    /**
     * [설계서 v1.3 인덱스 준수]
     * idx_notice_is_fixed_created_at 정렬 결과 반환
     */
    @GetMapping
    public ResponseEntity<Page<Notice>> getNoticeList(
            @PageableDefault(size = 10) Pageable pageable) {
        // Service에서 이미 정렬 로직이 포함된 Page를 반환함
        return ResponseEntity.ok(noticeService.getNoticeList(pageable));
    }

    /**
     * [설계서 12번 테이블 정의서 준수]
     * 제약조건: 조회 시 view_count 1 증가 반영
     * 주의: 엔티티 직접 반환 시 Member 정보 노출 주의
     */
    @GetMapping("/{id}")
    public ResponseEntity<Notice> getNoticeDetail(@PathVariable Long id) {
        // Service 내부에서 view_count 증가 로직이 설계서대로 수행됨
        return ResponseEntity.ok(noticeService.getNoticeDetail(id));
    }
}