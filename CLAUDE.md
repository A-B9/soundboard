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
- `config/` — `AdminProperties`, `BootstrapProperties`, `AdminConfigValidator`, `LoginRateLimitProperties`, `AudioConfig` (exposes a singleton Apache Tika `@Bean` for content-type detection)
- `audit/` — `AuditLogger` (`@Component`, named SLF4J logger `"AUDIT"`), `AuditAction` enum
- `exceptions/` — `SoundNotFoundException`, `ValidationExceptionHandler`
- `util/`, `audio/`, `mapper/`, `annotation/` — Constants, enums, audio storage config (`AudioStorageProperties`), MapStruct mappers, custom annotations (`@CurrentUser`)

**Public endpoints**: `POST /api/soundboard/user/register` (always creates `Role.USER`), `POST /api/soundboard/user/login`

**Authenticated endpoints** (require `Authorization: Bearer <token>`):
- `POST /api/soundboard/user/password-reset` — change own password; requires `currentPassword` + `newPassword` (`ChangePasswordRequest`); returns a fresh JWT in `{ token }`; 400 if current password wrong or new equals current; this is the **only endpoint usable while `mustChangePassword=true`** (see Security)
- `POST /api/soundboard/sounds` — multipart upload (`soundRequest` JSON part + `file` part)
- `GET /api/soundboard/sounds` — paginated list; query params: `page`, `size`, `sortBy`, `ascending` (sort/page), plus optional `category`. `sortBy` is constrained by `@Pattern` to `createdAt|recentUpdate|name|category` (invalid value → 400), preventing arbitrary property injection into the JPA `Sort`. Optional `category` (case-insensitive, 400 on invalid value) and `tag` (case-insensitive) filters; params may be combined; returns `PagedResponse<ResponseBodyModel>`
- `GET /api/soundboard/sounds/{id}`
- `PATCH /api/soundboard/sounds/{id}` — partial update (`PatchSoundRequest` JSON body); owner-only (404 if not owned); at least one field required (400 otherwise); returns updated `GetSoundResponse`
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
- Password strength: minimum 12 characters. `RegisterRequest` and `ChangePasswordRequest` use `.*[^a-zA-Z0-9].*` (any non-alphanumeric). `CreateAdminUserRequest` uses a stricter explicit allow-list regex (`!@#$%^&*()_+-=[]{};':"\\|,.<>/?`) — these are intentionally different; align before relying on interchangeability. `username` and `displayName` in `CreateAdminUserRequest` are capped at 50 characters via `@Size(max=50)`.
- `GlobalExceptionHandler` (`web/`) handles `ResponseStatusException` → `ProblemDetail` JSON (RFC 7807); `IllegalArgumentException` and `MethodArgumentTypeMismatchException` also handled there. `ValidationExceptionHandler` (`exceptions/`) handles `MethodArgumentNotValidException` (field errors map) and `HttpMessageNotReadableException` — including enum deserialization failures, which return `{"error":"Invalid value '...' for field '...'"}`.
- `POST /api/soundboard/user/register` and `POST /api/soundboard/user/login` use dedicated request DTOs — JPA entity is never bound directly from HTTP request body
- CORS allow-list driven by `app.cors.allowed-origins` (defaults to `http://localhost:3000`); applied to `/api/**` only; allowed methods `GET/POST/DELETE/PATCH/OPTIONS`, allowed headers `Authorization`/`Content-Type`, credentials disabled.
- Security headers: `X-Content-Type-Options`, `X-Frame-Options: SAMEORIGIN`, HSTS (1y, includeSubDomains), `Referrer-Policy: no-referrer`, cache-control
- HTTPS redirect controlled by `security.require-https` (default `false`; set `true` in prod via `application-prod.properties`)
- **Login rate limiting**: `LoginRateLimitFilter` enforces per-IP Bucket4j token-bucket limits on `POST /api/soundboard/user/login` only. Exhausted bucket → HTTP 429 with `{"error":"Too many login attempts. Please try again later."}`. The bucket key (client IP) is read **only** from `request.getRemoteAddr()` — never from request headers, which are forgeable and would let an attacker bypass the limit by rotating `X-Forwarded-For`. Real client IPs are resolved by Tomcat via `server.forward-headers-strategy=native`, which honours forwarded headers only when the request arrives from a trusted proxy. Configured via `app.rate-limit.login.{capacity, refill-tokens, refill-period-seconds}` (defaults 10 / 10 / 60 s). Integration tests override to `capacity=1000` in `application-test.properties` to avoid interfering with login test suites that make many requests.
- **Audit logging**: all security-relevant events (user create/delete, active toggle, password-change flag, bootstrap, login attempts) flow through `AuditLogger` to the named SLF4J logger `"AUDIT"` at `WARN` level. Actions: `BOOTSTRAP_SUPER_ADMIN_CREATED`, `USER_ACTIVE_TOGGLED`, `USER_MUST_CHANGE_PASSWORD_SET`, `USER_CREATED`, `USER_HARD_DELETED`, `DISK_FILE_DELETE_FAILED`, `LOGIN_FAILED`, `LOGIN_SUCCESSFUL` (the last two emitted from `UserService` on the login path). Format: `action=<ACTION> [role='<ROLE>' actor='<USERNAME>'] <details>`. Route independently via `logback-spring.xml` `<logger name="AUDIT">` if needed.

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

**Entities**: `Users` (id, username, password, displayName, createdAt, active, role, mustChangePassword) and `SoundEntity` (id, name, description, contentType, createdAt, storedName, size, ownedBy, active, category, tags via `@ElementCollection`, recentUpdate). The audio bytes live on disk only — there is no LOB column.

**`SoundRepository` query methods**: `findAllByOwnedBy` (pageable + list variants), `findByIdAndOwnedBy`, `findAllByOwnedByAndCategory` (pageable, by `SoundCategoryEnum`), `findAllByOwnedByAndTag` (pageable, case-insensitive tag match via `UPPER()`), `findAllByOwnedByAndCategoryAndTag` (pageable, intersection of both filters), `searchByOwner` (LIKE on name, scoped to user), `search` (LIKE on name, all users).

**DTOs**: `SoundDTO` (id, name, description, ownedBy, category, tags, createdAt, recentUpdate), `GetSoundResponse` (same shape, implements `ResponseBodyModel`), `UserDTO` (id, username, displayName, createdAt, role, active), `AudioDownload` (record: `contentType` + `Resource`) — returned by `SoundService.getAudioFile()` so the download endpoint streams bytes plus their content type without the controller re-reading `SoundEntity`.

**Request models** (`requestModels/`): `RegisterRequest` (username `@Size(max=50)`, password — strength constraints), `LoginRequest` (username, password), `SoundRequestModel` (name `@Size(min=1,max=30)`, description `@Size(min=1,max=250)`), `PatchSoundRequest` (name `@Size(min=1)`, description `@Size(max=250)`, category, tags `@Size(max=25)` — all optional, at least one required at service layer), `ChangePasswordRequest` (currentPassword, newPassword — password strength constraints), `PatchUserRequest` (mustChangePassword), `CreateAdminUserRequest` (username, password, role, displayName). Length caps on free-text inputs bound the attack surface (oversized payloads, log/DB bloat) and are rejected with 400 before reaching the service layer.

**Response models** (`responseModels/`): `sound/` (`CreateSoundResponse`, `GetSoundResponse`, `ResponseBodyModel`), `user/` (`RegisterResponse`, `LoginResponse`, `ChangePasswordResponse`), `PagedResponse<T>` (generic wrapper with `content`, `page`, `size`, `totalElements`, `totalPages`, `first`, `last`; built via `PagedResponse.from(Page<T>)`).

## Audio Storage

Files saved to local filesystem via `LocalAudioStorageService` using a date-hierarchy layout (`YYYY/MM/DD/<uuid>.<ext>`). Audio bytes are **not** persisted in the database — `SoundEntity` only holds `storedName` (the on-disk path); downloads stream from disk via `storageService.getAudioResource(storedName)`.

**Upload validation** (`SoundService.validateAudio`): two-stage MIME check. (1) the declared multipart `Content-Type` must be non-null and in `allowedMimeTypes`; (2) Apache Tika sniffs the file's magic bytes (`tika.detect`) and the detected type must both *equal* the declared type and itself be in `allowedMimeTypes`. Any mismatch (e.g. a non-audio file renamed to `.mp3`) throws `IllegalArgumentException` → 400. The Tika instance is the `@Bean` from `config/AudioConfig`.
- `app.audio-storage.base-path` (default `./SoundAudio`; set to `/dockerContainerApp/SoundAudio` in Docker via `APP_AUDIO_STORAGE_BASE_PATH` env var) — also defines `allowedMimeTypes` (`audio/mp3`, `audio/mpeg`, `audio/wav`, `audio/wave`). `audio/mpeg` is the standard IANA MIME type sent by most HTTP clients for MP3 files.
- `app.sounds.directory` (default `./sounds`) — legacy property present in `application.properties`; not used by any service.
- In Docker, audio files are persisted across redeploys via the `soundboard-audio-data` named volume mounted at `/dockerContainerApp/SoundAudio`.

Max upload size: 10 MB (`spring.servlet.multipart.max-file-size`).

## Testing

- `src/test/java/com/soundboard/soundboard/unit/service/` — Mockito unit tests (`TestUserService`, `TestSoundService`, `TestAdminUserService`, `TestJWTService`); `TestMeService` tests `UserService.changePassword` — the class is named for the old `MeService` which no longer exists; `TestLocalAudioStorageService` (9 tests, uses JUnit `@TempDir`) covers `storeAudioFile` (date-hierarchy layout, extension handling, uniqueness), `getAudioResource` (happy path, missing file, path traversal `SecurityException`), and `deleteAudioFile` (happy path, missing file, path traversal `SecurityException`)
- `src/test/java/com/soundboard/soundboard/unit/mapper/` — Lightweight Spring context mapper tests (`TestMapper`)
- `src/test/java/com/soundboard/soundboard/unit/bootstrap/` — Unit tests for `SuperAdminBootstrapper` (`TestSuperAdminBootstrapper`)
- `src/test/java/com/soundboard/soundboard/unit/config/` — Unit tests for config validation (`TestAdminProperties`)
- `src/test/java/com/soundboard/soundboard/unit/security/` — Unit tests for `MyUserPrincipal` (`TestMyUserPrincipal`), `LoginRateLimitFilter` (`TestLoginRateLimitFilter`)
- `src/test/java/com/soundboard/soundboard/unit/audit/` — Unit tests for `AuditLogger` (`TestAuditLogger`); uses Logback `ListAppender<ILoggingEvent>` attached to the `"AUDIT"` logger — no file I/O, no mocking
- `src/test/java/com/soundboard/soundboard/integration/` — TestContainers integration tests with `BaseIntegrationTest` (`src/test/java/com/soundboard/soundboard/integration/BaseIntegrationTest.java`), organised under:
  - `src/test/java/com/soundboard/soundboard/integration/controller/sound/` (`CreateTests`, `GetTests`, `PatchTests`, `FilterTests`, `DeleteTests`)
  - `src/test/java/com/soundboard/soundboard/integration/controller/user/` (`RegisterTests`, `LoginTests`, `ChangePasswordTests`, `LoginRateLimitTests`, `LoginTimingTests` — measures the failed-login timing side-channel: a real-user/wrong-password login vs. a no-such-user login should be timing-indistinguishable, relying on `DaoAuthenticationProvider`'s dummy BCrypt compare)
  - `src/test/java/com/soundboard/soundboard/integration/rateLimiter/` (`RateLimitTest`)
  - `src/test/java/com/soundboard/soundboard/integration/controller/admin/` (`AdminSecurityTests`, `AdminUserCreateTests`, `AdminUserGetTests`, `AdminUserHardDeleteTests`, `AdminUserListTests`, `AdminUserToggleActiveTests`)
  - `src/test/java/com/soundboard/soundboard/integration/bootstrap/` (`SuperAdminBootstrapIntegrationTest`)
- Shared seed data in `src/test/java/com/soundboard/soundboard/integration/fixtures/SoundSeeder.java`
- `src/test/java/com/soundboard/soundboard/TestJwtHelper.java` — shared JWT utility for integration tests
- `src/test/resources/application-test.properties` — test profile config (TestContainers overrides datasource via `@DynamicPropertySource`)
- `src/test/java/com/soundboard/soundboard/docs/` — internal notes on test design and security fixes
- Target: 85% minimum line coverage across all layers; enforced by JaCoCo in the `verify` phase

## CI (GitHub Actions)

- **build-project.yml** — triggered on push/PR to `master`; runs `mvn -B verify` with JDK 21 (Temurin); JaCoCo 0.8.12 enforces 85% line coverage at the `verify` phase — build fails if coverage drops below threshold
- **code-quality.yml** — SpotBugs on push/PR to `master`; parses `target/spotbugsXml.xml`, posts a summary comment on PRs, fails the job on any High-severity bug. OWASP Dependency Check job is present but commented out.

## Docker

Three-stage Dockerfile: (1) Maven 3.9.6 / Temurin 21 builds the fat JAR; (2) `eclipse-temurin:21-jre-alpine` extracts Spring Boot layers via `jarmode=tools`; (3) minimal `eclipse-temurin:21-jre-alpine` runtime assembles the layers for faster rebuilds. Port 8080. `RUN mkdir -p /dockerContainerApp/SoundAudio` ensures the audio directory exists in the image.

`compose.yaml` runs the app + `postgres:16` with two persistent volumes: `soundboard-db-data` (Postgres data) and `soundboard-audio-data` (audio files at `/dockerContainerApp/SoundAudio`). Audio storage path is set via `APP_AUDIO_STORAGE_BASE_PATH=/dockerContainerApp/SoundAudio`. Postgres healthcheck gates app startup. Use `./deploy.sh` for a one-command stop / build / up cycle — **must rebuild the image** (`docker compose restart` alone will not pick up code or config changes).
