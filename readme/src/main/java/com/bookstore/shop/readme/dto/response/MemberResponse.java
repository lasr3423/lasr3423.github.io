package com.bookstore.shop.readme.dto.response;

import com.bookstore.shop.readme.domain.Member;

public record MemberResponse(
        Long id,
        String email,
        String name,
        String phone,
        String address,
        String memberRole,
        String memberStatus,
        String provider
) {
    public MemberResponse(Member m) {
        this(
                m.getId(),
                m.getEmail(),
                m.getName(),
                m.getPhone(),
                m.getAddress(),
                m.getMemberRole().name(),
                m.getMemberStatus().name(),
                m.getProvider().name()
        );
    }
}
