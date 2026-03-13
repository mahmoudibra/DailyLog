# DailyWorkTracker

A Kotlin/JVM desktop application built with Jetpack Compose Desktop for tracking daily work entries, objectives, time, and reminders. Features a Material 3 design system with English and Arabic (RTL) localization support.

## Features

### Daily Log
- Record daily work entries with a rich text input
- Interactive monthly calendar picker with entry count indicators per day
- Tag system with custom colors for categorizing logs
- Streak tracking to encourage consistent logging
- Quick action cards for fast navigation

### Log History
- Browse past daily logs in a card-based list (last 50 entries)
- Expandable detail view showing all entries and tags for a given day
- Entry count summaries per log

### Objectives
- Set **yearly** and **quarterly** goals
- Checklist sub-items for each objective with progress tracking
- Status management: In Progress, Completed, Cancelled
- Visual progress indicators (completion percentage)
- Navigate across years and quarters

### Time Tracking
- Start/stop timer for active work sessions
- Add manual time entries with start and end times
- Categorize entries (General, Meeting, Coding, Review, Planning, or custom)
- Daily summary with total time and per-category breakdown

### Analytics
- Current streak, best streak, and total days logged
- Entry overview: total entries, average per day, most active day
- Objective completion statistics with progress bars
- Tag usage frequency with relative bar charts
- Weekly and daily activity breakdowns (last 14 days)

### Export
- Export data in **Plain Text**, **CSV**, or **Markdown** format
- Configurable date range
- Choose what to include: work entries, tags, objectives
- Live preview before exporting
- Native file save dialog

### Settings
- Language selection: English / Arabic (RTL)
- Configurable morning and afternoon reminder times
- Delete all data (with confirmation)

### Notifications
- Scheduled morning reminder to start logging work
- Afternoon reminder if no entries have been logged that day
- Native macOS notifications via AppleScript

## Tech Stack

| Component       | Technology                                  |
|-----------------|---------------------------------------------|
| Language        | Kotlin 2.1.0                                |
| UI Framework    | Jetpack Compose Desktop 1.10.2 (Material 3) |
| Database        | SQLDelight 2.0.2 (SQLite, type-safe queries)  |
| Async           | kotlinx-coroutines 1.9.0                    |
| Date/Time       | kotlinx-datetime 0.6.1                      |
| Localization    | Compose Multiplatform Resources              |
| Build System    | Gradle (Kotlin DSL)                         |

## Project Structure

Multi-module Gradle project organized by feature:

```
DailyWorkTracker/
├── core/                        # Shared module
│   ├── data/
│   │   └── DatabaseProvider.kt  # SQLDelight database lifecycle (daily_work_tracker.db)
│   ├── sqldelight/              # .sq files defining schema + queries (SQLDelight codegen)
│   ├── ui/
│   │   ├── designsystem/
│   │   │   ├── tokens/          # ColorTokens, TypographyTokens, SpacingTokens
│   │   │   ├── components/      # DSButton, DSCard, DSTagChip, DSTextField, etc.
│   │   │   └── WorkTrackerTheme.kt
│   │   └── localization/        # AppLocale (EN/AR), LocalizationProvider
│   └── presentation/
│       └── viewmodels/ViewModel.kt  # Base ViewModel with coroutine scope
│
├── features/
│   ├── dailylog/                # Daily log & log history
│   │   ├── data/models/         # DailyLog, WorkEntry, Tag
│   │   ├── data/datasource/     # LogLocalDataSource, TagLocalDataSource
│   │   ├── data/repository/     # LogRepository, TagRepository
│   │   └── ui/screens/          # DailyLogScreen, LogListScreen
│   │
│   ├── objectives/              # Yearly & quarterly goals
│   │   ├── data/models/         # Objective, ChecklistItem, ObjectiveType/Status
│   │   ├── data/repository/     # ObjectiveRepository
│   │   └── ui/screens/          # ObjectivesScreen
│   │
│   ├── timetracking/            # Timer & manual time entries
│   │   ├── data/models/         # TimeEntry
│   │   ├── data/repository/     # TimeEntryRepository
│   │   ├── presentation/        # TimeTrackingViewModel
│   │   └── ui/screens/          # TimeTrackingScreen
│   │
│   ├── analytics/               # Dashboard & statistics
│   │   ├── data/models/         # DailyStats, WeeklyStats, TagStats, StreakInfo, etc.
│   │   ├── data/repository/     # AnalyticsRepository
│   │   ├── presentation/        # AnalyticsViewModel
│   │   └── ui/screens/          # AnalyticsScreen
│   │
│   ├── export/                  # Data export (TXT, CSV, MD)
│   │   ├── data/models/         # ExportFormat, ExportOptions, ExportResult
│   │   ├── data/repository/     # ExportRepository
│   │   ├── presentation/        # ExportViewModel
│   │   └── ui/screens/          # ExportScreen
│   │
│   └── settings/                # App configuration
│       ├── data/models/         # Setting (key-value)
│       ├── data/repository/     # SettingsRepository
│       └── ui/screens/          # SettingsScreen
│
├── features/ (also includes)
│   ├── reviews/                 # Daily reviews & weekly summaries
│   ├── focuszones/              # Focus zone analysis by time/category
│   └── timebudgets/             # Time budget allocation goals
│
└── src/                         # App entry point
    └── main/kotlin/.../
        ├── Main.kt              # Bootstrap: DB init, dependency wiring, window setup
        ├── ui/App.kt            # Root layout with side rail navigation
        └── notifications/
            ├── ReminderScheduler.kt   # Coroutine-based scheduled reminders
            └── MacOSNotification.kt   # Native macOS notifications via osascript
```

### Architecture

Each feature module follows a layered architecture:

```
data/models/         →  Data classes & enums
data/datasource/     →  SQLDelight-backed data sources (using generated *Queries classes)
data/repository/     →  Repository abstraction over data sources
domain/usecases/     →  Business logic (where applicable)
presentation/        →  ViewModels with StateFlow/mutableState
ui/screens/          →  Compose UI screens
```

- **No DI framework** — dependencies are manually wired in `Main.kt`
- **State management** — Compose `mutableStateOf` + Kotlin `StateFlow` in ViewModels
- **Coroutines** — async operations scoped to ViewModel lifecycle via `viewModelScope`

## Database

SQLite file-based database (`daily_work_tracker.db`) with the following schema:

| Table            | Description                                      |
|------------------|--------------------------------------------------|
| `daily_logs`     | One row per date, tracks created/updated times   |
| `work_entries`   | Individual work items linked to a daily log      |
| `tags`           | Reusable tags with custom colors                 |
| `log_tags`       | Many-to-many junction between logs and tags      |
| `objectives`     | Yearly/quarterly goals with type and status      |
| `checklist_items`| Sub-tasks within objectives, ordered by position |
| `time_entries`   | Timer entries with category, start/end times     |
| `settings`       | Key-value store for app configuration            |

Schema is defined in `.sq` files at `core/src/main/sqldelight/com/booking/worktracker/data/`. SQLDelight generates type-safe Kotlin query classes at compile time.

## Download & Install

Pre-built macOS installers are published to **GitHub Releases** on every push to `main`.

### From GitHub Releases

1. Go to the [Releases page](../../releases) of this repository.
2. Find the latest release and download the installer:
   - **`DailyWorkTracker-*.dmg`** — Drag-and-drop disk image
   - **`DailyWorkTracker-*.pkg`** — Standard macOS package installer
3. Install:
   - **DMG**: Open the `.dmg`, drag **DailyWorkTracker** into your Applications folder.
   - **PKG**: Double-click the `.pkg` and follow the installer prompts.

### Using GitHub CLI

```bash
# Download the latest release assets
gh release download --repo <OWNER>/DailyReminder --pattern "*.dmg"
gh release download --repo <OWNER>/DailyReminder --pattern "*.pkg"
```

> **Note:** Replace `<OWNER>` with the GitHub username or organization.

## Build & Run (from source)

### Prerequisites

- JDK 17+
- macOS (for native distribution and notifications)

### Commands

```bash
# Run the application
./gradlew run

# Build macOS .dmg installer
./gradlew packageDmg

# Build macOS .pkg installer
./gradlew packagePkg
```

### Application Window

- Default size: 900 x 700 dp
- Left side rail navigation (220 dp) with 7 screens
- Main class: `com.booking.worktracker.MainKt`

## Design System

The `core` module provides a shared Material 3 design system:

- **Theme**: `WorkTrackerTheme` — light theme with custom color palette
- **Color tokens**: Primary, secondary, tertiary colors; card-specific colors (blue, green, orange); tag color palette
- **Typography tokens**: Full Material 3 type scale
- **Spacing tokens**: Consistent padding/margin values (`screenPadding`, `sectionSpacing`, `small`, `medium`, `large`)
- **Reusable components**: Buttons, text fields, cards, tag chips, section headers, empty states, loading indicators, color picker

## Localization

- Supports **English** and **Arabic** (with RTL layout)
- Uses Compose Multiplatform Resources (`stringResource()`, `pluralStringResource()`)
- Language preference persisted in settings and applied on startup
- Runtime locale switching updates the entire UI
