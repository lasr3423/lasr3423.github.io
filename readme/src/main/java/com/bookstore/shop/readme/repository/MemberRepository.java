package com.bookstore.shop.readme.repository;

import com.bookstore.shop.readme.domain.AuthProvider;
import com.bookstore.shop.readme.domain.Member;
import com.bookstore.shop.readme.domain.MemberRole;
import com.bookstore.shop.readme.domain.MemberStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface MemberRepository extends JpaRepository<Member, Long> {
    // 이메일 조회
    Optional<Member> findByEmail(String email);
    // 소셜 로그인용 : provider + providerId 로 회원 조회
    Optional<Member> findByProviderAndProviderId(AuthProvider provider, String providerId);
    // 이메일 중복 확인
    Boolean existsByEmail(String email);
    // 회원 검색 (등급/상태/키워드)
    Page<Member> findAllByKeywordAndRoleAndStatus(Pageable pageable, String keyword, MemberRole role, MemberStatus status);

}
