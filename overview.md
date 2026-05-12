# Menú del Día App — Product Characterization & Task List

**Project codename:** TBD
**Author:** Amit
**Date:** May 2026
**Status:** In development — MVP v1

---

## 1. Executive Summary

A mobile-first map app that helps locals (and later, tourists) in Barcelona find restaurants serving **menú del día** — the traditional Spanish fixed-price weekday lunch — near them, right now.

The product competes with `menudia.app` and the `menudeldia.lovable.app` prototype. The bet: **win on map UX and on trustworthy ratings specific to the menú del día itself** (not generic restaurant ratings, which often don't reflect the lunch menu quality).

**MVP target:** ship iOS + Android apps to Barcelona users, with real backend data, search, filters,
user accounts (Google + Apple Sign-In), and a polished map UX. Web platform is deferred to v2.
Ratings/reviews come in v2.

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
- Web platform

**Not in scope for MVP:** restaurants as users (no self-publish flow — we curate manually).

---

## 3. Positioning & Differentiators

The reference apps already cover the basics. We win on two things:

1. **Map UX.** Faster, smoother, more legible than competitors. Pin clustering done right, location
   accuracy that feels native, bottom sheet that doesn't fight the map. Search and filters built in
   from the start.
2. **Menú del día-specific ratings (v2).** Generic Google ratings don't tell you if the lunch is good. We rate the *menu del día itself*: portion size, freshness, value-for-money, dish variety on rotation.

**Out of scope for v1** (deliberately): ratings, reviews, restaurant onboarding, reservations,
payments, web platform. These are v2+.

---

## 4. MVP Scope (v1)

### In scope

- **Map view** with restaurant pins (clustered when zoomed out)
- **User location** dot on the map with smooth follow/recenter
- **Bottom sheet** that surfaces nearby restaurants, expanding to detail when a pin is tapped
- **Restaurant detail screen:** name, address, today's menu (dishes + price), opening hours,
  distance from user, photo, walking directions CTA
- **Search** — by restaurant name, dish/menu item, or cuisine type
- **Filters** — open now, price range, distance, cuisine type
- **Authentication** — Sign in with Google + Sign in with Apple (no email/password in v1)
- **Walking directions** — "Get directions" CTA deep-links to Google Maps / Apple Maps with walking
  mode
- **Two platforms** — iOS + Android (Compose Multiplatform shared UI)
- **Backend** — real restaurant + menu data served from own API (architecture TBD — see open
  questions)
- **Data sourcing** — own backend is primary (menu data, curated info, photos); Google Places API
  fills gaps (missing photos, supplemental hours) server-side only — the app never calls Google
  Places directly
- **Spanish + English UI strings** (Catalan optional but cheap to add)
- **Polished UI/UX** — full pass on map screen, cards, detail screen, theme (see section 9b)

### Out of scope (v2+)

- Web / Wasm platform
- Ratings, reviews, photos uploaded by users
- Favorites / saved restaurants
- Restaurant self-publish portal
- Push notifications
- Reservations / bookings
- Monetization (free for everyone in v1)

---

## 5. User Flow (MVP)

```
App open
  ↓
Login screen (if not authenticated)
  → Sign in with Google or Sign in with Apple
  ↓
Map centers on user (or on Barcelona center if location denied)
  ↓
Pins load from API → cluster when zoomed out → user dot visible on map
  ↓
[Optional] Tap search bar → search by name / dish / cuisine → results highlight on map
[Optional] Tap filter icon → filter panel (open now, price, distance, cuisine) → pins update
  ↓
User taps pin → bottom sheet expands with that restaurant's card
  ↓
User taps card → full restaurant detail (menu, price, hours, photo, distance, phone)
  ↓
Tap "Get directions" → opens Google Maps / Apple Maps in walking mode
  ↓
User backs out → map state preserved
```

**Critical UX details:**

- Pin tap and bottom-sheet card are **synced** — tapping pin highlights card (swiping card to
  highlight pin is v1.1 stretch goal)
- "Recenter on me" button always accessible
- Map should not jank when pins re-cluster on zoom
- Bottom sheet has 3 states: peek (nearest restaurant only), half (list of ~5), full (full detail of selected)
- Pins visually distinct for "open now" vs "no menu today" (greyed out)

---

## 6. Technical Architecture

### Stack

| Layer                | Choice                                       | Notes                                                                       |
|----------------------|----------------------------------------------|-----------------------------------------------------------------------------|
| **Mobile UI**        | Kotlin Multiplatform + Compose Multiplatform | Shared UI across iOS + Android                                              |
| **Backend**          | TBD — Spring Boot + Kotlin **or** Firebase   | See open questions; architecture decision before Week 5                     |
| **Database**         | PostgreSQL + PostGIS **or** Firestore        | Depends on backend choice                                                   |
| **Map (mobile)**     | Google Maps SDK                              | Native on iOS + Android via expect/actual                                   |
| **Places data**      | Google Places API                            | Backend only — enriches DB at seed/curation time; never called from the app |
| **Auth**             | TBD — Firebase Auth **or** custom JWT        | Depends on backend choice; covers Google + Apple Sign-In                    |
| **DI**               | Metro (Zac Sweers)                           | Compile-time DI, full KMP support, no KAPT/KSP needed                       |
| **Networking**       | Ktor Client                                  | KMP-native HTTP                                                             |
| **Serialization**    | kotlinx.serialization                        | KMP-native                                                                  |
| **State management** | Compose state + ViewModel pattern            | Standard Compose Multiplatform                                              |
| **Build / CI**       | Gradle + GitHub Actions                      |                                                                             |

### Data sourcing split

| Data type                                                | Source                                                                                                |
|----------------------------------------------------------|-------------------------------------------------------------------------------------------------------|
| Menu price, dishes (firsts/seconds/desserts), menu notes | **Own backend** — primary                                                                             |
| Restaurant name, address, lat/lng, phone, cuisine type   | **Own backend** — curated manually                                                                    |
| Opening hours                                            | **Own backend** primary; backend fetches from Google Places API at seed/curation time if missing      |
| Photos                                                   | **Own backend** if available; backend fetches from Google Places API at seed/curation time if missing |
| Walking directions                                       | Google Maps / Apple Maps deep-link (no in-app routing)                                                |

### Project structure

```
menu-del-dia/
├── composeApp/                  # Compose Multiplatform app
│   ├── src/
│   │   ├── commonMain/          # Shared UI, view models, navigation, search, filters, auth UI
│   │   ├── androidMain/         # Google Maps Android wrapper, Google Sign-In
│   │   └── iosMain/             # Google Maps iOS wrapper, Sign in with Apple
│   └── build.gradle.kts
├── shared/                      # Shared business logic
│   ├── src/
│   │   ├── commonMain/          # Domain models, repositories, use cases, networking, search/filter logic
│   │   └── ...                  # Platform-specific impls (location, auth, URI launcher)
│   └── build.gradle.kts
├── backend/                     # Spring Boot + Kotlin (or Firebase functions)
│   ├── src/main/kotlin/
│   └── build.gradle.kts
└── iosApp/                      # iOS entry point (Xcode project)
```

### Backend API (v1 endpoints)

| Method | Endpoint                                                                                                               | Purpose                                              |
|--------|------------------------------------------------------------------------------------------------------------------------|------------------------------------------------------|
| `GET`  | `/api/v1/restaurants?lat={lat}&lng={lng}&radius={m}&q={query}&openNow={bool}&cuisine={type}&minPrice={n}&maxPrice={n}` | Nearby restaurants — supports search + filter params |
| `GET`  | `/api/v1/restaurants/{id}`                                                                                             | Restaurant detail with today's menu                  |
| `GET`  | `/api/v1/restaurants/{id}/menu/today`                                                                                  | Today's menu only                                    |
| `POST` | `/api/v1/auth/google`                                                                                                  | Exchange Google ID token for session                 |
| `POST` | `/api/v1/auth/apple`                                                                                                   | Exchange Apple identity token for session            |
| `GET`  | `/api/v1/health`                                                                                                       | Health check                                         |

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
      "cuisineType": "Mediterranean",
      "thumbnailUrl": "https://...",
      "distanceMeters": 350,
      "isOpenNow": true
    }
  ]
}
```

**Response shape (`/restaurants/{id}`):** same fields + `address`, `openingHours`, `phone`,
`dishesToday[]`, `photos[]`, `descriptionEs`, `descriptionEn`, `googlePlaceId`.

### Data model

```
restaurants
  id, name, address, lat, lng, geom (PostGIS Point),
  phone, photo_urls[], description_es, description_en,
  opening_hours (jsonb), cuisine_type, google_place_id,
  created_at, updated_at

menus
  id, restaurant_id, date, price, currency,
  dishes (jsonb: {firsts: [...], seconds: [...], desserts: [...]}),
  notes, created_at

users
  id, external_id (Google/Apple sub), provider (google|apple),
  email, display_name, avatar_url, created_at, last_login

-- v2 tables
-- ratings, reviews, favorites
```

---

## 7. Risks & Mitigations

| Risk                                                           | Impact                              | Mitigation                                                                                         |
|----------------------------------------------------------------|-------------------------------------|----------------------------------------------------------------------------------------------------|
| Backend architecture decision delayed                          | High — auth and deploy depend on it | Decide Spring Boot vs Firebase before week 5.                                                      |
| Google + Apple Sign-In on KMP is non-trivial                   | Medium — expect/actual boilerplate  | Build auth spike early in week 6; use Firebase Auth if it simplifies KMP integration.              |
| Google Places API costs at scale                               | Medium                              | Free tier covers MVP; cap daily requests; own backend is primary so Places calls are rare.         |
| Manual data curation doesn't scale                             | Low for MVP                         | Fine for 20–30 restaurants. Plan for self-publish in v2.                                           |
| Solo developer, extended scope                                 | High                                | Prioritize: map + search + filters first, auth second, full UI polish last. Ship TestFlight early. |
| iOS deployment requires Mac + Apple Developer account ($99/yr) | Low but real                        | Budget the $99. Sign in with Apple also requires a paid account.                                   |

---

## 8. Success Metrics (MVP)

- **Technical:** App ships on iOS + Android. Map loads in <2s on 4G. No crashes in first 50
  sessions. Search returns results in <500ms.
- **Product:** 20+ Barcelona restaurants curated. Map UX feels noticeably smoother than menudia.app.
  Search and filters feel fast and intuitive.
- **Distribution:** TestFlight + Play Store internal testing mid-development (not just at end).
  Public App Store + Google Play submission by end of MVP phase.

---

## 9. Task List

### Week 1 — Foundations ✓

- [x] **T1.1** Set up Gradle project: KMP + Compose Multiplatform (iOS, Android targets)
- [x] **T1.3** Google Maps SDK on Android with custom price+emoji markers
- [x] **T1.4** Google Maps SDK on iOS (`UIKitView` wrapper)
- [x] **T1.5** `expect`/`actual` `MapView` composable in `commonMain`
- [x] **T1.6** Backend project set up, `/health` endpoint
- [ ] **T1.7** Buy domain, set up Google Cloud project, get Maps API keys (Android + iOS — two
  separate keys)
- ~~**T1.2** Wasm spike~~ — *cancelled; web platform moved to v2*

### Week 2 — Core data flow ✓

- [x] **T2.1** Domain models: `Restaurant`, `Menu`, `Dish`, `OpeningHours`
- [x] **T2.2** Mock data: hand-curated Barcelona restaurants (real names, real coords, plausible
  menus)
- [x] **T2.3** `RestaurantRepository` with `MockRestaurantRepository` + `RemoteRepository` (Ktor
  skeleton)
- [x] **T2.4** Backend: Postgres schema, seed from mock data,
  `GET /api/v1/restaurants?lat&lng&radius`
- [x] **T2.5** Backend: `GET /api/v1/restaurants/{id}` and `GET /api/v1/restaurants/{id}/menu/today`
- [x] **T2.6** Metro DI wired: `AppGraph`, `RestaurantRepository`, `HttpClient`
- [x] **T2.7** Top-level navigation scaffold

### Week 3 — Map experience (mostly ✓)

- [x] **T3.1** `MapView` wired to repository — renders live pins from mock data
- [x] **T3.3** User location: `expect`/`actual` `rememberLocationState()` (Android:
  FusedLocationProviderClient; iOS: stub)
- [x] **T3.4** Location permission flow + fallback to Barcelona center (41.3851, 2.1734)
- [x] **T3.5** "Recenter on me" FAB — shown when permission granted, animates camera to user
- [x] **T3.6** Bottom sheet with list/detail states using `BottomSheetScaffold`
- [x] **T3.7** Pin tap → bottom sheet expands to that restaurant
- [ ] **T3.2** Pin clustering (Android: Maps Utils; iOS: `GMUClusterManager`)

### Week 4 — Detail screen & UI/UX polish ✓ (mostly)

- [x] **T4.1** Restaurant detail UI: photo header, name, today's menu (dishes + price), hours,
  address, phone
- [x] **T4.2** Walking directions CTA — `expect`/`actual` `UriLauncher` + `walkingDirectionsUri`
  deep-link to Google Maps (Android: `google.navigation:`) / Apple Maps (iOS:
  `maps.apple.com?dirflg=w`)
- [x] **T4.3** Distance from user (`haversineMeters` in shared util; `MapViewModel` recomputes
  client-side on user-location change)
- [x] **T4.4** i18n setup: Compose Resources `values/strings.xml` (EN default) +
  `values-es/strings.xml`; 25+ user-facing strings extracted
- [x] **T4.5** App icon + adaptive icon (Android) + splash screen — *scaffolding only:*
  `core-splashscreen` lib wired, saffron splash theme, `installSplashScreen()` in `MainActivity`.
  Placeholder grid art retained — drop in real PNGs / vector when ready
- [x] **T4.6** Empty states + error states — `EmptySheetState` (filtered vs no-menus variants with
  appropriate CTAs) + `ErrorState` with Retry button in `MapScreen`
- [~] **T4.7** UI/UX polish pass — most of section 9b done; **U17** (custom typography) and **U19**
  (dark map tiles) deferred as future polish
- [x] **T4.8** iOS location stub → real `CLLocationManager` wrapper in `LocationState.ios.kt`;
  `NSLocationWhenInUseUsageDescription` added to `Info.plist`

### Week 5 — Search & Filters ✓

- [x] **T5.1** Search UI: floating search bar on map screen (`MapSearchBar`), results filtered in
  real-time on map
- [x] **T5.2** Backend: add `q`, `openNow`, `cuisine`, `minPrice`, `maxPrice` query params to
  `/api/v1/restaurants`; `cuisine_type` added to DB schema + seeder
- [x] **T5.3** Search logic in `RestaurantRepository` — `SearchFilterState` +
  `FilterRestaurantsUseCase`
  in `commonMain`; `MapViewModel` refactored to reactive `combine` for live updates
- [x] **T5.4** Filter UI: `FilterPanel` as `ModalBottomSheet` with chips for open now, price range,
  cuisine type
- [x] **T5.5** Filter state in `MapViewModel` — filter params flow reactively into repository calls
- [ ] **T5.6** Add `google_place_id` field to restaurant schema; backend enrichment script fetches
  missing photos/hours from Google Places API and stores them in DB (one-time per restaurant, not
  per request)
- [x] **T5.7** "Open now" visual state on pins — markers reflect `todayHasMenu` and selection state
- [x] **T5.8** Add `cuisineType` field to domain model, DTOs, mock data, and displayed in
  restaurant card
- [ ] **T5.9** Early TestFlight build (iOS) + Play Store internal testing build (Android) — get on
  real devices

### Week 6 — Authentication

- [ ] **T6.1** Decide auth backend: Firebase Auth (simpler KMP integration) vs custom JWT (depends
  on backend architecture choice — resolve by start of this week)
- [ ] **T6.2** Login screen UI: Google Sign-In button + Sign in with Apple button, skip/guest option
- [ ] **T6.3** Android: Google Sign-In SDK integration (Credential Manager API)
- [ ] **T6.4** iOS: Sign in with Apple (native `ASAuthorizationController`) + Google Sign-In iOS SDK
- [ ] **T6.5** `expect`/`actual` `AuthProvider` in `shared` — platform-specific sign-in flows
- [ ] **T6.6** Backend: validate Google ID token + Apple identity token, issue session, `users`
  table
- [ ] **T6.7** Auth state in app — show login screen if unauthenticated, persist session
- [ ] **T6.8** Sign out flow + account screen (minimal: show name/avatar + sign out button)

### Week 7 — Backend deploy & ship

- [ ] **T7.1** Backend: deploy to Railway / Fly.io / Hetzner (or Firebase if using Firebase)
- [ ] **T7.2** HTTPS, env vars, API key rotation, secrets management
- [ ] **T7.3** Connect app to production backend (remove mock data fallback)
- [ ] **T7.4** Manual QA on iOS + Android — golden path + edge cases
- [ ] **T7.5** App Store submission (iOS)
- [ ] **T7.6** Google Play production submission (Android)
- [ ] **T7.7** Domain setup + any web landing page (optional)

### Extra completed (not in original plan)

- [x] Custom Material 3 theme — saffron-orange / terracotta / olive palette, full light + dark mode
- [x] Custom price+emoji map markers (selected state with primary color highlight)
- [x] Animated camera pan to selected restaurant on pin tap
- [x] `MapView` `bottomPadding` parameter — map content padding matches sheet peek height so pins
  aren't hidden behind the bottom sheet
- [x] Status bar + navigation bar inset padding on search bar and FAB (`statusBarsPadding` /
  `navigationBarsPadding`)

### Stretch / cut-first if behind

- [ ] Pin ↔ card sync (swiping cards highlights pins)
- [ ] Catalan translations
- [ ] Custom map style (Google Maps style JSON for dark mode)
- [ ] Skeleton loading shimmer on bottom sheet
- [ ] Saved favorites (requires user account, which is now in scope)

---

## 9b. UI/UX Improvement Checklist

Focused pass to make the app feel polished and production-ready. All items are in scope for v1.
Grouped by screen.

### Map screen

- [x] **U1** "Recenter on me" FAB (bottom-right, above sheet peek) — done (T3.5)
- [x] **U2** Show user location dot on map — iOS now uses real `CLLocationManager` (T4.8)
- [x] **U3** "Open now" visual state on pins — grey out restaurants with no menu today (T5.7)
- [x] **U4** Bottom sheet drag handle — `sheetDragHandle = { BottomSheetDefaults.DragHandle() }`
  passed explicitly in `MapScreen`
- [x] **U5** Bottom sheet peek height — already 160dp in `MapScreen.kt`

### Restaurant card (bottom sheet list)

- [x] **U6** "Abierto / Cerrado" status badge — client-side computed via
  `Restaurant.isCurrentlyOpen()` (uses today's `OpeningHours` window + device clock)
- [x] **U7** Distance from user (e.g. "350m") — `haversineMeters` populated in `MapViewModel`
  (T4.3)
- [x] **U8** Cuisine type label next to price (e.g. "Mediterráneo · €12.50") for scannability (T5.8)
- [x] **U9** "Menú del día" + price row — `titleMedium` price in primary color, label in
  `labelLarge`

### Restaurant detail screen

- [x] **U10** TopAppBar title — restaurant name with ellipsis overflow
- [x] **U11** Hours section — bold today's row + inline "Open · closes 17:00" / "Closed" chip
- [x] **U12** Tappable phone — `tel:` URI via `rememberUriLauncher()`, underlined primary-color
  text
- [x] **U13** Restaurant description — locale-aware (`descriptionEs` for es/ca, `descriptionEn`
  otherwise) with graceful fallback to the other language
- [x] **U14** Replaced dish `AssistChip` with plain `Text` rows inside each section
- [x] **U15** "Get directions" filled button — walking deep-link CTA (T4.2)
- [x] **U16** Photo fallback — branded `🍽` emoji on `primaryContainer` background when
  `thumbnailUrl` is null (applied in detail screen, list card, and bottom-sheet detail card)

### Theme & global polish

- [ ] **U17** Custom typography — deferred (needs Google Font file bundled into
  `composeResources/font/` + Typography config in `MenuTheme`)
- [x] **U18** App icon + adaptive icon (Android) — scaffolding done; placeholder grid art retained
  for future replacement (T4.5)
- [ ] **U19** Dark mode map tiles — deferred (needs Google Maps dark style JSON applied via
  `MapProperties(mapStyleOptions = ...)` on Android + iOS)
- [x] **U20** Empty state screen — "No menús nearby" with recenter CTA + filtered-empty variant
  with "Clear filters" CTA (T4.6)

---

## 10. Decisions Locked

| Question                | Decision                                                                                                                                               |
|-------------------------|--------------------------------------------------------------------------------------------------------------------------------------------------------|
| Primary user (MVP)      | Locals first                                                                                                                                           |
| Differentiators         | Map UX + search/filters + menú del día-specific ratings (ratings v2)                                                                                   |
| Monetization (v1)       | Free for everyone                                                                                                                                      |
| Platforms (MVP)         | iOS + Android only — web deferred to v2                                                                                                                |
| Architecture            | KMP + Compose Multiplatform (iOS, Android)                                                                                                             |
| Map provider            | Google Maps SDK                                                                                                                                        |
| Data sourcing           | Own backend primary; Google Places API fills gaps server-side only — backend enriches DB at seed/curation time, app never calls Google Places directly |
| Navigation (directions) | Deep-link to Google Maps / Apple Maps in walking mode                                                                                                  |
| Search scope            | Restaurant name + dish/menu item + cuisine type                                                                                                        |
| Filters                 | Open now, price range, distance, cuisine type                                                                                                          |
| Authentication          | Sign in with Google + Sign in with Apple (no email/password in v1)                                                                                     |
| App store releases      | Mid-development (week 5), not just at end                                                                                                              |
| Timeline                | ~7 weeks                                                                                                                                               |
| Team                    | Solo                                                                                                                                                   |
| Geography               | Barcelona only                                                                                                                                         |

---

## 11. Open Questions

1. **Backend architecture.** Spring Boot + PostgreSQL or Firebase (Firestore + Firebase Auth + Cloud
   Functions)? This decision unblocks auth implementation (week 6) and deploy (week 7). Need to
   decide before week 5.
2. **Project name + domain.** Do you have one in mind?
3. **Apple Developer account.** Do you already have one, or need to budget the $99/yr? (Required for
   TestFlight + Sign in with Apple.)
4. **Hosting budget.** OK paying ~€5–20/mo for backend + Postgres + Maps API? (Firebase has a
   generous free tier that may cover MVP.)
5. **Restaurant photos.** Stock/placeholder photos for MVP, or shoot real ones for the 20–30 curated
   spots? Real photos make the app significantly more credible.
6. **Analytics.** Add Firebase Analytics / PostHog from day 1? Strongly recommended to validate v2
   direction.

---

*End of characterization.*
