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
        qna.setViewCount(0);

        qnaRepository.save(qna);
    }

    @Transactional
    public void answerQuestion(Long parentId, Long adminId, String content) {
        Qna parent = qnaRepository.findById(parentId).orElseThrow();
        Member admin = memberRepository.findById(adminId).orElseThrow();

        Qna answer = new Qna();
        answer.setParentId(parentId);
        answer.setMember(admin);
        answer.setDepth(1);
        answer.setTitle("RE: " + parent.getTitle());
        answer.setContent(content);
        answer.setCategory(parent.getCategory());
        answer.setQnaStatus(QnaStatus.COMPLETE);
        answer.setViewCount(0);

        qnaRepository.save(answer);

        parent.setQnaStatus(QnaStatus.COMPLETE);
        parent.setAnsweredAt(LocalDateTime.now());
    }

    @Transactional
    public Qna getQnaDetail(Long id, Long memberId) {
        Qna qna = qnaRepository.findByIdAndDeletedAtIsNull(id).orElseThrow();

        if (qna.getIsSecret() && !qna.getMember().getId().equals(memberId)) {
            throw new RuntimeException("비밀글 권한이 없습니다.");
        }

        qna.setViewCount(qna.getViewCount() + 1);
        return qna;
    }

    /** * [추가된 부분] 컨트롤러의 에러를 해결하는 메서드입니다.
     * 설계서 v1.3 Soft Delete 반영
     */
    @Transactional
    public void deleteQuestion(Long id, Long memberId) {
        // 1. 존재하는 질문인지 확인
        Qna qna = qnaRepository.findByIdAndDeletedAtIsNull(id)
                .orElseThrow(() -> new RuntimeException("존재하지 않거나 이미 삭제된 문의입니다."));

        // 2. [설계서 제약조건] 작성자 본인인지 확인
        if (!qna.getMember().getId().equals(memberId)) {
            throw new RuntimeException("본인의 문의글만 삭제할 수 있습니다.");
        }

        // 3. [설계서 v1.3 물리 삭제 대신 Soft Delete 처리]
        qna.setDeletedAt(LocalDateTime.now());
    }
}