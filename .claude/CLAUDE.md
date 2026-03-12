# DailyWorkTracker

A Kotlin/JVM desktop application built with Jetpack Compose Desktop for tracking daily work entries, objectives, and reminders.

## Tech Stack

- **Language**: Kotlin 1.9.22
- **UI**: Jetpack Compose Desktop 1.5.12 (Material 3)
- **Database**: SQLite via sqlite-jdbc 3.44.1.0 (file: `daily_work_tracker.db`)
- **Async**: kotlinx-coroutines
- **Date/Time**: kotlinx-datetime
- **Build**: Gradle (Kotlin DSL)

## Project Structure

Multi-module Gradle project with feature modules:

```
├── core/          # Shared: Database singleton, design system (theme, tokens, components)
├── dailylog/      # Daily log feature: entries, tags, log list
├── objectives/    # Objectives feature: yearly/quarterly goals with checklists
├── settings/      # App settings
└── src/           # App entry point (Main.kt, App.kt, notifications)
```

Each feature module follows a layered architecture:
- `data/models/` - Data classes
- `data/datasource/` - SQLite data sources (raw JDBC)
- `data/repository/` - Repository layer
- `domain/usecases/` - Business logic
- `presentation/viewmodels/` - UI state management
- `ui/screens/` - Compose screens

## Build & Run

```bash
./gradlew run                    # Run the app
./gradlew packageDmg             # Build macOS .dmg
```

## Key Conventions

- Package: `com.booking.worktracker`
- Main class: `com.booking.worktracker.MainKt`
- Manual dependency wiring in `Main.kt` (no DI framework)
- Database schema lives in `core/src/main/resources/database/schema.sql`
- Design system tokens in `core/.../ui/designsystem/tokens/` (colors, spacing, typography)
- SQLite accessed via raw JDBC `Connection` through `Database.getConnection()`
- Material 3 ExperimentalApi opt-in enabled globally via compiler args
