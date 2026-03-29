# 구글 OAuth 로그인 구현 상세 설명

> 프로젝트: README 온라인 도서 쇼핑몰
> 기술 스택: Java 21 / Spring Boot 3.5 / PostgreSQL / Vue.js 3
> OAuth 방식: Spring Security OAuth2 Client (Authorization Code Flow)

---

## 📋 목차

1. [OAuth 2.0 개념](#1-oauth-20-개념)
2. [구글 OAuth 전체 흐름](#2-구글-oauth-전체-흐름)
3. [Google Cloud Console 설정](#3-google-cloud-console-설정)
4. [백엔드 구현 (Spring Boot)](#4-백엔드-구현-spring-boot)
5. [프론트엔드 구현 (Vue.js 3)](#5-프론트엔드-구현-vuejs-3)
6. [JWT와의 통합](#6-jwt와의-통합)
7. [현재 프로젝트 산출물 수정 사항](#7-현재-프로젝트-산출물-수정-사항)
8. [현재 프로젝트 적용 체크리스트](#8-현재-프로젝트-적용-체크리스트)

---

## 1. OAuth 2.0 개념

OAuth 2.0은 제3자 애플리케이션이 사용자의 자격 증명(비밀번호) 없이 사용자를 대신해 서비스에 접근할 수 있도록 허가하는 **위임 인증 프로토콜**입니다.

### 등장 배경

```
[과거 방식] 사용자가 구글 비밀번호를 직접 앱에 입력
  → 앱이 비밀번호를 저장 → 보안 위험 매우 높음

[OAuth 방식] 앱은 비밀번호를 알 필요 없음
  → 구글이 직접 인증 → 앱에게 제한된 접근 권한만 부여
```

### Authorization Code Flow (서버 사이드 방식)

현재 프로젝트에서 사용하는 방식으로, 보안이 가장 높습니다.

```
1. 사용자 → 앱: "구글로 로그인" 클릭
2. 앱 → 구글: 인증 페이지로 리다이렉트 (client_id, redirect_uri, scope)
3. 사용자 → 구글: 구글 계정으로 로그인 + 권한 동의
4. 구글 → 앱 서버: redirect_uri로 Authorization Code 전달
5. 앱 서버 → 구글: Authorization Code로 AccessToken 교환
6. 구글 → 앱 서버: 구글 AccessToken + 사용자 정보
7. 앱 서버: 회원 조회/생성 → 자체 JWT 발급
8. 앱 서버 → 클라이언트: JWT 전달
```

> Authorization Code는 브라우저에 짧게 노출되는 1회용 코드이며,
> 실제 구글 AccessToken은 서버 사이드에서만 교환되어 클라이언트에 노출되지 않습니다.

---

## 2. 구글 OAuth 전체 흐름

```
Vue.js                  Spring Boot              Google OAuth
  |                          |                        |
  |── 구글 로그인 버튼 클릭 ──>|                        |
  |                          |                        |
  |<── redirect ─────────────|                        |
  |                          |                        |
  |── GET /oauth2/authorization/google ──────────────>|
  |                          |                        |
  |<── 구글 로그인 페이지 ────────────────────────────|
  |                          |                        |
  |── 구글 계정 로그인 + 동의 ───────────────────────>|
  |                          |                        |
  |                          |<── code 전달 ──────────|
  |                          |    (redirect_uri)       |
  |                          |                        |
  |                          |── code로 token 교환 ──>|
  |                          |<── 구글 AccessToken ───|
  |                          |                        |
  |                          |── 사용자 정보 요청 ────>|
  |                          |<── email, sub, name ───|
  |                          |                        |
  |                          |── DB 조회/자동 회원가입  |
  |                          |── 자체 JWT 발급         |
  |                          |                        |
  |<── redirect 프론트/oauth/callback?token=... ───────|
  |                          |                        |
  |── 토큰 저장 완료          |                        |
```

---

## 3. Google Cloud Console 설정

### 3-1. 프로젝트 생성 및 OAuth 클라이언트 ID 발급

1. [Google Cloud Console](https://console.cloud.google.com) 접속
2. 새 프로젝트 생성 (예: `readme-shop`)
3. `API 및 서비스` → `OAuth 동의 화면` 설정
   - 앱 이름, 사용자 지원 이메일, 개발자 이메일 입력
   - Scope 추가: `email`, `profile`, `openid`
4. `사용자 인증 정보` → `OAuth 2.0 클라이언트 ID` 생성
   - 애플리케이션 유형: **웹 애플리케이션**
   - 승인된 리다이렉션 URI 추가:
     ```
     http://localhost:8080/login/oauth2/code/google   (개발)
     https://your-domain.com/login/oauth2/code/google (운영)
     ```
5. **클라이언트 ID**와 **클라이언트 보안 비밀번호** 복사 및 보관

---

## 4. 백엔드 구현 (Spring Boot)

### 4-1. 의존성 (build.gradle)

```groovy
dependencies {
    implementation 'org.springframework.boot:spring-boot-starter-security'
    implementation 'org.springframework.boot:spring-boot-starter-oauth2-client'
    implementation 'org.springframework.boot:spring-boot-starter-data-jpa'
    implementation 'org.springframework.boot:spring-boot-starter-web'

    // JWT
    implementation 'io.jsonwebtoken:jjwt-api:0.12.6'
    runtimeOnly    'io.jsonwebtoken:jjwt-impl:0.12.6'
    runtimeOnly    'io.jsonwebtoken:jjwt-jackson:0.12.6'
}
```

### 4-2. application.yml

```yaml
spring:
  security:
    oauth2:
      client:
        registration:
          google:
            client-id: YOUR_GOOGLE_CLIENT_ID
            client-secret: YOUR_GOOGLE_CLIENT_SECRET
            redirect-uri: "{baseUrl}/login/oauth2/code/{registrationId}"
            scope:
              - email
              - profile

jwt:
  secret: "your-256-bit-secret-key-must-be-at-least-32-characters-long"
  access-token-expiration: 1800000
  refresh-token-expiration: 604800000

app:
  oauth2:
    # 소셜 로그인 성공 후 프론트엔드 리다이렉트 URL
    redirect-uri: "http://localhost:5173/oauth/callback"
```

### 4-3. Member Entity 수정 — provider 필드 추가

```java
// com/shop/domain/Member.java
@Entity
@Table(name = "member")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@DynamicInsert
public class Member extends BaseEntity {

    @Column(unique = true)
    private String email;

    @Column                        // OAuth 사용자는 비밀번호 없으므로 nullable
    private String password;

    @Column(nullable = false)
    private String name;

    private String phone;
    private String address;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private MemberRole memberRole = MemberRole.USER;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private MemberStatus memberStatus = MemberStatus.ACTIVATE;

    // ── OAuth 소셜 로그인 추가 필드 ──
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private AuthProvider provider = AuthProvider.LOCAL;  // LOCAL / GOOGLE / KAKAO

    @Column
    private String providerId;    // 구글: sub, 카카오: id

    @Column
    private LocalDateTime deletedAt;

    // 일반 회원가입용 팩토리 메서드
    public static Member create(String email, String encodedPassword,
                                String name, String phone, String address) {
        Member m = new Member();
        m.email    = email;
        m.password = encodedPassword;
        m.name     = name;
        m.phone    = phone;
        m.address  = address;
        m.provider = AuthProvider.LOCAL;
        return m;
    }

    // 소셜 로그인용 팩토리 메서드
    public static Member createOAuth(String email, String name,
                                     AuthProvider provider, String providerId) {
        Member m = new Member();
        m.email      = email;
        m.name       = name;
        m.provider   = provider;
        m.providerId = providerId;
        return m;
    }

    public void updateOAuthInfo(String name) {
        this.name = name;
    }
}
```

### 4-4. AuthProvider Enum

```java
// com/shop/domain/AuthProvider.java
public enum AuthProvider {
    LOCAL,    // 일반 이메일 회원가입
    GOOGLE,   // 구글 소셜 로그인
    KAKAO     // 카카오 소셜 로그인
}
```

### 4-5. OAuthUserInfo 인터페이스 + GoogleOAuth2UserInfo

```java
// com/shop/security/oauth2/OAuthUserInfo.java
public interface OAuthUserInfo {
    String getProviderId();
    String getEmail();
    String getName();
    AuthProvider getProvider();
}

// com/shop/security/oauth2/GoogleOAuth2UserInfo.java
public class GoogleOAuth2UserInfo implements OAuthUserInfo {

    private final Map<String, Object> attributes;

    public GoogleOAuth2UserInfo(Map<String, Object> attributes) {
        this.attributes = attributes;
    }

    @Override
    public String getProviderId() {
        return (String) attributes.get("sub");    // 구글 고유 사용자 ID
    }

    @Override
    public String getEmail() {
        return (String) attributes.get("email");
    }

    @Override
    public String getName() {
        return (String) attributes.get("name");
    }

    @Override
    public AuthProvider getProvider() {
        return AuthProvider.GOOGLE;
    }
}
```

### 4-6. CustomOAuth2UserService

OAuth2 인증이 완료된 후 Spring Security가 자동으로 이 서비스를 호출합니다.

```java
// com/shop/security/oauth2/CustomOAuth2UserService.java
@Service
@RequiredArgsConstructor
public class CustomOAuth2UserService extends DefaultOAuth2UserService {

    private final MemberRepository memberRepository;

    @Override
    public OAuth2User loadUser(OAuth2UserRequest userRequest) throws OAuth2AuthenticationException {
        OAuth2User oAuth2User = super.loadUser(userRequest);

        // registrationId: "google" or "kakao"
        String registrationId = userRequest.getClientRegistration().getRegistrationId();

        OAuthUserInfo userInfo = switch (registrationId) {
            case "google" -> new GoogleOAuth2UserInfo(oAuth2User.getAttributes());
            default       -> throw new OAuth2AuthenticationException("지원하지 않는 OAuth 제공자: " + registrationId);
        };

        Member member = processOAuthLogin(userInfo);

        return new DefaultOAuth2User(
            List.of(new SimpleGrantedAuthority("ROLE_" + member.getMemberRole().name())),
            Map.of(
                "memberId",   member.getId(),
                "email",      member.getEmail(),
                "name",       member.getName(),
                "role",       member.getMemberRole().name()
            ),
            "memberId"
        );
    }

    private Member processOAuthLogin(OAuthUserInfo userInfo) {
        // 기존 회원 조회 (provider + providerId 기준)
        return memberRepository
            .findByProviderAndProviderId(userInfo.getProvider(), userInfo.getProviderId())
            .map(member -> {
                // 이미 가입된 회원 → 이름 업데이트 (구글 프로필 변경 반영)
                member.updateOAuthInfo(userInfo.getName());
                return member;
            })
            .orElseGet(() -> {
                // 신규 회원 → 자동 가입
                // 이메일로 기존 일반 회원이 있는지도 확인 (계정 연동)
                memberRepository.findByEmail(userInfo.getEmail())
                    .ifPresent(existing -> {
                        throw new OAuth2AuthenticationException(
                            "이미 이메일(" + userInfo.getEmail() + ")로 가입된 계정이 있습니다. 일반 로그인을 이용해 주세요.");
                    });
                return memberRepository.save(
                    Member.createOAuth(
                        userInfo.getEmail(),
                        userInfo.getName(),
                        userInfo.getProvider(),
                        userInfo.getProviderId()
                    )
                );
            });
    }
}
```

### 4-7. OAuth2AuthenticationSuccessHandler

인증 성공 후 JWT를 발급하고 프론트엔드로 리다이렉트합니다.

```java
// com/shop/security/oauth2/OAuth2AuthenticationSuccessHandler.java
@Component
@RequiredArgsConstructor
public class OAuth2AuthenticationSuccessHandler
        extends SimpleUrlAuthenticationSuccessHandler {

    private final JwtUtil jwtUtil;
    private final RefreshTokenRepository refreshTokenRepository;

    @Value("${app.oauth2.redirect-uri}")
    private String redirectUri;

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request,
                                        HttpServletResponse response,
                                        Authentication authentication) throws IOException {

        DefaultOAuth2User oAuth2User = (DefaultOAuth2User) authentication.getPrincipal();

        Long   memberId = (Long)   oAuth2User.getAttribute("memberId");
        String role     = (String) oAuth2User.getAttribute("role");

        // JWT 발급
        String accessToken  = jwtUtil.generateAccessToken(memberId, role);
        String refreshToken = jwtUtil.generateRefreshToken(memberId);

        // RefreshToken DB 저장
        refreshTokenRepository.findByMemberId(memberId)
            .ifPresentOrElse(
                rt -> rt.rotate(refreshToken, jwtUtil.getRefreshTokenExpiration()),
                () -> refreshTokenRepository.save(
                    RefreshToken.create(memberId, refreshToken,
                                        jwtUtil.getRefreshTokenExpiration()))
            );

        // 프론트엔드로 토큰과 함께 리다이렉트
        String targetUrl = UriComponentsBuilder.fromUriString(redirectUri)
            .queryParam("accessToken", accessToken)
            .queryParam("refreshToken", refreshToken)
            .build().toUriString();

        getRedirectStrategy().sendRedirect(request, response, targetUrl);
    }
}
```

### 4-8. OAuth2AuthenticationFailureHandler

```java
// com/shop/security/oauth2/OAuth2AuthenticationFailureHandler.java
@Component
public class OAuth2AuthenticationFailureHandler
        extends SimpleUrlAuthenticationFailureHandler {

    @Value("${app.oauth2.redirect-uri}")
    private String redirectUri;

    @Override
    public void onAuthenticationFailure(HttpServletRequest request,
                                        HttpServletResponse response,
                                        AuthenticationException exception) throws IOException {

        String targetUrl = UriComponentsBuilder
            .fromUriString(redirectUri.replace("/callback", "/error"))
            .queryParam("error", exception.getLocalizedMessage())
            .build().toUriString();

        getRedirectStrategy().sendRedirect(request, response, targetUrl);
    }
}
```

### 4-9. SecurityConfig — OAuth2 통합

```java
// com/shop/config/SecurityConfig.java
@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
public class SecurityConfig {

    private final JwtAuthenticationFilter jwtAuthFilter;
    private final CustomOAuth2UserService customOAuth2UserService;
    private final OAuth2AuthenticationSuccessHandler oAuth2SuccessHandler;
    private final OAuth2AuthenticationFailureHandler oAuth2FailureHandler;

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
            .csrf(csrf -> csrf.disable())
            .sessionManagement(session ->
                session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
            .authorizeHttpRequests(auth -> auth
                .requestMatchers("/api/auth/**").permitAll()
                .requestMatchers(HttpMethod.GET,
                    "/api/product/**", "/api/notice/**", "/api/category/**").permitAll()
                .requestMatchers("/api/admin/**").hasAnyRole("MANAGER", "ADMIN")
                .anyRequest().authenticated()
            )
            // ── OAuth2 설정 추가 ──
            .oauth2Login(oauth2 -> oauth2
                .userInfoEndpoint(userInfo ->
                    userInfo.userService(customOAuth2UserService))
                .successHandler(oAuth2SuccessHandler)
                .failureHandler(oAuth2FailureHandler)
            )
            .addFilterBefore(jwtAuthFilter, UsernamePasswordAuthenticationFilter.class)
            .exceptionHandling(ex -> ex
                .authenticationEntryPoint((req, res, e) ->
                    res.sendError(HttpServletResponse.SC_UNAUTHORIZED, "인증이 필요합니다."))
            );

        return http.build();
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}
```

### 4-10. MemberRepository 추가 메서드

```java
// com/shop/repository/MemberRepository.java
public interface MemberRepository extends JpaRepository<Member, Long> {
    Optional<Member> findByEmail(String email);
    boolean existsByEmail(String email);
    // 소셜 로그인용 조회
    Optional<Member> findByProviderAndProviderId(AuthProvider provider, String providerId);
}
```

---

## 5. 프론트엔드 구현 (Vue.js 3)

### 5-1. 구글 로그인 버튼

```vue
<!-- src/views/member/SigninView.vue -->
<template>
  <div class="signin-page">
    <h2>로그인</h2>

    <!-- 일반 로그인 폼 -->
    <form @submit.prevent="handleSignin">
      <input v-model="email" type="email" placeholder="이메일" />
      <input v-model="password" type="password" placeholder="비밀번호" />
      <button type="submit">로그인</button>
    </form>

    <div class="divider">또는</div>

    <!-- 구글 로그인 버튼 -->
    <!-- Spring Security가 /oauth2/authorization/google URL을 자동 생성 -->
    <a href="/oauth2/authorization/google" class="btn-google">
      <img src="@/assets/google-icon.svg" alt="Google" />
      구글로 로그인
    </a>
  </div>
</template>
```

### 5-2. OAuth 콜백 페이지

```vue
<!-- src/views/member/OAuthCallbackView.vue -->
<template>
  <div class="oauth-callback">
    <p>로그인 처리 중...</p>
  </div>
</template>

<script setup>
import { onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { useAuthStore } from '@/store/auth'

const route     = useRoute()
const router    = useRouter()
const authStore = useAuthStore()

onMounted(async () => {
  const accessToken  = route.query.accessToken
  const refreshToken = route.query.refreshToken
  const error        = route.query.error

  if (error) {
    console.error('OAuth 로그인 실패:', error)
    router.push('/signin?error=' + encodeURIComponent(error))
    return
  }

  if (accessToken && refreshToken) {
    // Pinia store에 토큰 저장
    authStore.setTokens(accessToken, refreshToken)
    // URL에서 토큰 파라미터 제거 후 메인 페이지로 이동
    router.replace('/')
  }
})
</script>
```

### 5-3. Router 콜백 경로 등록

```javascript
// src/router/index.js
{
  path: '/oauth/callback',
  component: () => import('@/views/member/OAuthCallbackView.vue'),
  // 인증 없이 접근 가능 (토큰을 여기서 받아 저장하는 페이지)
}
```

---

## 6. JWT와의 통합

구글 OAuth 로그인은 **인증(Authentication) 수단**으로만 사용하고,
이후의 모든 API 통신은 **자체 발급한 JWT**로 처리합니다.

```
구글 OAuth → 사용자 신원 확인
           → 자체 JWT 발급 (AccessToken + RefreshToken)
           → 이후 일반 JWT 로그인과 완전히 동일한 방식으로 동작
```

이렇게 하면 OAuth와 일반 로그인 사용자를 동일한 JWT 기반 인증 체계로 통합 관리할 수 있습니다.

---

## 7. 현재 프로젝트 산출물 수정 사항

### DB설계서 — member 테이블 변경

| 컬럼명 | 자료형 | NULL | 기본값 | 설명 |
|---|---|---|---|---|
| `provider` | VARCHAR(20) | ❌ | `'LOCAL'` | 가입 방식 (LOCAL/GOOGLE/KAKAO) |
| `provider_id` | VARCHAR(200) | ✅ | null | OAuth 제공자 고유 ID |

`password` 컬럼 → NULL 허용으로 변경 (OAuth 회원은 비밀번호 없음)

### 클래스 & 시퀀스 다이어그램 추가 항목

**1-1 Entity:**
- `Member` 엔티티에 `provider`, `providerId` 필드 추가
- `AuthProvider` enum (LOCAL/GOOGLE/KAKAO) 추가

**1-2 레이어 구조:**
- `CustomOAuth2UserService`
- `OAuth2AuthenticationSuccessHandler`
- `OAuth2AuthenticationFailureHandler`
- `GoogleOAuth2UserInfo` (OAuthUserInfo 구현체)

**시퀀스 다이어그램 추가:**
- 구글 OAuth 로그인 흐름 (Authorization Code → JWT 발급)

### 프로젝트 구조도 추가 항목

```
com.shop.security/
  ├── JwtTokenProvider
  ├── JwtAuthenticationFilter
  ├── CustomUserDetailsService
  └── oauth2/                        ← 신규 패키지
      ├── CustomOAuth2UserService
      ├── OAuth2AuthenticationSuccessHandler
      ├── OAuth2AuthenticationFailureHandler
      ├── OAuthUserInfo              ← 인터페이스
      └── GoogleOAuth2UserInfo
```

---

## 8. 현재 프로젝트 적용 체크리스트

| 단계 | 작업 항목 |
|---|---|
| 1 | `build.gradle`에 `spring-boot-starter-oauth2-client` 추가 |
| 2 | Google Cloud Console에서 OAuth 클라이언트 ID 발급 |
| 3 | `application.yml`에 Google client-id / client-secret 등록 |
| 4 | `Member` 엔티티에 `provider`, `providerId` 필드 추가 |
| 5 | `AuthProvider` enum 생성 (LOCAL / GOOGLE / KAKAO) |
| 6 | `OAuthUserInfo` 인터페이스 + `GoogleOAuth2UserInfo` 구현체 작성 |
| 7 | `CustomOAuth2UserService` 작성 (자동 회원가입 로직 포함) |
| 8 | `OAuth2AuthenticationSuccessHandler` 작성 (JWT 발급 + 리다이렉트) |
| 9 | `OAuth2AuthenticationFailureHandler` 작성 |
| 10 | `SecurityConfig`에 `.oauth2Login()` 설정 추가 |
| 11 | `MemberRepository`에 `findByProviderAndProviderId()` 메서드 추가 |
| 12 | `DB` `member` 테이블에 `provider`, `provider_id` 컬럼 추가 마이그레이션 |
| 13 | `OAuthCallbackView.vue` 페이지 생성 (토큰 수신 + 저장) |
| 14 | Vue Router에 `/oauth/callback` 경로 등록 |
| 15 | 로그인 페이지에 구글 로그인 버튼 추가 |
| 16 | 신규 OAuth 회원 자동 가입 / 기존 이메일 계정 충돌 예외 처리 테스트 |
