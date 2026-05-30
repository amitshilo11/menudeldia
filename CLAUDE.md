# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this
repository.

## Rules

- Do what has been asked; nothing more, nothing less
- NEVER create files unless absolutely necessary — prefer editing existing files
- NEVER create documentation files unless explicitly requested
- ALWAYS read a file before editing it
- Keep files under 300 lines — split when they grow larger
- Validate input at system boundaries only

## Project Overview

**Menú del Día** — KMP + Compose Multiplatform map app for finding Barcelona fixed-price lunch
restaurants. Three Gradle modules:

| Module       | Purpose                                                                            |
|--------------|------------------------------------------------------------------------------------|
| `shared`     | Domain models, use cases, repositories, Ktor networking — compiled for all targets |
| `composeApp` | Compose Multiplatform UI (Android, iOS via iosApp, Web/Wasm)                       |
| `backend`    | Spring Boot + Kotlin REST API (Java 21, PostgreSQL + PostGIS)                      |

## Build Commands

```bash
# Android
./gradlew :composeApp:assembleDebug

# Web (Wasm — preferred for modern browsers)
./gradlew :composeApp:wasmJsBrowserDevelopmentRun

# Web (JS — broader compatibility)
./gradlew :composeApp:jsBrowserDevelopmentRun

# Backend
./gradlew :backend:bootRun

# Backend tests
./gradlew :backend:test

# Run a single test
./gradlew :backend:test --tests "com.menudeldia.auth.JwtServiceTest"

# iOS — open iosApp/ in Xcode and run
```

## Architecture

### Shared module — clean layers

```
domain/       ← pure Kotlin: Restaurant, Menu, OpeningHours models; repository interfaces; use cases
data/remote/  ← Ktor DTOs + API services (RestaurantApiService, AuthApiService)
data/local/   ← (mock repository + fixtures live in commonTest, not in prod binaries)
data/auth/    ← SessionStore (multiplatform settings), AuthRepositoryImpl
di/           ← Metro DI graph (AppGraph) — single @DependencyGraph interface
```

**DI**: Zac Sweers' Metro library (`@DependencyGraph`, `@Provides`, `@SingleIn`). `AppGraph` is the
single graph; everything is wired there.

**Networking**: Ktor client with `ContentNegotiation` (kotlinx.serialization), `Auth` plugin (bearer
token from `SessionStore`), and per-platform `apiBaseUrl`:

- Android: `http://10.0.2.2:8080` (emulator localhost)
- iOS/JVM: `http://localhost:8080`
- Wasm/JS: relative paths

### composeApp — UI layer

Navigation uses `sealed class Screen` with Compose Navigation. Screens:
`Login → Map → RestaurantDetail`, `Account`.

**Map**: `MapView` is an `expect` composable — each platform (`androidMain`, `iosMain`, `webMain` —
the latter shared by `jsMain` and `wasmJsMain`) provides an `actual` wrapping the native Google Maps
SDK. Don't add platform logic to `commonMain`.

**Auth**: `AuthProvider` is `expect`/`actual`. Android uses Credential Manager (Google Sign-In). iOS
uses Google Sign-In SDK via `GIDSignIn`. The common `AuthProviderHolder` bridges them to the shared
`AuthRepository`.

**Location**: `LocationState` is `expect`/`actual`. Use the common `UserLocation` data class.

### Backend — Spring Boot

Package structure: `com.menudeldia.*`

| Package      | Contents                                                                                  |
|--------------|-------------------------------------------------------------------------------------------|
| `restaurant` | JPA entity, repository, service, controller, mapper                                       |
| `auth`       | JWT filter, Google/Apple token verifiers, `UserService`, `AuthController`, `MeController` |
| `places`     | `GooglePlacesClient`, `PlacesEnrichmentService` (photo download + enrichment)             |
| `photo`      | `PhotoController` — serves stored restaurant photos                                       |
| `admin`      | `AdminController` (admin-token protected, restaurant seeding/enrichment)                  |
| `seed`       | `SeederService` — seeds from `seed.json` or CSV                                           |
| `config`     | CORS, security, `AppProperties`, `AdminTokenAuthorizationManager`                         |
| `geo`        | `GeoUtils` (distance math)                                                                |

**Database**: PostgreSQL + PostGIS. Flyway manages migrations —
`backend/src/main/resources/db/migration/`. `hibernate.ddl-auto=validate` so the schema must match
entity definitions exactly. Add new columns via a new `V<n>__description.sql` migration file.

**Security**: Spring Security + JWT. All endpoints require a valid JWT except `/api/v1/auth/**` and
`/actuator/health`. Admin endpoints additionally require `ADMIN_TOKEN` header. Rate limiting via
Bucket4j (60 reads/min, 10 auth/min).

**Configuration** (env vars / `.env` file in `backend/`):

- `GOOGLE_PLACES_API_KEY` — Google Places API
- `GOOGLE_OAUTH_CLIENT_ID` — Google OAuth client
- `JWT_SIGNING_KEY` — hex-encoded signing key (`openssl rand -hex 32`)
- `DB_URL`, `DB_USER`, `DB_PASSWORD`
- `PHOTOS_DIR` — local photo storage root (default `./var/photos`)
- `ADMIN_TOKEN` — for admin endpoints

Run dev profile with: `./gradlew :backend:bootRun -Pprofiles=dev`

## API Endpoints

| Method | Path                                      | Auth        |
|--------|-------------------------------------------|-------------|
| `POST` | `/api/v1/auth/google`                     | none        |
| `POST` | `/api/v1/auth/apple`                      | none        |
| `GET`  | `/api/v1/me`                              | JWT         |
| `GET`  | `/api/v1/restaurants?lat=&lng=&radius=`   | JWT         |
| `GET`  | `/api/v1/restaurants/{id}`                | JWT         |
| `GET`  | `/api/v1/restaurants/{id}/photos/{index}` | JWT         |
| `POST` | `/api/v1/admin/seed`                      | ADMIN_TOKEN |
| `POST` | `/api/v1/admin/enrich`                    | ADMIN_TOKEN |
| `GET`  | `/actuator/health`                        | none        |

## Key Patterns

- **expect/actual** is used for: `MapView`, `AuthProvider`, `LocationState`, `UriLauncher`,
  `apiBaseUrl`, `KmpLogger`. Add platform implementations to all source sets when adding a new
  `expect` — web `actual`s live in `webMain` (shared by `jsMain` and `wasmJsMain`), not in
  `wasmJsMain` directly.
- **Metro DI**: add new dependencies to `AppGraph.Companion` as `@Provides` functions. Use
  `@SingleIn(AppScope::class)` for singletons.
- **Flyway migrations**: never rename or edit existing migration files — always add a new `V<n>__`
  file.
- **RestaurantMapper** exists in both `shared` (DTO→domain) and `backend` (entity→DTO) — keep them
  separate.
