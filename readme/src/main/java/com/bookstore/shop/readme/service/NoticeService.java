package com.bookstore.shop.readme.service;

import com.bookstore.shop.readme.domain.Member;
import com.bookstore.shop.readme.domain.Notice;
import com.bookstore.shop.readme.dto.request.NoticeCreateRequest;
import com.bookstore.shop.readme.dto.response.NoticeResponse;
import com.bookstore.shop.readme.repository.MemberRepository;
import com.bookstore.shop.readme.repository.NoticeRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
@Transactional
public class NoticeService {

    private final NoticeRepository noticeRepository;
    private final MemberRepository memberRepository;

    // ── 공지사항 목록 (삭제되지 않은 것만) ──────────────────────────────────
    @Transactional(readOnly = true)
    public ResponseEntity<Page<NoticeResponse>> getNotices(Pageable pageable) {
        return ResponseEntity.ok(
                noticeRepository.findAllByDeletedAtIsNull(pageable).map(NoticeResponse::new));
    }

    // ── 공지사항 상세 (조회수 증가) ──────────────────────────────────────────
    public ResponseEntity<NoticeResponse> getNotice(Long noticeId) {
        Notice notice = noticeRepository.findById(noticeId)
                .orElseThrow(() -> new RuntimeException("공지사항을 찾을 수 없습니다."));
        if (notice.getDeletedAt() != null)
            throw new RuntimeException("삭제된 공지사항입니다.");
        notice.setViewCount(notice.getViewCount() + 1);
        return ResponseEntity.ok(new NoticeResponse(notice));
    }

    // ── 공지사항 등록 (관리자) ────────────────────────────────────────────────
    public ResponseEntity<Long> createNotice(NoticeCreateRequest req, Long memberId) {
        Member author = memberRepository.findById(memberId)
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

    // ── 공지사항 수정 (관리자) ────────────────────────────────────────────────
    public ResponseEntity<NoticeResponse> updateNotice(Long noticeId, NoticeCreateRequest req) {
        Notice notice = noticeRepository.findById(noticeId)
                .orElseThrow(() -> new RuntimeException("공지사항을 찾을 수 없습니다."));
        notice.setTitle(req.title());
        notice.setContent(req.content());
        notice.setPinned(req.pinned());
        return ResponseEntity.ok(new NoticeResponse(notice));
    }

    // ── 공지사항 삭제 soft delete (관리자) ───────────────────────────────────
    public ResponseEntity<String> deleteNotice(Long noticeId) {
        Notice notice = noticeRepository.findById(noticeId)
                .orElseThrow(() -> new RuntimeException("공지사항을 찾을 수 없습니다."));
        notice.setDeletedAt(LocalDateTime.now());
        return ResponseEntity.ok("공지사항이 삭제되었습니다.");
    }

    // ── 관리자 전체 목록 (삭제 포함) ─────────────────────────────────────────
    @Transactional(readOnly = true)
    public ResponseEntity<Page<NoticeResponse>> getAllNotices(Pageable pageable) {
        return ResponseEntity.ok(
                noticeRepository.findAll(pageable).map(NoticeResponse::new));
    }
}
