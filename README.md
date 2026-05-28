# Menú del Día

A mobile-first map app that helps people in Barcelona find restaurants serving **menú del día** —
the traditional Spanish fixed-price weekday lunch — near them, right now.

## What it does

- Map of Barcelona with custom price + cuisine-emoji pins, animated camera pan to selection
- Bottom sheet surfaces nearby restaurants (peek → list → detail)
- Search by name / cuisine and filters by open-now / price / cuisine
- Restaurant detail: today's menu (what the price includes), full opening hours, address, phone,
  description (ES/EN), photo gallery
- "Get directions" deep-links to Google Maps / Apple Maps in walking mode
- Sign in with Google or Apple; guest browsing also supported

## Stack

| Layer           | Choice                                                               |
|-----------------|----------------------------------------------------------------------|
| Mobile + Web UI | Kotlin Multiplatform + Compose Multiplatform (Android, iOS, JS+Wasm) |
| Backend         | Spring Boot 3 + Kotlin (Java 21)                                     |
| Database        | PostgreSQL 16 + PostGIS 3.4 (Flyway migrations)                      |
| Map (mobile)    | Google Maps SDK — native on Android / iOS via `expect`/`actual`      |
| Map (web)       | Google Maps JS API via Compose Web interop                           |
| Auth            | Custom HS256 JWT; Google ID token + Apple identity token verifiers   |
| Networking      | Ktor Client + kotlinx.serialization                                  |
| DI              | Metro (Zac Sweers) — compile-time, KMP-native                        |
| Image loading   | Coil 3                                                               |

## Modules

```
menu-del-dia/
├── composeApp/   # Compose Multiplatform UI
│   └── src/
│       ├── commonMain/    # Shared UI, ViewModels, navigation, search, filters, auth UI
│       ├── androidMain/   # Google Maps Android wrapper + Credential Manager (Google Sign-In)
│       ├── iosMain/       # Google Maps iOS wrapper + GIDSignIn / Sign in with Apple bridges
│       └── webMain/       # Google Maps JS wrapper (shared by jsMain and wasmJsMain)
├── shared/       # Domain models, use cases, repositories, Ktor remote layer (KMP)
├── backend/      # Spring Boot REST API
└── iosApp/       # Xcode entry point
```

## Building & running

### Android

```bash
./gradlew :composeApp:assembleDebug
```

### Web (Wasm — preferred for modern browsers)

```bash
./gradlew :composeApp:wasmJsBrowserDevelopmentRun
```

### Web (JS — broader compatibility)

```bash
./gradlew :composeApp:jsBrowserDevelopmentRun
```

### iOS

Open `iosApp/` in Xcode and run. (Requires a paid Apple Developer account for Sign in with Apple +
TestFlight.)

### Backend

```bash
# Local Postgres + PostGIS via Docker Compose
docker compose -f backend/docker-compose.yml up -d db

# Boot the API (reads backend/.env)
./gradlew :backend:bootRun -Pprofiles=dev

# Tests
./gradlew :backend:test
```

See [`backend/CLAUDE.md`](./backend/CLAUDE.md) for full backend architecture, schema, and config.

## Backend API (v1)

All routes under `/api/v1/`. JWT-protected unless marked otherwise.

| Method | Path                                                                  | Auth        |
|--------|-----------------------------------------------------------------------|-------------|
| `GET`  | `/actuator/health`                                                    | public      |
| `POST` | `/auth/google` · `/auth/apple`                                        | public      |
| `GET`  | `/me`                                                                 | JWT         |
| `GET`  | `/restaurants` (lat/lng/radius/q/openNow/cuisine[]/minPrice/maxPrice) | JWT         |
| `GET`  | `/restaurants/{id}`                                                   | JWT         |
| `GET`  | `/restaurants/{id}/photos/{n}`                                        | JWT         |
| `POST` | `/admin/seed` · `/admin/enrich`                                       | admin token |

## Configuration

Backend env vars (see `backend/.env.example`):

- `DB_URL` · `DB_USER` · `DB_PASSWORD`
- `GOOGLE_PLACES_API_KEY` — Places API
- `GOOGLE_OAUTH_CLIENT_ID` — audience for Google ID token verification
- `APPLE_BUNDLE_ID` — audience for Apple ID token verification
- `JWT_SIGNING_KEY` — HS256 key (`openssl rand -hex 32`)
- `PHOTOS_DIR` — local photo storage root (default `./var/photos`)
- `ADMIN_TOKEN` — required on `X-Admin-Token` header for admin endpoints

Client side (`local.properties`):

- Google Maps Android API key
- `GOOGLE_WEB_CLIENT_ID` — used by Android Credential Manager for Google Sign-In

iOS: `Config.xcconfig` sets `GID_CLIENT_ID` + `GID_REVERSED_CLIENT_ID` for the GIDSignIn SDK.

## Project documentation

- [`CLAUDE.md`](./CLAUDE.md) — root architecture overview + cross-module patterns
- [`backend/CLAUDE.md`](./backend/CLAUDE.md) — backend stack, schema, request flows, security
