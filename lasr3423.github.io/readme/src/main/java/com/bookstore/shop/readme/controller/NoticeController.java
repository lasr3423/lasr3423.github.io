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
     * [설계서 1-2] 공지사항 목록 조회
     * 제약조건: 상단 고정(isFixed) 우선 정렬 + 최신순
     */
    @GetMapping
    public ResponseEntity<Page<Notice>> getNoticeList(
            @PageableDefault(size = 10) Pageable pageable) {
        return ResponseEntity.ok(noticeService.getNoticeList(pageable));
    }

    /**
     * [설계서 1-2] 공지사항 상세 조회
     * 제약조건: 상세 조회 시 viewCount 1 증가 로직 포함
     */
    @GetMapping("/{id}")
    public ResponseEntity<Notice> getNoticeDetail(@PathVariable Long id) {
        return ResponseEntity.ok(noticeService.getNoticeDetail(id));
    }
}