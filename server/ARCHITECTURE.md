# Server Architecture

## Request Flow

```
HTTP Request → Ktor (Serialization + StatusPages) → Route → Repository → Exposed → PostgreSQL
```

The response flows back out the same path.

## Layers

### Entry Point — `Application.kt`

Bootstrap layer. Starts the Ktor Netty engine and calls `Application.module()`, which installs plugins in order. The order matters — Database must be configured before Authentication, since auth may need DB access.

### Plugins — `plugins/`

Each file is a Ktor plugin installer — an `Application` extension function that configures one concern:

| Plugin | File | Responsibility |
|--------|------|----------------|
| Serialization | `Serialization.kt` | Installs `ContentNegotiation` with kotlinx JSON for automatic request/response body serialization |
| StatusPages | `StatusPages.kt` | Global error handler. Catches exceptions and converts them to structured `ErrorResponse` JSON — prevents raw stack traces from leaking |
| Database | `Database.kt` | Sets up HikariCP connection pool, connects Exposed to PostgreSQL, and auto-creates tables via `SchemaUtils.create()` |
| Authentication | `Authentication.kt` | Configures JWT verification for protected routes (scheme `"auth-jwt"`) and provides `generateToken()` for issuing new tokens (7-day expiry) |
| Routing | `Routing.kt` | Composition root — creates repository instances, passes them into route functions |

**Install order** (defined in `Application.module()`): Serialization → StatusPages → Database → Authentication → Routing.

### Models — `models/`

| File | Responsibility |
|------|----------------|
| `Tables.kt` | Exposed table definitions (Kotlin objects). Map 1:1 to PostgreSQL tables. Used for query building and schema creation |
| `ApiModels.kt` | `@Serializable` data classes for HTTP request/response bodies (`RegisterRequest`, `LoginRequest`, `AuthResponse`, `ErrorResponse`). These are the API contract |

### Repository — `repository/`

Data access layer. Encapsulates all database operations inside Exposed `transaction {}` blocks.

Responsibilities:
- Translates between Exposed `ResultRow`s and domain records (`UserRecord`)
- Owns password hashing/verification (BCrypt)
- Hides database implementation from routes — routes never touch Exposed directly

### Routes — `routes/`

HTTP handler layer. Extension functions on `Route` that map endpoints to business logic.

Each route handler:
1. Deserializes the request body (via content negotiation)
2. Validates input
3. Calls the repository for data operations
4. Returns the appropriate HTTP response with status code

Routes receive dependencies (repositories) as function parameters, injected by `Routing.kt`.

## Architecture Decisions

### Database: Exposed (DSL API)

[Exposed](https://github.com/JetBrains/Exposed) is JetBrains' official Kotlin SQL library. It provides two APIs:

**DSL API (what this project uses)** — Type-safe SQL-like syntax using Kotlin objects as table definitions:

```kotlin
// Define table
object Users : Table("users") {
    val id = uuid("id")
    val email = text("email").uniqueIndex()
}

// Query — compile-time checked
Users.selectAll().where { Users.email eq "test@test.com" }

// Insert
Users.insert {
    it[id] = UUID.randomUUID()
    it[email] = "test@test.com"
}
```

Everything runs inside `transaction {}` blocks — Exposed manages connections and commits/rollbacks automatically.

**DAO API (not used)** — ORM layer on top of the DSL with entity classes. More convenient but less control over generated SQL.

**Why Exposed?** Lightweight, Kotlin-idiomatic, backed by JetBrains, no code generation step required. A good fit for a simple Ktor server.

#### Alternatives Considered

| Library | Style | Trade-offs |
|---------|-------|------------|
| **Ktorm** | DSL (similar to Exposed) | Lighter weight, pure DSL. Less community adoption |
| **jOOQ** | Code-generated type-safe SQL | Most powerful SQL DSL, but requires code generation step and commercial license for non-open-source databases |
| **Hibernate / JPA** | Full ORM | Java standard, huge ecosystem. Heavy, annotation-driven, not designed for Kotlin |
| **Spring Data JPA** | Repository abstraction over Hibernate | Auto-generates queries from method names. Requires Spring framework |
| **SQLDelight** | SQL-first, generates Kotlin from `.sq` files | Used in the desktop app. Great for multiplatform, less common server-side |
| **Komapper** | Kotlin-first ORM with KSP code generation | Type-safe without reflection, Kotlin-native. Smaller ecosystem |
| **JDBI** | Lightweight SQL mapper | Thin layer over JDBC, raw SQL, no type safety on queries |

### Authentication: JWT

Stateless token-based auth using `ktor-server-auth-jwt`. Tokens contain `userId` and `email` claims with 7-day expiry. Chosen over session-based auth for simplicity and because the desktop client doesn't need cookie management.

### Password Hashing: BCrypt

Industry-standard adaptive hashing via `at.favre.lib:bcrypt`. Cost factor of 12 (default). Chosen for its resistance to brute-force attacks and wide adoption.

### Connection Pooling: HikariCP

High-performance JDBC connection pool. Configured with max 10 connections and `TRANSACTION_REPEATABLE_READ` isolation. The standard choice for JVM database applications.

### Serialization: kotlinx-serialization

Kotlin-native, compile-time serialization via `@Serializable` annotations. Chosen over Jackson/Gson for Kotlin-first design, multiplatform compatibility, and no reflection overhead.
