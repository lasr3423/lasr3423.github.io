package com.bookstore.shop.readme.controller.admin;

import com.bookstore.shop.readme.dto.NoticeRequestDto;
import com.bookstore.shop.readme.dto.NoticeResponseDto;
import com.bookstore.shop.readme.service.NoticeService;
import com.bookstore.shop.readme.util.SecurityUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/admin/notice")
@RequiredArgsConstructor
@PreAuthorize("hasAnyRole('ADMIN', 'MANAGER')") // [UC-A-009] ADMIN/MANAGER만 접근
public class AdminNoticeController {

    private final NoticeService noticeService;

    // [FA-024] 관리자 공지사항 목록 조회
    @GetMapping("/list")
    public ResponseEntity<Page<NoticeResponseDto>> getNoticeList(
            @PageableDefault(size = 10) Pageable pageable) {
        return ResponseEntity.ok(noticeService.getNoticeList(pageable));
    }

    // [FA-025] 관리자 공지사항 상세 조회
    @GetMapping("/detail/{id}")
    public ResponseEntity<NoticeResponseDto> getNoticeDetail(@PathVariable Long id) {
        return ResponseEntity.ok(noticeService.getNoticeDetail(id));
    }

    // [FA-026] 공지사항 등록 — URL: /admin/notice/insert
    @PostMapping("/insert")
    public ResponseEntity<String> createNotice(@RequestBody NoticeRequestDto request) {
        Long adminId = SecurityUtil.getCurrentMemberId(); // [보완] 인증 모듈 연동
        noticeService.createNotice(request, adminId);
        return ResponseEntity.status(HttpStatus.CREATED).body("공지사항이 등록되었습니다.");
    }

    // [FA-027] 공지사항 수정 — URL: /admin/notice/update/{id}
    @PatchMapping("/update/{id}")
    public ResponseEntity<String> updateNotice(
            @PathVariable Long id,
            @RequestBody NoticeRequestDto request) {
        noticeService.updateNotice(id, request);
        return ResponseEntity.ok("공지사항이 수정되었습니다.");
    }

    // [FA-028] 공지사항 삭제 (Soft Delete) — URL: /admin/notice/{id}
    @DeleteMapping("/{id}")
    public ResponseEntity<String> deleteNotice(@PathVariable Long id) {
        noticeService.deleteNotice(id);
        return ResponseEntity.ok("공지사항이 삭제되었습니다.");
    }
}