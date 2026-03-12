# Daily Work Tracker for Booking.com - Implementation Plan

## Context

The user needs a **simple desktop application** to track daily work, progress toward objectives, and retrospective notes at Booking.com. The goal is to build consistent work logging habits through:

1. **Daily reminders** (10:30 AM and 4:30 PM) to prompt logging
2. **Quick logging interface** to capture what was accomplished each day
3. **Retrospective notes** for capturing learnings and improvement points
4. **Local SQLite database** for data persistence
5. **macOS-native experience** with minimal overhead

### Why This Matters
- Helps track progress toward quarterly objectives
- Provides material for 1-on-1s and performance reviews
- Captures retrospective insights in real-time (not weeks later)
- Builds accountability through daily logging habit

### User Preferences
- **Technology Stack:** Kotlin + Compose Desktop (modern, declarative UI similar to SwiftUI)
- **IDE:** Android Studio or IntelliJ IDEA (both already installed)
- **Scope:** Quick MVP (2-3 days) - get core functionality working, iterate later
- **Platform:** macOS native
- **Jira Integration:** Nice-to-have for later (not in MVP)

## Technology Stack

### Core Technologies
- **Kotlin** - Primary language
- **Compose Desktop** - Declarative UI framework (similar to SwiftUI/Jetpack Compose)
- **Gradle** - Build system
- **SQLite** - Local database via Exposed or JDBC
- **Kotlinx.datetime** - Date/time handling
- **Kotlinx.coroutines** - Async operations

### Why This Stack?
- ✅ No Xcode required - develop in Android Studio or IntelliJ IDEA
- ✅ Modern declarative UI (similar to SwiftUI syntax)
- ✅ Native macOS app with good performance
- ✅ Small to medium app size (~40MB)
- ✅ Easy integration with shell commands (Jira CLI, BK CLI) via ProcessBuilder
- ✅ Strong type safety with Kotlin
- ✅ User already has IntelliJ IDEA and Android Studio installed

## MVP Feature Set (2-3 Days)

### Day 1: Setup & Database
- [x] Create Compose Desktop project with Gradle
- [x] Set up SQLite database with core schema
- [x] Implement data models and repository layer
- [x] Basic window setup with navigation

### Day 2: Core UI & Logging
- [x] Daily log entry screen (text input, date picker, tags)
- [x] View past logs screen (list view with search by date)
- [x] Tag management (add/remove tags)
- [x] Save and retrieve logs from database

### Day 3: Reminders & Polish
- [x] Implement TWO daily reminders:
  - 10:30 AM reminder (start of workday - review/plan)
  - 4:30 PM reminder (before end of workday - log work)
- [x] macOS notification integration
- [x] App icon and menu bar presence
- [x] Basic settings (configure reminder times)

### MVP Explicitly EXCLUDES (Add Later)
- ❌ Jira integration (shell out to jira CLI)
- ❌ Retrospective points (structured went well/improve/action items)
- ❌ Objectives tracking with progress
- ❌ Analytics/charts
- ❌ Weekly summaries
- ❌ Export functionality (Markdown/PDF)
- ❌ Calendar view
- ❌ Notion/Slack integrations

## Database Schema

```sql
-- Core MVP tables
CREATE TABLE daily_logs (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    date TEXT NOT NULL UNIQUE,              -- ISO 8601 format (YYYY-MM-DD)
    content TEXT NOT NULL,                  -- Main work log (Markdown supported)
    created_at TEXT NOT NULL,               -- ISO 8601 timestamp
    updated_at TEXT NOT NULL                -- ISO 8601 timestamp
);

CREATE TABLE tags (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    name TEXT NOT NULL UNIQUE,
    color TEXT,                             -- Hex color for UI (e.g., "#4A90E2")
    created_at TEXT NOT NULL
);

CREATE TABLE daily_log_tags (
    daily_log_id INTEGER NOT NULL,
    tag_id INTEGER NOT NULL,
    PRIMARY KEY (daily_log_id, tag_id),
    FOREIGN KEY (daily_log_id) REFERENCES daily_logs(id) ON DELETE CASCADE,
    FOREIGN KEY (tag_id) REFERENCES tags(id) ON DELETE CASCADE
);

CREATE TABLE settings (
    key TEXT PRIMARY KEY,
    value TEXT NOT NULL,
    updated_at TEXT NOT NULL
);

-- Indexes for performance
CREATE INDEX idx_daily_logs_date ON daily_logs(date DESC);
CREATE INDEX idx_tags_name ON tags(name);
```

### Future Tables (Post-MVP)
- `retrospective_points` - Structured retrospective notes
- `objectives` - Quarterly/annual objectives
- `objective_progress` - Link daily work to objectives
- `jira_issues` - Cache Jira issue metadata

## Project Structure

```
daily-work-tracker/
├── build.gradle.kts                       # Gradle build configuration
├── settings.gradle.kts
├── gradle.properties
├── src/
│   └── main/
│       ├── kotlin/
│       │   └── com/booking/worktracker/
│       │       ├── Main.kt               # Application entry point
│       │       ├── ui/
│       │       │   ├── App.kt            # Root composable with navigation
│       │       │   ├── theme/
│       │       │   │   ├── Theme.kt      # Material Design theme
│       │       │   │   └── Colors.kt     # Color palette
│       │       │   └── screens/
│       │       │       ├── DailyLogScreen.kt    # Main logging interface
│       │       │       ├── LogListScreen.kt     # View past logs
│       │       │       └── SettingsScreen.kt    # App settings
│       │       ├── data/
│       │       │   ├── Database.kt       # SQLite connection setup
│       │       │   ├── models/
│       │       │   │   ├── DailyLog.kt   # Data class
│       │       │   │   ├── Tag.kt        # Data class
│       │       │   │   └── Settings.kt   # Data class
│       │       │   └── repository/
│       │       │       ├── LogRepository.kt     # CRUD operations
│       │       │       ├── TagRepository.kt     # Tag management
│       │       │       └── SettingsRepository.kt
│       │       └── notifications/
│       │           ├── ReminderScheduler.kt     # Schedule reminders
│       │           └── MacOSNotification.kt     # osascript wrapper
│       └── resources/
│           ├── icon.icns                 # macOS app icon
│           └── database/
│               └── schema.sql            # Database schema
└── README.md
```

## Implementation Details

### 1. Compose Desktop Setup

**build.gradle.kts** dependencies:
```kotlin
plugins {
    kotlin("jvm") version "1.9.22"
    id("org.jetbrains.compose") version "1.5.12"
}

dependencies {
    implementation(compose.desktop.currentOs)
    implementation(compose.material3)

    // Database
    implementation("org.xerial:sqlite-jdbc:3.44.1.0")
    implementation("org.jetbrains.exposed:exposed-core:0.45.0")
    implementation("org.jetbrains.exposed:exposed-dao:0.45.0")
    implementation("org.jetbrains.exposed:exposed-jdbc:0.45.0")

    // Date/Time
    implementation("org.jetbrains.kotlinx:kotlinx-datetime:0.5.0")

    // Coroutines
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.7.3")
}
```

### 2. macOS Notifications

Use existing pattern from `goal-reminder.sh`:

```kotlin
// notifications/MacOSNotification.kt
object MacOSNotification {
    fun send(title: String, message: String, soundName: String = "Glass") {
        val command = """
            osascript -e 'display notification "$message" with title "$title" sound name "$soundName"'
        """.trimIndent()

        ProcessBuilder("bash", "-c", command)
            .start()
            .waitFor()
    }
}
```

### 3. Reminder Scheduler

```kotlin
// notifications/ReminderScheduler.kt
class ReminderScheduler(
    private val logRepository: LogRepository,
    private val settingsRepository: SettingsRepository
) {
    private val scope = CoroutineScope(Dispatchers.Default + Job())

    fun start() {
        // Morning reminder: 10:30 AM
        scheduleDaily(hour = 10, minute = 30) {
            MacOSNotification.send(
                title = "Daily Work Tracker",
                message = "Good morning! Review yesterday and plan your day."
            )
        }

        // Afternoon reminder: 4:30 PM
        scheduleDaily(hour = 16, minute = 30) {
            val today = Clock.System.todayIn(TimeZone.currentSystemDefault())
            val hasLogged = logRepository.hasLogForDate(today)

            if (!hasLogged) {
                MacOSNotification.send(
                    title = "Daily Work Tracker",
                    message = "Time to log your work for today!"
                )
            }
        }
    }

    private fun scheduleDaily(hour: Int, minute: Int, action: () -> Unit) {
        scope.launch {
            while (true) {
                val now = Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault())
                val target = LocalDateTime(now.date, LocalTime(hour, minute))

                val delay = if (now.time < LocalTime(hour, minute)) {
                    // Today's target time
                    target.toInstant(TimeZone.currentSystemDefault()) - Clock.System.now()
                } else {
                    // Tomorrow's target time
                    val tomorrow = target.date.plus(1, DateTimeUnit.DAY)
                    LocalDateTime(tomorrow, LocalTime(hour, minute))
                        .toInstant(TimeZone.currentSystemDefault()) - Clock.System.now()
                }

                delay(delay.inWholeMilliseconds)
                action()
            }
        }
    }
}
```

### 4. Daily Log Screen (UI)

```kotlin
// ui/screens/DailyLogScreen.kt
@Composable
fun DailyLogScreen(
    logRepository: LogRepository,
    tagRepository: TagRepository
) {
    var selectedDate by remember { mutableStateOf(Clock.System.todayIn(TimeZone.currentSystemDefault())) }
    var content by remember { mutableStateOf("") }
    var selectedTags by remember { mutableStateOf<Set<Tag>>(emptySet()) }
    var availableTags by remember { mutableStateOf<List<Tag>>(emptyList()) }

    LaunchedEffect(Unit) {
        availableTags = tagRepository.getAllTags()

        // Load existing log if present
        logRepository.getLogForDate(selectedDate)?.let { log ->
            content = log.content
            selectedTags = log.tags.toSet()
        }
    }

    Column(modifier = Modifier.padding(16.dp).fillMaxSize()) {
        // Date picker
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text("Date:", style = MaterialTheme.typography.titleMedium)
            Spacer(Modifier.width(8.dp))
            Text(selectedDate.toString(), style = MaterialTheme.typography.bodyLarge)
            // TODO: Add date picker button
        }

        Spacer(Modifier.height(16.dp))

        // Main content area
        OutlinedTextField(
            value = content,
            onValueChange = { content = it },
            label = { Text("What did you accomplish today?") },
            modifier = Modifier.fillMaxWidth().height(300.dp),
            placeholder = { Text("• Fixed bug in payment system\n• Reviewed 3 PRs\n• Planning meeting for Q2") }
        )

        Spacer(Modifier.height(16.dp))

        // Tag selection
        Text("Tags:", style = MaterialTheme.typography.titleSmall)
        FlowRow(modifier = Modifier.padding(vertical = 8.dp)) {
            availableTags.forEach { tag ->
                FilterChip(
                    selected = tag in selectedTags,
                    onClick = {
                        selectedTags = if (tag in selectedTags) {
                            selectedTags - tag
                        } else {
                            selectedTags + tag
                        }
                    },
                    label = { Text(tag.name) }
                )
                Spacer(Modifier.width(8.dp))
            }
        }

        Spacer(Modifier.height(16.dp))

        // Save button
        Button(
            onClick = {
                logRepository.saveLog(
                    date = selectedDate,
                    content = content,
                    tags = selectedTags.toList()
                )
                // Show success message
            },
            modifier = Modifier.align(Alignment.End)
        ) {
            Text("Save Log")
        }
    }
}
```

### 5. Database Setup

```kotlin
// data/Database.kt
object Database {
    private lateinit var connection: Connection

    fun init(databasePath: String = getDefaultDatabasePath()) {
        connection = DriverManager.getConnection("jdbc:sqlite:$databasePath")
        createTables()
    }

    private fun getDefaultDatabasePath(): String {
        val homeDir = System.getProperty("user.home")
        val appSupportDir = "$homeDir/Library/Application Support/DailyWorkTracker"
        File(appSupportDir).mkdirs()
        return "$appSupportDir/worktracker.db"
    }

    private fun createTables() {
        val schema = this::class.java.getResource("/database/schema.sql")?.readText()
        schema?.let { sql ->
            connection.createStatement().execute(sql)
        }
    }

    fun getConnection(): Connection = connection
}
```

## Critical Files to Create

### Phase 1: Project Setup (Day 1 Morning)
1. `build.gradle.kts` - Gradle configuration with Compose Desktop
2. `src/main/kotlin/com/booking/worktracker/Main.kt` - Application entry point
3. `src/main/kotlin/com/booking/worktracker/data/Database.kt` - SQLite setup
4. `src/main/resources/database/schema.sql` - Database schema

### Phase 2: Data Layer (Day 1 Afternoon)
5. `src/main/kotlin/com/booking/worktracker/data/models/DailyLog.kt` - Data model
6. `src/main/kotlin/com/booking/worktracker/data/models/Tag.kt` - Data model
7. `src/main/kotlin/com/booking/worktracker/data/repository/LogRepository.kt` - CRUD operations
8. `src/main/kotlin/com/booking/worktracker/data/repository/TagRepository.kt` - Tag operations

### Phase 3: UI (Day 2)
9. `src/main/kotlin/com/booking/worktracker/ui/App.kt` - Root composable with navigation
10. `src/main/kotlin/com/booking/worktracker/ui/theme/Theme.kt` - Material Design theme
11. `src/main/kotlin/com/booking/worktracker/ui/screens/DailyLogScreen.kt` - Main logging UI
12. `src/main/kotlin/com/booking/worktracker/ui/screens/LogListScreen.kt` - View past logs

### Phase 4: Notifications (Day 3 Morning)
13. `src/main/kotlin/com/booking/worktracker/notifications/MacOSNotification.kt` - osascript wrapper
14. `src/main/kotlin/com/booking/worktracker/notifications/ReminderScheduler.kt` - Scheduler

### Phase 5: Polish (Day 3 Afternoon)
15. `src/main/kotlin/com/booking/worktracker/ui/screens/SettingsScreen.kt` - Settings UI
16. `src/main/resources/icon.icns` - macOS app icon
17. `README.md` - Documentation

## Verification Steps

### After Day 1 (Database & Setup)
```bash
cd daily-work-tracker
./gradlew run

# Verify:
# - App window opens
# - Database file created at ~/Library/Application Support/DailyWorkTracker/worktracker.db
# - Can connect to database with sqlite3 CLI
```

### After Day 2 (Core UI)
```bash
./gradlew run

# Verify:
# - Can type in daily log text area
# - Can add tags (create new tags)
# - Can save log (check database with sqlite3)
# - Can view saved log in list screen
# - Can edit existing log for today's date
```

### After Day 3 (Complete MVP)
```bash
./gradlew run

# Verify:
# - Receives notification at 10:30 AM (test by changing system time)
# - Receives notification at 4:30 PM if no log exists
# - Does NOT receive 4:30 PM notification if already logged
# - Can configure reminder times in settings
# - App runs in menu bar/background
# - Can quit and restart without losing data

# Build standalone app:
./gradlew package

# Verify:
# - .app file created in build/compose/binaries/main/app/
# - Can drag to /Applications
# - Runs independently of Gradle
```

### Manual Testing Checklist
- [ ] Create log for today with multiple tags
- [ ] Edit today's log (verify updates saved)
- [ ] Create logs for past 7 days
- [ ] Search/filter logs by date
- [ ] Add new tag, verify it appears in tag list
- [ ] Close and reopen app, verify data persists
- [ ] Change reminder time in settings
- [ ] Verify morning reminder triggers at 10:30 AM
- [ ] Verify afternoon reminder triggers at 4:30 PM
- [ ] Verify afternoon reminder skips if already logged

## Future Enhancements (Post-MVP)

### Phase 2: Retrospectives & Objectives (Days 4-6)
- Add `retrospective_points` table
- Structured retrospective UI (went well / improve / action items)
- Add `objectives` table
- Objective tracking screen with progress bars
- Link daily logs to objectives

### Phase 3: Jira Integration (Days 7-8)
- Shell out to `/opt/homebrew/bin/jira` CLI
- Auto-fetch issues assigned to user updated today
- Display in daily log screen for quick selection
- Cache issue metadata in `jira_issues` table

### Phase 4: Analytics & Export (Days 9-10)
- Weekly summary view (aggregate tags, hours, patterns)
- Simple charts (work distribution, energy trends)
- Export to Markdown for 1-on-1s
- Export to PDF (optional)

### Phase 5: Advanced Features (Days 11-14)
- Calendar view with visual month layout
- Notion integration (push weekly summaries)
- BK CLI integration (team context)
- Templates for different work types
- Search by keyword/tag with filters

## Key Design Decisions

1. **Why Kotlin + Compose Desktop over Electron?**
   - User prefers native feel without Xcode
   - Smaller app size than Electron (~40MB vs ~150MB)
   - Modern declarative UI similar to SwiftUI
   - User already has IntelliJ/Android Studio

2. **Why SQLite over cloud database?**
   - Personal tool, no sharing required
   - Zero configuration
   - File-based = easy backups
   - No network dependency

3. **Why skip Jira integration in MVP?**
   - Core value is building logging habit
   - Manual logging works fine initially
   - Integration adds complexity
   - Can be added in Phase 3 after habit established

4. **Why TWO daily reminders?**
   - 10:30 AM: Review yesterday, plan today (sets intention)
   - 4:30 PM: Log actual accomplishments (captures reality)
   - Dual approach improves adoption and completeness

5. **Why macOS notifications vs in-app reminders?**
   - User can see reminder even when app is closed
   - Matches existing `goal-reminder.sh` pattern user is familiar with
   - Less intrusive than modal dialogs

## Success Criteria

### MVP is successful if:
✅ User logs work at least 5 days per week consistently
✅ Logging takes less than 2 minutes per day
✅ Reminders are helpful, not annoying
✅ Data never lost (reliable persistence)
✅ App starts quickly (under 2 seconds)

### Long-term success:
✅ Becomes primary source for 1-on-1 preparation
✅ Retrospective insights captured in real-time
✅ Objective progress visible at a glance
✅ Reduces stress about "what did I do this quarter?"
