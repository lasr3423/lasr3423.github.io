package com.bookstore.shop.readme.controller;

import com.bookstore.shop.readme.domain.Member;
import com.bookstore.shop.readme.domain.Notice;
import com.bookstore.shop.readme.dto.request.NoticeCreateRequest;
import com.bookstore.shop.readme.dto.response.NoticeResponse;
import com.bookstore.shop.readme.repository.MemberRepository;
import com.bookstore.shop.readme.repository.NoticeRepository;
import com.bookstore.shop.readme.security.CustomUserDetails;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

// [신규] 공지사항 API
// - GET /api/notice        : 공지사항 목록 (비로그인 허용 — SecurityConfig 이미 permitAll)
// - GET /api/notice/{id}   : 공지사항 상세
// - POST /api/notice       : 등록 (MANAGER / ADMIN 전용)
// - PUT /api/notice/{id}   : 수정 (MANAGER / ADMIN 전용)
// - DELETE /api/notice/{id}: 삭제 soft delete (MANAGER / ADMIN 전용)
@RestController
@RequestMapping("/api/notice")
@RequiredArgsConstructor
public class NoticeApiController {

    private final NoticeRepository noticeRepository;
    private final MemberRepository memberRepository;

    @GetMapping
    public ResponseEntity<Page<NoticeResponse>> getNotices(
            @PageableDefault(size = 10, sort = "pinned",
                    direction = Sort.Direction.DESC) Pageable pageable) {
        return ResponseEntity.ok(
                noticeRepository.findAllByDeletedAtIsNull(pageable).map(NoticeResponse::new));
    }

    @GetMapping("/{noticeId}")
    public ResponseEntity<NoticeResponse> getNotice(@PathVariable Long noticeId) {
        Notice notice = noticeRepository.findById(noticeId)
                .orElseThrow(() -> new RuntimeException("공지사항을 찾을 수 없습니다."));
        if (notice.getDeletedAt() != null) throw new RuntimeException("삭제된 공지사항입니다.");
        notice.setViewCount(notice.getViewCount() + 1);
        noticeRepository.save(notice);
        return ResponseEntity.ok(new NoticeResponse(notice));
    }

    @PostMapping
    @PreAuthorize("hasAnyRole('MANAGER', 'ADMIN')")
    public ResponseEntity<Long> createNotice(
            @RequestBody NoticeCreateRequest req,
            @AuthenticationPrincipal CustomUserDetails user) {
        Member author = memberRepository.findById(user.getMemberId())
                .orElseThrow(() -> new RuntimeException("회원을 찾을 수 없습니다."));
        Notice notice = Notice.builder()
                .author(author)
                .title(req.title())
                .content(req.content())
                .pinned(req.pinned())
                .build();
        noticeRepository.save(notice);
        return ResponseEntity.status(201).body(notice.getId());
    }

    @PutMapping("/{noticeId}")
    @PreAuthorize("hasAnyRole('MANAGER', 'ADMIN')")
    public ResponseEntity<NoticeResponse> updateNotice(
            @PathVariable Long noticeId,
            @RequestBody NoticeCreateRequest req) {
        Notice notice = noticeRepository.findById(noticeId)
                .orElseThrow(() -> new RuntimeException("공지사항을 찾을 수 없습니다."));
        notice.setTitle(req.title());
        notice.setContent(req.content());
        notice.setPinned(req.pinned());
        noticeRepository.save(notice);
        return ResponseEntity.ok(new NoticeResponse(notice));
    }

    @DeleteMapping("/{noticeId}")
    @PreAuthorize("hasAnyRole('MANAGER', 'ADMIN')")
    public ResponseEntity<String> deleteNotice(@PathVariable Long noticeId) {
        Notice notice = noticeRepository.findById(noticeId)
                .orElseThrow(() -> new RuntimeException("공지사항을 찾을 수 없습니다."));
        notice.setDeletedAt(java.time.LocalDateTime.now()); // soft delete
        noticeRepository.save(notice);
        return ResponseEntity.ok("공지사항이 삭제되었습니다.");
    }
}
