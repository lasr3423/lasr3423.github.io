package com.bookstore.shop.readme.security;

import com.bookstore.shop.readme.domain.Member;
import com.bookstore.shop.readme.domain.MemberRole;
import com.bookstore.shop.readme.domain.MemberStatus;
import lombok.Getter;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;
import java.util.List;

@Getter
public class CustomUserDetails implements UserDetails {

    private final Long memberId;
    private final String email;
    private final String password;
    private final MemberRole memberRole;
    private final MemberStatus memberStatus;

    /** 회원 엔티티 기반 생성 (일반 로그인 / CustomUserDetailsService 사용 시) */
    public CustomUserDetails(Member member) {
        this.memberId = member.getId();
        this.email = member.getEmail();
        this.password = member.getPassword();
        this.memberRole = member.getMemberRole();
        this.memberStatus = member.getMemberStatus();
    }

    /** JWT 클레임 기반 생성 (JwtAuthenticationFilter — DB 조회 없이 토큰만으로 생성) */
    public CustomUserDetails(Long memberId, MemberRole memberRole) {
        this.memberId = memberId;
        this.email = null;
        this.password = null;
        this.memberRole = memberRole;
        this.memberStatus = MemberStatus.ACTIVATE; // JWT 서명/만료 검증 통과 = 유효한 사용자
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return List.of(new SimpleGrantedAuthority("ROLE_" + memberRole.name()));
    }

    @Override
    public String getPassword() {
        return password;
    }

    @Override
    public String getUsername() {
        return email;
    }

    @Override
    public boolean isAccountNonExpired() {
        return true;
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }

    @Override
    public boolean isAccountNonLocked() {
        return memberStatus != MemberStatus.DEACTIVATE;
    }

    @Override
    public boolean isEnabled() {
        return memberStatus == MemberStatus.ACTIVATE;
    }

}
