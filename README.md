# ReadMe Docker Guide

이 저장소는 아래 3개 컨테이너 기준으로 동작합니다.

- `frontend`: Vue + Nginx
- `backend`: Spring Boot 3 / Java 21
- `postgres`: PostgreSQL 16

기본 구성은 Docker Desktop이 설치된 macOS, Windows 둘 다 바로 사용할 수 있게 맞춰져 있습니다. Apple Silicon 맥에서는 `linux/arm64`, 일반 윈도우 PC에서는 `linux/amd64` 이미지가 자동 선택됩니다.

## 1. 팀 공용 환경변수 준비

루트에서 `.env.example`을 복사해서 `.env`를 만듭니다.

macOS / Linux:

```bash
cp .env.example .env
```

Windows PowerShell:

```powershell
Copy-Item .env.example .env
```

필수로 확인할 값은 아래입니다.

- `DOCKER_IMAGE_NAMESPACE`: Docker Hub 사용자명 또는 조직명
- `JWT_SECRET`: 운영/공용 환경이면 반드시 변경
- `APP_BASE_URL`
- `FRONT_OAUTH_REDIRECT_URI`
- `CORS_ALLOWED_ORIGINS`
- `KAKAO_REDIRECT_URI`

주의:

- `FRONTEND_PORT`를 바꾸면 위 URL 값들도 같이 맞춰야 합니다.
- 기본값은 `http://localhost` 기준입니다.
- 백엔드는 호스트 `8202`, 프론트는 호스트 `80`, DB는 호스트 `5420`으로 연결됩니다.

## 2. 로컬 실행

이미지 빌드 + 실행:

```bash
docker compose up --build -d
```

로그 확인:

```bash
docker compose logs -f
```

중지:

```bash
docker compose down
```

DB 볼륨까지 같이 삭제:

```bash
docker compose down -v
```

## 3. Docker Hub 같은 레포지토리로 업로드

먼저 로그인합니다.

```bash
docker login
```

macOS / Linux:

```bash
chmod +x ./scripts/docker-publish.sh
./scripts/docker-publish.sh your-dockerhub-id v1
```

Windows PowerShell:

```powershell
./scripts/docker-publish.ps1 -Namespace your-dockerhub-id -Tag v1
```

이 스크립트는 아래 이미지를 둘 다 멀티플랫폼으로 푸시합니다.

- `your-dockerhub-id/readme-backend:v1`
- `your-dockerhub-id/readme-frontend:v1`

대상 플랫폼:

- `linux/amd64`
- `linux/arm64`

## 4. 다른 팀원이 받는 방법

이미지를 이미 푸시해 둔 상태라면 `.env`에서 아래만 맞춘 뒤 실행하면 됩니다.

- `DOCKER_IMAGE_NAMESPACE`
- `IMAGE_TAG`

그리고:

```bash
docker compose pull
docker compose up -d --no-build
```

## 5. 현재 프로젝트 구조

- `/readme`: Spring Boot 백엔드
- `/readme_vue`: Vue 프론트엔드
- `/docker-compose.yml`: 통합 실행
- `/.env.example`: 팀 공용 환경변수 템플릿
- `/docker-bake.hcl`: 멀티플랫폼 이미지 빌드 정의
- `/scripts/docker-publish.sh`
- `/scripts/docker-publish.ps1`
