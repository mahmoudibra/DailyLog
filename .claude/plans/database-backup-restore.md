# Plan: Database Backup & Restore with Custom Server

## Context

The DailyTracker app stores all data in a local SQLite file (`daily_tracker.db`). Users want the ability to upload snapshots of their database to a remote server so they can:
- Keep multiple timestamped backups
- Download and restore any snapshot to their local app
- Protect against data loss

Both the server and client live in this monorepo as two independent projects side by side (no shared code):

```
DailyReminder/              (repo root — no build system here)
├── desktop/                (entire existing app codebase moved here)
│   ├── build.gradle.kts
│   ├── settings.gradle.kts
│   ├── core/
│   ├── features/
│   ├── src/
│   └── ...
└── server/                 (new Ktor project)
    ├── build.gradle.kts
    ├── settings.gradle.kts
    └── src/
```

**Pre-requisite step:** Move all existing app files into a `desktop/` directory before any feature work.

1. **Server** (`server/`) — Ktor-based REST API with email/password auth and file storage (own `build.gradle.kts`, independent dependencies)
2. **Client** (`desktop/`) — new `features/sync` module + changes to `core/database`

---

## Part 1: Server (`server/` directory — independent Ktor project)

### Tech Stack
- **Ktor Server** (Netty engine) — stays in the Kotlin ecosystem
- **Exposed** (JetBrains ORM) + PostgreSQL — for user accounts and snapshot metadata
- **BCrypt** — password hashing
- **JWT** — stateless auth tokens
- **Local filesystem or S3** — for storing uploaded `.db` files
- **Deployment** — Docker Compose locally, AWS later

### API Endpoints

| Method | Path | Auth | Description |
|--------|------|------|-------------|
| POST | `/auth/register` | No | Create account (email, password) |
| POST | `/auth/login` | No | Login, returns JWT |
| POST | `/snapshots` | JWT | Upload a `.db` file snapshot |
| GET | `/snapshots` | JWT | List user's snapshots (id, name, size, date) |
| GET | `/snapshots/{id}/download` | JWT | Download a snapshot file |
| DELETE | `/snapshots/{id}` | JWT | Delete a snapshot |

### Database Schema (PostgreSQL)

```sql
CREATE TABLE users (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    email TEXT UNIQUE NOT NULL,
    password_hash TEXT NOT NULL,
    created_at TIMESTAMPTZ DEFAULT now()
);

CREATE TABLE snapshots (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    user_id UUID REFERENCES users(id) ON DELETE CASCADE,
    name TEXT NOT NULL,          -- user-provided or auto-generated
    file_path TEXT NOT NULL,     -- path on disk or S3 key
    file_size BIGINT NOT NULL,
    schema_version INTEGER,     -- SQLDelight schema version for compatibility
    created_at TIMESTAMPTZ DEFAULT now()
);
```

### Server Project Structure
```
server/                            -- independent project, NOT part of root Gradle build
├── build.gradle.kts               -- standalone Gradle build (own plugins, dependencies)
├── settings.gradle.kts            -- standalone settings (single-module project)
├── src/main/kotlin/
│   ├── Application.kt          -- Ktor setup, install plugins
│   ├── plugins/
│   │   ├── Authentication.kt   -- JWT config
│   │   ├── Routing.kt          -- route registration
│   │   └── Serialization.kt    -- content negotiation
│   ├── routes/
│   │   ├── AuthRoutes.kt       -- register/login
│   │   └── SnapshotRoutes.kt   -- CRUD + upload/download
│   ├── models/
│   │   ├── User.kt
│   │   ├── Snapshot.kt
│   │   └── ApiModels.kt        -- request/response DTOs
│   ├── repository/
│   │   ├── UserRepository.kt
│   │   └── SnapshotRepository.kt
│   └── storage/
│       └── FileStorageService.kt  -- save/retrieve .db files
├── Dockerfile
└── docker-compose.yml          -- server + postgres
```

---

## Part 2: Client Changes (`desktop/` project)

### 2a. Add Ktor Client + Serialization Dependencies

**File: `desktop/build.gradle.kts` (root)**
- Add Ktor Client BOM / version variable (~3.1.x)
- Add kotlinx-serialization plugin

**File: `desktop/features/sync/build.gradle.kts`**
- Ktor Client (CIO engine)
- Ktor Client Content Negotiation + kotlinx-serialization-json
- Ktor Client Auth (for JWT bearer token)
- Dependencies on `core:database` and `core:designsystem`

### 2b. Core Database Changes

**File: `desktop/core/database/.../data/DatabaseProvider.kt`**

Add two methods:
- `exportDatabaseFile(): File` — closes the DB connection, copies `daily_tracker.db` to a temp file, reopens DB, returns the copy
- `importDatabaseFile(file: File)` — closes DB, replaces `daily_tracker.db` with the provided file, calls `init()` to reopen

These are the critical operations that make snapshot upload/download possible.

### 2c. New Feature Module: `features/sync`

Follows the same layered architecture as export/settings:

```
desktop/features/sync/
├── build.gradle.kts
└── src/main/kotlin/com/booking/worktracker/
    ├── data/
    │   ├── models/
    │   │   ├── AuthModels.kt        -- LoginRequest, RegisterRequest, AuthResponse (JWT)
    │   │   └── SnapshotModels.kt    -- SnapshotInfo (id, name, size, date, schemaVersion)
    │   ├── datasource/
    │   │   └── SyncRemoteDataSource.kt  -- @Inject @Singleton, Ktor Client HTTP calls
    │   └── repository/
    │       └── SyncRepository.kt    -- @Inject @Singleton, orchestrates auth + snapshot ops
    ├── domain/
    │   └── usecases/
    │       ├── LoginUseCase.kt
    │       ├── RegisterUseCase.kt
    │       ├── UploadSnapshotUseCase.kt   -- calls DatabaseProvider.exportDatabaseFile() + upload
    │       ├── DownloadSnapshotUseCase.kt -- downloads file + calls DatabaseProvider.importDatabaseFile()
    │       ├── ListSnapshotsUseCase.kt
    │       └── DeleteSnapshotUseCase.kt
    ├── presentation/
    │   └── viewmodels/
    │       └── SyncViewModel.kt     -- @Inject, extends ViewModel, manages auth state + snapshot list
    ├── ui/
    │   └── screens/
    │       └── SyncScreen.kt        -- Login/Register form + snapshot list with upload/download/delete
    └── di/
        └── SyncComponent.kt        -- @Component, parent: DatabaseComponent, exposes syncViewModel
```

### 2d. DI Wiring for Ktor Client

The `SyncRemoteDataSource` needs an `HttpClient` instance. Options:
- Create it inside `SyncRemoteDataSource` constructor (simplest, avoids new core module)
- The HttpClient is configured with: base URL (from settings or hardcoded), content negotiation (JSON), and bearer auth (JWT token)

### 2e. Auth Token Storage

Store the JWT token and user email locally via the existing `settings` table (or a new `sync_settings` key-value approach using `SettingsQueries`). The token is loaded on app start and attached to requests.

### 2f. Navigation & UI Integration

**File: `desktop/src/.../ui/App.kt`**
- Add `SYNC` to the `Screen` enum
- Add nav item with sync/cloud icon
- Add `SyncScreen` composable in the when-block

**SyncScreen UI layout:**
1. **Not logged in** → Show login/register form (email + password fields, toggle between modes)
2. **Logged in** → Show:
   - User email + logout button
   - "Upload Snapshot" button (with optional name field)
   - List of snapshots: name, date, size — each with download and delete actions
   - Download triggers confirmation dialog (warns it replaces local data)

### 2g. Settings Integration

**File: `desktop/features/settings/.../ui/screens/SettingsScreen.kt`**
- Add "Server URL" text field in settings so user can point to their own server instance

---

## Implementation Order

0. **Repo restructure** — move all existing app files into `desktop/`, verify the app still builds and runs from there
1. **Server (`server/`)** — set up Ktor project, auth routes, snapshot CRUD, Docker setup
2. **Client: `desktop/core/database`** — add `exportDatabaseFile()` and `importDatabaseFile()` to DatabaseProvider
3. **Client: `desktop/features/sync`** — scaffold module, add dependencies, implement layers bottom-up (models → datasource → repository → usecases → viewmodel → screen → DI)
4. **Client: app integration** — add to navigation, settings, `desktop/build.gradle.kts`
5. **Test end-to-end** — run server locally via Docker, register, upload, list, download, verify data integrity

## Verification

- Start server with `docker-compose up`
- Run the app, navigate to Sync screen
- Register a new account, verify login works
- Upload a snapshot, verify it appears in the list with correct metadata
- Add some data to the app, upload another snapshot
- Download the first snapshot, verify the app data reverts
- Delete a snapshot from the server, verify it's removed from the list
