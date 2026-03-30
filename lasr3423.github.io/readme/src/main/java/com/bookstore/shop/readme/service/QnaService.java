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
public class QnaService {
    private final QnaRepository qnaRepository;
    private final MemberRepository memberRepository;

    @Transactional
    public void answerQuestion(Long parentId, Long adminId, String content) {
        Qna parent = qnaRepository.findById(parentId).orElseThrow();
        Member admin = memberRepository.findById(adminId).orElseThrow();

        // 답변 생성
        Qna answer = new Qna();
        answer.setParentId(parentId);
        answer.setMember(admin);
        answer.setTitle("RE: " + parent.getTitle());
        answer.setContent(content);
        answer.setDepth(1);
        answer.setQnaStatus(QnaStatus.COMPLETE);
        answer.setAnsweredAt(LocalDateTime.now());

        qnaRepository.save(answer);

        // 시퀀스 2-7: 부모 질문 상태 업데이트
        parent.setQnaStatus(QnaStatus.COMPLETE);
        parent.setAnsweredAt(LocalDateTime.now());
    }
}