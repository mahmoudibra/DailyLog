# DailyWorkTracker

A Kotlin/JVM desktop application built with Jetpack Compose Desktop for tracking daily work entries, objectives, and reminders.

## Tech Stack

- **Language**: Kotlin 2.1.0
- **UI**: Jetpack Compose Desktop 1.10.2 (Material 3)
- **Database**: SQLite via sqlite-jdbc 3.44.1.0
- **Async**: kotlinx-coroutines 1.9.0
- **Date/Time**: kotlinx-datetime 0.6.1
- **Build**: Gradle (Kotlin DSL)

## Project Structure

Multi-module Gradle project with feature modules:

```
├── core/           # Shared: Database singleton, design system (theme, tokens, components)
├── dailylog/       # Daily log feature: entries, tags, log list
├── objectives/     # Objectives feature: yearly/quarterly goals with checklists
├── settings/       # App settings
├── timetracking/   # Time tracking feature
├── analytics/      # Analytics and reporting
├── export/         # Data export functionality
└── src/            # App entry point (Main.kt, App.kt, notifications)
```

Each feature module follows a layered architecture:

- `data/models/` — Data classes
- `data/datasource/` — SQLite data sources (raw JDBC)
- `data/repository/` — Repository layer
- `domain/usecases/` — Business logic
- `presentation/viewmodels/` — UI state management
- `ui/screens/` — Compose screens

## Build & Run

```bash
./gradlew run                    # Run the app
./gradlew packageDmg             # Build macOS .dmg
```

## Requirements

- JDK 17+
- macOS (for native distribution packaging)
