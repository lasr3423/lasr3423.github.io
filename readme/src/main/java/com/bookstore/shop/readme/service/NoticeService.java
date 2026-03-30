package com.bookstore.shop.readme.service;

import com.bookstore.shop.readme.domain.Member;
import com.bookstore.shop.readme.domain.Notice;
import com.bookstore.shop.readme.dto.NoticeRequest;
import com.bookstore.shop.readme.repository.MemberRepository;
import com.bookstore.shop.readme.repository.NoticeRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true) // 기본적으로 읽기 전용 설정
public class NoticeService {
    private final NoticeRepository noticeRepository;
    private final MemberRepository memberRepository;

    // [설계서 1-2] 공지사항 목록 조회 (삭제되지 않은 것 + 고정글 우선)
    public Page<Notice> getNoticeList(Pageable pageable) {
        return noticeRepository.findAllByDeletedAtIsNullOrderByIsFixedDescCreatedAtDesc(pageable);
    }

    // [설계서 1-2] 공지사항 등록
    @Transactional
    public void createNotice(Long adminId, NoticeRequest request) {
        Member admin = memberRepository.findById(adminId)
                .orElseThrow(() -> new RuntimeException("관리자를 찾을 수 없습니다."));

        Notice notice = new Notice();
        notice.setMember(admin);
        notice.setTitle(request.getTitle());
        notice.setContent(request.getContent());
        notice.setIsFixed(request.getIsFixed() != null && request.getIsFixed());

        noticeRepository.save(notice);
    }

    // [설계서 1-2] 공지사항 상세 조회 (조회수 증가 + Soft Delete 체크)
    @Transactional
    public Notice getNoticeDetail(Long id) {
        // 제약조건: 삭제되지 않은 공지사항만 상세 조회 가능
        Notice notice = noticeRepository.findByIdAndDeletedAtIsNull(id)
                .orElseThrow(() -> new RuntimeException("존재하지 않거나 삭제된 공지사항입니다."));

        notice.setViewCount(notice.getViewCount() + 1);
        return notice;
    }

    // [설계서 1-2] 공지사항 수정
    @Transactional
    public void updateNotice(Long id, NoticeRequest request) {
        Notice notice = noticeRepository.findByIdAndDeletedAtIsNull(id)
                .orElseThrow(() -> new RuntimeException("수정할 수 없는 공지사항입니다."));

        notice.setTitle(request.getTitle());
        notice.setContent(request.getContent());
        notice.setIsFixed(request.getIsFixed());
        // BaseEntity에 의해 updatedAt은 자동 처리되거나 명시적으로 처리
    }

    // [설계서 1-2] 공지사항 삭제 (Soft Delete)
    @Transactional
    public void deleteNotice(Long id) {
        Notice notice = noticeRepository.findByIdAndDeletedAtIsNull(id)
                .orElseThrow(() -> new RuntimeException("이미 삭제되었거나 존재하지 않는 공지사항입니다."));

        // 제약조건: 실제 DELETE가 아닌 deletedAt 업데이트
        notice.setDeletedAt(LocalDateTime.now());
    }
}