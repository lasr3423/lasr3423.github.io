package com.bookstore.shop.readme.service;

import com.bookstore.shop.readme.domain.Member;
import com.bookstore.shop.readme.domain.QnA;
import com.bookstore.shop.readme.domain.QnaStatus;
import com.bookstore.shop.readme.dto.request.QnACreateRequest;
import com.bookstore.shop.readme.dto.request.QnAUpdateRequest;
import com.bookstore.shop.readme.dto.response.QnAResponse;
import com.bookstore.shop.readme.repository.MemberRepository;
import com.bookstore.shop.readme.repository.QnARepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

/**
 * QnA Service — DB설계서 트리 구조 기반 구현
 *
 * 계층 규칙:
 *   depth=0 : 사용자 최초 질문 (parent=null)
 *   depth=1 : 관리자 답변     (parent=depth-0 질문)
 *   depth=2 : 사용자 재문의   (parent=depth-1 답변)
 *   depth=3 : 관리자 재답변   (parent=depth-2 재문의)
 *   depth=4 : 최대 깊이, 이후 추가 불가
 *
 * 답변 등록 = 자식 QnA 레코드 생성 (depth + 1)
 * 답변 완료 시 최상위 질문(depth=0)의 qnaStatus → COMPLETE
 */
@Service
@RequiredArgsConstructor
@Transactional
public class QnAService {

    private final QnARepository    qnaRepository;
    private final MemberRepository memberRepository;

    // ── 공개 QnA 목록 (비밀글 제외, depth=0 질문만) ───────────────────────────
    @Transactional(readOnly = true)
    public ResponseEntity<Page<QnAResponse>> getQnAs(Pageable pageable) {
        return ResponseEntity.ok(
                qnaRepository.findAllByDepthAndIsSecretFalseAndDeletedAtIsNull(0, pageable)
                        .map(QnAResponse::new));
    }

    // ── QnA 단건 상세 조회 — REQ-Q-006 ─────────────────────────────────────
    @Transactional(readOnly = true)
    public ResponseEntity<QnAResponse> getQnADetail(Long qnaId, Long memberId) {
        QnA qna = findActiveQnA(qnaId);
        // 비밀글이면 작성자 또는 관리자만 조회 가능
        if (qna.isSecret() && (memberId == null || !qna.getMember().getId().equals(memberId)))
            throw new RuntimeException("비밀글입니다.");
        return ResponseEntity.ok(new QnAResponse(qna));
    }

    // ── QnA 등록 ─────────────────────────────────────────────────────────────
    // depth=0: parentId=null → 최초 질문
    // depth>0: parentId 있음 → 자식(재문의/답변) 생성
    public ResponseEntity<Long> createQnA(QnACreateRequest req, Long memberId) {
        Member member = memberRepository.findById(memberId)
                .orElseThrow(() -> new RuntimeException("회원을 찾을 수 없습니다."));

        QnA parent = null;
        int depth = 0;
        if (req.parentId() != null) {
            parent = findActiveQnA(req.parentId());
            depth = parent.getDepth() + 1;
            if (depth > 4)
                throw new RuntimeException("최대 답변 깊이(4)를 초과했습니다.");
        }

        QnA qna = QnA.builder()
                .member(member)
                .parent(parent)
                .depth(depth)
                .category(req.category() != null ? req.category() : "기타")
                .title(req.title())
                .content(req.content())
                .isSecret(req.isSecret())
                .build();
        qnaRepository.save(qna);
        return ResponseEntity.status(201).body(qna.getId());
    }

    // ── 관리자 답변 등록 — FA-040 계열 ──────────────────────────────────────
    // 답변 = 질문의 자식 QnA 레코드 생성 (depth = 부모.depth + 1)
    // 최상위 질문(depth=0)의 qnaStatus → COMPLETE 처리
    public ResponseEntity<QnAResponse> answerQnA(Long parentQnaId, String title, String content, Long adminMemberId) {
        QnA parent = findActiveQnA(parentQnaId);
        Member admin = memberRepository.findById(adminMemberId)
                .orElseThrow(() -> new RuntimeException("회원을 찾을 수 없습니다."));

        int childDepth = parent.getDepth() + 1;
        if (childDepth > 4)
            throw new RuntimeException("최대 답변 깊이(4)를 초과했습니다.");

        // 자식 QnA(답변) 레코드 생성
        QnA answer = QnA.builder()
                .member(admin)
                .parent(parent)
                .depth(childDepth)
                .category(parent.getCategory())   // 부모 카테고리 상속
                .title(title)
                .content(content)
                .isSecret(false)                  // 답변은 공개
                .qnaStatus(QnaStatus.COMPLETE)    // 답변 자체 상태
                .build();
        qnaRepository.save(answer);

        // 최상위 질문(depth=0) 찾아서 COMPLETE 처리
        QnA root = findRootQnA(parent);
        root.setQnaStatus(QnaStatus.COMPLETE);
        root.setAnsweredAt(LocalDateTime.now());

        return ResponseEntity.ok(new QnAResponse(root));
    }

    // ── 관리자 QnA 상태 변경 — FA-040 (WAITING/PROCESSING/COMPLETE) ──────────
    public ResponseEntity<String> updateQnAStatus(Long qnaId, String status) {
        QnA qna = findActiveQnA(qnaId);
        qna.setQnaStatus(QnaStatus.valueOf(status.toUpperCase()));
        return ResponseEntity.ok("QnA 상태가 변경되었습니다.");
    }

    // ── QnA 수정 (본인 + 답변 전만) — REQ-M-023 ─────────────────────────────
    public ResponseEntity<QnAResponse> updateQnA(Long qnaId, QnAUpdateRequest req, Long memberId) {
        QnA qna = findActiveQnA(qnaId);
        if (!qna.getMember().getId().equals(memberId))
            throw new RuntimeException("수정 권한이 없습니다.");
        // 답변이 달렸으면(자식 존재) 수정 불가
        boolean hasAnswer = !qnaRepository.findAllByParentIdAndDeletedAtIsNull(qnaId).isEmpty();
        if (hasAnswer)
            throw new RuntimeException("답변이 완료된 QnA는 수정할 수 없습니다.");
        qna.setTitle(req.title());
        qna.setContent(req.content());
        return ResponseEntity.ok(new QnAResponse(qna));
    }

    // ── QnA 삭제 soft delete (본인만) ────────────────────────────────────────
    public ResponseEntity<String> deleteQnA(Long qnaId, Long memberId) {
        QnA qna = findActiveQnA(qnaId);
        if (!qna.getMember().getId().equals(memberId))
            throw new RuntimeException("삭제 권한이 없습니다.");
        qna.setDeletedAt(LocalDateTime.now());
        return ResponseEntity.ok("QnA가 삭제되었습니다.");
    }

    // ── 마이페이지 내 QnA 목록 — REQ-M-021 (depth=0 질문만) ──────────────────
    @Transactional(readOnly = true)
    public ResponseEntity<Page<QnAResponse>> getMyQnAs(Long memberId, Pageable pageable) {
        return ResponseEntity.ok(
                qnaRepository.findAllByDepthAndMemberIdAndDeletedAtIsNull(0, memberId, pageable)
                        .map(QnAResponse::new));
    }

    // ── 마이페이지 내 QnA 상세 — REQ-M-022 ──────────────────────────────────
    @Transactional(readOnly = true)
    public ResponseEntity<QnAResponse> getMyQnADetail(Long qnaId, Long memberId) {
        QnA qna = qnaRepository.findByIdAndMemberIdAndDeletedAtIsNull(qnaId, memberId)
                .orElseThrow(() -> new RuntimeException("QnA를 찾을 수 없거나 조회 권한이 없습니다."));
        return ResponseEntity.ok(new QnAResponse(qna));
    }

    // ── 관리자 QnA 상세 조회 — FA-039 ───────────────────────────────────────
    @Transactional(readOnly = true)
    public ResponseEntity<QnAResponse> getAdminQnADetail(Long qnaId) {
        QnA qna = qnaRepository.findById(qnaId)
                .orElseThrow(() -> new RuntimeException("QnA를 찾을 수 없습니다."));
        return ResponseEntity.ok(new QnAResponse(qna));
    }

    // ── 관리자 답변 수정 — FA-042 ────────────────────────────────────────────
    // 트리 구조: 답변 QnA 레코드(depth>=1)의 content 직접 수정
    public ResponseEntity<QnAResponse> updateAdminAnswer(Long answerQnaId, String content) {
        QnA answerQna = qnaRepository.findById(answerQnaId)
                .orElseThrow(() -> new RuntimeException("답변을 찾을 수 없습니다."));
        if (answerQna.getDepth() == 0)
            throw new RuntimeException("질문 레코드입니다. 답변 ID를 입력하세요.");
        answerQna.setContent(content);
        return ResponseEntity.ok(new QnAResponse(answerQna));
    }

    // ── 관리자 답변 삭제 — FA-043 ────────────────────────────────────────────
    // 답변 QnA 레코드 soft delete + 최상위 질문 상태 WAITING으로 복원
    public ResponseEntity<String> deleteAdminAnswer(Long answerQnaId) {
        QnA answerQna = qnaRepository.findById(answerQnaId)
                .orElseThrow(() -> new RuntimeException("답변을 찾을 수 없습니다."));
        if (answerQna.getDepth() == 0)
            throw new RuntimeException("질문 레코드입니다. 답변 ID를 입력하세요.");

        // 답변 soft delete
        answerQna.setDeletedAt(LocalDateTime.now());

        // 최상위 질문 상태 WAITING으로 복원
        QnA root = findRootQnA(answerQna);
        root.setQnaStatus(QnaStatus.WAITING);
        root.setAnsweredAt(null);

        return ResponseEntity.ok("답변이 삭제되었습니다.");
    }

    // ── 관리자 전체 QnA 목록 (depth=0 질문만) ─────────────────────────────────
    @Transactional(readOnly = true)
    public ResponseEntity<Page<QnAResponse>> getAllQnAs(boolean unansweredOnly, Pageable pageable) {
        Page<QnAResponse> result = unansweredOnly
                ? qnaRepository.findAllByDepthAndQnaStatusAndDeletedAtIsNull(0, QnaStatus.WAITING, pageable)
                        .map(QnAResponse::new)
                : qnaRepository.findAllByDepthAndDeletedAtIsNull(0, pageable)
                        .map(QnAResponse::new);
        return ResponseEntity.ok(result);
    }

    // ── 내부 유틸 ────────────────────────────────────────────────────────────

    /** 삭제되지 않은 QnA 조회 (공통 예외 처리) */
    private QnA findActiveQnA(Long qnaId) {
        QnA qna = qnaRepository.findById(qnaId)
                .orElseThrow(() -> new RuntimeException("QnA를 찾을 수 없습니다."));
        if (qna.getDeletedAt() != null)
            throw new RuntimeException("삭제된 QnA입니다.");
        return qna;
    }

    /**
     * depth=0 최상위 질문 탐색
     * 부모를 따라 올라가며 parent==null인 루트를 반환
     */
    private QnA findRootQnA(QnA qna) {
        QnA current = qna;
        while (current.getParent() != null) {
            current = current.getParent();
        }
        return current;
    }
}
