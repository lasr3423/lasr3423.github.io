package com.bookstore.shop.readme.service;

import com.bookstore.shop.readme.domain.Member;
import com.bookstore.shop.readme.domain.Notice;
import com.bookstore.shop.readme.dto.NoticeRequestDto;
import com.bookstore.shop.readme.dto.NoticeResponseDto;
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

    // [FA-024 / FN-001] 목록 조회 — is_fixed 상단, 최신순
    public Page<NoticeResponseDto> getNoticeList(Pageable pageable) {
        return noticeRepository.findAllByOrderByIsFixedDescCreatedAtDesc(pageable)
                .map(NoticeResponseDto::from);
    }

    // [FA-025 / FN-002] 상세 조회 + view_count 증가
    // [클래스 다이어그램 준수] 반환 타입: NoticeResponseDto
    @Transactional
    public NoticeResponseDto getNoticeDetail(Long id) {
        Notice notice = noticeRepository.findByIdAndDeletedAtIsNull(id)
                .orElseThrow(() -> new RuntimeException("존재하지 않거나 삭제된 공지사항입니다."));
        notice.setViewCount(notice.getViewCount() + 1);
        return NoticeResponseDto.from(notice);
    }

    // [FA-026] 등록
    // [클래스 다이어그램 준수] 시그니처: createNotice(NoticeRequest, Long)
    @Transactional
    public void createNotice(NoticeRequestDto request, Long adminId) {
        Member admin = memberRepository.findById(adminId)
                .orElseThrow(() -> new RuntimeException("관리자를 찾을 수 없습니다."));

        Notice notice = new Notice();
        notice.setMember(admin);
        notice.setTitle(request.getTitle());
        notice.setContent(request.getContent());
        notice.setIsFixed(request.getIsFixed() != null && request.getIsFixed());
        noticeRepository.save(notice);
    }

    // [FA-027] 수정 — updated_at은 BaseEntity @PreUpdate 자동 갱신
    @Transactional
    public void updateNotice(Long id, NoticeRequestDto request) {
        Notice notice = noticeRepository.findByIdAndDeletedAtIsNull(id)
                .orElseThrow(() -> new RuntimeException("수정할 수 없는 공지사항입니다."));
        notice.setTitle(request.getTitle());
        notice.setContent(request.getContent());
        notice.setIsFixed(request.getIsFixed() != null && request.getIsFixed());
    }

    // [FA-028] Soft Delete — deleted_at 기록
    @Transactional
    public void deleteNotice(Long id) {
        Notice notice = noticeRepository.findByIdAndDeletedAtIsNull(id)
                .orElseThrow(() -> new RuntimeException("이미 삭제되었거나 존재하지 않는 공지사항입니다."));
        notice.setDeletedAt(LocalDateTime.now());
    }
}