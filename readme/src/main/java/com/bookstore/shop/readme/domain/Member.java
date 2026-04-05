package com.bookstore.shop.readme.domain;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Entity
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Table(name = "member")
public class Member extends BaseEntity {

    @Column(length = 100, nullable = false, unique = true)
    private String email;

    @Column(length = 255)
    private String password;

    @Column(length = 50, nullable = false)
    private String name;

    @Column(length = 20)
    private String phone;

    @Column(length = 100)
    private String address;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private MemberRole memberRole = MemberRole.USER;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private MemberStatus memberStatus = MemberStatus.ACTIVATE;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private AuthProvider provider = AuthProvider.LOCAL;

    @Column(name = "provider_id")
    private String providerId;

    @Column(nullable = false)
    private Boolean marketingAgreed = false;

    @Column(name = "deleted_at", columnDefinition = "TIMESTAMP DEFAULT NULL")
    private LocalDateTime deletedAt;

    /* ── 일반 회원가입 팩토리 ── */
    public static Member create(String email,
                                String encodedPassword,
                                String name,
                                String phone,
                                String address,
                                Boolean marketingAgreed) {
        Member m = new Member();
        m.email = email;
        m.password = encodedPassword;
        m.name = name;
        m.phone = phone;
        m.address = address;
        m.provider = AuthProvider.LOCAL;
        m.marketingAgreed = (marketingAgreed != null) ? marketingAgreed : false;
        return m;
    }

    /* ── 소셜 로그인 팩토리 ── */
    public static Member createOAuth(String email,
                                     String name,
                                     AuthProvider provider,
                                     String providerId) {
        Member m = new Member();
        m.email = email;
        m.name = name;
        m.provider = provider;
        m.providerId = providerId;
        return m;
    }

    public void updateOAuthInfo(String name) {
        this.name = name;
    }

    public void updateInfo(String name, String phone, String address) {
        this.name = name; this.phone = phone; this.address = address;
    }

    public void updatePassword(String encodedPassword) {
        this.password = encodedPassword;
    }

    public void withdraw() {
        this.memberStatus = MemberStatus.DELETE;
        this.deletedAt = LocalDateTime.now();
    }

}