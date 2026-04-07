package com.bookstore.shop.readme.controller;

import com.bookstore.shop.readme.dto.request.NoticeCreateRequest;
import com.bookstore.shop.readme.dto.response.NoticeResponse;
import com.bookstore.shop.readme.security.CustomUserDetails;
import com.bookstore.shop.readme.service.NoticeService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

// [수정] Repository 직접 주입 제거 → NoticeService 위임으로 변경
@RestController
@RequestMapping("/api/notice")
@RequiredArgsConstructor
public class NoticeApiController {

    private final NoticeService noticeService;

    @GetMapping
    public ResponseEntity<Page<NoticeResponse>> getNotices(
            @PageableDefault(size = 10, sort = "pinned",
                    direction = Sort.Direction.DESC) Pageable pageable) {
        return noticeService.getNotices(pageable);
    }

    @GetMapping("/{noticeId}")
    public ResponseEntity<NoticeResponse> getNotice(@PathVariable Long noticeId) {
        return noticeService.getNotice(noticeId);
    }

    @PostMapping
    @PreAuthorize("hasAnyRole('MANAGER', 'ADMIN')")
    public ResponseEntity<Long> createNotice(
            @RequestBody NoticeCreateRequest req,
            @AuthenticationPrincipal CustomUserDetails user) {
        return noticeService.createNotice(req, user.getMemberId());
    }

    @PutMapping("/{noticeId}")
    @PreAuthorize("hasAnyRole('MANAGER', 'ADMIN')")
    public ResponseEntity<NoticeResponse> updateNotice(
            @PathVariable Long noticeId,
            @RequestBody NoticeCreateRequest req) {
        return noticeService.updateNotice(noticeId, req);
    }

    @DeleteMapping("/{noticeId}")
    @PreAuthorize("hasAnyRole('MANAGER', 'ADMIN')")
    public ResponseEntity<String> deleteNotice(@PathVariable Long noticeId) {
        return noticeService.deleteNotice(noticeId);
    }
}
