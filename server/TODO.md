# Backend Completion Plan

## Phase 1: Input Validation Enhancement
**Modify:** `src/main/kotlin/.../routes/AuthRoutes.kt`

- Trim email (not password) before validation
- Add email regex: `^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\.[A-Za-z]{2,}$`
- Email max length: 254 (RFC 5321)
- Password max length: 72 (BCrypt truncation limit)
- Keep existing blank + 6-char minimum checks
- Validation stays inline in route handlers (no separate layer)

---

## Phase 2: CallLogging Plugin
**Modify:** `Application.kt`

- Install `CallLogging` plugin (already in ktor-server-core, no new dep)
- Level: INFO, default format (method, path, status, duration)
- Install first in the plugin chain so it wraps everything

---

## Phase 3: CORS Configuration
**Create:** `plugins/Cors.kt`
**Modify:** `Application.kt`

- CORS dependency already in build.gradle.kts
- Allow POST/GET/PUT/DELETE/OPTIONS, Authorization + ContentType headers
- `anyHost()` for dev (tighten for production)
- Install after CallLogging, before Serialization

---

## Phase 4: Database Migrations (Flyway)
**Modify:** `build.gradle.kts`, `plugins/Database.kt`
**Create:** `src/main/resources/db/migration/V1__Create_users_table.sql`

- Add `flyway-core` + `flyway-database-postgresql` deps
- Migration SQL matches current Users table schema exactly
- Run Flyway before Exposed connects; remove `SchemaUtils.create(Users)`
- `baselineOnMigrate(true)` for existing databases
- Remove hardcoded `driverClassName` — let HikariCP auto-detect (enables H2 in tests)
- For H2 (tests): detect URL prefix and fall back to `SchemaUtils.create()`

---

## Phase 5: Tests
**Create:** `src/test/kotlin/.../AuthRoutesTest.kt`, `HealthRoutesTest.kt`, `src/test/resources/application-test.conf`
**Modify:** `build.gradle.kts`

- Add test deps: `h2:2.2.224`, `ktor-client-content-negotiation`
- H2 in PostgreSQL compatibility mode (fast, no Docker needed)
- Test config with H2 URL, test JWT secret

**Test cases:**
- Health: GET /health returns 200 + "ok"
- Register: happy path (201 + token), duplicate email (409), blank email (400), blank password (400), short password (400), invalid email format (400), password too long (400)
- Login: happy path (200 + token), wrong password (401), nonexistent user (401)

---

## Phase 6: Rate Limiting
**Create:** `plugins/RateLimit.kt`
**Modify:** `build.gradle.kts`, `Application.kt`, `routes/AuthRoutes.kt`

- Add `ktor-server-rate-limit` dep
- Named rate limit "auth": 10 requests / 60 seconds per IP
- Apply only to `/auth` routes
- Install after Authentication, before Routing

---

## Phase 7: Server CI/CD
**Modify:** `.github/workflows/ci.yml`

- Add `server-test` job (parallel with desktop jobs):
  - ubuntu-latest, Java 21, `working-directory: server`
  - `./gradlew test` then `./gradlew buildFatJar`
  - Include PostgreSQL service for future integration tests

---

## Phase 8: API Documentation (OpenAPI/Swagger)
**Create:** `src/main/resources/openapi/documentation.yaml`, `plugins/Swagger.kt`
**Modify:** `build.gradle.kts`, `Application.kt`

- Add `ktor-server-openapi` + `ktor-server-swagger` deps
- Hand-written OpenAPI YAML (simpler than code-gen for 3 endpoints)
- Swagger UI served at `/docs`

---

## Final Plugin Order
```
CallLogging → CORS → Serialization → StatusPages → Database → Authentication → RateLimit → Routing → Swagger
```

## New Dependencies
```kotlin
// Production
implementation("org.flywaydb:flyway-core:10.20.1")
implementation("org.flywaydb:flyway-database-postgresql:10.20.1")
implementation("io.ktor:ktor-server-rate-limit:$ktorVersion")
implementation("io.ktor:ktor-server-openapi:$ktorVersion")
implementation("io.ktor:ktor-server-swagger:$ktorVersion")

// Test
testImplementation("com.h2database:h2:2.2.224")
testImplementation("io.ktor:ktor-client-content-negotiation:$ktorVersion")
```

## Verification
1. `./gradlew test` — all tests pass
2. `./start.sh` — Docker starts, Flyway runs migration, server boots
3. Test endpoints manually: register, login, health
4. Visit `/docs` for Swagger UI
5. Rapid-fire 11+ requests to `/auth/login` → 429 (rate limited)
6. Logs show request method, path, status, duration
