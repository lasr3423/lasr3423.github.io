package com.bookstore.shop.readme.service;

import com.bookstore.shop.readme.domain.Member;
import com.bookstore.shop.readme.domain.Product;
import com.bookstore.shop.readme.domain.QnA;
import com.bookstore.shop.readme.dto.request.QnACreateRequest;
import com.bookstore.shop.readme.dto.response.QnAResponse;
import com.bookstore.shop.readme.repository.MemberRepository;
import com.bookstore.shop.readme.repository.ProductRepository;
import com.bookstore.shop.readme.repository.QnARepository;
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
public class QnAService {

    private final QnARepository     qnaRepository;
    private final MemberRepository  memberRepository;
    private final ProductRepository productRepository;

    // ── 특정 상품 공개 QnA 목록 ──────────────────────────────────────────────
    @Transactional(readOnly = true)
    public ResponseEntity<Page<QnAResponse>> getQnAs(Long productId, Pageable pageable) {
        return ResponseEntity.ok(
                qnaRepository.findAllByProductIdAndSecretFalseAndDeletedAtIsNull(productId, pageable)
                        .map(QnAResponse::new));
    }

    // ── QnA 등록 ─────────────────────────────────────────────────────────────
    public ResponseEntity<Long> createQnA(QnACreateRequest req, Long memberId) {
        Member member = memberRepository.findById(memberId)
                .orElseThrow(() -> new RuntimeException("회원을 찾을 수 없습니다."));
        Product product = null;
        if (req.productId() != null) {
            product = productRepository.findById(req.productId())
                    .orElseThrow(() -> new RuntimeException("상품을 찾을 수 없습니다."));
        }
        QnA qna = QnA.builder()
                .member(member)
                .product(product)
                .title(req.title())
                .content(req.content())
                .secret(req.secret())
                .build();
        qnaRepository.save(qna);
        return ResponseEntity.status(201).body(qna.getId());
    }

    // ── 관리자 답변 등록 ─────────────────────────────────────────────────────
    public ResponseEntity<QnAResponse> answerQnA(Long qnaId, String answer, Long adminMemberId) {
        QnA qna = qnaRepository.findById(qnaId)
                .orElseThrow(() -> new RuntimeException("QnA를 찾을 수 없습니다."));
        Member admin = memberRepository.findById(adminMemberId)
                .orElseThrow(() -> new RuntimeException("회원을 찾을 수 없습니다."));
        qna.setAnswer(answer);
        qna.setAnsweredBy(admin);
        qna.setAnsweredAt(LocalDateTime.now());
        return ResponseEntity.ok(new QnAResponse(qna));
    }

    // ── QnA 삭제 soft delete (본인만) ────────────────────────────────────────
    public ResponseEntity<String> deleteQnA(Long qnaId, Long memberId) {
        QnA qna = qnaRepository.findById(qnaId)
                .orElseThrow(() -> new RuntimeException("QnA를 찾을 수 없습니다."));
        if (!qna.getMember().getId().equals(memberId))
            throw new RuntimeException("삭제 권한이 없습니다.");
        qna.setDeletedAt(LocalDateTime.now());
        return ResponseEntity.ok("QnA가 삭제되었습니다.");
    }

    // ── 관리자 전체 QnA 목록 ─────────────────────────────────────────────────
    @Transactional(readOnly = true)
    public ResponseEntity<Page<QnAResponse>> getAllQnAs(boolean unansweredOnly, Pageable pageable) {
        Page<QnAResponse> result = unansweredOnly
                ? qnaRepository.findAllByAnswerIsNullAndDeletedAtIsNull(pageable).map(QnAResponse::new)
                : qnaRepository.findAllByDeletedAtIsNull(pageable).map(QnAResponse::new);
        return ResponseEntity.ok(result);
    }
}
