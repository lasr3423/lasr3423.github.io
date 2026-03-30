package com.bookstore.shop.readme.repository;

import com.bookstore.shop.readme.domain.Member;
import com.bookstore.shop.readme.domain.MemberRole;
import com.bookstore.shop.readme.domain.MemberStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface MemberRepository extends JpaRepository<Member, Long> {
    // 회원 관리
    // 이메일 중복 확인
    Boolean existByEmail(String keyword);
    // 이름 검색
    Page<Member> findByNameContainingIgnoreCase(String keyword, Pageable pageable);
    // 이메일 검색
    Page<Member> findByEmailContainingIgnoreCase(String keyword, Pageable pageable);
    // 이름 또는 이메일로 검색
    Page<Member> findByNameContainingIgnoreCaseOrEmailContainingIgnoreCase(String keyword, Pageable pageable);
    // 상태 별 검색
    Page<Member> findByMemberStatus(MemberStatus status, Pageable pageable);
    // 등급 별 검색
    Page<Member> findByMemberRole(MemberRole role, Pageable pageable);
}
