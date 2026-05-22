# Backend — Task Breakdown

Phased checklist. Each task is a self-contained unit of work that ends with something verifiable.
Reference: `PLAN.md` (strategy) and `ARCHITECTURE.md` (technical).

---

## Phase 0 — Cleanup

- [x] **B0.1** Delete `/server/` Ktor scaffold (Application.kt, db/, routes/, build.gradle.kts) —
  confirm no other module references it
- [x] **B0.2** Update root `settings.gradle.kts` to remove `:server` and add `:backend`
- [x] **B0.3** Decide whether shared module's `RestaurantDto` / `MenuDto` need any DTO-shape changes
  for the new API; if so, list them — don't change yet
  - `priceIncludesEs`/`priceIncludesEn` missing from `RestaurantDto` — add before Phase 1 seed
  - `rating` in DTO but no DB column — stays null until Phase 2 (Google Places)
  - `MenuDto` unused in v1 (no menus table)

---

## Phase 1 — Foundation (skeleton + DB + seed)

### 1.1 — Spring Boot scaffold

- [x] **B1.1.1** Create `/backend/` Gradle module — `build.gradle.kts` with Spring Boot 3.x, Kotlin
  2.x, JPA, Web, Validation, Security, Flyway, Postgres driver, kotlinx-serialization
- [x] **B1.1.2** `MenuDelDiaApplication.kt` main class
- [x] **B1.1.3** `application.yml` + `application-dev.yml` + `application-prod.yml` per ARCHITECTURE
  §9
- [x] **B1.1.4** `.env.example` documenting every env var (no values)
- [x] **B1.1.5** Health controller — `GET /api/v1/health` → `OK`
- [x] **B1.1.6** `GlobalExceptionHandler` — maps validation errors → 400, not-found → 404,
  rate-limit → 429, unexpected → 500
- [x] **B1.1.7** Logback JSON encoder config for structured logs

### 1.2 — Local Postgres + PostGIS

- [x] **B1.2.1** `docker-compose.yml` with `postgis/postgis:16-3.4` image, named volume,
  `POSTGRES_DB=menudeldia`
- [ ] **B1.2.2** Verify `CREATE EXTENSION postgis` works on first boot
- [ ] **B1.2.3** Verify connection from `bootRun` with default `application-dev.yml`

### 1.3 — Schema (Flyway)

- [x] **B1.3.1** `V1__init.sql` — `restaurants` table per ARCHITECTURE §3 (with
  `geom GEOGRAPHY(Point, 4326)` generated column, GiST index, JSONB columns)
- [x] **B1.3.2** `V1__init.sql` — `users` table per §3
- [x] **B1.3.3** Triggers/`@PreUpdate` for `updated_at`
- [ ] **B1.3.4** Verify `./gradlew :backend:bootRun` runs migrations cleanly on empty DB

### 1.4 — Restaurant entity + repo + service + controller

- [x] **B1.4.1** `Restaurant` JPA entity — all columns, JSONB via `@JdbcTypeCode(SqlTypes.JSON)`,
  `geom` mapped read-only
- [x] **B1.4.2** `Cuisine` enum (`SPANISH`, `MEDITERRANEAN`, `ASIAN`, `JAPANESE`, `ITALIAN`,
  `MEXICAN`, `MIDDLE_EASTERN`, `OTHER`) + emoji map
- [x] **B1.4.3** `RestaurantRepository extends JpaRepository<Restaurant, UUID>` + native PostGIS
  query for `findNearby(lat, lng, radius, filters)`
- [x] **B1.4.4** `RestaurantService.findNearby(...)` — orchestrates repo + (later) enrichment
- [x] **B1.4.5** `RestaurantDto` mirroring shared module's wire shape; `RestaurantMapper` (entity →
  DTO, computes `distanceMeters` and `isOpenNow`)
- [x] **B1.4.6** `RestaurantController` — `GET /api/v1/restaurants` with all query params (`lat`,
  `lng`, `radius`, `q`, `openNow`, `cuisine[]`, `minPrice`, `maxPrice`);
  `GET /api/v1/restaurants/{id}`
- [x] **B1.4.7** Bean Validation on query DTO (`@DecimalMin/Max` for lat/lng, `@Max(10000)` for
  radius)

### 1.5 — Translation script + seed

- [x] **B1.5.1** `scripts/translate-seed.main.kts` — reads xlsx via Apache POI, calls Anthropic API
  for ES + EN translations of (cuisine_type, menu_details, hours), writes `seed.json`
- [x] **B1.5.2** Hebrew → `Cuisine` enum mapping table inside the script (manual + curated)
- [x] **B1.5.3** Parse `Hours / Offer Window` Hebrew → structured `weekday_hours` JSON (
  `{mon: "12:30-16:00", ...}`); fall back to "Mon–Fri lunch" defaults if unparseable
- [ ] **B1.5.4** Run script once, manually review `seed.json`, commit
- [x] **B1.5.5** `SeederService` — on `ApplicationReadyEvent`, if `restaurants` table is empty, load
  `seed.json` and persist
- [ ] **B1.5.6** Smoke: `curl /api/v1/restaurants?lat=41.3851&lng=2.1734&radius=5000` returns ≥100
  rows with ES + EN fields populated

---

## Phase 2 — Google Places enrichment + photo proxy

### 2.1 — Places client

- [x] **B2.1.1** `places/GooglePlacesClient` using Spring `RestClient` — wraps Place Details (new
  API v1) and Place Photos endpoints
- [x] **B2.1.2** Field mask config — request only fields we use (
  `location, photos, regularOpeningHours, formattedAddress, internationalPhoneNumber, websiteUri`)
- [x] **B2.1.3** Error handling — wrap 4xx/5xx into typed exceptions; circuit-breaker on repeated
  failures (Resilience4j)

### 2.2 — Enrichment service

- [x] **B2.2.1** `PlacesEnrichmentService.refreshIfStale(rows)` — picks up to 5 stalest rows where
  `places_fetched_at` is null or older than 1h, refreshes in parallel with semaphore
- [x] **B2.2.2** Per-row dedupe map (Caffeine, 60s TTL) — prevents two concurrent requests from
  refreshing the same row
- [x] **B2.2.3** Wire enrichment into `RestaurantService.findNearby()` — call before mapping to DTO

### 2.3 — Photo storage + serving

- [x] **B2.3.1** `PhotoStorageService.downloadPhotos(placeId, photoNames)` — downloads to
  `/data/photos/{restaurantId}/{n}.jpg`, idempotent (skip if file exists for current
  `places_fetched_at`)
- [x] **B2.3.2** `PhotoController` — `GET /api/v1/restaurants/{id}/photos/{n}` streams file with
  `Cache-Control: public, max-age=86400, immutable` and ETag
- [x] **B2.3.3** Mapper updates `thumbnailUrl` and `photos[]` to point at our endpoint URLs (never
  Google)
- [x] **B2.3.4** Disk usage cap — alert (log at WARN) if `/data/photos` exceeds 4GB

### 2.4 — Verification

- [ ] **B2.4.1** Test: drop the DB, seed, hit `/restaurants` once → all rows enriched, photos on
  disk
- [ ] **B2.4.2** Test: hit `/restaurants` twice within 1h → second call makes zero Google API
  requests
- [ ] **B2.4.3** Test: simulate Google 429 → API still responds with stale data, no 5xx propagated
  to client

---

## Phase 3 — Authentication

### 3.1 — Spring Security baseline

- [x] **B3.1.1** `SecurityFilterChain` bean — public for
  `/api/v1/{health, restaurants/**, auth/**}`, authenticated for `/api/v1/me/**`
- [x] **B3.1.2** CORS config — allow client domain(s) from env var, methods GET/POST, headers
  Content-Type + Authorization
- [x] **B3.1.3** CSRF disabled (we're a stateless JSON API with bearer tokens)

### 3.2 — JWT issuance + filter

- [x] **B3.2.1** `JwtService.issue(userId)` and `verify(token)` — HS256, 30-day expiry, signing key
  from env
- [x] **B3.2.2** `JwtAuthFilter extends OncePerRequestFilter` — extracts `Authorization: Bearer …`,
  validates, populates `SecurityContext`
- [x] **B3.2.3** 401 response shape `{ "error": "unauthorized", "message": "..." }`

### 3.3 — Google + Apple sign-in

- [x] **B3.3.1** Add `google-api-client` + `nimbus-jose-jwt` dependencies
- [x] **B3.3.2** `GoogleIdTokenVerifierBean` — verifier with our OAuth client ID as audience
- [x] **B3.3.3** `AppleIdTokenVerifier` — uses `nimbus-jose-jwt` with Apple's JWKS URL (
  `https://appleid.apple.com/auth/keys`)
- [x] **B3.3.4** `User` entity + `UserRepository`
- [x] **B3.3.5** `UserService.upsertFromIdToken(provider, claims)` — creates or updates user, sets
  `last_login`
- [x] **B3.3.6** `AuthController` — `POST /api/v1/auth/google` and `POST /api/v1/auth/apple`, both
  return `{accessToken, user}`
- [x] **B3.3.7** `MeController` — `GET /api/v1/me` returns the authenticated user
- [x] **B3.3.8** Bucket4j: stricter limit on `/auth/*` (10/min per IP)

### 3.4 — Verification

- [x] **B3.4.1** Test with a real Google ID token (via Google's playground) → receive our JWT →
  `/me` returns user
- [x] **B3.4.2** Test invalid/expired token → 401
- [x] **B3.4.3** Test missing Bearer header on `/me` → 401

---

## Phase 4 — Hardening + deploy

### 4.1 — Production hardening

- [x] **B4.1.1** Bucket4j rate limit filter on read endpoints (60/min/IP)
- [x] **B4.1.2** HTTP security headers (`HSTS`, `X-Frame-Options`, `X-Content-Type-Options`, CSP
  allowing image-src self)
- [x] **B4.1.3** Spring Boot Actuator — expose only `/actuator/health` publicly; rest behind
  `X-Admin-Token` header
- [x] **B4.1.4** Application metrics — Micrometer + Prometheus endpoint (admin-only)
- [ ] **B4.1.5** Verify no secrets in committed files — `git secrets` or `truffleHog` in CI

### 4.2 — Containerization

- [x] **B4.2.1** `Dockerfile` — multi-stage Gradle build, JRE-21 slim runtime, non-root user
- [x] **B4.2.2** `docker-compose.prod.yml` — Caddy (TLS) → Spring Boot (port 8080) →
  Postgres+PostGIS, named volumes for `pgdata` and `photos`
- [x] **B4.2.3** Caddyfile — auto-issues Let's Encrypt cert, proxies to backend, gzip, basic
  security headers
- [x] **B4.2.4** Health-check in compose — `wget -qO- http://localhost:8080/api/v1/health`

### 4.3 — CI

- [ ] **B4.3.1** GitHub Actions workflow — on PR: `./gradlew :backend:check` (test + lint +
  dependency-check)
- [ ] **B4.3.2** On push to `main`: build + push Docker image to GHCR
- [ ] **B4.3.3** Deploy step is manual for v1 — SSH to VPS, `docker compose pull && up -d` (document
  in `docs/deploy.md` later)

### 4.4 — VPS deploy

- [ ] **B4.4.1** Provision Hetzner CX22 (or DO equivalent) — Docker preinstalled image
- [ ] **B4.4.2** Configure DNS — `api.menu-del-dia.example` → VPS IP
- [ ] **B4.4.3** SSH, clone deploy directory, `cp .env.example .env`, fill secrets,
  `docker compose -f docker-compose.prod.yml up -d`
- [ ] **B4.4.4** Verify HTTPS endpoint live: `curl https://api.menu-del-dia.example/api/v1/health` →
  `OK`
- [ ] **B4.4.5** Connect mobile client to prod backend, smoke-test golden path

### 4.5 — Operational basics

- [ ] **B4.5.1** Log shipping — for v1, just `docker logs` is fine; document log paths
- [ ] **B4.5.2** DB backup — nightly `pg_dump` cron to a separate volume; document restore procedure
- [ ] **B4.5.3** Photos volume backup — weekly `tar` to remote storage (or skip — they re-fetch from
  Google)
- [ ] **B4.5.4** Uptime monitoring — UptimeRobot or BetterStack on `/api/v1/health` (free tier)

---

## Cut-First If Behind

If timeline slips, cut these without blocking MVP launch:

- Apple Sign-In (ship with Google only; Apple is App Store policy requirement, so it must come back
  before submission)
- Rate limiting (Bucket4j) — defer; abuse is unlikely at MVP scale
- Photo proxy — fall back to surfacing Google photo URLs directly, accept the cost
- PostGIS — fall back to bounding-box + in-app Haversine

## Stretch Goals

- Redis cache layer for hot reads (only if profiling shows DB bottleneck)
- WebFlux migration if we hit async-IO limits (unlikely at MVP scale)
- Multi-language descriptions beyond ES/EN (Catalan)
- Per-restaurant push notifications when menu posted (v2)
