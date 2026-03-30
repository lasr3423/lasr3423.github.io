# JWT 로그인 구현 상세 설명

> 프로젝트: README 온라인 도서 쇼핑몰
> 기술 스택: Java 21 / Spring Boot 3.5 / PostgreSQL / Vue.js 3

---

## 📋 목차

1. [JWT란 무엇인가](#1-jwt란-무엇인가)
2. [AccessToken과 RefreshToken 전략](#2-accesstoken과-refreshtoken-전략)
3. [RefreshToken이 필요한 이유](#3-refreshtoken이-필요한-이유)
4. [백엔드 구현 (Spring Boot)](#4-백엔드-구현-spring-boot)
5. [프론트엔드 구현 (Vue.js 3)](#5-프론트엔드-구현-vuejs-3)
6. [전체 인증 흐름 정리](#6-전체-인증-흐름-정리)
7. [현재 프로젝트 적용 체크리스트](#7-현재-프로젝트-적용-체크리스트)

---

## 1. JWT란 무엇인가

JWT(JSON Web Token)는 당사자 간에 정보를 JSON 형태로 안전하게 전송하기 위한 **자가 포함(self-contained) 토큰** 표준입니다.
서버가 세션을 별도로 저장하지 않아도 되기 때문에 **Stateless** 아키텍처에 적합합니다.

### JWT 구조

JWT는 점(`.`)으로 구분된 세 부분으로 구성됩니다.

```
xxxxx.yyyyy.zzzzz
헤더  .페이로드.서명
```

**헤더 (Header)**

```json
{
  "alg": "HS256",
  "typ": "JWT"
}
```

**페이로드 (Payload)** — 클레임(Claim)이라 부르는 데이터를 담음

```json
{
  "sub": "1",
  "email": "user@test.com",
  "role": "USER",
  "iat": 1711612800,
  "exp": 1711614600
}
```

> ⚠️ 페이로드는 Base64로 인코딩된 것이지 암호화된 것이 아닙니다.
> 누구나 디코딩해서 내용을 볼 수 있으므로 **비밀번호, 카드번호 같은 민감 정보는 절대 담지 않습니다.**

**서명 (Signature)**

```
HMACSHA256(
  Base64UrlEncode(헤더) + "." + Base64UrlEncode(페이로드),
  SECRET_KEY
)
```

서명은 토큰이 위변조되지 않았음을 검증합니다.
SECRET_KEY를 알지 못하는 한 서명을 올바르게 만들 수 없습니다.

---

## 2. AccessToken과 RefreshToken 전략

JWT를 하나만 쓰면 탈취 시 만료까지 막을 방법이 없습니다. 그래서 두 가지 토큰을 조합합니다.

| 항목 | AccessToken | RefreshToken |
|---|---|---|
| 목적 | API 인증 | AccessToken 재발급 |
| 유효기간 | 짧게 (30분 ~ 2시간) | 길게 (7일 ~ 30일) |
| 전달 방식 | HTTP 헤더 `Authorization: Bearer` | 요청 바디 or HttpOnly 쿠키 |
| 저장 위치 | 클라이언트 메모리 / sessionStorage | 서버 DB or Redis + 클라이언트 쿠키 |
| 탈취 위험 | 짧은 수명으로 피해 최소화 | 서버에서 무효화 가능 |

**전체 발급 흐름**

```
로그인
  → AccessToken (30분) + RefreshToken (7일) 발급
  → RefreshToken은 서버 DB에 저장

API 호출
  → AccessToken을 Authorization 헤더에 첨부

AccessToken 만료
  → RefreshToken으로 새 AccessToken 자동 요청

RefreshToken 만료 또는 탈취 감지
  → 재로그인 요구
```

---

## 3. RefreshToken이 필요한 이유

### 3-1. JWT의 구조적 한계 — 무효화 불가능

JWT는 Stateless이기 때문에 한번 발급된 토큰은 서버가 직접 취소할 방법이 없습니다.

**실제 공격 시나리오**

```
1. 공격자가 AccessToken 탈취
2. 유효기간이 7일 → 7일 동안 계정 장악 가능
3. 서버 입장에서 유효한 토큰이므로 막을 방법 없음
4. 피해자가 비밀번호를 바꿔도 이미 발급된 토큰은 여전히 유효
```

RefreshToken 구조를 사용하면 AccessToken 유효기간을 30분으로 줄여
**탈취되더라도 피해 시간이 최대 30분으로 제한**됩니다.

### 3-2. 강제 로그아웃 가능

RefreshToken은 서버 DB에 저장되므로 삭제하면 더 이상 갱신이 불가합니다.

```
관리자가 특정 회원 강퇴(DEACTIVATE)
  → DB에서 RefreshToken 삭제
  → 현재 AccessToken은 최대 30분 후 만료
  → 재발급 시 RefreshToken 없음 → 재로그인 강제
```

### 3-3. Refresh Token Rotation — 탈취 감지

사용할 때마다 새 토큰으로 교체하는 전략입니다.

```
정상 사용:
  RefreshToken_A로 갱신 → 서버: A 삭제 + B 발급

공격자가 이미 사용된 A를 나중에 사용:
  → 서버: "이미 교체된 토큰" → 탈취 의심
  → 해당 회원의 모든 RefreshToken 무효화
```

### 3-4. 현재 프로젝트에서 필요한 기능

| 기능 ID | 기능명 | RefreshToken 필요 이유 |
|---|---|---|
| FA-004 | 회원 강퇴 | 강퇴 즉시 접근 차단 |
| FM-003 | 로그아웃 | 서버 측 완전한 토큰 무효화 |
| FM-010 | 회원 탈퇴 | 탈퇴 후 모든 세션 즉시 종료 |
| FM-009 | 비밀번호 변경 | 기존 발급 토큰 전체 무효화 |

---

## 4. 백엔드 구현 (Spring Boot)

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
jwt:
  secret: "your-256-bit-secret-key-must-be-at-least-32-characters-long"
  access-token-expiration: 1800000     # 30분 (ms)
  refresh-token-expiration: 604800000  # 7일 (ms)
```

### 4-3. RefreshToken Entity

```java
// com/shop/domain/RefreshToken.java
@Entity
@Table(name = "refresh_token")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class RefreshToken {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private Long memberId;

    @Column(nullable = false, length = 512)
    private String token;

    @Column(nullable = false)
    private LocalDateTime expiresAt;

    public static RefreshToken create(Long memberId, String token, long expirationMs) {
        RefreshToken rt = new RefreshToken();
        rt.memberId = memberId;
        rt.token = token;
        rt.expiresAt = LocalDateTime.now().plusNanos(expirationMs * 1_000_000L);
        return rt;
    }

    public void rotate(String newToken, long expirationMs) {
        this.token = newToken;
        this.expiresAt = LocalDateTime.now().plusNanos(expirationMs * 1_000_000L);
    }

    public boolean isExpired() {
        return LocalDateTime.now().isAfter(this.expiresAt);
    }
}
```

### 4-4. JwtUtil

```java
// com/shop/util/JwtUtil.java
@Component
public class JwtUtil {

    private final SecretKey secretKey;
    private final long accessTokenExpiration;
    private final long refreshTokenExpiration;

    public JwtUtil(
        @Value("${jwt.secret}") String secret,
        @Value("${jwt.access-token-expiration}") long accessExp,
        @Value("${jwt.refresh-token-expiration}") long refreshExp
    ) {
        this.secretKey = Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
        this.accessTokenExpiration = accessExp;
        this.refreshTokenExpiration = refreshExp;
    }

    public String generateAccessToken(Long memberId, String role) {
        return Jwts.builder()
            .subject(String.valueOf(memberId))
            .claim("role", role)
            .issuedAt(new Date())
            .expiration(new Date(System.currentTimeMillis() + accessTokenExpiration))
            .signWith(secretKey)
            .compact();
    }

    public String generateRefreshToken(Long memberId) {
        return Jwts.builder()
            .subject(String.valueOf(memberId))
            .issuedAt(new Date())
            .expiration(new Date(System.currentTimeMillis() + refreshTokenExpiration))
            .signWith(secretKey)
            .compact();
    }

    public Claims parseClaims(String token) {
        return Jwts.parser()
            .verifyWith(secretKey)
            .build()
            .parseSignedClaims(token)
            .getPayload();
    }

    public Long getMemberId(String token) {
        return Long.parseLong(parseClaims(token).getSubject());
    }

    public String getRole(String token) {
        return parseClaims(token).get("role", String.class);
    }

    public boolean validateToken(String token) {
        try {
            parseClaims(token);
            return true;
        } catch (ExpiredJwtException e) {
            throw e;
        } catch (JwtException | IllegalArgumentException e) {
            return false;
        }
    }

    public long getRemainingTime(String token) {
        Date exp = parseClaims(token).getExpiration();
        return exp.getTime() - System.currentTimeMillis();
    }

    public long getRefreshTokenExpiration() { return refreshTokenExpiration; }
}
```

### 4-5. JwtAuthenticationFilter

```java
// com/shop/security/JwtAuthenticationFilter.java
@Component
@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtUtil jwtUtil;
    private final Set<String> blacklist = Collections.synchronizedSet(new HashSet<>());

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain)
                                    throws ServletException, IOException {

        String token = extractToken(request);

        if (token != null) {
            if (blacklist.contains(token)) {
                sendError(response, "로그아웃된 토큰입니다.");
                return;
            }
            try {
                if (jwtUtil.validateToken(token)) {
                    Long memberId = jwtUtil.getMemberId(token);
                    String role   = jwtUtil.getRole(token);

                    UsernamePasswordAuthenticationToken auth =
                        new UsernamePasswordAuthenticationToken(
                            memberId, null,
                            List.of(new SimpleGrantedAuthority("ROLE_" + role))
                        );
                    auth.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                    SecurityContextHolder.getContext().setAuthentication(auth);
                }
            } catch (ExpiredJwtException e) {
                sendError(response, "만료된 토큰입니다.");
                return;
            }
        }

        filterChain.doFilter(request, response);
    }

    private String extractToken(HttpServletRequest request) {
        String bearer = request.getHeader("Authorization");
        if (StringUtils.hasText(bearer) && bearer.startsWith("Bearer ")) {
            return bearer.substring(7);
        }
        return null;
    }

    private void sendError(HttpServletResponse response, String message) throws IOException {
        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        response.setContentType("application/json;charset=UTF-8");
        response.getWriter().write("{\"message\":\"" + message + "\"}");
    }

    public void addToBlacklist(String token) {
        blacklist.add(token);
    }
}
```

### 4-6. SecurityConfig

```java
// com/shop/config/SecurityConfig.java
@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
public class SecurityConfig {

    private final JwtAuthenticationFilter jwtAuthFilter;

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
            .csrf(csrf -> csrf.disable())
            .sessionManagement(session ->
                session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
            .authorizeHttpRequests(auth -> auth
                .requestMatchers("/api/auth/signup", "/api/auth/signin",
                                 "/api/auth/refresh").permitAll()
                .requestMatchers(HttpMethod.GET,
                                 "/api/product/**", "/api/notice/**",
                                 "/api/category/**").permitAll()
                .requestMatchers("/api/admin/**").hasAnyRole("MANAGER", "ADMIN")
                .anyRequest().authenticated()
            )
            .addFilterBefore(jwtAuthFilter, UsernamePasswordAuthenticationFilter.class)
            .exceptionHandling(ex -> ex
                .authenticationEntryPoint((req, res, e) ->
                    res.sendError(HttpServletResponse.SC_UNAUTHORIZED, "인증이 필요합니다."))
                .accessDeniedHandler((req, res, e) ->
                    res.sendError(HttpServletResponse.SC_FORBIDDEN, "권한이 없습니다."))
            );

        return http.build();
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public AuthenticationManager authenticationManager(
            AuthenticationConfiguration config) throws Exception {
        return config.getAuthenticationManager();
    }
}
```

### 4-7. MemberService (회원가입 / 로그인 / 로그아웃 / 토큰 갱신)

```java
// com/shop/service/MemberService.java
@Service
@RequiredArgsConstructor
@Transactional
public class MemberService {

    private final MemberRepository memberRepository;
    private final RefreshTokenRepository refreshTokenRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;
    private final JwtAuthenticationFilter jwtFilter;

    /* 회원가입 */
    public ResponseEntity<String> signup(SignupRequest req) {
        if (memberRepository.existsByEmail(req.getEmail())) {
            throw new DuplicateEmailException("이미 사용 중인 이메일입니다.");
        }
        Member member = Member.create(
            req.getEmail(),
            passwordEncoder.encode(req.getPassword()),
            req.getName(), req.getPhone(), req.getAddress()
        );
        memberRepository.save(member);
        return ResponseEntity.status(201).body("회원가입이 완료되었습니다.");
    }

    /* 로그인 */
    public ResponseEntity<TokenResponse> signin(SigninRequest req) {
        Member member = memberRepository.findByEmail(req.getEmail())
            .orElseThrow(() -> new AuthException("이메일 또는 비밀번호가 올바르지 않습니다."));

        if (!passwordEncoder.matches(req.getPassword(), member.getPassword()))
            throw new AuthException("이메일 또는 비밀번호가 올바르지 않습니다.");

        if (member.getMemberStatus() != MemberStatus.ACTIVATE)
            throw new AuthException("접근이 제한된 계정입니다.");

        String accessToken  = jwtUtil.generateAccessToken(
                                  member.getId(), member.getMemberRole().name());
        String refreshToken = jwtUtil.generateRefreshToken(member.getId());

        // RefreshToken DB 저장 (기존 토큰 교체)
        refreshTokenRepository.findByMemberId(member.getId())
            .ifPresentOrElse(
                rt -> rt.rotate(refreshToken, jwtUtil.getRefreshTokenExpiration()),
                () -> refreshTokenRepository.save(
                    RefreshToken.create(member.getId(), refreshToken,
                                        jwtUtil.getRefreshTokenExpiration()))
            );

        return ResponseEntity.ok(new TokenResponse(accessToken, refreshToken));
    }

    /* 로그아웃 */
    public ResponseEntity<String> signout(String accessToken) {
        Long memberId = jwtUtil.getMemberId(accessToken);
        jwtFilter.addToBlacklist(accessToken);           // AccessToken 블랙리스트
        refreshTokenRepository.deleteByMemberId(memberId); // RefreshToken 삭제
        return ResponseEntity.ok("로그아웃 되었습니다.");
    }

    /* RefreshToken으로 AccessToken 재발급 */
    public ResponseEntity<TokenResponse> refresh(String refreshToken) {
        if (!jwtUtil.validateToken(refreshToken))
            throw new AuthException("유효하지 않은 RefreshToken입니다.");

        Long memberId = jwtUtil.getMemberId(refreshToken);

        RefreshToken stored = refreshTokenRepository.findByMemberId(memberId)
            .orElseThrow(() -> new AuthException("로그인이 필요합니다."));

        if (!stored.getToken().equals(refreshToken) || stored.isExpired())
            throw new AuthException("만료되었거나 유효하지 않은 RefreshToken입니다.");

        Member member = memberRepository.findById(memberId)
            .orElseThrow(() -> new AuthException("회원을 찾을 수 없습니다."));

        // Rotation: 새 토큰 발급 + 기존 토큰 교체
        String newAccessToken  = jwtUtil.generateAccessToken(
                                     memberId, member.getMemberRole().name());
        String newRefreshToken = jwtUtil.generateRefreshToken(memberId);
        stored.rotate(newRefreshToken, jwtUtil.getRefreshTokenExpiration());

        return ResponseEntity.ok(new TokenResponse(newAccessToken, newRefreshToken));
    }
}
```

### 4-8. AuthController

```java
// com/shop/controller/AuthController.java
@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final MemberService memberService;

    @PostMapping("/signup")
    public ResponseEntity<String> signup(@RequestBody @Valid SignupRequest req) {
        return memberService.signup(req);
    }

    @PostMapping("/signin")
    public ResponseEntity<TokenResponse> signin(@RequestBody @Valid SigninRequest req) {
        return memberService.signin(req);
    }

    @PostMapping("/signout")
    public ResponseEntity<String> signout(HttpServletRequest request) {
        String token = extractToken(request);
        return memberService.signout(token);
    }

    @PostMapping("/refresh")
    public ResponseEntity<TokenResponse> refresh(@RequestBody RefreshRequest req) {
        return memberService.refresh(req.getRefreshToken());
    }

    private String extractToken(HttpServletRequest request) {
        String bearer = request.getHeader("Authorization");
        if (bearer != null && bearer.startsWith("Bearer ")) return bearer.substring(7);
        throw new AuthException("토큰이 없습니다.");
    }
}
```

### 4-9. DTO 정의

```java
// SignupRequest
public record SignupRequest(
    @NotBlank String email,
    @NotBlank @Size(min = 8) String password,
    @NotBlank String name,
    String phone,
    String address
) {}

// SigninRequest
public record SigninRequest(
    @NotBlank String email,
    @NotBlank String password
) {}

// TokenResponse
public record TokenResponse(
    String accessToken,
    String refreshToken
) {}

// RefreshRequest
public record RefreshRequest(
    @NotBlank String refreshToken
) {}
```

### 4-10. DB 테이블 — refresh_token

```sql
CREATE TABLE refresh_token (
    id         BIGSERIAL PRIMARY KEY,
    member_id  BIGINT        NOT NULL REFERENCES member(id),
    token      VARCHAR(512)  NOT NULL,
    expires_at TIMESTAMP     NOT NULL,
    created_at TIMESTAMP     NOT NULL DEFAULT now(),
    updated_at TIMESTAMP
);

CREATE UNIQUE INDEX idx_refresh_token_member_id ON refresh_token(member_id);
```

---

## 5. 프론트엔드 구현 (Vue.js 3)

### 5-1. Pinia Auth Store

```javascript
// src/store/auth.js
import { defineStore } from 'pinia'
import { ref, computed } from 'vue'
import { authApi } from '@/api/auth'

export const useAuthStore = defineStore('auth', () => {
  // AccessToken: 메모리에만 저장 (XSS 방어)
  const accessToken  = ref(null)
  // RefreshToken: localStorage (또는 HttpOnly 쿠키 권장)
  const refreshToken = ref(localStorage.getItem('refreshToken'))

  const isLoggedIn = computed(() => !!accessToken.value)

  async function signin(email, password) {
    const { data } = await authApi.signin({ email, password })
    accessToken.value  = data.accessToken
    refreshToken.value = data.refreshToken
    localStorage.setItem('refreshToken', data.refreshToken)
  }

  async function signout() {
    try { await authApi.signout() } finally { clearTokens() }
  }

  async function refreshAccessToken() {
    const stored = localStorage.getItem('refreshToken')
    if (!stored) throw new Error('RefreshToken 없음')

    const { data } = await authApi.refresh({ refreshToken: stored })
    accessToken.value  = data.accessToken
    refreshToken.value = data.refreshToken
    localStorage.setItem('refreshToken', data.refreshToken)
    return data.accessToken
  }

  function clearTokens() {
    accessToken.value  = null
    refreshToken.value = null
    localStorage.removeItem('refreshToken')
  }

  return { accessToken, isLoggedIn, signin, signout, refreshAccessToken, clearTokens }
})
```

### 5-2. Axios 인터셉터 (401 자동 갱신)

```javascript
// src/api/axios.js
import axios from 'axios'
import { useAuthStore } from '@/store/auth'
import router from '@/router'

const api = axios.create({
  baseURL: import.meta.env.VITE_API_BASE_URL || 'http://localhost:8080',
  timeout: 10000,
})

// 요청 인터셉터: AccessToken 자동 첨부
api.interceptors.request.use((config) => {
  const authStore = useAuthStore()
  if (authStore.accessToken) {
    config.headers.Authorization = `Bearer ${authStore.accessToken}`
  }
  return config
})

// 응답 인터셉터: 401 → 자동 토큰 갱신 후 재시도
let isRefreshing = false
let failedQueue  = []

function processQueue(error, token = null) {
  failedQueue.forEach(p => error ? p.reject(error) : p.resolve(token))
  failedQueue = []
}

api.interceptors.response.use(
  (response) => response,
  async (error) => {
    const originalRequest = error.config

    if (error.response?.status === 401 && !originalRequest._retry) {
      if (isRefreshing) {
        return new Promise((resolve, reject) => {
          failedQueue.push({ resolve, reject })
        }).then(token => {
          originalRequest.headers.Authorization = `Bearer ${token}`
          return api(originalRequest)
        })
      }

      originalRequest._retry = true
      isRefreshing = true

      try {
        const authStore = useAuthStore()
        const newToken  = await authStore.refreshAccessToken()
        processQueue(null, newToken)
        originalRequest.headers.Authorization = `Bearer ${newToken}`
        return api(originalRequest)
      } catch (refreshError) {
        processQueue(refreshError, null)
        useAuthStore().clearTokens()
        router.push('/signin')
        return Promise.reject(refreshError)
      } finally {
        isRefreshing = false
      }
    }

    return Promise.reject(error)
  }
)

export default api
```

### 5-3. Vue Router 가드

```javascript
// src/router/index.js
router.beforeEach(async (to, from, next) => {
  const authStore = useAuthStore()

  // 앱 시작 시 RefreshToken으로 AccessToken 복구 시도
  if (!authStore.isLoggedIn && localStorage.getItem('refreshToken')) {
    try {
      await authStore.refreshAccessToken()
    } catch {
      authStore.clearTokens()
    }
  }

  if (to.meta.requiresAuth && !authStore.isLoggedIn) {
    return next({ path: '/signin', query: { redirect: to.fullPath } })
  }

  next()
})
```

---

## 6. 전체 인증 흐름 정리

```
[회원가입]
Vue → POST /api/auth/signup { email, pw, name, ... }
Spring → BCrypt 암호화 → member 테이블 저장
       → 201 Created

[로그인]
Vue → POST /api/auth/signin { email, password }
Spring → 이메일 조회 → BCrypt 검증 → 상태 확인
       → AccessToken(30분) + RefreshToken(7일) 발급
       → refresh_token 테이블 저장
       → 200 OK { accessToken, refreshToken }
Vue → accessToken: Pinia 메모리 저장
   → refreshToken: localStorage 저장

[API 호출]
Vue → Authorization: Bearer {accessToken} 헤더 첨부
Spring → JwtAuthenticationFilter
       → 블랙리스트 확인 → 토큰 검증 → memberId 추출
       → SecurityContext 저장 → Controller 진입

[AccessToken 만료 (401)]
Vue axios 인터셉터 감지
  → POST /api/auth/refresh { refreshToken }
Spring → RefreshToken 검증 (DB 비교 + 만료 확인)
       → 새 AccessToken + 새 RefreshToken 발급 (Rotation)
       → DB RefreshToken 교체
Vue → 새 토큰 저장 → 원래 요청 자동 재시도

[로그아웃]
Vue → POST /api/auth/signout (Authorization 헤더 포함)
Spring → AccessToken 블랙리스트 등록
       → refresh_token 테이블에서 삭제
Vue → Pinia store 초기화 → localStorage 제거
```

---

## 7. 현재 프로젝트 적용 체크리스트

| 단계 | 작업 항목 | 파일 위치 |
|---|---|---|
| 1 | jjwt 0.12.6 의존성 추가 | `build.gradle` |
| 2 | jwt 시크릿·만료시간 설정 | `application.yml` |
| 3 | `refresh_token` 테이블 생성 | `schema.sql` / DB설계서 반영 |
| 4 | `RefreshToken` Entity 작성 | `com/shop/domain/` |
| 5 | `RefreshTokenRepository` 작성 | `com/shop/repository/` |
| 6 | `JwtUtil` 작성 | `com/shop/util/` |
| 7 | `JwtAuthenticationFilter` 작성 | `com/shop/security/` |
| 8 | `CustomUserDetailsService` 작성 | `com/shop/security/` |
| 9 | `SecurityConfig` 설정 | `com/shop/config/` |
| 10 | `MemberService` signup/signin/signout/refresh | `com/shop/service/` |
| 11 | `AuthController` 4개 엔드포인트 | `com/shop/controller/` |
| 12 | Pinia `auth.js` store 작성 | `src/store/` |
| 13 | `axios.js` 인터셉터 설정 | `src/api/` |
| 14 | `router/index.js` beforeEach 가드 | `src/router/` |
| 15 | 관리자 권한 분리 테스트 | ROLE_MANAGER / ROLE_ADMIN |
| 16 | 토큰 탈취 시나리오 테스트 | 블랙리스트 동작 확인 |
