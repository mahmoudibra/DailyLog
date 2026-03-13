# DailyWorkTracker

A Kotlin/JVM desktop application built with Jetpack Compose Desktop for tracking daily work entries, objectives, and reminders.

## Tech Stack

- **Language**: Kotlin 2.1.0
- **UI**: Jetpack Compose Desktop 1.10.2 (Material 3)
- **Database**: SQLDelight 2.0.2 (SQLite, type-safe queries) — file: `daily_work_tracker.db`
- **DI**: kotlin-inject 0.7.2 (compile-time DI via KSP 2.1.0-1.0.29)
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
- `di/` - kotlin-inject component (in DI-enabled modules)

## Build & Run

```bash
./gradlew run                    # Run the app
./gradlew packageDmg             # Build macOS .dmg
```

## Key Conventions

- Package: `com.booking.worktracker`
- Main class: `com.booking.worktracker.MainKt`
- **Dependency injection (kotlin-inject)**: Uses compile-time DI via kotlin-inject + KSP. Annotate classes with `@Inject`. Use `@Singleton` (from `com.booking.worktracker.di`) on DataSources and Repositories to ensure single instances. UseCases and ViewModels are not singleton — they get fresh instances. Constructor parameters have no default values; kotlin-inject wires everything.
- **AppComponent (core)**: `AppComponent` in `core/.../di/AppComponent.kt` is the root `@Component @Singleton` that provides app-wide dependencies (e.g. `DailyWorkTrackerDatabase`). Accessed via `AppComponent.instance`.
- **DI Component per feature module**: Each DI-enabled module has a `@Component` class in its `di/` package (e.g. `DailyLogComponent`) that takes `AppComponent` as a parent via `@Component val parent: AppComponent`. Feature components must **not** use `@Singleton` (the parent already owns that scope). The component only exposes ViewModels — never expose DataSources, Repositories, or UseCases. Access the component via `XxxComponent.instance` (lazy singleton using `XxxComponent::class.create(AppComponent.instance)`).
- **UI screens get ViewModels from component**: Screens use `viewModel { XxxComponent.instance.xxxViewModel }`. Never access repositories or data sources directly from screens — all data logic belongs in ViewModels.
- **Cross-module consumers not yet on DI**: Modules not yet migrated to kotlin-inject wire dependencies manually, e.g. `LogRepository(LogLocalDataSource(DatabaseProvider.getDatabase()))`. Migrate them to DI over time.
- **Always use AndroidX Lifecycle ViewModel**: All ViewModels must extend `androidx.lifecycle.ViewModel` (from `org.jetbrains.androidx.lifecycle:lifecycle-viewmodel-compose`). Never create custom ViewModel base classes — use the official AndroidX ViewModel which provides built-in `viewModelScope` and proper lifecycle management.
- Database schema defined in `.sq` files at `core/src/main/sqldelight/com/booking/worktracker/data/`
- SQLDelight generates `DailyWorkTrackerDatabase` with typed `*Queries` classes
- `DatabaseProvider` class in `core/.../data/DatabaseProvider.kt` manages driver and database lifecycle. Has `@Inject @Singleton` for DI, plus a `companion object` with static methods for backward compatibility.
- Design system tokens in `core/.../ui/designsystem/tokens/` (colors, spacing, typography)
- Material 3 ExperimentalApi opt-in enabled globally via compiler args
