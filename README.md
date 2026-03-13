# DailyWorkTracker

A Kotlin/JVM desktop application built with **Jetpack Compose Desktop** for tracking daily work entries, objectives, time, habits, and reviews. Features a Material 3 design system with English and Arabic (RTL) localization.

## Features

| Feature | Description |
|---------|-------------|
| **Daily Log** | Record work entries with calendar picker, color-coded tags, and streak tracking |
| **Log History** | Browse past logs in expandable card-based list (last 50 entries) |
| **Objectives** | Yearly and quarterly goals with checklists and progress tracking |
| **Time Tracking** | Start/stop timer and manual entries with category breakdown |
| **Analytics** | Streaks, entry stats, objective completion, tag usage, weekly activity |
| **Reviews** | Daily reflections (went well / improve / priorities) and weekly summaries |
| **Focus Zones** | Analyze focus ratings by hour, day, and category |
| **Time Budgets** | Set time allocation targets per category linked to objectives |
| **Habits** | Track daily habits with completion history and objective linking |
| **Export** | Export data as Plain Text, CSV, or Markdown with live preview |
| **Settings** | Language (EN/AR), reminder times, data management |
| **Notifications** | Scheduled morning and afternoon reminders via native macOS notifications |

## Tech Stack

| Component | Technology | Version |
|-----------|-----------|---------|
| Language | Kotlin | 2.1.0 |
| UI | Jetpack Compose Desktop (Material 3) | 1.10.2 |
| Database | SQLDelight (SQLite) | 2.0.2 |
| DI | kotlin-inject (compile-time via KSP) | 0.7.2 |
| Async | kotlinx-coroutines | 1.9.0 |
| Date/Time | kotlinx-datetime | 0.6.1 |
| ViewModel | AndroidX Lifecycle ViewModel | 2.8.4 |
| Build | Gradle (Kotlin DSL) | - |

## Module Dependency Diagram

```
                          ┌──────────────────────────┐
                          │        :app (root)        │
                          │    Main.kt / App.kt       │
                          └─────────┬────────────────-┘
                                    │
                  ┌─────────────────┼──────────────────┐
                  │                 │                   │
          ┌───────▼──────┐  ┌──────▼───────┐  ┌───────▼────────┐
          │  :features:  │  │  :features:  │  │  :features:    │
          │  dailylog    │  │  objectives  │  │  timetracking  │
          └───────┬──────┘  └──────┬───────┘  └───────┬────────┘
                  │                │                   │
          ┌───────▼──────┐  ┌─────▼────────┐  ┌──────▼─────────┐
          │  :features:  │  │  :features:  │  │  :features:    │
          │  analytics   │  │  settings    │  │  export        │
          └───────┬──────┘  └─────┬────────┘  └──────┬─────────┘
                  │               │                   │
          ┌───────▼──────┐  ┌────▼─────────┐  ┌─────▼──────────┐
          │  :features:  │  │  :features:  │  │  :features:    │
          │  habits      │  │  focuszones  │  │  timebudgets   │
          └───────┬──────┘  └────┬─────────┘  └─────┬──────────┘
                  │              │                   │
                  │       ┌──────▼──────┐            │
                  │       │  :features: │            │
                  │       │  reviews    │            │
                  │       └──────┬──────┘            │
                  │              │                   │
    ──────────────┴──────────────┴───────────────────┴──────────
     All feature modules depend on ▼
    ──────────────────────────────────────────────────────────-─
                  │                          │
        ┌────────▼─────────┐     ┌──────────▼──────────┐
        │  :core:database  │     │  :core:designsystem  │
        │  SQLDelight,     │     │  Theme, tokens,       │
        │  DatabaseProvider│     │  components, i18n     │
        └────────┬─────────┘     └─────────────────────-┘
                 │
          ┌──────▼──────┐
          │  :core:di   │
          │  kotlin-    │
          │  inject     │
          └─────────────┘
```

### Cross-feature dependencies

Some feature modules also depend on other feature modules:

```
:features:reviews     ──▶ :features:dailylog, :features:timetracking, :features:objectives
:features:focuszones  ──▶ :features:timetracking
:features:timebudgets ──▶ :features:timetracking, :features:objectives
```

## Project Structure

```
DailyWorkTracker/
├── core/
│   ├── di/                  # kotlin-inject runtime, @Singleton annotation, ViewModel
│   ├── database/            # SQLDelight schema (.sq), DatabaseProvider, DatabaseComponent
│   └── designsystem/        # WorkTrackerTheme, tokens, components, localization
│
├── features/
│   ├── dailylog/            # Daily log entries, tags, log history
│   ├── objectives/          # Yearly & quarterly goals with checklists
│   ├── timetracking/        # Timer & manual time entries
│   ├── analytics/           # Dashboard & statistics
│   ├── reviews/             # Daily reviews & weekly summaries
│   ├── focuszones/          # Focus zone analysis by hour/category
│   ├── timebudgets/         # Time budget targets per category
│   ├── habits/              # Daily habit tracking
│   ├── export/              # Data export (TXT, CSV, MD)
│   └── settings/            # App configuration
│
└── src/                     # App entry point
    └── main/kotlin/.../
        ├── Main.kt          # Bootstrap: DB init, window setup, reminders
        ├── ui/App.kt        # Root layout with side rail navigation (11 screens)
        └── notifications/   # ReminderScheduler, MacOSNotification (osascript)
```

### Feature Module Architecture

Each feature follows a layered architecture:

```
di/              → kotlin-inject @Component (exposes only ViewModels)
data/models/     → Data classes & enums
data/datasource/ → SQLDelight-backed data sources
data/repository/ → Repository abstraction
domain/usecases/ → Business logic (where applicable)
presentation/    → ViewModels (AndroidX Lifecycle) with StateFlow
ui/screens/      → Compose UI screens
```

## Database

SQLite file-based database (`daily_work_tracker.db`). Schema defined in `.sq` files at `core/database/src/main/sqldelight/`. SQLDelight generates type-safe Kotlin query classes at compile time.

| Table | Description |
|-------|-------------|
| `daily_logs` | One row per date with timestamps |
| `work_entries` | Work items linked to a daily log |
| `tags` | Reusable tags with custom colors |
| `log_tags` | Many-to-many junction: logs to tags |
| `objectives` | Yearly/quarterly goals with type and status |
| `checklist_items` | Sub-tasks within objectives |
| `time_entries` | Timer entries with category, start/end, focus rating |
| `daily_reviews` | Daily reflections (went well, improve, priorities) |
| `weekly_summaries` | Auto-generated weekly summary data |
| `time_budgets` | Time allocation targets per category/period |
| `habits` | Trackable habits with icon, color, objective link |
| `habit_completions` | Daily habit completion records |
| `settings` | Key-value store for app configuration |

## Download & Install

Pre-built macOS `.dmg` installers are published to **GitHub Releases** on every push to `main`.

1. Go to the [Releases page](../../releases) of this repository.
2. Download the latest **`DailyWorkTracker-*.dmg`**.
3. Open the `.dmg` and drag **DailyWorkTracker** into your Applications folder.

Or via GitHub CLI:

```bash
gh release download --repo mahmoudibra/DailyLog --pattern "*.dmg"
```

## Build & Run (from source)

**Prerequisites:** JDK 17+, macOS (for native distribution and notifications)

```bash
./gradlew run            # Run the application
./gradlew packageDmg     # Build macOS .dmg installer
./gradlew detekt         # Run static analysis
./gradlew test           # Run tests
```

## CI/CD

GitHub Actions pipeline (`.github/workflows/ci.yml`):

| Job | Runner | Trigger |
|-----|--------|---------|
| **Lint** (detekt) | ubuntu-latest | Push + PR |
| **Test** | ubuntu-latest | Push + PR |
| **Build** | ubuntu-latest | Push + PR |
| **Package & Release** | macos-latest | Push to `main` only |
| **Dependency Submission** | ubuntu-latest | Push to `main` only |

The **Package & Release** job builds a `.dmg` and creates a GitHub Release with the installer attached.

## Localization

- **English** and **Arabic** (with full RTL layout support)
- Compose Multiplatform Resources (`stringResource()`, `pluralStringResource()`)
- Runtime locale switching — persisted in settings and applied on startup
