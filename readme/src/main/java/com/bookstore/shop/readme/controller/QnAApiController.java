package com.bookstore.shop.readme.controller;

import com.bookstore.shop.readme.dto.request.QnACreateRequest;
import com.bookstore.shop.readme.dto.response.QnAResponse;
import com.bookstore.shop.readme.security.CustomUserDetails;
import com.bookstore.shop.readme.service.QnAService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

// [수정] Repository 직접 주입 제거 → QnAService 위임으로 변경
@RestController
@RequestMapping("/api/qna")
@RequiredArgsConstructor
public class QnAApiController {

    private final QnAService qnaService;

    @GetMapping
    public ResponseEntity<Page<QnAResponse>> getQnAs(
            @RequestParam Long productId,
            @PageableDefault(size = 10, sort = "createdAt",
                    direction = Sort.Direction.DESC) Pageable pageable) {
        return qnaService.getQnAs(productId, pageable);
    }

    @PostMapping
    public ResponseEntity<Long> createQnA(
            @RequestBody QnACreateRequest req,
            @AuthenticationPrincipal CustomUserDetails user) {
        return qnaService.createQnA(req, user.getMemberId());
    }

    @PostMapping("/{qnaId}/answer")
    @PreAuthorize("hasAnyRole('MANAGER', 'ADMIN')")
    public ResponseEntity<QnAResponse> answerQnA(
            @PathVariable Long qnaId,
            @RequestBody Map<String, String> body,
            @AuthenticationPrincipal CustomUserDetails user) {
        return qnaService.answerQnA(qnaId, body.get("answer"), user.getMemberId());
    }

    @DeleteMapping("/{qnaId}")
    public ResponseEntity<String> deleteQnA(
            @PathVariable Long qnaId,
            @AuthenticationPrincipal CustomUserDetails user) {
        return qnaService.deleteQnA(qnaId, user.getMemberId());
    }
}
