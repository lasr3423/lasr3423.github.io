package com.bookstore.shop.readme.repository;

import com.bookstore.shop.readme.domain.AuthProvider;
import com.bookstore.shop.readme.domain.Member;
import com.bookstore.shop.readme.domain.MemberRole;
import com.bookstore.shop.readme.domain.MemberStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.Optional;

public interface MemberRepository extends JpaRepository<Member, Long> {

    Optional<Member> findByEmail(String email);

    Optional<Member> findByProviderAndProviderId(AuthProvider provider, String providerId);

    boolean existsByEmail(String email);

    boolean existsByEmailAndIdNot(String email, Long id);

    Page<Member> findByNameContainingOrEmailContaining(String name, String email, Pageable pageable);

    Page<Member> findAllByMemberStatus(MemberStatus memberStatus, Pageable pageable);

    @Query("""
            SELECT m
            FROM Member m
            WHERE (:keyword IS NULL OR m.name LIKE %:keyword% OR m.email LIKE %:keyword%)
              AND (:status IS NULL OR m.memberStatus = :status)
            """)
    Page<Member> searchByKeywordAndStatus(
            @Param("keyword") String keyword,
            @Param("status") MemberStatus status,
            Pageable pageable);

    Page<Member> findAllByMemberRole(MemberRole memberRole, Pageable pageable);

    long countByMemberStatus(MemberStatus memberStatus);

    long countByCreatedAtBetween(LocalDateTime start, LocalDateTime end);
}
