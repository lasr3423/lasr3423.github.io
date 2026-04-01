java
.allowedOrigins(
    "http://localhost:5173",    // 개발용 — 배포 시 필요 없음
    "https://your-domain.com"   // 플레이스홀더 — 실제 도메인으로 바꿔야 함
)
지금은 도메인이 코드에 하드코딩되어 있어요. 배포할 때마다 코드를 수정하고 다시 빌드해야 하는 구조예요.

권장 방법 — 환경변수로 분리
application.yml에 이미 ${환경변수} 패턴을 잘 사용하고 있어요 (DB_USERNAME, JWT_SECRET 등). CORS 도메인도 같은 방식으로 처리하면 됩니다.

1단계 — application.yml에 항목 추가

yaml
# application.yml 에 추가
app:
  base-url: ${APP_BASE_URL:http://localhost:8202}   # 이미 있음
  cors:
    allowed-origins: ${CORS_ALLOWED_ORIGINS:http://localhost:5173}
콜론(:) 뒤가 기본값이에요. 환경변수가 없으면 개발용 주소가 자동으로 사용돼요.

2단계 — WebMvcConfig.java에서 값을 주입받아 사용

java
@Configuration
public class WebMvcConfig implements WebMvcConfigurer {

    // application.yml의 값을 자동으로 읽어옴
    @Value("${app.cors.allowed-origins}")
    private String allowedOrigins;

    @Bean
    public RestTemplate restTemplate() {
        return new RestTemplate();
    }

    @Override
    public void addCorsMappings(CorsRegistry registry) {
        registry.addMapping("/api/**")
                .allowedOrigins(allowedOrigins.split(","))  // 쉼표로 여러 도메인 구분
                .allowedMethods("GET", "POST", "PUT", "PATCH", "DELETE", "OPTIONS")
                .allowedHeaders("*")
                .allowCredentials(true)
                .maxAge(3600);
    }
}
```

`.split(",")` 를 사용하면 환경변수 하나로 여러 도메인을 관리할 수 있어요.

---

## 환경별 설정값

**개발 환경 (로컬)** — `.env` 또는 환경변수 없이 기본값 사용
```
환경변수 설정 없음
→ application.yml 기본값: http://localhost:5173
→ CORS 허용 대상: http://localhost:5173
```

**배포 환경 (서버)** — Docker나 서버에서 환경변수 설정
```
CORS_ALLOWED_ORIGINS=https://kyobo-shop.com,https://www.kyobo-shop.com
→ CORS 허용 대상: 두 도메인 모두 허용
Docker 배포 시 환경변수 설정 방법
현재 프로젝트가 Docker 배포를 계획하고 있으니, docker-compose.yml에서 아래처럼 설정해요.

yaml
# docker-compose.yml
services:
  spring:
    environment:
      - CORS_ALLOWED_ORIGINS=https://kyobo-shop.com,https://www.kyobo-shop.com
      - APP_BASE_URL=https://kyobo-shop.com
      - FRONT_OAUTH_REDIRECT_URI=https://kyobo-shop.com/oauth/callback
또는 .env 파일로 분리해서 관리해요.

bash
# .env (docker-compose가 자동으로 읽음)
CORS_ALLOWED_ORIGINS=https://kyobo-shop.com,https://www.kyobo-shop.com
APP_BASE_URL=https://kyobo-shop.com
FRONT_OAUTH_REDIRECT_URI=https://kyobo-shop.com/oauth/callback
도메인을 정하는 기준
배포 도메인을 결정할 때 고려해야 할 것들이에요.

http:// vs https:// — 배포 시에는 반드시 https를 사용해야 해요. allowCredentials(true)가 설정되어 있는데, 브라우저는 https가 아닌 환경에서 쿠키/인증 헤더 전달을 차단해요.

www 포함 여부 — https://kyobo-shop.com과 https://www.kyobo-shop.com은 브라우저 입장에서 다른 Origin이에요. 둘 다 쓴다면 쉼표로 둘 다 등록해야 합니다.

포트 번호 — 배포 후에는 80(http)/443(https) 포트를 사용하므로 포트 번호를 명시하지 않아도 돼요.

정리
환경	CORS 도메인 설정 방법
로컬 개발	환경변수 없이 기본값 http://localhost:5173 자동 사용
Docker 배포	docker-compose.yml 또는 .env에 CORS_ALLOWED_ORIGINS 설정
도메인 결정 시점	실제 도메인이 생겼을 때 .env만 수정, 코드 재빌드 불필요