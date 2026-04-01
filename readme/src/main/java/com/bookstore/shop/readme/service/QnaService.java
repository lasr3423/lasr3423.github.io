package com.bookstore.shop.readme.service;

import com.bookstore.shop.readme.domain.Member;
import com.bookstore.shop.readme.domain.MemberRole;
import com.bookstore.shop.readme.domain.Qna;
import com.bookstore.shop.readme.domain.QnaStatus;
import com.bookstore.shop.readme.dto.QnaRequestDto;
import com.bookstore.shop.readme.dto.QnaResponseDto;
import com.bookstore.shop.readme.repository.MemberRepository;
import com.bookstore.shop.readme.repository.QnaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class QnaService {

    private final QnaRepository qnaRepository;
    private final MemberRepository memberRepository;

    // ─────────────────────────────────────────────
    // 관리자 — 전체 목록 조회 (상태 필터)
    // ─────────────────────────────────────────────
    public Page<QnaResponseDto> getAdminQnaList(QnaStatus qnaStatus, Pageable pageable) {
        Page<Qna> page = (qnaStatus != null)
                ? qnaRepository.findByParentIsNullAndDeletedAtIsNullAndQnaStatus(qnaStatus, pageable)
                : qnaRepository.findByParentIsNullAndDeletedAtIsNull(pageable);
        return page.map(QnaResponseDto::from);
    }

    // 관리자 — 상세 조회
    public QnaResponseDto getAdminQnaDetail(Long id) {
        Qna qna = qnaRepository.findByIdAndDeletedAtIsNull(id)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 QnA입니다."));
        qna.increaseViewCount();
        List<QnaResponseDto> children = qnaRepository.findByParentIdAndDeletedAtIsNull(id)
                .stream().map(QnaResponseDto::from).collect(Collectors.toList());
        return QnaResponseDto.withChildren(qna, children);
    }

    // 관리자 — 상태 변경
    @Transactional
    public void updateQnaStatus(Long id, QnaStatus qnaStatus) {
        Qna qna = qnaRepository.findByIdAndDeletedAtIsNull(id)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 QnA입니다."));
        qna.updateStatus(qnaStatus);
    }

    // 관리자 — 답변 등록
    @Transactional
    public void insertAnswer(Long parentId, QnaRequestDto.Answer dto, Long adminMemberId) {
        Qna parent = qnaRepository.findByIdAndDeletedAtIsNull(parentId)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 QnA입니다."));
        Member admin = memberRepository.findById(adminMemberId)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 회원입니다."));

        Qna answer = Qna.builder()
                .parent(parent)
                .depth(parent.getDepth() + 1)
                .member(admin)
                .category(parent.getCategory())
                .title("RE: " + parent.getTitle())
                .content(dto.getContent())
                .viewCount(0)
                .qnaStatus(QnaStatus.COMPLETE)
                .isSecret(false)
                .build();
        qnaRepository.save(answer);
        parent.updateStatus(QnaStatus.COMPLETE);
    }

    // 관리자 — 답변 수정
    @Transactional
    public void updateAnswer(Long id, QnaRequestDto.Update dto) {
        Qna qna = qnaRepository.findByIdAndDeletedAtIsNull(id)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 QnA입니다."));
        qna.updateContent(dto.getContent());
    }

    // 관리자 — 삭제
    @Transactional
    public void deleteQna(Long id) {
        Qna qna = qnaRepository.findByIdAndDeletedAtIsNull(id)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 QnA입니다."));
        qna.softDelete();
    }

    // ─────────────────────────────────────────────
    // 유저 — 전체 목록 조회 (비밀글 처리 포함)
    // ─────────────────────────────────────────────
    public Page<QnaResponseDto> getQnaList(Pageable pageable) {
        return qnaRepository.findByParentIsNullAndDeletedAtIsNull(pageable)
                .map(QnaResponseDto::from);
    }

    // 유저 — 상세 조회
    public QnaResponseDto getQnaDetail(Long id, Long loginMemberId) {
        Qna qna = qnaRepository.findByIdAndDeletedAtIsNull(id)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 QnA입니다."));
        if (qna.isSecret() && !qna.getMember().getId().equals(loginMemberId)) {
            throw new IllegalArgumentException("비밀글은 작성자만 조회할 수 있습니다.");
        }
        qna.increaseViewCount();
        List<QnaResponseDto> children = qnaRepository.findByParentIdAndDeletedAtIsNull(id)
                .stream().map(QnaResponseDto::from).collect(Collectors.toList());
        return QnaResponseDto.withChildren(qna, children);
    }

    // 유저 — 질문 등록
    @Transactional
    public void writeQna(QnaRequestDto.Write dto, Long memberId) {
        Member member = memberRepository.findById(memberId)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 회원입니다."));
        Qna qna = Qna.builder()
                .parent(null)
                .depth(0)
                .member(member)
                .category(dto.getCategory())
                .title(dto.getTitle())
                .content(dto.getContent())
                .viewCount(0)
                .qnaStatus(QnaStatus.WAITING)
                .isSecret(dto.isSecret())
                .build();
        qnaRepository.save(qna);
    }

    // 유저 — n차 재문의
    @Transactional
    public void writeReply(Long parentId, QnaRequestDto.Reply dto, Long memberId) {
        Qna parent = qnaRepository.findByIdAndDeletedAtIsNull(parentId)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 QnA입니다."));
        if (parent.getDepth() >= 4) {
            throw new IllegalArgumentException("최대 depth(4)를 초과할 수 없습니다.");
        }
        Member member = memberRepository.findById(memberId)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 회원입니다."));
        Qna reply = Qna.builder()
                .parent(parent)
                .depth(parent.getDepth() + 1)
                .member(member)
                .category(parent.getCategory())
                .title(dto.getTitle())
                .content(dto.getContent())
                .viewCount(0)
                .qnaStatus(QnaStatus.WAITING)
                .isSecret(parent.isSecret())
                .build();
        qnaRepository.save(reply);
    }

    // 유저 마이페이지 — 본인 질문 목록
    public Page<QnaResponseDto> getMyQnaList(Long memberId, Pageable pageable) {
        return qnaRepository.findByMemberIdAndParentIsNullAndDeletedAtIsNull(memberId, pageable)
                .map(QnaResponseDto::from);
    }

    // 유저 마이페이지 — 본인 질문 수정
    @Transactional
    public void updateMyQna(Long id, QnaRequestDto.Update dto, Long memberId) {
        Qna qna = qnaRepository.findByIdAndDeletedAtIsNull(id)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 QnA입니다."));
        if (!qna.getMember().getId().equals(memberId)) {
            throw new IllegalArgumentException("본인의 문의만 수정할 수 있습니다.");
        }
        if (qna.getQnaStatus() == QnaStatus.COMPLETE) {
            throw new IllegalArgumentException("답변 완료된 문의는 수정할 수 없습니다.");
        }
        qna.updateContent(dto.getContent());
    }

    // 유저 마이페이지 — 본인 질문 삭제
    @Transactional
    public void deleteMyQna(Long id, Long memberId) {
        Qna qna = qnaRepository.findByIdAndDeletedAtIsNull(id)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 QnA입니다."));
        if (!qna.getMember().getId().equals(memberId)) {
            throw new IllegalArgumentException("본인의 문의만 삭제할 수 있습니다.");
        }
        qna.softDelete();
    }
}