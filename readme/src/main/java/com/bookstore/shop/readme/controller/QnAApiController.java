package com.bookstore.shop.readme.controller;

import com.bookstore.shop.readme.domain.Member;
import com.bookstore.shop.readme.domain.Product;
import com.bookstore.shop.readme.domain.QnA;
import com.bookstore.shop.readme.dto.request.QnACreateRequest;
import com.bookstore.shop.readme.dto.response.QnAResponse;
import com.bookstore.shop.readme.repository.MemberRepository;
import com.bookstore.shop.readme.repository.ProductRepository;
import com.bookstore.shop.readme.repository.QnARepository;
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

import java.time.LocalDateTime;
import java.util.Map;

// [신규] Q&A API
// - GET /api/qna?productId=  : 특정 상품 공개 QnA 목록
// - POST /api/qna             : QnA 등록 (로그인 필수)
// - POST /api/qna/{id}/answer : 관리자 답변 등록
// - DELETE /api/qna/{id}      : 삭제 soft delete (본인 또는 관리자)
@RestController
@RequestMapping("/api/qna")
@RequiredArgsConstructor
public class QnAApiController {

    private final QnARepository     qnaRepository;
    private final MemberRepository  memberRepository;
    private final ProductRepository productRepository;

    @GetMapping
    public ResponseEntity<Page<QnAResponse>> getQnAs(
            @RequestParam Long productId,
            @PageableDefault(size = 10, sort = "createdAt",
                    direction = Sort.Direction.DESC) Pageable pageable) {
        return ResponseEntity.ok(
                qnaRepository.findAllByProductIdAndSecretFalseAndDeletedAtIsNull(productId, pageable)
                        .map(QnAResponse::new));
    }

    @PostMapping
    public ResponseEntity<Long> createQnA(
            @RequestBody QnACreateRequest req,
            @AuthenticationPrincipal CustomUserDetails user) {
        Member member = memberRepository.findById(user.getMemberId())
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

    /** 관리자 답변 등록 */
    @PostMapping("/{qnaId}/answer")
    @PreAuthorize("hasAnyRole('MANAGER', 'ADMIN')")
    public ResponseEntity<QnAResponse> answerQnA(
            @PathVariable Long qnaId,
            @RequestBody Map<String, String> body,
            @AuthenticationPrincipal CustomUserDetails user) {
        QnA qna = qnaRepository.findById(qnaId)
                .orElseThrow(() -> new RuntimeException("QnA를 찾을 수 없습니다."));
        Member admin = memberRepository.findById(user.getMemberId())
                .orElseThrow(() -> new RuntimeException("회원을 찾을 수 없습니다."));
        qna.setAnswer(body.get("answer"));
        qna.setAnsweredBy(admin);
        qna.setAnsweredAt(LocalDateTime.now());
        qnaRepository.save(qna);
        return ResponseEntity.ok(new QnAResponse(qna));
    }

    @DeleteMapping("/{qnaId}")
    public ResponseEntity<String> deleteQnA(
            @PathVariable Long qnaId,
            @AuthenticationPrincipal CustomUserDetails user) {
        QnA qna = qnaRepository.findById(qnaId)
                .orElseThrow(() -> new RuntimeException("QnA를 찾을 수 없습니다."));
        if (!qna.getMember().getId().equals(user.getMemberId())) {
            throw new RuntimeException("삭제 권한이 없습니다.");
        }
        qna.setDeletedAt(LocalDateTime.now()); // soft delete
        qnaRepository.save(qna);
        return ResponseEntity.ok("QnA가 삭제되었습니다.");
    }
}
