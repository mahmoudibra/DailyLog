# DailyWorkTracker

A Kotlin/JVM desktop application built with Jetpack Compose Desktop for tracking daily work entries, objectives, and reminders.

## Tech Stack

- **Language**: Kotlin 2.1.0
- **UI**: Jetpack Compose Desktop 1.10.2 (Material 3)
- **Database**: SQLDelight 2.0.2 (SQLite, type-safe queries) — file: `daily_work_tracker.db`
- **Async**: kotlinx-coroutines
- **Date/Time**: kotlinx-datetime
- **Build**: Gradle (Kotlin DSL)

## Project Structure

Multi-module Gradle project with feature modules:

```
├── core/          # Shared: DatabaseProvider, SQLDelight .sq files, design system (theme, tokens, components)
├── features/
│   ├── dailylog/      # Daily log feature: entries, tags, log list
│   ├── objectives/    # Objectives feature: yearly/quarterly goals with checklists
│   ├── timetracking/  # Timer & manual time entries
│   ├── analytics/     # Dashboard & statistics
│   ├── export/        # Data export (TXT, CSV, MD)
│   ├── reviews/       # Daily reviews & weekly summaries
│   ├── focuszones/    # Focus zone analysis
│   ├── timebudgets/   # Time budget allocation
│   └── settings/      # App settings
└── src/           # App entry point (Main.kt, App.kt, notifications)
```

Each feature module follows a layered architecture:
- `data/models/` - Data classes
- `data/datasource/` - SQLDelight-backed data sources (using generated *Queries classes)
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
- **Dependency wiring**: No DI framework. Each class declares its dependencies as constructor parameters with default values that self-create, e.g. `class LogRepository(private val localDataSource: LogLocalDataSource = LogLocalDataSource())`. Never pass dependencies from the outside — each class creates its own. Data sources take `db: DailyWorkTrackerDatabase = DatabaseProvider.getDatabase()` as their constructor param.
- **UI screens own their ViewModels**: Each `@Composable` screen creates its own ViewModel via `remember { XxxViewModel() }`. Never pass ViewModels or repositories as parameters to screens — screens are self-contained.
- Database schema defined in `.sq` files at `core/src/main/sqldelight/com/booking/worktracker/data/`
- SQLDelight generates `DailyWorkTrackerDatabase` with typed `*Queries` classes
- `DatabaseProvider` object in `core/.../data/DatabaseProvider.kt` manages driver and database lifecycle
- Design system tokens in `core/.../ui/designsystem/tokens/` (colors, spacing, typography)
- Material 3 ExperimentalApi opt-in enabled globally via compiler args
