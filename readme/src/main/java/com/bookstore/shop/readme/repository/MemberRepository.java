package com.bookstore.shop.readme.repository;

import com.bookstore.shop.readme.domain.AuthProvider;
import com.bookstore.shop.readme.domain.Member;
import com.bookstore.shop.readme.domain.MemberRole;
import com.bookstore.shop.readme.domain.MemberStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDateTime;
import java.util.Optional;

public interface MemberRepository extends JpaRepository<Member, Long> {

    // ── 기본 조회 ─────────────────────────────────────────────────────────

    // 이메일로 회원 조회 (로그인, 비밀번호 재설정)
    Optional<Member> findByEmail(String email);

    // 소셜 로그인용: provider + providerId 로 회원 조회
    Optional<Member> findByProviderAndProviderId(AuthProvider provider, String providerId);

    // ── 중복 확인 ─────────────────────────────────────────────────────────

    // 이메일 중복 확인 (회원가입)
    boolean existsByEmail(String email);

    // 이메일 중복 확인 (회원정보 수정 시 본인 제외)
    boolean existsByEmailAndIdNot(String email, Long id);

    // 이름 또는 이메일에 키워드가 포함된 회원 검색
    // → WHERE name LIKE '%keyword%' OR email LIKE '%keyword%'
    Page<Member> findByNameContainingOrEmailContaining(String name, String email, Pageable pageable);

    // 상태별 회원 목록 (ACTIVATE / DEACTIVATE / DELETE)
    Page<Member> findAllByMemberStatus(MemberStatus memberStatus, Pageable pageable);

    // 등급별 회원 목록 (USER / MANAGER / ADMIN)
    Page<Member> findAllByMemberRole(MemberRole memberRole, Pageable pageable);

    // 상태별 회원 수 (대시보드 통계용)
    long countByMemberStatus(MemberStatus memberStatus);

    // 대시보드 — 기간별 신규 가입 수
    long countByCreatedAtBetween(LocalDateTime start, LocalDateTime end);
}
