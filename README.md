# DailyTracker

A productivity suite for tracking daily work entries, objectives, time, habits, and reviews. The project is a monorepo with two independent sub-projects:

| Project | Description |
|---------|-------------|
| [**desktop/**](desktop/) | Compose Desktop app (macOS) with Material 3, SQLite, and full offline support |
| [**server/**](server/) | Ktor backend providing JWT authentication and PostgreSQL storage |

Each project has its own Gradle build and can be developed independently.

## Tech Stack

| Layer | Technology | Version |
|-------|-----------|---------|
| **Desktop** | Kotlin, Jetpack Compose Desktop (Material 3) | 2.1.0 / 1.10.2 |
| **Desktop DB** | SQLDelight (SQLite) | 2.0.2 |
| **Desktop DI** | kotlin-inject (compile-time via KSP) | 0.7.2 |
| **Server** | Kotlin, Ktor (Netty) | 2.1.0 / 3.1.1 |
| **Server DB** | Exposed ORM + PostgreSQL 16 | 0.58.0 |
| **Server Auth** | JWT + BCrypt | - |
| **Infrastructure** | Docker & Docker Compose | - |

## Getting Started

### Prerequisites

- **JDK 17+** (desktop), **JDK 21+** (server)
- **Docker & Docker Compose** (for the server's PostgreSQL database)
- **macOS** (for desktop native distribution and notifications)

### Desktop App

```bash
cd desktop
./gradlew run                # Run the app
./gradlew packageDmg         # Build macOS .dmg installer
```

### Server

The server runs alongside a PostgreSQL 16 database managed via Docker Compose.

```bash
cd server

# 1. Configure environment
cp .env.example .env         # Edit .env to set DATABASE_PASSWORD, JWT_SECRET, etc.

# 2. Start PostgreSQL + server (Docker)
./start.sh                   # Runs docker-compose up -d

# 3. Server is available at http://localhost:8080

# 4. Stop everything
./stop.sh                    # Runs docker-compose down
```

#### Running the server locally (without Docker for the app)

If you prefer running just the server with Gradle while using Docker only for the database:

```bash
cd server
docker-compose up -d postgres   # Start only PostgreSQL
./gradlew run                   # Run the Ktor server locally
```

#### Building a fat JAR

```bash
cd server
./gradlew buildFatJar           # Output: build/libs/*-all.jar
```

## Running Tests

```bash
# Desktop tests
cd desktop
./gradlew test

# Desktop static analysis (detekt)
./gradlew detekt

# Server tests
cd server
./gradlew test
```

## CI/CD

GitHub Actions pipeline (`.github/workflows/ci.yml`) runs on every push and PR:

| Job | Description |
|-----|-------------|
| **Lint** | detekt static analysis (desktop) |
| **Test** | Unit and integration tests |
| **Build** | Compile and verify both projects |
| **Package & Release** | Build macOS `.dmg` and publish to GitHub Releases (push to `main` only) |

## Configuration

### Server Environment Variables

Create a `.env` file in `server/` (gitignored). Docker Compose reads it automatically.

| Variable | Description | Default |
|----------|-------------|---------|
| `PORT` | Server port | `8080` |
| `DATABASE_URL` | JDBC PostgreSQL URL | `jdbc:postgresql://localhost:5432/dailytracker` |
| `DATABASE_USER` | Database username | `dailytracker` |
| `DATABASE_PASSWORD` | Database password | `dailytracker` |
| `JWT_SECRET` | Secret for signing JWT tokens | dev default (**change in production**) |

See each project's README for more details:
- [Desktop README](desktop/README.md) — features, module architecture, database schema, localization
- [Server README](server/README.md) — API endpoints, production deployment, architecture
