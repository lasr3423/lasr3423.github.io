package com.bookstore.shop.readme.controller.admin;

import com.bookstore.shop.readme.domain.QnaStatus;
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
@RequestMapping("/admin/qna")
@RequiredArgsConstructor
@PreAuthorize("hasAnyRole('MANAGER', 'ADMIN')")
public class AdminQnaController {

    private final QnaService qnaService;

    // FA-032: QnA 목록 조회
    @GetMapping("/list")
    public ResponseEntity<Page<QnaResponseDto>> getQnaList(
            @RequestParam(required = false) QnaStatus status,
            @PageableDefault(size = 10) Pageable pageable) {
        return ResponseEntity.ok(qnaService.getAdminQnaList(status, pageable));
    }

    // FA-033: QnA 질문 상세 조회
    @GetMapping("/detail/{id}")
    public ResponseEntity<QnaResponseDto> getQnaDetail(@PathVariable Long id) {
        return ResponseEntity.ok(qnaService.getAdminQnaDetail(id));
    }

    // FA-034: QnA 상태 변경
    @PatchMapping("/{id}/status")
    public ResponseEntity<Void> updateStatus(
            @PathVariable Long id,
            @RequestBody QnaRequestDto.StatusUpdate dto) {
        qnaService.updateQnaStatus(id, dto.getQnaStatus());
        return ResponseEntity.ok().build();
    }

    // FA-035: QnA 답변 등록
    @PostMapping("/{id}/insert")
    public ResponseEntity<Void> insertAnswer(
            @PathVariable Long id,
            @RequestBody QnaRequestDto.Answer dto) {
        Long adminId = SecurityUtil.getCurrentMemberId();
        qnaService.insertAnswer(id, dto, adminId);
        return ResponseEntity.ok().build();
    }

    // FA-036: QnA 답변 수정
    @PatchMapping("/update/{id}")
    public ResponseEntity<Void> updateAnswer(
            @PathVariable Long id,
            @RequestBody QnaRequestDto.Update dto) {
        qnaService.updateAnswer(id, dto);
        return ResponseEntity.ok().build();
    }

    // FA-037: QnA 답변 삭제
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteQna(@PathVariable Long id) {
        qnaService.deleteQna(id);
        return ResponseEntity.ok().build();
    }
}