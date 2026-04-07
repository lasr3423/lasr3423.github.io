package com.bookstore.shop.readme.service;

import com.bookstore.shop.readme.domain.AuthProvider;
import com.bookstore.shop.readme.domain.Member;
import com.bookstore.shop.readme.domain.MemberStatus;
import com.bookstore.shop.readme.domain.RefreshToken;
import com.bookstore.shop.readme.dto.ResetPasswordRequest;
import com.bookstore.shop.readme.dto.SigninRequest;
import com.bookstore.shop.readme.dto.SignupRequest;
import com.bookstore.shop.readme.dto.TokenResponse;
import com.bookstore.shop.readme.dto.request.ChangePasswordRequest;
import com.bookstore.shop.readme.dto.request.UpdateMemberRequest;
import com.bookstore.shop.readme.dto.response.MemberResponse;
import com.bookstore.shop.readme.repository.MemberRepository;
import com.bookstore.shop.readme.repository.RefreshTokenRepository;
import com.bookstore.shop.readme.security.TokenBlacklistStore;
import com.bookstore.shop.readme.util.JwtUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional
public class MemberService {

    private final MemberRepository       memberRepository;
    private final RefreshTokenRepository refreshTokenRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;
    private final TokenBlacklistStore blacklistStore;

    // 회원가입
    public ResponseEntity<String> signup(SignupRequest req) {
        if (memberRepository.existsByEmail(req.email()))
            throw new RuntimeException("이미 사용 중인 이메일입니다.");

        memberRepository.save(Member.create(
                req.email(),
                passwordEncoder.encode(req.password()),
                req.name(),
                req.phone(),
                req.address(),
                req.marketingAgreed()
        ));
        return ResponseEntity.status(201).body("회원가입이 완료되었습니다.");
    }

    // 로그인
    public ResponseEntity<TokenResponse> signin(SigninRequest req) {
        Member member = memberRepository.findByEmail(req.email())
                .orElseThrow(() -> new RuntimeException("이메일 혹은 패스워드 오류입니다."));

        if (!passwordEncoder.matches(req.password(), member.getPassword()))
            throw new RuntimeException("이메일 혹은 패스워드 오류입니다.");

        if (member.getMemberStatus() != MemberStatus.ACTIVATE)
            throw new RuntimeException("접근이 제한된 계정입니다.");

        return ResponseEntity.ok(issueTokens(member));
    }

    // 비밀번호 재설정 (이메일 인증 없이 임시 재설정)
    public ResponseEntity<String> resetPassword(ResetPasswordRequest req) {
        Member member = memberRepository.findByEmail(req.email())
                .orElseThrow(() -> new RuntimeException("가입된 이메일이 없습니다."));

        if (member.getProvider() != AuthProvider.LOCAL)
            throw new RuntimeException("소셜 로그인 계정은 비밀번호를 변경할 수 없습니다.");
        if (!req.newPassword().equals(req.confirmPassword()))
            throw new RuntimeException("비밀번호가 일치하지 않습니다.");
        member.updatePassword(passwordEncoder.encode((req.newPassword())));
        return ResponseEntity.ok("비밀번호가 변경되었습니다.");
    }

    // 로그아웃
    public ResponseEntity<String> signout(String accessToken) {
        Long memberId = jwtUtil.getMemberId(accessToken);
        blacklistStore.add(accessToken);
        refreshTokenRepository.deleteByMemberId(memberId);
        return ResponseEntity.ok("로그아웃 되었습니다.");
    }

    // RefreshToken → 새 AccessToken 발급 (Rotation)
    public ResponseEntity<TokenResponse> refresh(String refreshToken) {
        if (!jwtUtil.validateToken(refreshToken))
            throw new RuntimeException("유효하지 않은 RefreshToken입니다.");

        Long memberId = jwtUtil.getMemberId(refreshToken);
        RefreshToken stored = refreshTokenRepository.findByMemberId(memberId)
                .orElseThrow(() -> new RuntimeException("로그인이 필요합니다."));

        if (!stored.getToken().equals(refreshToken) || stored.isExpired())
            throw new RuntimeException("만료되었거나 유효하지 않은 RefreshToken입니다.");

        Member member = memberRepository.findById(memberId)
                .orElseThrow(() -> new RuntimeException("회원을 찾을 수 없습니다."));

        String newAccess  = jwtUtil.generateAccessToken(memberId, member.getMemberRole().name());
        String newRefresh = jwtUtil.generateRefreshToken(memberId);
        stored.rotate(newRefresh, jwtUtil.getRefreshTokenExpiration());

        return ResponseEntity.ok(new TokenResponse(newAccess, newRefresh));
    }

    // ── 마이페이지 기능 ────────────────────────────────────────────────────

    // 내 정보 조회
    @Transactional(readOnly = true)
    public ResponseEntity<MemberResponse> getMyInfo(Long memberId) {
        Member member = memberRepository.findById(memberId)
                .orElseThrow(() -> new RuntimeException("회원을 찾을 수 없습니다."));
        return ResponseEntity.ok(new MemberResponse(member));
    }

    // 회원 정보 수정 (이름 / 전화번호 / 주소)
    public ResponseEntity<MemberResponse> updateInfo(Long memberId, UpdateMemberRequest req) {
        Member member = memberRepository.findById(memberId)
                .orElseThrow(() -> new RuntimeException("회원을 찾을 수 없습니다."));
        member.updateInfo(req.name(), req.phone(), req.address());
        return ResponseEntity.ok(new MemberResponse(member));
    }

    // 비밀번호 변경 (현재 비밀번호 확인 필수)
    public ResponseEntity<String> changePassword(Long memberId, ChangePasswordRequest req) {
        Member member = memberRepository.findById(memberId)
                .orElseThrow(() -> new RuntimeException("회원을 찾을 수 없습니다."));
        if (member.getProvider() != AuthProvider.LOCAL)
            throw new RuntimeException("소셜 로그인 계정은 비밀번호를 변경할 수 없습니다.");
        if (!passwordEncoder.matches(req.currentPassword(), member.getPassword()))
            throw new RuntimeException("현재 비밀번호가 일치하지 않습니다.");
        if (!req.newPassword().equals(req.confirmPassword()))
            throw new RuntimeException("새 비밀번호가 일치하지 않습니다.");
        member.updatePassword(passwordEncoder.encode(req.newPassword()));
        return ResponseEntity.ok("비밀번호가 변경되었습니다.");
    }

    // 회원 탈퇴 (soft delete — memberStatus = DELETE, deleted_at 기록)
    public ResponseEntity<String> withdraw(Long memberId) {
        Member member = memberRepository.findById(memberId)
                .orElseThrow(() -> new RuntimeException("회원을 찾을 수 없습니다."));
        member.withdraw();
        refreshTokenRepository.deleteByMemberId(memberId);
        return ResponseEntity.ok("회원 탈퇴가 완료되었습니다.");
    }

    // ── 토큰 발급 공통 메서드 ─────────────────────────────────────────────
    private TokenResponse issueTokens(Member member) {
        String accessToken  = jwtUtil.generateAccessToken(member.getId(), member.getMemberRole().name());
        String refreshToken = jwtUtil.generateRefreshToken(member.getId());

        refreshTokenRepository.findByMemberId(member.getId())
                .ifPresentOrElse(
                        rt -> rt.rotate(refreshToken, jwtUtil.getRefreshTokenExpiration()),
                        () -> refreshTokenRepository.save(
                                RefreshToken.create(member.getId(), refreshToken,
                                        jwtUtil.getRefreshTokenExpiration()))
                );
        return new TokenResponse(accessToken, refreshToken);
    }
}
