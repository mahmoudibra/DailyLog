# DailyTracker Server

Ktor backend for the DailyTracker app. Provides JWT-based authentication with PostgreSQL storage.

## Tech Stack

- Kotlin 2.1.0 / Ktor 3.1.1 (Netty)
- Exposed 0.58.0 (SQL ORM) + PostgreSQL 16
- HikariCP connection pooling
- JWT authentication + BCrypt password hashing
- kotlinx-serialization

## Getting Started

### Prerequisites

- JDK 21+
- Docker & Docker Compose (for database)

### Quick Start (Docker)

1. Copy the example environment file and adjust values:
   ```bash
   cp .env.example .env
   ```

2. Start PostgreSQL and the server:
   ```bash
   ./start.sh
   ```

3. Server runs at `http://localhost:8080`, PostgreSQL at `localhost:5432`.

4. Stop everything:
   ```bash
   ./stop.sh
   ```

### Local Development (without Docker)

1. Start PostgreSQL (via Docker or locally):
   ```bash
   docker-compose up -d postgres
   ```

2. Run the server with Gradle:
   ```bash
   ./gradlew run
   ```

### Build

```bash
./gradlew buildFatJar          # Fat JAR at build/libs/*-all.jar
./gradlew test                 # Run tests
```

## Configuration

All configuration lives in `src/main/resources/application.conf` with environment variable overrides.

Create a `.env` file (gitignored) for local overrides — Docker Compose reads it automatically.

| Variable | Description | Default |
|----------|-------------|---------|
| `PORT` | Server port | `8080` |
| `DATABASE_URL` | JDBC PostgreSQL URL | `jdbc:postgresql://localhost:5432/dailytracker` |
| `DATABASE_USER` | Database username | `dailytracker` |
| `DATABASE_PASSWORD` | Database password | `dailytracker` |
| `JWT_SECRET` | Secret for signing JWT tokens | (dev default, **must change in production**) |
| `UPLOAD_DIR` | Directory for file uploads | `/app/uploads` |

## API Endpoints

### Authentication

| Method | Path | Body | Response |
|--------|------|------|----------|
| POST | `/auth/register` | `{ "email": "...", "password": "..." }` | `{ "token": "...", "email": "..." }` |
| POST | `/auth/login` | `{ "email": "...", "password": "..." }` | `{ "token": "...", "email": "..." }` |

JWT tokens expire after 7 days. Use the token in the `Authorization: Bearer <token>` header for protected routes.

## Production Deployment

**Do not use the default secrets in production.** Set strong values for `DATABASE_PASSWORD` and `JWT_SECRET` via one of:

- **GitHub Secrets** — inject via CI/CD workflow (`${{ secrets.JWT_SECRET }}`)
- **Cloud Secret Managers** — AWS Secrets Manager, GCP Secret Manager, Azure Key Vault
- **Docker Swarm / Kubernetes Secrets** — native secret management for container orchestration

## Project Structure

See [ARCHITECTURE.md](ARCHITECTURE.md) for a detailed breakdown of each layer.
