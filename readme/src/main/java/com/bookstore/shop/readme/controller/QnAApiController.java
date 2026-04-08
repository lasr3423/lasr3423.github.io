package com.bookstore.shop.readme.controller;

import com.bookstore.shop.readme.dto.request.QnACreateRequest;
import com.bookstore.shop.readme.dto.request.QnAUpdateRequest;
import com.bookstore.shop.readme.dto.response.QnAResponse;
import com.bookstore.shop.readme.security.CustomUserDetails;
import com.bookstore.shop.readme.service.QnAService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/qna")
@RequiredArgsConstructor
public class QnAApiController {

    private final QnAService qnaService;

    // 공개 QnA 목록 (비밀글 제외, depth=0 질문만) — REQ-Q-001
    @GetMapping
    public ResponseEntity<Page<QnAResponse>> getQnAs(
            @PageableDefault(size = 10, sort = "createdAt",
                    direction = Sort.Direction.DESC) Pageable pageable) {
        return qnaService.getQnAs(pageable);
    }

    // QnA 단건 상세 조회 — REQ-Q-006 (비밀글: 본인/관리자만)
    @GetMapping("/{qnaId}")
    public ResponseEntity<QnAResponse> getQnADetail(
            @PathVariable Long qnaId,
            @AuthenticationPrincipal CustomUserDetails user) {
        Long memberId = (user != null) ? user.getMemberId() : null;
        return qnaService.getQnADetail(qnaId, memberId);
    }

    // QnA 등록 — REQ-Q-002
    // parentId=null → depth=0 최초 질문 / parentId 있음 → 재문의 (depth+1)
    @PostMapping
    public ResponseEntity<Long> createQnA(
            @RequestBody QnACreateRequest req,
            @AuthenticationPrincipal CustomUserDetails user) {
        return qnaService.createQnA(req, user.getMemberId());
    }

    // QnA 수정 (본인 + 미답변만) — REQ-M-023
    @PutMapping("/{qnaId}")
    public ResponseEntity<QnAResponse> updateQnA(
            @PathVariable Long qnaId,
            @RequestBody QnAUpdateRequest req,
            @AuthenticationPrincipal CustomUserDetails user) {
        return qnaService.updateQnA(qnaId, req, user.getMemberId());
    }

    // QnA 삭제 — REQ-M-024
    @DeleteMapping("/{qnaId}")
    public ResponseEntity<String> deleteQnA(
            @PathVariable Long qnaId,
            @AuthenticationPrincipal CustomUserDetails user) {
        return qnaService.deleteQnA(qnaId, user.getMemberId());
    }
}
