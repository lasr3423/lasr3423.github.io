# Spring Security 로그인 구현 상세 설명

> 프로젝트: README 온라인 도서 쇼핑몰
> 기술 스택: Java 21 / Spring Boot 3.5 / PostgreSQL / Vue.js 3
> 인증 방식: Spring Security Form Login (세션 기반)

---

## 📋 목차

1. [Spring Security란 무엇인가](#1-spring-security란-무엇인가)
2. [Spring Security 기본 동작 원리](#2-spring-security-기본-동작-원리)
3. [세션 기반 vs JWT 기반 비교](#3-세션-기반-vs-jwt-기반-비교)
4. [Filter Chain 구조](#4-filter-chain-구조)
5. [백엔드 구현 (Spring Boot)](#5-백엔드-구현-spring-boot)
6. [프론트엔드 구현 (Vue.js 3)](#6-프론트엔드-구현-vuejs-3)
7. [권한(Role) 기반 접근 제어](#7-권한role-기반-접근-제어)
8. [현재 프로젝트에서의 선택 가이드](#8-현재-프로젝트에서의-선택-가이드)
9. [현재 프로젝트 적용 체크리스트](#9-현재-프로젝트-적용-체크리스트)

---

## 1. Spring Security란 무엇인가

Spring Security는 Spring 기반 애플리케이션의 **인증(Authentication)** 과 **인가(Authorization)** 를 담당하는 보안 프레임워크입니다.

### 인증(Authentication) vs 인가(Authorization)

| 개념 | 의미 | 예시 |
|---|---|---|
| **인증** | "당신이 누구인가?" 확인 | 로그인 성공 여부 |
| **인가** | "당신이 무엇을 할 수 있는가?" 확인 | 관리자 페이지 접근 권한 |

### Spring Security가 처리하는 것들

- 로그인 / 로그아웃 처리
- 비밀번호 암호화 (BCrypt)
- 세션 관리 또는 JWT 필터 통합
- URL별 접근 권한 제어
- CSRF 공격 방어
- CORS 설정
- Remember-Me (자동 로그인)
- OAuth2 소셜 로그인 통합

---

## 2. Spring Security 기본 동작 원리

### 핵심 컴포넌트

```
클라이언트 요청
    ↓
[Filter Chain]                ← 모든 요청이 통과하는 필터 체인
    ↓
[AuthenticationManager]       ← 인증 처리 위임
    ↓
[AuthenticationProvider]      ← 실제 인증 로직
    ↓
[UserDetailsService]          ← DB에서 사용자 정보 로드
    ↓
[SecurityContextHolder]       ← 인증 정보 저장 (요청 처리 중 유지)
    ↓
Controller
```

### SecurityContextHolder

인증에 성공한 사용자의 정보를 **현재 스레드**에 저장합니다.
이후 Controller에서 `@AuthenticationPrincipal`로 꺼내 씁니다.

```java
// 인증 정보 저장 (Spring Security 내부에서 자동 처리)
SecurityContextHolder.getContext().setAuthentication(authentication);

// 현재 로그인한 사용자 꺼내기
Authentication auth = SecurityContextHolder.getContext().getAuthentication();
UserDetails user = (UserDetails) auth.getPrincipal();
```

---

## 3. 세션 기반 vs JWT 기반 비교

Spring Security는 기본적으로 **세션 기반** 인증을 사용합니다.
현재 프로젝트는 Vue.js SPA + REST API 구조이므로 두 방식의 차이를 이해하는 것이 중요합니다.

| 항목 | 세션 기반 (Spring Security 기본) | JWT 기반 |
|---|---|---|
| 인증 정보 저장 위치 | 서버 메모리 / Redis | 클라이언트 (토큰) |
| 상태 | Stateful | Stateless |
| 확장성 | 서버 증설 시 세션 공유 필요 | 토큰 자체에 정보 포함 |
| 로그아웃 | 서버에서 즉시 세션 삭제 | 블랙리스트 별도 관리 필요 |
| REST API 친화성 | 낮음 (쿠키 의존) | 높음 (헤더 기반) |
| SPA 적합성 | 중간 | 높음 |
| CSRF 방어 | 필요 (쿠키 기반) | 불필요 (헤더 기반) |
| 현재 프로젝트 권장 | Vue.js SPA이므로 비권장 | **권장** |

> 이 문서에서는 Spring Security의 **세션 기반 Form Login 방식**을 먼저 완전히 이해하고,
> 이후 JWT 방식으로 전환하는 흐름으로 설명합니다.

---

## 4. Filter Chain 구조

Spring Security의 모든 동작은 **Filter Chain** 을 통해 이루어집니다.
각 필터는 순서대로 실행되며, 특정 필터가 요청을 처리하면 이후 필터로 넘기거나 응답을 반환합니다.

```
클라이언트 요청
    ↓
SecurityContextPersistenceFilter   ← 세션에서 SecurityContext 복원
    ↓
UsernamePasswordAuthenticationFilter ← POST /login 처리 (Form Login)
    ↓
BasicAuthenticationFilter          ← HTTP Basic 인증 처리
    ↓
RememberMeAuthenticationFilter     ← Remember-Me 쿠키 처리
    ↓
AnonymousAuthenticationFilter      ← 미인증 사용자를 Anonymous로 처리
    ↓
ExceptionTranslationFilter         ← 인증/인가 예외를 HTTP 응답으로 변환
    ↓
FilterSecurityInterceptor          ← URL별 접근 권한 최종 확인
    ↓
Controller / Resource
```

### JWT 방식에서의 필터 위치

```
커스텀 JwtAuthenticationFilter      ← UsernamePasswordAuthenticationFilter 앞에 삽입
    ↓
UsernamePasswordAuthenticationFilter (비활성화 또는 유지)
    ↓
...
```

---

## 5. 백엔드 구현 (Spring Boot)

### 5-1. 의존성 (build.gradle)

```groovy
dependencies {
    implementation 'org.springframework.boot:spring-boot-starter-security'
    implementation 'org.springframework.boot:spring-boot-starter-data-jpa'
    implementation 'org.springframework.boot:spring-boot-starter-web'
    implementation 'org.springframework.boot:spring-boot-starter-thymeleaf'  // Form Login 시 선택
    // Spring Security + Thymeleaf 통합 (선택)
    implementation 'org.thymeleaf.extras:thymeleaf-extras-springsecurity6'
}
```

### 5-2. UserDetails 구현 — 사용자 정보 래퍼

Spring Security는 인증에 사용할 사용자 정보를 `UserDetails` 인터페이스로 요구합니다.

```java
// com/shop/security/CustomUserDetails.java
@Getter
public class CustomUserDetails implements UserDetails {

    private final Long   memberId;
    private final String email;
    private final String password;
    private final MemberRole memberRole;
    private final MemberStatus memberStatus;

    public CustomUserDetails(Member member) {
        this.memberId     = member.getId();
        this.email        = member.getEmail();
        this.password     = member.getPassword();
        this.memberRole   = member.getMemberRole();
        this.memberStatus = member.getMemberStatus();
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        // "ROLE_USER", "ROLE_MANAGER", "ROLE_ADMIN" 형태로 권한 반환
        return List.of(new SimpleGrantedAuthority("ROLE_" + memberRole.name()));
    }

    @Override
    public String getPassword() { return password; }

    @Override
    public String getUsername() { return email; }   // Spring Security는 username으로 식별

    // 계정 상태 메서드 — memberStatus 기반으로 판단
    @Override
    public boolean isAccountNonExpired()  { return true; }

    @Override
    public boolean isAccountNonLocked() {
        return memberStatus != MemberStatus.DEACTIVATE;  // 강퇴 회원 잠금
    }

    @Override
    public boolean isCredentialsNonExpired() { return true; }

    @Override
    public boolean isEnabled() {
        return memberStatus == MemberStatus.ACTIVATE;    // 탈퇴 회원 비활성
    }
}
```

### 5-3. UserDetailsService 구현 — DB에서 사용자 로드

```java
// com/shop/security/CustomUserDetailsService.java
@Service
@RequiredArgsConstructor
public class CustomUserDetailsService implements UserDetailsService {

    private final MemberRepository memberRepository;

    @Override
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        Member member = memberRepository.findByEmail(email)
            .orElseThrow(() ->
                new UsernameNotFoundException("회원을 찾을 수 없습니다: " + email));

        return new CustomUserDetails(member);
    }
}
```

### 5-4. SecurityConfig (세션 기반 Form Login)

```java
// com/shop/config/SecurityConfig.java
@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
public class SecurityConfig {

    private final CustomUserDetailsService userDetailsService;

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
            // CSRF: Form Login + 세션 기반이면 활성화 (REST API만 쓰면 비활성화)
            .csrf(csrf -> csrf
                .ignoringRequestMatchers("/api/**")   // API 경로는 CSRF 제외
            )

            // URL별 접근 권한 설정
            .authorizeHttpRequests(auth -> auth
                // 누구나 접근 가능
                .requestMatchers("/", "/signin", "/signup").permitAll()
                .requestMatchers(HttpMethod.GET,
                    "/product/**", "/notice/**", "/category/**").permitAll()
                // 관리자 전용
                .requestMatchers("/admin/**").hasAnyRole("MANAGER", "ADMIN")
                // 나머지는 로그인 필요
                .anyRequest().authenticated()
            )

            // Form Login 설정
            .formLogin(form -> form
                .loginPage("/signin")                    // 커스텀 로그인 페이지 URL
                .loginProcessingUrl("/api/auth/signin")  // 로그인 폼 POST URL
                .usernameParameter("email")              // 폼 필드명 (기본: username)
                .passwordParameter("password")
                .defaultSuccessUrl("/", true)            // 로그인 성공 후 이동
                .failureUrl("/signin?error=true")        // 로그인 실패 후 이동
                // REST API 방식이면 핸들러로 JSON 응답 반환
                .successHandler(authenticationSuccessHandler())
                .failureHandler(authenticationFailureHandler())
            )

            // 로그아웃 설정
            .logout(logout -> logout
                .logoutUrl("/api/auth/signout")
                .logoutSuccessUrl("/signin")
                .deleteCookies("JSESSIONID")             // 세션 쿠키 삭제
                .invalidateHttpSession(true)             // 서버 세션 무효화
                // REST API 방식이면 핸들러로 JSON 응답 반환
                .logoutSuccessHandler(logoutSuccessHandler())
            )

            // Remember-Me (자동 로그인) 설정 — 선택
            .rememberMe(remember -> remember
                .userDetailsService(userDetailsService)
                .tokenValiditySeconds(7 * 24 * 60 * 60) // 7일
                .rememberMeParameter("remember-me")
            )

            // 세션 관리
            .sessionManagement(session -> session
                .sessionCreationPolicy(SessionCreationPolicy.IF_REQUIRED)
                .maximumSessions(1)                      // 동시 로그인 1개 제한
                .maxSessionsPreventsLogin(false)         // 새 로그인이 기존 세션 만료
            )

            // UserDetailsService + PasswordEncoder 연결
            .userDetailsService(userDetailsService);

        return http.build();
    }

    // ── 로그인 성공: JSON 응답 (REST API 방식) ──
    @Bean
    public AuthenticationSuccessHandler authenticationSuccessHandler() {
        return (request, response, authentication) -> {
            CustomUserDetails userDetails = (CustomUserDetails) authentication.getPrincipal();
            response.setContentType("application/json;charset=UTF-8");
            response.setStatus(HttpServletResponse.SC_OK);
            response.getWriter().write(
                "{\"message\":\"로그인 성공\","
                + "\"memberId\":" + userDetails.getMemberId() + ","
                + "\"role\":\"" + userDetails.getMemberRole() + "\"}"
            );
        };
    }

    // ── 로그인 실패: JSON 응답 ──
    @Bean
    public AuthenticationFailureHandler authenticationFailureHandler() {
        return (request, response, exception) -> {
            response.setContentType("application/json;charset=UTF-8");
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            response.getWriter().write(
                "{\"message\":\"이메일 또는 비밀번호가 올바르지 않습니다.\"}"
            );
        };
    }

    // ── 로그아웃 성공: JSON 응답 ──
    @Bean
    public LogoutSuccessHandler logoutSuccessHandler() {
        return (request, response, authentication) -> {
            response.setContentType("application/json;charset=UTF-8");
            response.setStatus(HttpServletResponse.SC_OK);
            response.getWriter().write("{\"message\":\"로그아웃 되었습니다.\"}");
        };
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

### 5-5. AuthController (추가 API 엔드포인트)

Form Login은 Spring Security가 `/api/auth/signin` POST를 자동 처리하므로
Controller가 거의 필요 없지만, 회원가입 등 추가 기능을 위해 작성합니다.

```java
// com/shop/controller/AuthController.java
@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final MemberService memberService;

    // 회원가입: Spring Security 자동 처리 대상이 아니므로 직접 구현
    @PostMapping("/signup")
    public ResponseEntity<String> signup(@RequestBody @Valid SignupRequest req) {
        return memberService.signup(req);
    }

    // 현재 로그인한 사용자 정보 조회
    @GetMapping("/me")
    public ResponseEntity<MemberResponse> getMe(
            @AuthenticationPrincipal CustomUserDetails userDetails) {

        if (userDetails == null) {
            return ResponseEntity.status(401).build();
        }
        return ResponseEntity.ok(memberService.getMyInfo(userDetails.getMemberId()));
    }
}
```

### 5-6. Controller에서 현재 사용자 꺼내기

```java
// 방법 1: @AuthenticationPrincipal (권장)
@GetMapping("/mypage")
public ResponseEntity<MemberResponse> getMyPage(
        @AuthenticationPrincipal CustomUserDetails userDetails) {
    return ResponseEntity.ok(memberService.getMyPage(userDetails.getMemberId()));
}

// 방법 2: SecurityContextHolder 직접 접근
@GetMapping("/mypage")
public ResponseEntity<MemberResponse> getMyPage() {
    Authentication auth = SecurityContextHolder.getContext().getAuthentication();
    CustomUserDetails userDetails = (CustomUserDetails) auth.getPrincipal();
    return ResponseEntity.ok(memberService.getMyPage(userDetails.getMemberId()));
}
```

### 5-7. MemberService — 회원가입 + 로그인 검증

```java
// com/shop/service/MemberService.java
@Service
@RequiredArgsConstructor
@Transactional
public class MemberService {

    private final MemberRepository  memberRepository;
    private final PasswordEncoder   passwordEncoder;

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

    // 로그인은 Spring Security가 CustomUserDetailsService를 통해 자동 처리
    // 추가 검증이 필요하면 CustomUserDetails.isEnabled() / isAccountNonLocked()에서 처리
}
```

### 5-8. 예외 처리 — 인증/인가 실패

```java
// com/shop/config/SecurityConfig.java 내부에 추가
.exceptionHandling(ex -> ex
    // 인증 없이 보호 자원 접근 시 → 401
    .authenticationEntryPoint((request, response, authException) -> {
        response.setContentType("application/json;charset=UTF-8");
        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        response.getWriter().write("{\"message\":\"로그인이 필요합니다.\"}");
    })
    // 권한 없는 자원 접근 시 → 403
    .accessDeniedHandler((request, response, accessDeniedException) -> {
        response.setContentType("application/json;charset=UTF-8");
        response.setStatus(HttpServletResponse.SC_FORBIDDEN);
        response.getWriter().write("{\"message\":\"접근 권한이 없습니다.\"}");
    })
)
```

### 5-9. CORS 설정 (Vue.js와 통신)

```java
// com/shop/config/WebMvcConfig.java
@Configuration
public class WebMvcConfig implements WebMvcConfigurer {

    @Override
    public void addCorsMappings(CorsRegistry registry) {
        registry.addMapping("/api/**")
            .allowedOrigins("http://localhost:5173")   // Vue dev server
            .allowedMethods("GET", "POST", "PUT", "PATCH", "DELETE", "OPTIONS")
            .allowedHeaders("*")
            .allowCredentials(true)   // 세션 쿠키 전송 허용 (중요!)
            .maxAge(3600);
    }
}
```

---

## 6. 프론트엔드 구현 (Vue.js 3)

세션 기반 인증은 브라우저가 **JSESSIONID 쿠키**를 자동으로 관리합니다.
Vue.js에서는 `axios`의 `withCredentials: true` 설정이 핵심입니다.

### 6-1. Axios 설정

```javascript
// src/api/axios.js
import axios from 'axios'

const api = axios.create({
  baseURL: 'http://localhost:8080',
  timeout: 10000,
  withCredentials: true,   // 세션 쿠키 자동 전송 (필수)
})

// 401 응답 → 로그인 페이지로 이동
api.interceptors.response.use(
  (response) => response,
  (error) => {
    if (error.response?.status === 401) {
      window.location.href = '/signin'
    }
    return Promise.reject(error)
  }
)

export default api
```

### 6-2. Pinia Auth Store

```javascript
// src/store/auth.js
import { defineStore } from 'pinia'
import { ref, computed } from 'vue'
import api from '@/api/axios'

export const useAuthStore = defineStore('auth', () => {
  const member    = ref(null)
  const isLoggedIn = computed(() => !!member.value)

  // 로그인: Spring Security가 처리 (/api/auth/signin POST)
  // Content-Type: application/x-www-form-urlencoded (Form Login 기본값)
  async function signin(email, password) {
    const params = new URLSearchParams()
    params.append('email',    email)
    params.append('password', password)

    const { data } = await api.post('/api/auth/signin', params, {
      headers: { 'Content-Type': 'application/x-www-form-urlencoded' }
    })

    // 로그인 성공 후 사용자 정보 조회
    await fetchMe()
    return data
  }

  // 현재 사용자 정보 조회
  async function fetchMe() {
    try {
      const { data } = await api.get('/api/auth/me')
      member.value = data
    } catch {
      member.value = null
    }
  }

  // 로그아웃
  async function signout() {
    await api.post('/api/auth/signout')
    member.value = null
  }

  // 앱 시작 시 로그인 상태 복구
  async function initialize() {
    await fetchMe()
  }

  return { member, isLoggedIn, signin, signout, initialize }
})
```

### 6-3. 앱 시작 시 로그인 상태 복구

```javascript
// src/main.js
import { createApp } from 'vue'
import { createPinia } from 'pinia'
import App from './App.vue'
import router from './router'
import { useAuthStore } from './store/auth'

const app  = createApp(App)
const pinia = createPinia()
app.use(pinia)
app.use(router)

// 세션이 살아있으면 자동으로 로그인 상태 복구
const authStore = useAuthStore()
authStore.initialize().then(() => {
  app.mount('#app')
})
```

### 6-4. Vue Router 가드

```javascript
// src/router/index.js
router.beforeEach(async (to, from, next) => {
  const authStore = useAuthStore()

  if (to.meta.requiresAuth && !authStore.isLoggedIn) {
    return next({ path: '/signin', query: { redirect: to.fullPath } })
  }

  if (to.meta.requiresAdmin &&
      !['MANAGER', 'ADMIN'].includes(authStore.member?.role)) {
    return next({ path: '/' })
  }

  next()
})
```

---

## 7. 권한(Role) 기반 접근 제어

### 7-1. URL 레벨 접근 제어 (SecurityConfig)

```java
.authorizeHttpRequests(auth -> auth
    // 비회원 허용
    .requestMatchers("/api/auth/signup", "/api/auth/signin").permitAll()
    .requestMatchers(HttpMethod.GET, "/api/product/**").permitAll()

    // MANAGER 또는 ADMIN
    .requestMatchers("/api/admin/**").hasAnyRole("MANAGER", "ADMIN")

    // ADMIN만
    .requestMatchers("/api/admin/member/*/role").hasRole("ADMIN")
    .requestMatchers("/api/admin/member/*/status").hasRole("ADMIN")

    // 로그인한 사용자만
    .anyRequest().authenticated()
)
```

### 7-2. 메서드 레벨 접근 제어 (@PreAuthorize)

`@EnableMethodSecurity`를 SecurityConfig에 추가하면 메서드 단위로 권한을 지정할 수 있습니다.

```java
// com/shop/config/SecurityConfig.java
@Configuration
@EnableWebSecurity
@EnableMethodSecurity(prePostEnabled = true)   // ← 추가
public class SecurityConfig { ... }

// com/shop/service/MemberService.java
@PreAuthorize("hasRole('ADMIN')")
public ResponseEntity<String> changeMemberRole(Long memberId, MemberRole newRole) {
    // ADMIN만 실행 가능
}

@PreAuthorize("hasAnyRole('MANAGER', 'ADMIN')")
public ResponseEntity<Page<MemberResponse>> getMemberList(Pageable pageable) {
    // MANAGER 또는 ADMIN만 실행 가능
}

@PreAuthorize("#memberId == authentication.principal.memberId")
public ResponseEntity<MemberResponse> getMyPage(Long memberId) {
    // 본인만 자신의 마이페이지 접근 가능
}
```

### 7-3. 현재 프로젝트 권한 매핑

| 역할 | SecurityConfig 설정 | 접근 가능 URL |
|---|---|---|
| **비회원** | `permitAll()` | 상품 조회, 공지사항, 로그인/회원가입 |
| **USER** | `authenticated()` | 마이페이지, 장바구니, 주문, 결제, 리뷰, QnA |
| **MANAGER** | `hasAnyRole("MANAGER","ADMIN")` | 상품·주문·배송·QnA·공지 관리 |
| **ADMIN** | `hasRole("ADMIN")` | 회원 강퇴·권한 변경 + MANAGER 전체 |

---

## 8. 현재 프로젝트에서의 선택 가이드

현재 프로젝트는 **Vue.js 3 SPA + Spring Boot REST API** 구조입니다.
세션 기반과 JWT 기반 중 어느 것을 선택할지 정리합니다.

### 세션 기반 Form Login을 선택해야 할 때

- 서버 사이드 렌더링(SSR, Thymeleaf) 방식일 때
- 단일 서버 운영 (스케일 아웃 불필요)
- 구현 복잡도를 최소화하고 싶을 때
- Spring Security의 기본 보안 기능을 최대한 활용하고 싶을 때

### JWT 기반을 선택해야 할 때 (현재 프로젝트 권장)

- Vue.js / React 같은 SPA와 REST API 분리 구조
- 모바일 앱과 동일한 API를 공유할 때
- 서버를 수평 확장(다중 인스턴스)할 때
- 마이크로서비스 아키텍처

### 결론: 현재 프로젝트는 JWT + Spring Security 조합

```
Spring Security가 담당:          JWT가 담당:
  - PasswordEncoder (BCrypt)       - Stateless 인증 토큰 발급
  - @PreAuthorize 권한 제어         - AccessToken / RefreshToken 관리
  - SecurityContextHolder 관리      - 토큰 검증 필터
  - CORS / CSRF 설정               - 토큰 기반 사용자 식별
  - 예외 처리 (401, 403)
```

즉, Spring Security의 **보안 프레임워크 기능**은 그대로 사용하면서
**인증 방식만 세션 → JWT로 교체**하는 것이 현재 프로젝트의 올바른 선택입니다.

### 두 방식의 SecurityConfig 핵심 차이

```java
// 세션 기반 (Form Login)
http
  .sessionManagement(session ->
      session.sessionCreationPolicy(SessionCreationPolicy.IF_REQUIRED))  // 세션 사용
  .formLogin(form -> form.loginProcessingUrl("/api/auth/signin") ...)
  .logout(logout -> logout.invalidateHttpSession(true) ...)

// JWT 기반 (현재 프로젝트 적용)
http
  .sessionManagement(session ->
      session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))    // 세션 미사용
  .formLogin(form -> form.disable())                                     // Form Login 비활성
  .httpBasic(basic -> basic.disable())
  .addFilterBefore(jwtAuthFilter, UsernamePasswordAuthenticationFilter.class)
```

---

## 9. 현재 프로젝트 적용 체크리스트

### 세션 기반 Form Login 적용 시

| 단계 | 작업 항목 |
|---|---|
| 1 | `spring-boot-starter-security` 의존성 추가 |
| 2 | `CustomUserDetails` 구현 (UserDetails 인터페이스) |
| 3 | `CustomUserDetailsService` 구현 (UserDetailsService 인터페이스) |
| 4 | `SecurityConfig` 작성 (formLogin, logout, authorizeHttpRequests) |
| 5 | `AuthController` 작성 (signup, me 엔드포인트) |
| 6 | CORS 설정 (`allowCredentials: true`) |
| 7 | `axios` `withCredentials: true` 설정 |
| 8 | Pinia store에서 `URLSearchParams`로 로그인 요청 |
| 9 | 앱 시작 시 `/api/auth/me`로 로그인 상태 복구 |
| 10 | Vue Router `beforeEach` 가드 설정 |

### JWT 기반으로 전환 시 (추가/변경 사항)

| 단계 | 작업 항목 |
|---|---|
| 1 | `jjwt` 의존성 추가 |
| 2 | `SessionCreationPolicy.STATELESS` 변경 |
| 3 | `formLogin().disable()` 추가 |
| 4 | `JwtUtil` 작성 |
| 5 | `JwtAuthenticationFilter` 작성 및 `addFilterBefore` 등록 |
| 6 | `MemberService.signin()` 직접 구현 (JWT 발급) |
| 7 | `RefreshToken` 엔티티 + Repository 추가 |
| 8 | `AuthController`에 `/signin`, `/signout`, `/refresh` 추가 |
| 9 | Pinia store에서 토큰 메모리 저장 방식으로 변경 |
| 10 | axios 인터셉터에서 `Authorization: Bearer` 헤더 자동 첨부 |
| 11 | 401 감지 시 RefreshToken으로 자동 갱신 로직 추가 |
