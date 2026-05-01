# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Commands

```bash
# Build and run all tests
mvn -B -ntp clean install

# Compile only
mvn clean compile

# Run tests
mvn test

# Run a single test class
mvn test -Dtest=ClassName

# Package JAR (skip tests)
mvn clean package -DskipTests

# Run locally
mvn spring-boot:run

# Docker build and push
docker buildx build --platform linux/amd64 -t <image>:tag --push .
```

## Architecture

Spring Boot 3.5.7 REST API (Java 21) with JWT authentication, MongoDB persistence, and RBAC. Runs on port 8080, context path `/api`.

**Request flow:** HTTP → `JwtAuthenticationFilter` → Controller → Service → MongoDB repository

**Layer structure:**
- `controller/` — `AuthController` (public: `/auth/register`, `/auth/login`), `AdminController` (protected: `/user/profile`, `/admin/secret`, `/public/hello`)
- `security/` — `JwtService` (HMAC-SHA256 token generation/validation), `JwtAuthenticationFilter`, `CustomUserDetailsService`, `CustomUserDetails`
- `config/` — `SecurityConfig` (filter chain, CORS, CSRF-off, stateless sessions, `@PreAuthorize` method security), `AppConfig` (`PasswordEncoder` bean + seed admin user)
- `security/` — `CustomOAuth2UserService` (non-OIDC OAuth2 user load/create), `CustomOidcUserService`, `OAuth2LoginSuccessHandler`, `CookieOAuth2AuthorizationRequestRepository`
- `entity/` — `User` and `Role` MongoDB documents; `Role` linked via `@DBRef`; `User` có `provider` + `providerId` cho OAuth2
- `repository/` — Spring Data MongoDB repositories; `UserRepository.findByUsername()`, `findByEmail()`, `findByProviderAndProviderId()`
- `dto/` — `RegisterRequest`, `LoginRequest`, `AuthResponse`, `UserDto`

**Authorization model:** Role-based via Spring `@PreAuthorize`. Roles stored in MongoDB `Role` collection and referenced from `User`.

**OAuth2 / Google login flow:**
- Google dùng OIDC (có scope `openid`) → Spring Security gọi `OidcUserService` mặc định, principal là `DefaultOidcUser`
- `OAuth2LoginSuccessHandler` dùng `instanceof` pattern để handle cả 2 loại principal: `CustomOAuth2UserDetails` (non-OIDC) và `DefaultOidcUser` (OIDC/Google)
- Khi gặp `DefaultOidcUser`, gọi `CustomOAuth2UserService.findOrCreateUser()` để tìm/tạo user trong MongoDB
- `PasswordEncoder` bean đặt trong `AppConfig` (không phải `SecurityConfig`) để tránh circular dependency: `SecurityConfig` → `CustomOAuth2UserService` → `PasswordEncoder` → `SecurityConfig`
- Redirect sau login: `{frontend-redirect-uri}?token={jwt}&username={username}`

## Configuration

`src/main/resources/application.yml` contains MongoDB URI and JWT settings. Override via environment variables in production:

```yaml
spring.data.mongodb.uri                          # MongoDB connection
app.jwt.secret                                   # 32-char HMAC secret
app.jwt.expiration-ms                            # Token TTL (default: 86400000 = 24h)
GOOGLE_CLIENT_ID                                 # Google OAuth2 client ID
GOOGLE_CLIENT_SECRET                             # Google OAuth2 client secret
app.oauth2.frontend-redirect-uri                 # Frontend callback URL (e.g. http://localhost:5174/oauth2/callback)
```

**Google Cloud Console:** Authorized redirect URI phải là `{baseUrl}/api/login/oauth2/code/google` (chú ý prefix `/api` theo context-path).

## Git Workflows

### "push lên master" — Full auto-merge workflow
Khi user yêu cầu **push lên master**, thực hiện đầy đủ các bước sau theo thứ tự:
1. Tạo branch mới từ master (đặt tên theo convention: `fix/...`, `feat/...`, `docs/...`)
2. Commit toàn bộ thay đổi lên branch mới và push lên remote
3. Tạo GitHub issue mô tả nội dung thay đổi
4. Tạo Pull Request vào master (link với issue vừa tạo)
5. Merge Pull Request vào master
6. Xóa branch vừa merge
7. Checkout về master và pull code mới nhất về local

### "push và để tôi review" — PR-only workflow
Khi user yêu cầu **push và để tôi review**, chỉ thực hiện:
1. Tạo branch mới từ master (đặt tên theo convention: `fix/...`, `feat/...`, `docs/...`)
2. Commit toàn bộ thay đổi lên branch mới và push lên remote
3. Tạo GitHub issue mô tả nội dung thay đổi
4. Tạo Pull Request vào master (link với issue vừa tạo)
5. Dừng lại — **không merge**, chờ user review và quyết định

## Infrastructure

**Docker Hub:** `trivip002/admin-api` — build with `--platform linux/amd64`

**Kubernetes (ArgoCD):**
- Namespace: `admin-api`
- Deployment: 2 replicas, RollingUpdate (maxSurge=1, maxUnavailable=0)
- Secret name: `admin-api-secret` — keys: `MONGODB_URI`, `JWT_SECRET`
- Health check endpoint: `GET /api/actuator/health`
- GitOps repo: `devapihub/argocd`, manifest path: `app/admin-api/k8s/admin-api-deployment.yaml`

**MongoDB:** `mongodb://admin:<password>@103.165.144.81:27017/shop_dev?authSource=admin`

## CI/CD

GitHub Actions (`.github/workflows/git-action.yml`) on push to `master`:
1. Maven build (`mvn -B -ntp clean install`, Java 21, MongoDB service container on port 27017)
2. Docker buildx → DockerHub `trivip002/admin-api` (tagged with short git SHA + `latest`)
3. Updates ArgoCD GitOps repo with new image tag
4. Sends Telegram deployment notification
