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

    public CustomUserDetails(Member member) {
        this.memberId = member.getId();
        this.email = member.getEmail();
        this.password = member.getPassword();
        this.memberRole = member.getMemberRole();
        this.memberStatus = member.getMemberStatus();
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
