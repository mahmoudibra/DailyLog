# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Overview

Ktor backend server for the DailyTracker app. Currently auth-only (register/login) with JWT authentication and PostgreSQL via Exposed ORM. Part of the DailyReminder monorepo but has its own independent Gradle build.

## Build & Run Commands

```bash
# Local development (requires PostgreSQL running)
./gradlew run

# Build fat JAR for deployment
./gradlew buildFatJar

# Run tests
./gradlew test

# Docker (starts PostgreSQL + server)
./start.sh          # docker-compose up -d
./stop.sh           # docker-compose down
```

## Tech Stack

- **Kotlin 2.1.0** / **Ktor 3.1.1** (Netty engine)
- **Exposed 0.58.0** (SQL ORM) with PostgreSQL 16
- **HikariCP** connection pooling
- **JWT** authentication (java-jwt via ktor-server-auth-jwt)
- **BCrypt** for password hashing
- **kotlinx-serialization** for JSON

## Architecture

Single-module Ktor app using the plugin pattern:

```
src/main/kotlin/com/dailytracker/server/
├── Application.kt          # Entry point, installs plugins in order
├── plugins/
│   ├── Serialization.kt    # kotlinx.serialization JSON
│   ├── StatusPages.kt      # Global exception handling
│   ├── Database.kt         # HikariCP + Exposed setup, schema creation
│   ├── Authentication.kt   # JWT config + token generation
│   └── Routing.kt          # Wires repositories → routes
├── models/
│   ├── Tables.kt           # Exposed table definitions (Users)
│   └── ApiModels.kt        # @Serializable request/response DTOs
├── repository/
│   └── UserRepository.kt   # DB operations (Exposed transactions)
└── routes/
    └── AuthRoutes.kt       # POST /auth/register, POST /auth/login
```

**Plugin install order matters** — defined in `Application.module()`: Serialization → StatusPages → Database → Authentication → Routing.

## Key Conventions

- **Package**: `com.dailytracker.server`
- **No DI framework** — repositories are instantiated directly in `configureRouting()` and passed to route functions
- **Exposed tables** are Kotlin objects in `models/Tables.kt`; schema is auto-created via `SchemaUtils.create()` on startup
- **Routes** are extension functions on `Route`, grouped by feature (e.g., `Route.authRoutes()`)
- **Config** is in `application.conf` (HOCON) with env var overrides (`${?ENV_VAR}` pattern)
- **All DB operations** run inside Exposed `transaction {}` blocks in repository methods

## Configuration

Defaults in `src/main/resources/application.conf`, overridable via environment variables:

| Setting | Env Var | Default |
|---------|---------|---------|
| Server port | `PORT` | 8080 |
| Database URL | `DATABASE_URL` | `jdbc:postgresql://localhost:5432/dailytracker` |
| Database user | `DATABASE_USER` | dailytracker |
| Database password | `DATABASE_PASSWORD` | dailytracker |
| JWT secret | `JWT_SECRET` | (hardcoded dev default) |

## API Endpoints

- `POST /auth/register` — body: `{email, password}` → `{token, email}`
- `POST /auth/login` — body: `{email, password}` → `{token, email}`

JWT tokens include `userId` and `email` claims, expire after 7 days. Protected routes use `authenticate("auth-jwt")`.
