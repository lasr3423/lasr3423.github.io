package com.bookstore.shop.readme.controller;

import com.bookstore.shop.readme.dto.NoticeResponseDto;
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

    // [FN-001] 공지사항 목록 조회 — 비회원 포함 접근 가능
    @GetMapping("/list")
    public ResponseEntity<Page<NoticeResponseDto>> getNoticeList(
            @PageableDefault(size = 10) Pageable pageable) {
        return ResponseEntity.ok(noticeService.getNoticeList(pageable));
    }

    // [FN-002] 공지사항 상세 조회 + view_count 증가
    @GetMapping("/detail/{id}")
    public ResponseEntity<NoticeResponseDto> getNoticeDetail(@PathVariable Long id) {
        return ResponseEntity.ok(noticeService.getNoticeDetail(id));
    }
}