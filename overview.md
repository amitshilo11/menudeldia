# Menú del Día App — Product Characterization & Task List

**Project codename:** TBD
**Author:** Amit
**Date:** May 2026
**Status:** Pre-development, MVP scoping

---

## 1. Executive Summary

A mobile-first map app that helps locals (and later, tourists) in Barcelona find restaurants serving **menú del día** — the traditional Spanish fixed-price weekday lunch — near them, right now.

The product competes with `menudia.app` and the `menudeldia.lovable.app` prototype. The bet: **win on map UX and on trustworthy ratings specific to the menú del día itself** (not generic restaurant ratings, which often don't reflect the lunch menu quality).

**MVP target:** ship a working app to Barcelona users in **1–2 months**, solo, with mock data, basic map, and the core map + bottom sheet + restaurant detail flow. Ratings/reviews come in v2.

---

## 2. Target Users

### v1 (MVP) — Locals
- Office workers in Barcelona looking for affordable lunch within walking distance of their workplace
- Repeat-use behavior — same user, different days, different neighborhoods
- Barcelona-only geography
- Spanish/Catalan/English UI

### v2 — Tourists
- One-time use, language-first (English/multi-language)
- Higher reliance on photos, translations, and dietary filters
- Discovery beyond their immediate location

**Not in scope:** restaurants as users (no self-publish flow in MVP — we curate manually).

---

## 3. Positioning & Differentiators

The reference apps already cover the basics. We win on two things:

1. **Map UX.** Faster, smoother, more legible than competitors. Pin clustering done right, location accuracy that feels native, bottom sheet that doesn't fight the map.
2. **Menú del día-specific ratings (v2).** Generic Google ratings don't tell you if the lunch is good. We rate the *menu del día itself*: portion size, freshness, value-for-money, dish variety on rotation.

**Out of scope for v1** (deliberately): filters, ratings, reviews, accounts, favorites, restaurant onboarding, reservations, payments. These are v2+.

---

## 4. MVP Scope (v1, 1–2 months)

### In scope

- **Map view** with restaurant pins (clustered when zoomed out)
- **User location** on the map with smooth follow/recenter
- **Bottom sheet** that surfaces nearby restaurants, expanding to detail when a pin is tapped
- **Restaurant detail screen:** name, address, today's menu (dishes + price), opening hours, distance from user, photo
- **Mock data** — JSON file shipped with the app, ~20–30 hand-curated Barcelona restaurants
- **Backend skeleton** — Spring Boot + Kotlin service that returns the same mock data over HTTP, ready to swap from local to remote without UI changes
- **Three platforms** — iOS, Android, Web (Compose Multiplatform shared UI)
- **Spanish + English UI strings** (Catalan optional but cheap to add)

### Out of scope (v2+)

- Ratings, reviews, photos uploaded by users
- User accounts (anonymous device-ID-based later)
- Filters (cuisine, price, distance, open now, dietary)
- Favorites / saved restaurants
- Search by dish or neighborhood
- Restaurant self-publish portal
- Push notifications
- Reservations / bookings
- Monetization (free for users, free for everyone in v1)

---

## 5. User Flow (MVP)

```
App open
  ↓
Request location permission
  ↓
Map centers on user (or on Barcelona center if denied)
  ↓
Pins load from API → cluster when zoomed out
  ↓
User taps pin → bottom sheet expands with that restaurant
  ↓
User taps card → full restaurant detail (menu, price, hours, photo, "Open in Maps" CTA)
  ↓
User backs out → map state preserved
```

**Critical UX details:**
- Pin tap and bottom-sheet card are **synced** — tapping pin highlights card, swiping card highlights pin (deferred to v1.1 if tight on time; v1 is just pin-tap → sheet)
- "Recenter on me" button always accessible
- Map should not jank when pins re-cluster on zoom
- Bottom sheet has 3 states: peek (nearest restaurant only), half (list of ~5), full (full detail of selected)

---

## 6. Technical Architecture

### Stack

| Layer | Choice | Notes |
|---|---|---|
| **Mobile + Web UI** | Kotlin Multiplatform + Compose Multiplatform | Shared UI across iOS, Android, Web (Wasm) |
| **Backend** | Spring Boot + Kotlin | REST API, PostgreSQL with PostGIS for geo queries |
| **Database** | PostgreSQL + PostGIS | Geographic queries (`ST_DWithin`, `ST_Distance`) |
| **Map (mobile)** | Google Maps SDK | Native on iOS + Android via expect/actual |
| **Map (web)** | Google Maps JavaScript API | Wrapped in Compose Web wrapper |
| **DI** | Metro (Zac Sweers) | Compile-time DI via Kotlin compiler plugin, Dagger-familiar API, full KMP support (JVM/JS/Wasm/Native), no KAPT/KSP needed |
| **Networking** | Ktor Client | KMP-native HTTP |
| **Serialization** | kotlinx.serialization | KMP-native |
| **State management** | Compose state + ViewModel pattern | Standard Compose Multiplatform |
| **Build / CI** | Gradle + GitHub Actions (or Bitbucket if preferred) | |

### Critical architectural call-out: Google Maps on Web

Google Maps does **not** have an official Compose Multiplatform Web binding. We solve this with the **`expect/actual`** pattern:

- Define a common `MapView` composable in `commonMain`
- `androidMain` → wraps Google Maps Android SDK in `AndroidView`
- `iosMain` → wraps `GMSMapView` (Google Maps iOS SDK) via `UIKitView`
- `wasmJsMain` → wraps Google Maps JS API via JS interop, rendered in a `<div>` inside the Compose Web canvas

This is the single biggest technical risk in the project. **Build a spike of this in week 1** before committing to the rest of the architecture. If web is too painful, we fall back to: KMP for iOS+Android, separate Next.js app for web sharing the backend (option 3 from your earlier choices).

### Project structure (proposed)

```
menu-del-dia/
├── composeApp/                  # Compose Multiplatform app
│   ├── src/
│   │   ├── commonMain/          # Shared UI, view models, navigation
│   │   ├── androidMain/         # Google Maps Android wrapper
│   │   ├── iosMain/             # Google Maps iOS wrapper
│   │   └── wasmJsMain/          # Google Maps JS wrapper
│   └── build.gradle.kts
├── shared/                      # Shared business logic
│   ├── src/
│   │   ├── commonMain/          # Domain models, repositories, use cases, networking
│   │   └── ...                  # Platform-specific impls (location, etc.)
│   └── build.gradle.kts
├── backend/                     # Spring Boot + Kotlin
│   ├── src/main/kotlin/
│   └── build.gradle.kts
└── iosApp/                      # iOS entry point (Xcode project)
```

### Backend API (v1 endpoints)

| Method | Endpoint | Purpose |
|---|---|---|
| `GET` | `/api/v1/restaurants?lat={lat}&lng={lng}&radius={m}` | Nearby restaurants for map |
| `GET` | `/api/v1/restaurants/{id}` | Restaurant detail with today's menu |
| `GET` | `/api/v1/health` | Health check |

**Response shape (`/restaurants` list):**

```json
{
  "restaurants": [
    {
      "id": "uuid",
      "name": "Casa Lolea",
      "lat": 41.3851,
      "lng": 2.1734,
      "menuPrice": 14.50,
      "currency": "EUR",
      "todayHasMenu": true,
      "thumbnailUrl": "https://..."
    }
  ]
}
```

**Response shape (`/restaurants/{id}`):** same fields + `address`, `openingHours`, `phone`, `dishesToday[]`, `photos[]`, `description`.

### Data model (PostgreSQL)

```
restaurants
  id, name, address, lat, lng, geom (PostGIS Point),
  phone, photo_urls[], description_es, description_en,
  opening_hours (jsonb), created_at, updated_at

menus
  id, restaurant_id, date, price, currency,
  dishes (jsonb: {firsts: [...], seconds: [...], desserts: [...]}),
  notes, created_at

-- v2 tables (not in MVP)
-- ratings, reviews, users, favorites
```

---

## 7. Risks & Mitigations

| Risk | Impact | Mitigation |
|---|---|---|
| Google Maps on Compose Wasm is painful | High — could derail web | Spike it in week 1. Fallback: separate Next.js web app. |
| Google Maps API costs at scale | Medium | Free tier is generous for MVP; monitor usage. Mapbox/MapLibre as v2 fallback if needed. |
| Manual data curation doesn't scale | Low for MVP | Fine for 20–30 restaurants. Plan for restaurant self-publish in v2. |
| Solo developer, 1–2 month timeline | High | Aggressively cut scope. Ship ugly. Ratings come later. |
| iOS deployment requires Mac + Apple Developer account ($99/yr) | Low but real | Confirm you have Mac access; budget the $99. |
| Compose Multiplatform Web (Wasm) still maturing | Medium | Accept some rough edges on web; mobile is primary. |

---

## 8. Success Metrics (MVP)

- **Technical:** App ships on iOS, Android, and Web with the same codebase. Map loads in <2s on 4G. No crashes in first 50 sessions.
- **Product:** 20+ Barcelona restaurants curated. Map UX feels noticeably smoother than menudia.app (subjective, validate with 5–10 users).
- **Distribution:** TestFlight + Play Store internal testing track + a public web URL by end of month 2.

---

## 9. Task List

Tasks grouped by week. Aggressive but doable solo if scope holds.

### Week 1 — Foundations & risk spike

- [ ] **T1.1** Set up Gradle project: KMP + Compose Multiplatform (iOS, Android, Wasm targets)
- [ ] **T1.2** **Spike Google Maps on Wasm** (highest risk — do first). Get a working map with one pin rendered on web. If blocked >3 days, switch web to separate Next.js.
- [ ] **T1.3** Set up Google Maps SDK on Android with one pin
- [ ] **T1.4** Set up Google Maps SDK on iOS with one pin (`UIKitView` wrapper)
- [ ] **T1.5** Define `expect`/`actual` `MapView` composable in `commonMain`
- [ ] **T1.6** Set up Spring Boot + Kotlin backend project, basic `/health` endpoint
- [ ] **T1.7** Buy domain, set up Google Cloud project, get Maps API keys (Android, iOS, JS — three separate keys)

### Week 2 — Core data flow & UI shell

- [ ] **T2.1** Define domain models in `commonMain`: `Restaurant`, `Menu`, `Dish`, `OpeningHours`
- [ ] **T2.2** Create mock JSON file with 20–30 hand-curated Barcelona restaurants (real names, real coordinates, plausible menus)
- [ ] **T2.3** Build `RestaurantRepository` with two impls: `LocalMockRepository` (reads JSON) and `RemoteRepository` (Ktor Client → backend)
- [ ] **T2.4** Backend: Postgres + PostGIS schema, seed with the same 20–30 restaurants, `GET /api/v1/restaurants` endpoint with radius query
- [ ] **T2.5** Backend: `GET /api/v1/restaurants/{id}` endpoint
- [ ] **T2.6** Set up Metro DI: define `AppGraph` with `@DependencyGraph`, wire `RestaurantRepository`, `LocationProvider`, and Ktor `HttpClient` as `@Provides` / `@Inject` dependencies
- [ ] **T2.7** Build top-level navigation scaffold (Compose Multiplatform navigation)

### Week 3 — Map experience

- [ ] **T3.1** Wire `MapView` to repository — render pins from data
- [ ] **T3.2** Implement pin clustering (Android: Maps Utils library; iOS: `GMUClusterManager`; Web: `MarkerClusterer`)
- [ ] **T3.3** Implement user location: `expect`/`actual` `LocationProvider` (FusedLocationProvider on Android, `CLLocationManager` on iOS, `navigator.geolocation` on web)
- [ ] **T3.4** Permission flow: request location, handle denied state (fallback to Barcelona center: 41.3851, 2.1734)
- [ ] **T3.5** "Recenter on me" floating action button
- [ ] **T3.6** Bottom sheet shell with 3 states (peek, half, full) using Compose Material 3 `BottomSheetScaffold`
- [ ] **T3.7** Pin tap → bottom sheet expands to that restaurant (basic card)

### Week 4 — Detail screen, polish, ship

- [ ] **T4.1** Restaurant detail UI: photo header, name, today's menu (dishes + price), hours, address, phone
- [ ] **T4.2** "Open in Google Maps" / "Open in Apple Maps" CTA (`expect`/`actual` URL builder)
- [ ] **T4.3** Distance from user calculation (Haversine, shared in `commonMain`)
- [ ] **T4.4** i18n setup: Spanish + English string resources (Compose Multiplatform `stringResource`)
- [ ] **T4.5** App icon, splash screen, basic branding (placeholder OK if needed)
- [ ] **T4.6** Loading states, empty states, error states (no internet, no restaurants in radius)
- [ ] **T4.7** Backend: deploy Spring Boot to a free/cheap host (Railway, Fly.io, or Hetzner — avoid AWS for solo MVP)
- [ ] **T4.8** Configure HTTPS, env vars, API key rotation procedure
- [ ] **T4.9** TestFlight build + Play Store internal testing build
- [ ] **T4.10** Web deploy (Vercel, Netlify, or Cloudflare Pages)
- [ ] **T4.11** Manual QA on all 3 platforms with real Barcelona walking test

### Stretch / cut-first if behind

- [ ] Pin ↔ card sync (swiping cards highlights pins)
- [ ] Catalan translations
- [ ] Custom map style (Mapbox-style aesthetic via Google Maps style JSON)
- [ ] Skeleton loading shimmer on bottom sheet

---

## 10. Decisions Locked

| Question | Decision |
|---|---|
| Primary user (MVP) | Locals first |
| Differentiators | Map UX + menú del día-specific ratings (ratings v2) |
| Monetization (v1) | Free for everyone |
| Architecture | KMP + Compose Multiplatform (iOS, Android, Wasm) |
| Map provider | Google Maps |
| Backend | Spring Boot + Kotlin, PostgreSQL + PostGIS |
| Data sourcing (MVP) | Manual curation by Amit |
| MVP feature scope | Map + bottom sheet + restaurant detail only |
| User accounts | None in MVP |
| Map interaction | Pins clustered, tap pin → bottom sheet |
| Timeline | 1–2 months |
| Team | Solo |
| Geography | Barcelona only |

---

## 11. Open Questions to Resolve Before Coding

1. **Project name + domain.** Do you have one in mind?
2. **Apple Developer account.** Do you already have one, or need to budget the $99/yr?
3. **Hosting budget.** Are you OK paying ~€5–20/mo for backend hosting + Postgres + Maps API?
4. **Restaurant photos.** Do we use stock/placeholder photos for MVP, or shoot/source real ones for the 20–30 curated spots? (Real photos make the app *significantly* more credible — worth one weekend of walking around with a phone.)
5. **Analytics.** Add Firebase Analytics / PostHog from day 1 to measure usage? (Strongly recommended — you'll need this to validate the v2 direction.)
6. **Backup plan if Wasm map fails.** Confirm you're OK with separate Next.js web app as the fallback after the week-1 spike.

---

*End of characterization. Once these open questions are resolved, we can move to detailed sprint planning and start cutting tickets.*