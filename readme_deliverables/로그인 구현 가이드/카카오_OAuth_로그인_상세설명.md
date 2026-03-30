# 카카오 OAuth 로그인 구현 상세 설명

> 프로젝트: README 온라인 도서 쇼핑몰
> 기술 스택: Java 21 / Spring Boot 3.5 / PostgreSQL / Vue.js 3
> OAuth 방식: 수동 구현 (Kakao REST API 직접 호출)

---

## 📋 목차

1. [구글 OAuth와의 차이점](#1-구글-oauth와의-차이점)
2. [카카오 OAuth 전체 흐름](#2-카카오-oauth-전체-흐름)
3. [Kakao Developers 설정](#3-kakao-developers-설정)
4. [백엔드 구현 (Spring Boot)](#4-백엔드-구현-spring-boot)
5. [프론트엔드 구현 (Vue.js 3)](#5-프론트엔드-구현-vuejs-3)
6. [구글 + 카카오 공통 처리 구조](#6-구글--카카오-공통-처리-구조)
7. [현재 프로젝트 산출물 수정 사항](#7-현재-프로젝트-산출물-수정-사항)
8. [현재 프로젝트 적용 체크리스트](#8-현재-프로젝트-적용-체크리스트)

---

## 1. 구글 OAuth와의 차이점

| 항목 | 구글 OAuth | 카카오 OAuth |
|---|---|---|
| Spring 기본 지원 | ✅ 내장 (`google`) | ❌ 직접 등록 필요 |
| 구현 방식 | `oauth2-client` 자동 처리 | 수동 REST API 호출 (권장) |
| 사용자 ID 키 | `sub` (String UUID) | `id` (Long 정수) |
| 이메일 제공 | 기본 제공 | 비즈니스 앱 검수 후 제공 (선택 동의) |
| 닉네임 위치 | 루트 레벨 `name` | `kakao_account.profile.nickname` |
| 토큰 교환 URL | `https://oauth2.googleapis.com/token` | `https://kauth.kakao.com/oauth/token` |
| 사용자 정보 URL | `https://www.googleapis.com/oauth2/v3/userinfo` | `https://kapi.kakao.com/v2/user/me` |
| redirect_uri 등록 | Google Cloud Console | Kakao Developers |

### 왜 수동 구현을 권장하는가

`spring-boot-starter-oauth2-client`로도 카카오를 처리할 수 있지만,
수동 구현을 사용하면 다음 장점이 있습니다.

- 카카오 API 응답 구조에 맞게 자유롭게 파싱 가능
- `scope`, 동의 항목, 추가 파라미터 제어가 쉬움
- 카카오 API 버전 변경에 유연하게 대응
- 실무에서 더 많이 사용되는 방식

---

## 2. 카카오 OAuth 전체 흐름

```
Vue.js                  Spring Boot              Kakao OAuth
  |                          |                        |
  |── 카카오 로그인 클릭 ─────>|                        |
  |<── 카카오 인증 URL 반환 ───|                        |
  |                          |                        |
  |── 카카오 인증 페이지로 이동 ────────────────────────>|
  |<── 카카오 로그인 + 동의 ──────────────────────────|
  |                          |                        |
  |                          |<── code 전달 ──────────|
  |                          |    (/oauth/kakao/callback)
  |                          |                        |
  |                          |── code로 토큰 교환 ────>|
  |                          |   kauth.kakao.com/oauth/token
  |                          |<── kakaoAccessToken ───|
  |                          |                        |
  |                          |── 사용자 정보 요청 ────>|
  |                          |   kapi.kakao.com/v2/user/me
  |                          |<── id, email, nickname |
  |                          |                        |
  |                          |── DB 조회/자동 회원가입  |
  |                          |── 자체 JWT 발급         |
  |                          |                        |
  |<── redirect 프론트/oauth/callback?token=... ───────|
  |                          |                        |
  |── 토큰 저장 완료          |                        |
```

---

## 3. Kakao Developers 설정

### 3-1. 애플리케이션 등록

1. [Kakao Developers](https://developers.kakao.com) 접속 → 로그인
2. `내 애플리케이션` → `애플리케이션 추가하기`
   - 앱 이름, 사업자명 입력
3. 앱 생성 후 **REST API 키** 복사

### 3-2. 플랫폼 설정 (Web)

`앱 설정` → `플랫폼` → `Web 플랫폼 등록`

```
사이트 도메인:
  http://localhost:8080   (개발)
  https://your-domain.com (운영)
```

### 3-3. Redirect URI 등록

`제품 설정` → `카카오 로그인` → `활성화` ON
`Redirect URI` 추가:

```
http://localhost:8080/api/oauth/kakao/callback   (개발)
https://your-domain.com/api/oauth/kakao/callback (운영)
```

### 3-4. 동의 항목 설정

`제품 설정` → `카카오 로그인` → `동의 항목`

| 항목 | 설정 | 비고 |
|---|---|---|
| 닉네임 | 필수 동의 | 프로필 이름 |
| 프로필 사진 | 선택 동의 | |
| 카카오계정(이메일) | 선택 동의 | 비즈니스 앱 검수 필요 (개발 중에는 테스터 계정만 가능) |

> ⚠️ 이메일은 카카오 비즈니스 앱 검수를 통과해야 일반 사용자에게 받을 수 있습니다.
> 개발 단계에서는 Kakao Developers에 등록된 테스터 계정만 이메일을 제공받을 수 있습니다.

### 3-5. Client Secret 설정 (권장)

`제품 설정` → `카카오 로그인` → `보안` → Client Secret 코드 생성 및 활성화

---

## 4. 백엔드 구현 (Spring Boot)

> OAuth2 Client 의존성을 추가하지 않고 `spring-boot-starter-web`의 RestTemplate 또는 WebClient를 사용합니다.

### 4-1. 의존성 (build.gradle)

```groovy
dependencies {
    implementation 'org.springframework.boot:spring-boot-starter-security'
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
kakao:
  client-id: YOUR_KAKAO_REST_API_KEY
  client-secret: YOUR_KAKAO_CLIENT_SECRET   # 선택 (보안 설정 활성화 시 필수)
  redirect-uri: http://localhost:8080/api/oauth/kakao/callback

app:
  oauth2:
    redirect-uri: "http://localhost:5173/oauth/callback"

jwt:
  secret: "your-256-bit-secret-key-must-be-at-least-32-characters-long"
  access-token-expiration: 1800000
  refresh-token-expiration: 604800000
```

### 4-3. KakaoUserInfo DTO

카카오 `/v2/user/me` API 응답 구조를 반영한 DTO입니다.

```java
// com/shop/dto/KakaoUserInfo.java
@Getter
@JsonIgnoreProperties(ignoreUnknown = true)
public class KakaoUserInfo {

    private Long id;                          // 카카오 회원 고유 ID

    @JsonProperty("kakao_account")
    private KakaoAccount kakaoAccount;

    @Getter
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class KakaoAccount {
        private String email;
        private Profile profile;

        @Getter
        @JsonIgnoreProperties(ignoreUnknown = true)
        public static class Profile {
            private String nickname;

            @JsonProperty("profile_image_url")
            private String profileImageUrl;
        }
    }

    // 편의 메서드
    public String getEmail() {
        if (kakaoAccount == null) return null;
        return kakaoAccount.getEmail();
    }

    public String getNickname() {
        if (kakaoAccount == null || kakaoAccount.getProfile() == null) return "카카오사용자";
        return kakaoAccount.getProfile().getNickname();
    }
}
```

### 4-4. KakaoOAuthService (핵심)

카카오 API와의 2단계 통신(토큰 교환 → 사용자 정보)을 처리합니다.

```java
// com/shop/service/KakaoOAuthService.java
@Service
@RequiredArgsConstructor
@Slf4j
public class KakaoOAuthService {

    @Value("${kakao.client-id}")
    private String clientId;

    @Value("${kakao.client-secret:}")    // 없으면 빈 문자열
    private String clientSecret;

    @Value("${kakao.redirect-uri}")
    private String redirectUri;

    private final RestTemplate restTemplate = new RestTemplate();
    private final ObjectMapper objectMapper = new ObjectMapper();

    // 1단계: Authorization Code → 카카오 AccessToken 교환
    public String getKakaoAccessToken(String code) {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);

        MultiValueMap<String, String> params = new LinkedMultiValueMap<>();
        params.add("grant_type",   "authorization_code");
        params.add("client_id",    clientId);
        params.add("redirect_uri", redirectUri);
        params.add("code",         code);
        if (!clientSecret.isBlank()) {
            params.add("client_secret", clientSecret);
        }

        HttpEntity<MultiValueMap<String, String>> request = new HttpEntity<>(params, headers);

        try {
            ResponseEntity<Map> response = restTemplate.postForEntity(
                "https://kauth.kakao.com/oauth/token",
                request,
                Map.class
            );
            return (String) response.getBody().get("access_token");
        } catch (Exception e) {
            log.error("카카오 토큰 교환 실패", e);
            throw new OAuthException("카카오 액세스 토큰 발급에 실패했습니다.");
        }
    }

    // 2단계: 카카오 AccessToken → 사용자 정보 조회
    public KakaoUserInfo getKakaoUserInfo(String kakaoAccessToken) {
        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(kakaoAccessToken);
        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);

        HttpEntity<Void> request = new HttpEntity<>(headers);

        try {
            ResponseEntity<KakaoUserInfo> response = restTemplate.exchange(
                "https://kapi.kakao.com/v2/user/me",
                HttpMethod.GET,
                request,
                KakaoUserInfo.class
            );
            return response.getBody();
        } catch (Exception e) {
            log.error("카카오 사용자 정보 조회 실패", e);
            throw new OAuthException("카카오 사용자 정보를 가져오는데 실패했습니다.");
        }
    }
}
```

### 4-5. OAuthMemberService — 공통 회원 처리

구글과 카카오 둘 다 사용할 수 있는 공통 회원 조회/가입 서비스입니다.

```java
// com/shop/service/OAuthMemberService.java
@Service
@RequiredArgsConstructor
@Transactional
public class OAuthMemberService {

    private final MemberRepository memberRepository;
    private final JwtUtil jwtUtil;
    private final RefreshTokenRepository refreshTokenRepository;

    // OAuth 로그인 처리: 기존 회원 조회 or 신규 자동 가입 → JWT 발급
    public TokenResponse processOAuthLogin(AuthProvider provider,
                                           String providerId,
                                           String email,
                                           String name) {
        Member member = memberRepository
            .findByProviderAndProviderId(provider, providerId)
            .map(existing -> {
                existing.updateOAuthInfo(name);  // 닉네임 최신화
                return existing;
            })
            .orElseGet(() -> {
                // 동일 이메일로 일반 가입된 계정이 있으면 충돌 안내
                if (email != null) {
                    memberRepository.findByEmail(email).ifPresent(dup -> {
                        throw new DuplicateEmailException(
                            "이미 이메일로 가입된 계정이 있습니다. 일반 로그인을 이용해 주세요.");
                    });
                }
                // 이메일 없는 경우 임시 이메일 생성 (provider + id 조합)
                String resolvedEmail = (email != null)
                    ? email
                    : provider.name().toLowerCase() + "_" + providerId + "@oauth.placeholder";

                return memberRepository.save(
                    Member.createOAuth(resolvedEmail, name, provider, providerId)
                );
            });

        // JWT 발급
        String accessToken  = jwtUtil.generateAccessToken(
                                  member.getId(), member.getMemberRole().name());
        String refreshToken = jwtUtil.generateRefreshToken(member.getId());

        // RefreshToken 저장 (Rotation)
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
```

### 4-6. KakaoOAuthController

```java
// com/shop/controller/KakaoOAuthController.java
@RestController
@RequestMapping("/api/oauth/kakao")
@RequiredArgsConstructor
public class KakaoOAuthController {

    @Value("${kakao.client-id}")
    private String clientId;

    @Value("${kakao.redirect-uri}")
    private String redirectUri;

    @Value("${app.oauth2.redirect-uri}")
    private String frontendRedirectUri;

    private final KakaoOAuthService  kakaoOAuthService;
    private final OAuthMemberService oAuthMemberService;

    // 1단계: 카카오 인증 URL 반환
    @GetMapping("/authorize")
    public ResponseEntity<Map<String, String>> authorize() {
        String kakaoAuthUrl = "https://kauth.kakao.com/oauth/authorize"
            + "?client_id=" + clientId
            + "&redirect_uri=" + redirectUri
            + "&response_type=code"
            + "&scope=profile_nickname,account_email";

        return ResponseEntity.ok(Map.of("authUrl", kakaoAuthUrl));
    }

    // 2단계: 카카오 콜백 처리 (code 수신 → 토큰 교환 → 사용자 정보 → JWT 발급)
    @GetMapping("/callback")
    public ResponseEntity<Void> callback(
            @RequestParam String code,
            HttpServletResponse response) throws IOException {

        // 카카오 AccessToken 교환
        String kakaoAccessToken = kakaoOAuthService.getKakaoAccessToken(code);

        // 카카오 사용자 정보 조회
        KakaoUserInfo userInfo = kakaoOAuthService.getKakaoUserInfo(kakaoAccessToken);

        // 회원 처리 + JWT 발급
        TokenResponse tokens = oAuthMemberService.processOAuthLogin(
            AuthProvider.KAKAO,
            String.valueOf(userInfo.getId()),
            userInfo.getEmail(),
            userInfo.getNickname()
        );

        // 프론트엔드 콜백 페이지로 리다이렉트 (토큰 포함)
        String redirectUrl = UriComponentsBuilder
            .fromUriString(frontendRedirectUri)
            .queryParam("accessToken",  tokens.accessToken())
            .queryParam("refreshToken", tokens.refreshToken())
            .build().toUriString();

        response.sendRedirect(redirectUrl);
        return ResponseEntity.status(302).build();
    }
}
```

### 4-7. SecurityConfig — 카카오 콜백 경로 허용 추가

```java
.authorizeHttpRequests(auth -> auth
    .requestMatchers("/api/auth/**").permitAll()
    .requestMatchers("/api/oauth/**").permitAll()   // ← 카카오 OAuth 경로 허용
    .requestMatchers(HttpMethod.GET,
        "/api/product/**", "/api/notice/**", "/api/category/**").permitAll()
    .requestMatchers("/api/admin/**").hasAnyRole("MANAGER", "ADMIN")
    .anyRequest().authenticated()
)
```

---

## 5. 프론트엔드 구현 (Vue.js 3)

### 5-1. 카카오 로그인 버튼

```vue
<!-- src/views/member/SigninView.vue -->
<template>
  <div class="signin-page">
    <h2>로그인</h2>

    <!-- 일반 로그인 폼 -->
    <form @submit.prevent="handleSignin">
      <input v-model="email"    type="email"    placeholder="이메일" />
      <input v-model="password" type="password" placeholder="비밀번호" />
      <button type="submit">로그인</button>
    </form>

    <div class="divider">또는</div>

    <!-- 구글 로그인 (Spring Security 자동 URL) -->
    <a href="/oauth2/authorization/google" class="btn-google">
      구글로 로그인
    </a>

    <!-- 카카오 로그인 (서버에서 URL 받아서 이동) -->
    <button @click="handleKakaoLogin" class="btn-kakao">
      카카오로 로그인
    </button>
  </div>
</template>

<script setup>
import { authApi } from '@/api/auth'

async function handleKakaoLogin() {
  try {
    // 백엔드에서 카카오 인증 URL 받아오기
    const { data } = await authApi.getKakaoAuthUrl()
    // 카카오 인증 페이지로 이동
    window.location.href = data.authUrl
  } catch (e) {
    console.error('카카오 로그인 URL 가져오기 실패', e)
  }
}
</script>
```

### 5-2. Auth API 모듈

```javascript
// src/api/auth.js
import api from './axios'

export const authApi = {
  signup:  (data) => api.post('/api/auth/signup', data),
  signin:  (data) => api.post('/api/auth/signin', data),
  signout: ()     => api.post('/api/auth/signout'),
  refresh: (data) => api.post('/api/auth/refresh', data),

  // 카카오 OAuth
  getKakaoAuthUrl: () => api.get('/api/oauth/kakao/authorize'),
}
```

### 5-3. OAuth 콜백 페이지 (구글/카카오 공통)

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
    router.push('/signin?error=' + encodeURIComponent(error))
    return
  }

  if (accessToken && refreshToken) {
    authStore.setTokens(accessToken, refreshToken)
    router.replace('/')
  }
})
</script>
```

---

## 6. 구글 + 카카오 공통 처리 구조

두 소셜 로그인은 `OAuthMemberService`에서 공통으로 처리됩니다.

```
구글 로그인 흐름:
  CustomOAuth2UserService (Spring Security 자동 호출)
    → OAuthMemberService.processOAuthLogin(GOOGLE, sub, email, name)
    → OAuth2SuccessHandler: JWT 발급 + 프론트 리다이렉트

카카오 로그인 흐름:
  KakaoOAuthController.callback()
    → KakaoOAuthService: 토큰 교환 → 사용자 정보
    → OAuthMemberService.processOAuthLogin(KAKAO, id, email, nickname)
    → JWT 발급 + 프론트 리다이렉트
```

이렇게 하면 provider가 NAVER, APPLE 등으로 늘어나도 `OAuthMemberService`와 `OAuthMemberService`만 확장하면 됩니다.

---

## 7. 현재 프로젝트 산출물 수정 사항

### DB설계서 — member 테이블 변경

구글 OAuth와 동일하게 적용됩니다.

| 컬럼명 | 자료형 | NULL | 기본값 | 설명 |
|---|---|---|---|---|
| `provider` | VARCHAR(20) | ❌ | `'LOCAL'` | 가입 방식 (LOCAL/GOOGLE/KAKAO) |
| `provider_id` | VARCHAR(200) | ✅ | null | OAuth 제공자 고유 ID |

`password` 컬럼 → NULL 허용으로 변경

### 클래스 & 시퀀스 다이어그램 추가 항목

**1-2 레이어 구조 추가:**
- `KakaoOAuthController` — `/api/oauth/kakao/authorize`, `/api/oauth/kakao/callback`
- `KakaoOAuthService` — 카카오 REST API 2단계 호출
- `OAuthMemberService` — 구글/카카오 공통 회원 처리 + JWT 발급
- `KakaoUserInfo` DTO

**시퀀스 다이어그램 추가:**
- 카카오 OAuth 로그인 흐름 (Authorize URL → code → 토큰 교환 → 사용자 정보 → JWT 발급)

### 프로젝트 구조도 추가 항목

```
com.shop.controller/
  └── KakaoOAuthController           ← 신규

com.shop.service/
  ├── KakaoOAuthService              ← 신규
  └── OAuthMemberService             ← 신규 (구글/카카오 공통)

com.shop.dto/
  └── KakaoUserInfo                  ← 신규

com.shop.security/oauth2/
  ├── CustomOAuth2UserService        ← 구글 처리
  ├── OAuth2AuthenticationSuccessHandler
  ├── OAuth2AuthenticationFailureHandler
  ├── OAuthUserInfo                  ← 인터페이스
  └── GoogleOAuth2UserInfo           ← 구글 구현체
```

---

## 8. 현재 프로젝트 적용 체크리스트

| 단계 | 작업 항목 |
|---|---|
| 1 | Kakao Developers에서 앱 생성 + REST API 키 발급 |
| 2 | 카카오 로그인 활성화 + Redirect URI 등록 |
| 3 | 동의 항목 설정 (닉네임 필수, 이메일 선택) |
| 4 | `application.yml`에 kakao client-id / redirect-uri 등록 |
| 5 | `KakaoUserInfo` DTO 작성 (중첩 구조 주의) |
| 6 | `KakaoOAuthService` 작성 (1단계: 토큰 교환, 2단계: 사용자 정보) |
| 7 | `OAuthMemberService` 작성 (구글/카카오 공통 회원 처리) |
| 8 | `KakaoOAuthController` 작성 (`/authorize`, `/callback`) |
| 9 | `SecurityConfig`에 `/api/oauth/**` permitAll 추가 |
| 10 | `Member` 엔티티 `provider`, `providerId` 필드 추가 (구글과 공통) |
| 11 | `AuthProvider` enum (LOCAL/GOOGLE/KAKAO) 추가 |
| 12 | `MemberRepository.findByProviderAndProviderId()` 추가 |
| 13 | DB `member` 테이블에 `provider`, `provider_id` 컬럼 추가 |
| 14 | 로그인 페이지에 카카오 로그인 버튼 추가 |
| 15 | `authApi.getKakaoAuthUrl()` API 함수 추가 |
| 16 | `OAuthCallbackView.vue` 공용 콜백 페이지 확인 (구글과 동일) |
| 17 | 이메일 없는 카카오 사용자 처리 로직 테스트 |
| 18 | 동일 이메일 일반 가입 계정과 충돌 예외 처리 테스트 |
| 19 | 개발 단계: Kakao Developers에 테스터 계정 등록 후 이메일 수신 테스트 |
