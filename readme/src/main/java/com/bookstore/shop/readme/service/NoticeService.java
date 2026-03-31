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
@Transactional(readOnly = true)
public class NoticeService {
    private final NoticeRepository noticeRepository;
    private final MemberRepository memberRepository;

    // [설계서 인덱스 준수] idx_notice_is_fixed_created_at 활용 조회
    public Page<Notice> getNoticeList(Pageable pageable) {
        return noticeRepository.findAllByDeletedAtIsNullOrderByIsFixedDescCreatedAtDesc(pageable);
    }

    // [설계서 테이블 정의서 준수] member_id(FK), view_count(0) 초기값 반영
    @Transactional
    public void createNotice(Long adminId, NoticeRequest request) {
        Member admin = memberRepository.findById(adminId)
                .orElseThrow(() -> new RuntimeException("관리자를 찾을 수 없습니다."));

        Notice notice = new Notice();
        notice.setMember(admin);
        notice.setTitle(request.getTitle());     // VARCHAR(255) NOT NULL
        notice.setContent(request.getContent()); // TEXT NOT NULL

        // NULL 체크 후 설계서 기본값 반영
        notice.setIsFixed(request.getIsFixed() != null && request.getIsFixed());
        notice.setViewCount(0); // INTEGER NOT NULL 기본값 0

        noticeRepository.save(notice);
    }

    // [설계서 제약조건 준수] view_count 증가 및 Soft Delete 체크
    @Transactional
    public Notice getNoticeDetail(Long id) {
        Notice notice = noticeRepository.findByIdAndDeletedAtIsNull(id)
                .orElseThrow(() -> new RuntimeException("존재하지 않거나 삭제된 공지사항입니다."));

        // 설계서 INTEGER view_count 증가
        notice.setViewCount(notice.getViewCount() + 1);
        return notice;
    }

    // [설계서 수정일 준수]
    @Transactional
    public void updateNotice(Long id, NoticeRequest request) {
        Notice notice = noticeRepository.findByIdAndDeletedAtIsNull(id)
                .orElseThrow(() -> new RuntimeException("수정할 수 없는 공지사항입니다."));

        notice.setTitle(request.getTitle());
        notice.setContent(request.getContent());
        notice.setIsFixed(request.getIsFixed() != null && request.getIsFixed());
    }

    // [설계서 Soft Delete 준수] deleted_at 컬럼 업데이트
    @Transactional
    public void deleteNotice(Long id) {
        Notice notice = noticeRepository.findByIdAndDeletedAtIsNull(id)
                .orElseThrow(() -> new RuntimeException("이미 삭제되었거나 존재하지 않는 공지사항입니다."));

        notice.setDeletedAt(LocalDateTime.now());
    }
}