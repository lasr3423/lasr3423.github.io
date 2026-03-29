package com.bookstore.shop.readme.service;


import com.bookstore.shop.readme.domain.AuthProvider;
import com.bookstore.shop.readme.domain.Member;
import com.bookstore.shop.readme.repository.MemberRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional
public class OAuthMemberService {
    private final MemberRepository memberRepository;
    /* 기존 회원 조회 or 신규 자동 가입 */
    public Member findOrCreate(AuthProvider provider, String providerId,
                               String email, String name) {
        return memberRepository
                .findByProviderAndProviderId(provider, providerId)
                .map(existing -> {
                    existing.updateOAuthInfo(name);  // 최신 이름 반영
                    return existing;
                })
                .orElseGet(() -> {
                    // 동일 이메일 일반 계정 충돌 확인
                    if (email != null) {
                        memberRepository.findByEmail(email).ifPresent(dup -> {
                            throw new RuntimeException(
                                    "이미 이메일로 가입된 계정이 있습니다. 일반 로그인을 이용해 주세요.");
                        });
                    }
                    // 이메일 없는 경우 임시 이메일 생성
                    String resolvedEmail = (email != null)
                            ? email
                            : provider.name().toLowerCase() + "_" + providerId + "@oauth.placeholder";

                    return memberRepository.save(
                            Member.createOAuth(resolvedEmail, name, provider, providerId));
                });
    }
}
