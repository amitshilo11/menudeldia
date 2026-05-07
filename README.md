# Menú del Día

A mobile-first map app that helps locals (and later, tourists) in Barcelona find restaurants serving **menú del día** — the traditional Spanish fixed-price weekday lunch — near them, right now.

> **MVP target:** ship to Barcelona users in 1–2 months, solo, with mock data, basic map, and the core map + bottom sheet + restaurant detail flow.

---

## What it does

- Shows a map of Barcelona with pins for restaurants serving menú del día today
- Clusters pins when zoomed out, expands on zoom
- Bottom sheet surfaces nearby restaurants (peek → list → detail)
- Tap a pin → restaurant detail: today's menu, price, opening hours, address, photo
- User location with recenter button; falls back to Barcelona center if location denied

## Differentiators

1. **Map UX** — faster, smoother, more legible than competitors (menudia.app)
2. **Menú del día-specific ratings (v2)** — rates portion size, freshness, value, dish variety — not generic restaurant stars

---

## Stack

| Layer | Choice |
|---|---|
| Mobile + Web UI | Kotlin Multiplatform + Compose Multiplatform (iOS, Android, Wasm) |
| Backend | Spring Boot + Kotlin |
| Database | PostgreSQL + PostGIS |
| Map (mobile) | Google Maps SDK (Android + iOS via `expect`/`actual`) |
| Map (web) | Google Maps JS API via Compose Web interop |
| Networking | Ktor Client |
| Serialization | kotlinx.serialization |
| DI | Metro (Zac Sweers) |

---

## Project structure

```
menu-del-dia/
├── composeApp/       # Compose Multiplatform UI (Android, iOS, Web)
│   └── src/
│       ├── commonMain/    # Shared UI, ViewModels, navigation
│       ├── androidMain/   # Google Maps Android wrapper
│       ├── iosMain/       # Google Maps iOS wrapper
│       └── wasmJsMain/    # Google Maps JS wrapper
├── shared/           # Domain models, repositories, networking (KMP)
├── server/           # Spring Boot REST API
└── iosApp/           # Xcode entry point
```

---

## Backend API

| Method | Endpoint | Purpose |
|---|---|---|
| `GET` | `/api/v1/restaurants?lat=&lng=&radius=` | Nearby restaurants |
| `GET` | `/api/v1/restaurants/{id}` | Restaurant detail + today's menu |
| `GET` | `/api/v1/health` | Health check |

---

## Building & running

### Android

```shell
./gradlew :composeApp:assembleDebug
```

### Web (Wasm — modern browsers)

```shell
./gradlew :composeApp:wasmJsBrowserDevelopmentRun
```

### Web (JS — broader compatibility)

```shell
./gradlew :composeApp:jsBrowserDevelopmentRun
```

### Server

```shell
./gradlew :server:run
```

### iOS

Open [`/iosApp`](./iosApp) in Xcode and run, or use the IDE run configuration.

---

## MVP scope

**In:** map with pins, user location, bottom sheet, restaurant detail, mock data (~20–30 restaurants), Spring Boot backend skeleton, iOS + Android + Web.

**Out (v2+):** ratings, reviews, user accounts, filters, favorites, search, restaurant self-publish, push notifications, payments.

---

## Status

Pre-development — MVP scoping complete. See [`overview.md`](./overview.md) for full product spec, architecture decisions, risk register, and task list.
