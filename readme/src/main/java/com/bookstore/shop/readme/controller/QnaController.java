package com.bookstore.shop.readme.controller;

import com.bookstore.shop.readme.domain.Qna;
import com.bookstore.shop.readme.dto.QnaRequestDto;
import com.bookstore.shop.readme.dto.QnaResponseDto;
import com.bookstore.shop.readme.service.QnaService;
import com.bookstore.shop.readme.util.SecurityUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.awt.print.Pageable;

@RestController
@RequestMapping("/qna")
@RequiredArgsConstructor
public class QnaController {

    private final QnaService qnaService;

    // FQ-001: QnA 목록 조회
    @GetMapping("/list")
    public ResponseEntity<Page<QnaResponseDto>> getQnaList(
            @PageableDefault(size = 10) Pageable pageable) {
        return ResponseEntity.ok(qnaService.getQnaList(pageable));
    }

    // FQ-006: QnA 상세 조회
    @GetMapping("/detail/{id}")
    public ResponseEntity<QnaResponseDto> getQnaDetail(@PathVariable Long id) {
        Long loginMemberId = SecurityUtil.getCurrentMemberId();
        return ResponseEntity.ok(qnaService.getQnaDetail(id, loginMemberId));
    }

    // FQ-002, FQ-003: 질문 등록 (비밀글 포함)
    @PreAuthorize("isAuthenticated()")
    @PostMapping("/write")
    public ResponseEntity<Void> writeQna(@RequestBody QnaRequestDto.Write dto) {
        Long memberId = SecurityUtil.getCurrentMemberId();
        qnaService.writeQna(dto, memberId);
        return ResponseEntity.ok().build();
    }

    // FQ-004: n차 재문의
    @PreAuthorize("isAuthenticated()")
    @PostMapping("/{id}")
    public ResponseEntity<Void> writeReply(
            @PathVariable Long id,
            @RequestBody QnaRequestDto.Reply dto) {
        Long memberId = SecurityUtil.getCurrentMemberId();
        qnaService.writeReply(id, dto, memberId);
        return ResponseEntity.ok().build();
    }

    // FM-021: 마이페이지 — 본인 질문 목록
    @PreAuthorize("isAuthenticated()")
    @GetMapping("/mypage/list")
    public ResponseEntity<Page<QnaResponseDto>> getMyQnaList(
            @PageableDefault(size = 10) Pageable pageable) {
        Long memberId = SecurityUtil.getCurrentMemberId();
        return ResponseEntity.ok(qnaService.getMyQnaList(memberId, pageable));
    }

    // FM-023: 마이페이지 — 질문 수정
    @PreAuthorize("isAuthenticated()")
    @PatchMapping("/mypage/update/{id}")
    public ResponseEntity<Void> updateMyQna(
            @PathVariable Long id,
            @RequestBody QnaRequestDto.Update dto) {
        Long memberId = SecurityUtil.getCurrentMemberId();
        qnaService.updateMyQna(id, dto, memberId);
        return ResponseEntity.ok().build();
    }

    // FM-024: 마이페이지 — 질문 삭제
    @PreAuthorize("isAuthenticated()")
    @DeleteMapping("/mypage/{id}")
    public ResponseEntity<Void> deleteMyQna(@PathVariable Long id) {
        Long memberId = SecurityUtil.getCurrentMemberId();
        qnaService.deleteMyQna(id, memberId);
        return ResponseEntity.ok().build();
    }
}