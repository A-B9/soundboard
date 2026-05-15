# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Commands

```bash
# Build
mvn clean package

# Run (default profile, H2 in-memory DB)
./mvnw spring-boot:run

# Run tests
mvn test

# Run a single test class
mvn test -Dtest=TestUserService

# Code quality
mvn spotbugs:spotbugs

# Docker deploy (stops → builds → starts)
./deploy.sh
```

## Architecture

Spring Boot 4 / Java 21 REST API for audio file management with JWT authentication and role-based access control.

**Main package**: `com.soundboard.soundboard`

**Layers**:
- `web/` — REST controllers (`SoundController`, `UserController`, `AdminUserController`) and `GlobalExceptionHandler`
- `service/` — Business logic (`SoundService`, `UserService`, `AdminUserService`, `LocalAudioStorageService`, `MyUserDetailsService`, `IService`)
- `repository/` — Spring Data JPA repos (`SoundRepository`, `MyUserRepo`)
- `models/` — JPA entities, DTOs, `Role` enum, `requestModels/`, `responseModels/sound/`, `responseModels/user/`
- `security/` — `SecurityConfig`, `JWTService`, `MyUserPrincipal`, and `security/filter/` (`JwtFilter`, `LoginRateLimitFilter`)
- `bootstrap/` — `SuperAdminBootstrapper` (seeds SUPER_ADMIN on startup when configured)
- `config/` — `AdminProperties`, `BootstrapProperties`, `AdminConfigValidator`, `LoginRateLimitProperties`
- `audit/` — `AuditLogger` (`@Component`, named SLF4J logger `"AUDIT"`), `AuditAction` enum
- `exceptions/` — `SoundNotFoundException`, `ValidationExceptionHandler`
- `util/`, `audio/`, `mapper/`, `annotation/` — Constants, enums, audio storage config (`AudioStorageProperties`), MapStruct mappers, custom annotations (`@CurrentUser`)

**Public endpoints**: `POST /api/soundboard/user/register` (always creates `Role.USER`), `POST /api/soundboard/user/login`

**Authenticated endpoints** (require `Authorization: Bearer <token>`):
- `POST /api/soundboard/user/password-reset` — change own password; requires `currentPassword` + `newPassword` (`ChangePasswordRequest`); returns a fresh JWT in `{ token }`; 400 if current password wrong or new equals current; this is the **only endpoint usable while `mustChangePassword=true`** (see Security)
- `POST /api/soundboard/sounds` — multipart upload (`soundRequest` JSON part + `file` part)
- `GET /api/soundboard/sounds` — paginated list (`page`, `size`, `sortBy`, `ascending` query params)
- `GET /api/soundboard/sounds/{id}`
- `GET /api/soundboard/sounds/search?keyword=...`
- `GET /api/soundboard/sounds/{id}/download` — streams the audio file
- `DELETE /api/soundboard/sounds/{id}`

**Admin endpoints** (require `ADMIN` or `SUPER_ADMIN` role at URL level; further restricted by `@PreAuthorize` and service-layer scope checks):
- `GET /api/soundboard/admin/users` — list users; ADMIN sees only `USER`-role accounts, SUPER_ADMIN sees all
- `GET /api/soundboard/admin/users/{id}` — get user by id; ADMIN may only fetch `USER`-role accounts (403 otherwise)
- `PATCH /api/soundboard/admin/users/{id}/active` — toggle active flag (no body — flips current value); cannot target self (409); ADMIN may only toggle `USER`-role accounts (403 otherwise)
- `PATCH /api/soundboard/admin/users/{id}/password-reset` — set `mustChangePassword` boolean on a user (`PatchUserRequest`); ADMIN may only patch `USER`-role accounts (403 otherwise); cannot patch self (409)
- `POST /api/soundboard/admin/users` — create user with any `Role`; `displayName` defaults to `username` if omitted; `mustChangePassword` inherits `app.admin.force-password-change`; duplicate username → 409 (`SUPER_ADMIN` only)
- `DELETE /api/soundboard/admin/users/{id}/hard-delete` — permanently delete user and cascade their sounds + audio files; cannot delete self (409); cannot delete the last `SUPER_ADMIN` (409); returns 204 (`SUPER_ADMIN` only)

## Security

- Stateless JWT auth (JJWT 0.12.6); two custom filters in `security/filter/`: `LoginRateLimitFilter` (runs before `LogoutFilter`, position ~1099) → `JwtFilter` (runs before `UsernamePasswordAuthenticationFilter`, position ~1799)
- BCrypt strength 10 (`Constants.BCRYPT_STRENGTH`); CSRF disabled
- Tokens issued on `POST /api/soundboard/user/login`, expire after 2 hours, signed with HMAC-SHA (HS256/HS384/HS512 auto-selected from key byte length via `Keys.hmacShaKeyFor`) via `app.jwt.secret` (Base64); validated per-request via filter
- **JWT claims**: `sub` (username), `iss=soundboard` (required on parse — tokens with wrong/missing issuer are rejected), `role` (`USER`/`ADMIN`/`SUPER_ADMIN`), `mustChangePassword` (boolean; absent treated as `false`), `iat`, `exp`. Authorities are built from the JWT `role` claim per-request — no DB lookup; role changes take effect on next login.
- `JWTService` rejects startup under the `prod` profile if `app.jwt.secret` is still the dev default
- `JwtFilter` rejects tokens for disabled accounts (`Users.active = false`) via `AccountStatusUserDetailsChecker` — proceeds unauthenticated → 401
- Malformed/tampered JWTs caught in `JwtFilter` and result in 401, not 500
- **Forced password-change gate**: when the JWT `mustChangePassword` claim is `true`, `JwtFilter` short-circuits every request (even authenticated ones) with HTTP **403** and body `{"error":"Password change required before accessing this resource"}`, except `POST /api/soundboard/user/password-reset`. Enforcement is claim-based — clearing the DB flag has no effect until a new token is issued.
- **Role-based access**: two enforcement layers — (1) URL guard in `SecurityConfig` (`/api/soundboard/admin/**` requires `ADMIN` or `SUPER_ADMIN`); (2) `@PreAuthorize` on individual `AdminUserController` methods (`SUPER_ADMIN`-only on `POST` and `DELETE /{id}/hard-delete`). `@EnableMethodSecurity` + `AnnotationTemplateExpressionDefaults` bean enable SpEL template expressions.
- Password strength: minimum 12 characters. `RegisterRequest` and `ChangePasswordRequest` use `.*[^a-zA-Z0-9].*` (any non-alphanumeric). `CreateAdminUserRequest` uses a stricter explicit allow-list regex (`!@#$%^&*()_+-=[]{};':"\\|,.<>/?`) — these are intentionally different; align before relying on interchangeability.
- `POST /api/soundboard/user/register` and `POST /api/soundboard/user/login` use dedicated request DTOs — JPA entity is never bound directly from HTTP request body
- CORS allow-list driven by `app.cors.allowed-origins` (defaults to `http://localhost:3000`); applied to `/api/**` only; allowed methods `GET/POST/DELETE/OPTIONS`, allowed headers `Authorization`/`Content-Type`, credentials disabled. **Note: `PATCH` is not in the allowed methods** despite admin PATCH endpoints existing — browser-based calls to admin PATCH endpoints from a CORS origin will fail the preflight.
- Security headers: `X-Content-Type-Options`, `X-Frame-Options: SAMEORIGIN`, HSTS (1y, includeSubDomains), `Referrer-Policy: no-referrer`, cache-control
- HTTPS redirect controlled by `security.require-https` (default `false`; set `true` in prod via `application-prod.properties`)
- **Login rate limiting**: `LoginRateLimitFilter` enforces per-IP Bucket4j token-bucket limits on `POST /api/soundboard/user/login` only. Exhausted bucket → HTTP 429 with `{"error":"Too many login attempts. Please try again later."}`. IP extracted from `X-Forwarded-For` header first, falling back to `remoteAddr`. Configured via `app.rate-limit.login.{capacity, refill-tokens, refill-period-seconds}` (defaults 10 / 10 / 60 s). Integration tests override to `capacity=1000` in `application-test.properties` to avoid interfering with login test suites that make many requests.
- **Audit logging**: all security-relevant events (user create/delete, active toggle, password-change flag, bootstrap) flow through `AuditLogger` to the named SLF4J logger `"AUDIT"` at `WARN` level. Actions: `BOOTSTRAP_SUPER_ADMIN_CREATED`, `USER_ACTIVE_TOGGLED`, `USER_MUST_CHANGE_PASSWORD_SET`, `USER_CREATED`, `USER_HARD_DELETED`, `DISK_FILE_DELETE_FAILED`. Format: `action=<ACTION> [role='<ROLE>' actor='<USERNAME>'] <details>`. Route independently via `logback-spring.xml` `<logger name="AUDIT">` if needed.

## Bootstrap & Admin Config

- `SuperAdminBootstrapper` (implements `ApplicationRunner`) seeds a `SUPER_ADMIN` account on startup if `app.bootstrap.username` and `app.bootstrap.password` are set; skips if a `SUPER_ADMIN` already exists; emits an AUDIT WARN log on successful creation
- Bootstrapping is **mandatory** in the `prod` profile — app refuses to start if either credential is missing
- `app.admin.force-password-change` (default `true` via `@DefaultValue`) controls the `mustChangePassword` flag on the seeded account and on admin-created users; set to `false` in default `application.properties` for dev convenience; `AdminConfigValidator` logs a WARN at startup when the flag is `false`
- `BootstrapProperties` binds `app.bootstrap.*`; `AdminProperties` binds `app.admin.*`; `AdminConfigValidator` validates consistency

## Database & Profiles

| Profile | Database | DDL |
|---------|----------|-----|
| default | H2 in-memory (console at `/h2-console`) | `update` |
| `dev` | PostgreSQL localhost (`application-dev.properties`) | `update` |
| `prod` | PostgreSQL via env vars (`SPRING_DATASOURCE_URL/USERNAME/PASSWORD`, `JWT_SECRET`) | `validate` |
| `test` | TestContainers PostgreSQL | `create-drop` |

Activate with `-Dspring.profiles.active=dev` or `SPRING_PROFILES_ACTIVE=prod`.

**Entities**: `Users` (id, username, password, displayName, createdAt, active, role, mustChangePassword) and `SoundEntity` (id, name, description, contentType, audioFile LOB, createdAt, storedName, size, ownedBy, active, category, tags via `@ElementCollection`, recentUpdate).

**DTOs**: `SoundDTO` (id, name, description, ownedBy, category, tags, createdAt, recentUpdate), `GetSoundResponse` (same shape, implements `ResponseBodyModel`), `UserDTO` (id, username, displayName, createdAt, role, active).

**Request models** (`requestModels/`): `RegisterRequest` (username, password), `LoginRequest` (username, password), `SoundRequestModel` (name, description), `ChangePasswordRequest` (currentPassword, newPassword — password strength constraints), `PatchUserRequest` (mustChangePassword), `CreateAdminUserRequest` (username, password, role, displayName).

**Response models** (`responseModels/`): `sound/` (`CreateSoundResponse`, `GetSoundResponse`, `ResponseBodyModel`), `user/` (`RegisterResponse`, `LoginResponse`, `ChangePasswordResponse`).

## Audio Storage

Files saved to local filesystem via `LocalAudioStorageService`. Two related properties:
- `app.sounds.directory` (default `./sounds`) — used by `application.properties`
- `app.audio-storage.base-path` (default `./SoundAudio` from `AudioStorageProperties` record; tests override to `./test-audio-storage`) — also defines `allowedMimeTypes` (`audio/mp3`, `audio/wav`, `audio/wave`)

Max upload size: 10 MB (`spring.servlet.multipart.max-file-size`).

## Testing

- `src/test/.../unit/service/` — Mockito unit tests (`TestUserService`, `TestSoundService`, `TestAdminUserService`, `TestJWTService`); `TestMeService` tests `UserService.changePassword` — the class is named for the old `MeService` which no longer exists
- `src/test/.../unit/mapper/` — Lightweight Spring context mapper tests (`TestMapper`)
- `src/test/.../unit/bootstrap/` — Unit tests for `SuperAdminBootstrapper` (`TestSuperAdminBootstrapper`)
- `src/test/.../unit/config/` — Unit tests for config validation (`TestAdminProperties`)
- `src/test/.../unit/security/` — Unit tests for `MyUserPrincipal` (`TestMyUserPrincipal`), `LoginRateLimitFilter` (`TestLoginRateLimitFilter`)
- `src/test/.../unit/audit/` — Unit tests for `AuditLogger` (`TestAuditLogger`); uses Logback `ListAppender<ILoggingEvent>` attached to the `"AUDIT"` logger — no file I/O, no mocking
- `src/test/.../integration/` — TestContainers integration tests with `BaseIntegrationTest`, organised under:
  - `controller/sound/` (`CreateTests`, `GetTests`, `PatchTests`, `DeleteTests`)
  - `controller/user/` (`RegisterTests`, `LoginTests`, `ChangePasswordTests`, `LoginRateLimitTests`)
  - `controller/admin/` (`AdminSecurityTests`, `AdminUserCreateTests`, `AdminUserGetTests`, `AdminUserHardDeleteTests`, `AdminUserListTests`, `AdminUserToggleActiveTests`)
  - `bootstrap/` (`SuperAdminBootstrapIntegrationTest`)
- Shared seed data in `fixtures/SoundSeeder`
- `TestJwtHelper.java` — shared JWT utility for integration tests
- `src/test/resources/application-test.properties` — test profile config (TestContainers overrides datasource via `@DynamicPropertySource`)
- `src/test/.../docs/` — internal notes on test design and security fixes
- Target: 85% minimum line coverage across all layers

## CI (GitHub Actions)

- **build-project.yml** — triggered on push/PR to `master`; runs `mvn -B package` with JDK 21 (Temurin)
- **code-quality.yml** — SpotBugs on push/PR to `master`; parses `target/spotbugsXml.xml`, posts a summary comment on PRs, fails the job on any High-severity bug. OWASP Dependency Check job is present but commented out.

## Docker

Multi-stage Dockerfile (Maven 3.9.6 / Temurin 21 build → `eclipse-temurin:21-jre-jammy` runtime, port 8080). `compose.yaml` runs the app + `postgres:16` with a persistent `soundboard-db-data` volume and a Postgres healthcheck gating app startup. Use `./deploy.sh` for a one-command stop / build / up cycle.
