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

Spring Boot 4 / Java 21 REST API for audio file management with JWT authentication.

**Main package**: `com.soundboard.soundboard`

**Layers**:
- `web/` — REST controllers (`SoundController`, `UserController`) and `GlobalExceptionHandler`
- `service/` — Business logic (`SoundService`, `UserService`, `LocalAudioStorageService`, `MyUserDetailsService`, `IService`)
- `repository/` — Spring Data JPA repos (`SoundRepository`, `MyUserRepo`)
- `models/` — JPA entities, DTOs, `requestModels/`, `responseModels/sound/`, `responseModels/user/`
- `security/` — `SecurityConfig`, `JWTService`, `MyUserPrincipal`, and `security/filter/JwtFilter`
- `exceptions/` — `SoundNotFoundException`, `ValidationExceptionHandler`
- `util/`, `audio/`, `mapper/`, `annotation/` — Constants, enums, audio storage config (`AudioStorageProperties`), MapStruct mappers, custom annotations (`@CurrentUser`)

**Public endpoints**: `POST /register`, `POST /login`
**Protected endpoints** (require `Authorization: Bearer <token>`):
- `POST /api/soundboard/sounds` — multipart upload (`soundRequest` JSON part + `file` part)
- `GET /api/soundboard/sounds` — paginated list (`page`, `size`, `sortBy`, `ascending` query params)
- `GET /api/soundboard/sounds/{id}`
- `GET /api/soundboard/sounds/search?keyword=...`
- `GET /api/soundboard/sounds/{id}/download` — streams the audio file
- `DELETE /api/soundboard/sounds/{id}`

## Security

- Stateless JWT auth (JJWT 0.12.6); custom `JwtFilter` (in `security/filter/`) runs before `UsernamePasswordAuthenticationFilter`
- BCrypt strength 10 (`Constants.BCRYPT_STRENGTH`); CSRF disabled
- Tokens issued on `POST /login`, expire after 2 hours, signed with HMAC-SHA via `app.jwt.secret` (Base64); validated per-request via filter
- `JWTService` rejects startup under the `prod` profile if `app.jwt.secret` is still the dev default
- Malformed/tampered JWTs caught in `JwtFilter` and result in 401, not 500
- Password strength enforced at registration: minimum 12 characters, at least one special character (`RegisterRequest` constraints)
- `POST /register` and `POST /login` use dedicated request DTOs (`RegisterRequest`, `LoginRequest`) — JPA entity is never bound directly from HTTP request body
- CORS allow-list driven by `app.cors.allowed-origins` (defaults to `http://localhost:3000`); applied to `/api/**` only, methods `GET/POST/DELETE/OPTIONS`, credentials disabled
- Security headers: `X-Content-Type-Options`, `X-Frame-Options: SAMEORIGIN`, HSTS (1y, includeSubDomains), `Referrer-Policy: no-referrer`, cache-control
- HTTPS redirect controlled by `security.require-https` (default `false`; set `true` in prod via `application-prod.properties`)

## Database & Profiles

| Profile | Database | DDL |
|---------|----------|-----|
| default | H2 in-memory (console at `/h2-console`) | `update` |
| `dev` | PostgreSQL localhost (`application-dev.properties`) | `update` |
| `prod` | PostgreSQL via env vars (`SPRING_DATASOURCE_URL/USERNAME/PASSWORD`, `JWT_SECRET`) | `validate` |
| `test` | TestContainers PostgreSQL | `create-drop` |

Activate with `-Dspring.profiles.active=dev` or `SPRING_PROFILES_ACTIVE=prod`.

**Entities**: `Users` (id, username, password, displayName, createdAt, active) and `SoundEntity` (id, name, description, contentType, audioFile LOB, createdAt, storedName, size, ownedBy, active, category, tags via `@ElementCollection`, recentUpdate).

**DTOs**: `SoundDTO` (id, name, description, ownedBy, category, tags, createdAt, recentUpdate), `GetSoundResponse` (same shape, implements `ResponseBodyModel`), `UserDTO` (id, username, displayName, createdAt).

**Request models** (`requestModels/`): `RegisterRequest` (username, password — with `@Size`, `@Pattern` password strength constraints), `LoginRequest` (username, password), `SoundRequestModel` (name, description).

**Response models** (`responseModels/`): `sound/` (`CreateSoundResponse`, `GetSoundResponse`, `ResponseBodyModel`), `user/` (`RegisterResponse`, `LoginResponse`).

## Audio Storage

Files saved to local filesystem via `LocalAudioStorageService`. Two related properties:
- `app.sounds.directory` (default `./sounds`) — used by `application.properties`
- `app.audio-storage.base-path` (default `./SoundAudio` from `AudioStorageProperties` record; tests override to `./test-audio-storage`) — also defines `allowedMimeTypes` (`audio/mp3`, `audio/wav`, `audio/wave`)

Max upload size: 10 MB (`spring.servlet.multipart.max-file-size`).

## Testing

- `src/test/.../unit/service/` — Mockito unit tests for service layer (`TestUserService`, `TestSoundService`)
- `src/test/.../unit/mapper/` — Lightweight Spring context mapper tests (`TestMapper`)
- `src/test/.../integration/` — TestContainers integration tests with `BaseIntegrationTest`, organised under `controller/sound/` (`CreateTests`, `GetTests`, `PatchTests`, `DeleteTests`) and `controller/user/` (`RegisterTests`, `LoginTests`); shared seed data in `fixtures/SoundSeeder`
- `TestJwtHelper.java` — shared JWT utility for integration tests
- `src/test/resources/application-test.properties` — test profile config (TestContainers overrides datasource via `@DynamicPropertySource`)
- `src/test/.../docs/` — internal notes on test design and security fixes
- Target: 85% minimum line coverage across all layers

## CI (GitHub Actions)

- **build-project.yml** — triggered on push/PR to `master`; runs `mvn -B package` with JDK 21 (Temurin)
- **code-quality.yml** — SpotBugs on push/PR to `master`; parses `target/spotbugsXml.xml`, posts a summary comment on PRs, fails the job on any High-severity bug. OWASP Dependency Check job is present but commented out.

## Docker

Multi-stage Dockerfile (Maven 3.9.6 / Temurin 21 build → `eclipse-temurin:21-jre-jammy` runtime, port 8080). `compose.yaml` runs the app + `postgres:16` with a persistent `soundboard-db-data` volume and a Postgres healthcheck gating app startup. Use `./deploy.sh` for a one-command stop / build / up cycle.
