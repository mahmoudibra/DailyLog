# DailyTracker

A productivity suite for tracking daily work entries, objectives, time, habits, and reviews. The project is a monorepo with two independent sub-projects:

| Project | Description | Tech |
|---------|-------------|------|
| [**desktop/**](desktop/) | Compose Desktop app (macOS) with Material 3, SQLite, and full offline support | Kotlin, Jetpack Compose Desktop, SQLDelight, kotlin-inject |
| [**server/**](server/) | Ktor backend providing JWT authentication and PostgreSQL storage | Kotlin, Ktor, Exposed ORM, PostgreSQL |

Each project has its own Gradle build and can be developed independently.

## Quick Start

### Desktop App

```bash
cd desktop
./gradlew run
```

**Prerequisites:** JDK 17+, macOS

### Server

```bash
cd server
cp .env.example .env
./start.sh
```

**Prerequisites:** JDK 21+, Docker & Docker Compose

See each project's README for full details.
