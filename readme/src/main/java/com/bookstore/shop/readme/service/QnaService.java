package com.bookstore.shop.readme.service;

import com.bookstore.shop.readme.domain.Member;
import com.bookstore.shop.readme.domain.Qna;
import com.bookstore.shop.readme.domain.QnaStatus;
import com.bookstore.shop.readme.repository.MemberRepository;
import com.bookstore.shop.readme.repository.QnaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class QnaService {
    private final QnaRepository qnaRepository;
    private final MemberRepository memberRepository;

    // [설계서 2-7 1단계] 문의 등록
    @Transactional
    public void writeQuestion(Long memberId, String title, String content, String category, Boolean isSecret) {
        Member member = memberRepository.findById(memberId).orElseThrow();

        Qna qna = new Qna();
        qna.setMember(member);
        qna.setTitle(title);
        qna.setContent(content);
        qna.setCategory(category);
        qna.setIsSecret(isSecret != null && isSecret);
        qna.setDepth(0);
        qna.setQnaStatus(QnaStatus.WAITING);

        qnaRepository.save(qna);
    }

    // [설계서 2-7 2단계] 관리자 답변 등록
    @Transactional
    public void answerQuestion(Long parentId, Long adminId, String content) {
        Qna parent = qnaRepository.findById(parentId).orElseThrow();
        Member admin = memberRepository.findById(adminId).orElseThrow();

        Qna answer = new Qna();
        answer.setParentId(parentId);
        answer.setMember(admin);
        answer.setTitle("RE: " + parent.getTitle());
        answer.setContent(content);
        answer.setDepth(1);
        answer.setQnaStatus(QnaStatus.COMPLETE);
        answer.setAnsweredAt(LocalDateTime.now());

        qnaRepository.save(answer);

        // 부모 질문 상태 업데이트
        parent.setQnaStatus(QnaStatus.COMPLETE);
        parent.setAnsweredAt(LocalDateTime.now());
    }

    // [설계서 2-7 3단계] 상세 조회 (비밀글 제약조건 반영)
    public Qna getQnaDetail(Long id, Long memberId) {
        Qna qna = qnaRepository.findById(id).orElseThrow();

        // 제약조건: 비밀글일 경우 작성자 본인 혹은 관리자(임시 로직)만 열람 가능
        if (qna.getIsSecret() && !qna.getMember().getId().equals(memberId)) {
            throw new RuntimeException("비밀글은 작성자만 조회할 수 있습니다.");
        }

        return qna;
    }

    // [설계서 1-2] 문의 삭제 (Soft Delete)
    @Transactional
    public void deleteQuestion(Long id, Long memberId) {
        Qna qna = qnaRepository.findById(id).orElseThrow();

        // 작성자 본인 확인
        if (!qna.getMember().getId().equals(memberId)) {
            throw new RuntimeException("삭제 권한이 없습니다.");
        }

        qna.setDeletedAt(LocalDateTime.now());
    }
}