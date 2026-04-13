package com.bookstore.shop.readme.config;

import com.bookstore.shop.readme.domain.AuthProvider;
import com.bookstore.shop.readme.domain.Member;
import com.bookstore.shop.readme.domain.MemberRole;
import com.bookstore.shop.readme.domain.MemberStatus;
import com.bookstore.shop.readme.repository.MemberRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.core.annotation.Order;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

@Component
@Order(1)
@ConditionalOnProperty(name = "seed.members.enabled", havingValue = "true")
@RequiredArgsConstructor
public class MemberDummyDataInitializer implements CommandLineRunner {

    private final MemberRepository memberRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    public void run(String... args) {
        seedMembers(MemberRole.USER, "user", "일반회원");
        seedMembers(MemberRole.MANAGER, "manager", "매니저");
        seedMembers(MemberRole.ADMIN, "admin", "관리자");
    }

    private void seedMembers(MemberRole role, String emailPrefix, String namePrefix) {
        for (int i = 1; i <= 5; i += 1) {
            String email = emailPrefix + i + "@readme.test";

            if (memberRepository.existsByEmail(email)) {
                continue;
            }

            Member member = new Member();
            member.setEmail(email);
            member.setPassword(passwordEncoder.encode("test1234!"));
            member.setName(namePrefix + i);
            member.setPhone(String.format("010-7000-%04d", role.ordinal() * 100 + i));
            member.setAddress("서울시 테스트구 더미로 " + i);
            member.setMemberRole(role);
            member.setMemberStatus(MemberStatus.ACTIVATE);
            member.setProvider(AuthProvider.LOCAL);
            member.setMarketingAgreed(i % 2 == 0);

            memberRepository.save(member);
        }
    }
}
