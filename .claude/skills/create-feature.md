# Create Feature

Scaffolds an entire feature module for the DailyWorkTracker app, creating all layers from database schema through UI.

## When to Use

Use when the user asks to create a new feature, add a new module, or scaffold a new screen with its full data stack.

## Inputs

Ask the user for:
1. **Feature name** (e.g., "notes", "habits") — used for module directory, package names, and class prefixes
2. **Brief description** of what the feature does — used to determine models, fields, and CRUD operations
3. **Data model fields** — what fields the main entity should have (suggest reasonable defaults based on the description)

## Steps

### 1. Gradle Module Setup

Create `{feature}/build.gradle.kts` matching the existing module pattern:

```kotlin
plugins {
    kotlin("jvm") version "1.9.22"
    id("org.jetbrains.compose") version "1.5.12"
}

group = "com.booking.worktracker"
version = "1.0-SNAPSHOT"

repositories {
    google()
    mavenCentral()
    maven("https://maven.pkg.jetbrains.space/public/p/compose/dev")
}

dependencies {
    implementation(project(":core"))
    implementation(compose.desktop.currentOs)
    implementation(compose.material3)
    implementation(compose.materialIconsExtended)
    implementation("org.jetbrains.kotlinx:kotlinx-datetime:0.5.0")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.7.3")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-swing:1.7.3")
}

tasks.withType<org.jetbrains.kotlin.gradle.tasks.KotlinCompile> {
    kotlinOptions.freeCompilerArgs += "-opt-in=androidx.compose.material3.ExperimentalMaterial3Api"
}
```

Register the module in `settings.gradle.kts`:
```kotlin
include(":{feature}")
```

Add dependency in root `build.gradle.kts`:
```kotlin
implementation(project(":{feature}"))
```

### 2. Database Schema

Append new table(s) to `core/src/main/resources/database/schema.sql` using these conventions:
- `CREATE TABLE IF NOT EXISTS` for idempotent init
- `id INTEGER PRIMARY KEY AUTOINCREMENT`
- `created_at TEXT NOT NULL DEFAULT (datetime('now'))`
- `updated_at TEXT NOT NULL DEFAULT (datetime('now'))` for mutable entities
- Use `TEXT` for strings/dates, `INTEGER` for booleans (0/1) and ints
- `FOREIGN KEY` with `ON DELETE CASCADE` for child tables
- `CHECK` constraints for enum-like columns stored as TEXT

### 3. Data Models

Create `{feature}/src/main/kotlin/com/booking/worktracker/data/models/{Entity}.kt`:
- Plain `data class` with all DB columns as constructor parameters
- Types: `Int` for ids, `String` for text/dates, `Boolean` for flags
- Enums as separate `enum class` in the same file when needed
- Helper methods on the data class for computed display values

### 4. Local Data Source

Create `{feature}/src/main/kotlin/com/booking/worktracker/data/datasource/{Entity}LocalDataSource.kt`:
- Class with no constructor params (accesses DB via `Database.getConnection()`)
- Raw JDBC with `PreparedStatement` for all queries — no ORM
- Always close `ResultSet` and `Statement` after use
- Return domain model objects, never raw ResultSets
- Use `java.sql.Statement.RETURN_GENERATED_KEYS` for inserts that need the new ID
- Follow the pattern: prepare → set params → execute → read results → close → return

Standard CRUD methods:
```kotlin
fun getAll(limit: Int = 50): List<Entity>
fun getById(id: Int): Entity?
fun create(...fields...): Entity
fun update(id: Int, ...fields...): Entity
fun delete(id: Int)
```

### 5. Repository

Create `{feature}/src/main/kotlin/com/booking/worktracker/data/repository/{Entity}Repository.kt`:
- Takes `{Entity}LocalDataSource` as constructor parameter
- Thin delegation — each method calls through to the data source
- Keep the same method signatures as the data source

```kotlin
class {Entity}Repository(
    private val localDataSource: {Entity}LocalDataSource
) {
    fun getAll(limit: Int = 50): List<Entity> = localDataSource.getAll(limit)
    // ... etc
}
```

### 6. Use Cases

Create `{feature}/src/main/kotlin/com/booking/worktracker/domain/usecases/{feature}/{UseCaseName}UseCase.kt`:
- Each use case is a class taking the repository as constructor param
- Use `operator fun invoke(...)` for single-action use cases
- Return `Result<T>` wrapping success/failure
- Add input validation (e.g., `require(title.isNotBlank())`) before calling repository
- Group related use cases in the same file if they're small

### 7. ViewModel

Create `{feature}/src/main/kotlin/com/booking/worktracker/presentation/viewmodels/{Feature}ViewModel.kt`:
- Extends `ViewModel()` from core (provides `viewModelScope`)
- Takes repository as constructor parameter (not use cases — existing pattern is mixed, but ViewModels call repository directly)
- Expose state via `MutableStateFlow` (private) / `StateFlow` (public via `.asStateFlow()`)
- All data mutations in `viewModelScope.launch { ... }`
- Provide a `loadData()` or `load{Items}()` method called from `init`
- Each user action is a public method that updates state and reloads

### 8. UI Screen

Create `{feature}/src/main/kotlin/com/booking/worktracker/ui/screens/{Feature}Screen.kt`:
- Top-level `@Composable fun {Feature}Screen(repository: {Entity}Repository)` — receives repository, not ViewModel
- Use design system components: `DSScreenTitle`, `DSCard`, `DSButton`, `DSOutlinedButton`, `DSTextButton`, `DSIconButton`, `DSOutlinedTextField`, `DSEmptyState`, `DSSectionHeader`, `DSInfoBanner`, `DSDivider`, `DSLoadingIndicator`
- Use `SpacingTokens.screenPadding`, `SpacingTokens.sectionSpacing`, `SpacingTokens.medium`, `SpacingTokens.small` for layout
- Manage local UI state with `remember { mutableStateOf(...) }`
- Use `rememberCoroutineScope()` + `scope.launch` for async operations
- Use `LaunchedEffect` for loading data on param changes
- Dialogs as separate `@Composable` functions in the same file
- Use `LazyColumn` with `items()` for lists

### 9. Wire Into App

Update these files to integrate the new feature:

**`src/main/kotlin/com/booking/worktracker/Main.kt`:**
- Add new `{Entity}LocalDataSource` and `{Entity}Repository` instantiation alongside existing ones
- Pass new repository to `App()`

**`src/main/kotlin/com/booking/worktracker/ui/App.kt`:**
- Add new entry to `Screen` enum
- Add new `NavigationBarItem` in the bottom bar
- Add new `when` branch calling `{Feature}Screen(repository)`
- Accept new repository parameter in `App()` function signature

## Conventions

- Package: `com.booking.worktracker.*` throughout
- All Kotlin files, no Java
- No dependency injection framework — manual wiring in `Main.kt`
- Material 3 with `@OptIn(ExperimentalMaterial3Api::class)` where needed
- Use `kotlinx.datetime` types (not `java.time`)
- Icons from `androidx.compose.material.icons.filled.*`
