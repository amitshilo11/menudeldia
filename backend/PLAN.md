# Backend — Build Plan

**Codename:** Menú del Día Backend
**Stack:** Spring Boot + Kotlin
**Author:** Amit
**Date:** 2026-05-11
**Status:** Planning

---

## 1. Goals

The backend exists to do three things:

1. **Serve restaurant data** to the iOS + Android client — the curated menú del día list with
   location, photos, hours, descriptions in Spanish + English.
2. **Enrich curated data with Google Places** — fetch photos, opening hours, and metadata once per
   hour per restaurant; cache aggressively to keep Google API spend near zero.
3. **Authenticate users** via Google / Apple Sign-In (auth is optional in v1 — read endpoints are
   public).

Non-goals for v1: ratings, reviews, favorites, restaurant self-publishing, push notifications, web
platform.

---

## 2. Inputs We're Working From

- **`resturant-list.xlsx`** — 112 Barcelona restaurants curated by hand. Columns: `Record#`, `Name`,
  `Cuisine Type` (Hebrew), `Menu Details` (Hebrew), `alternative price` (always empty — ignore),
  `normal price` (€), `Hours / Offer Window` (Hebrew), `Google Maps Link`, `Place ID`, `web`,
  `Phone`.
- **Existing Ktor scaffold** at `/server/` — being replaced. Reference for the API contract and DB
  schema, but not the implementation.
- **Client domain model** at `shared/.../domain/model/Restaurant.kt` — already locked. The API
  response shape must match.

---

## 3. Locked Architectural Decisions

| Area              | Decision                                                                                                 | Reasoning                                                             |
|-------------------|----------------------------------------------------------------------------------------------------------|-----------------------------------------------------------------------|
| **Server stack**  | Spring Boot + Kotlin (replaces Ktor)                                                                     | User preference; Spring ecosystem (Security, JPA, Validation) is rich |
| **Data layer**    | Spring Data JPA + Hibernate                                                                              | Idiomatic Spring Boot; JSONB via `@JdbcTypeCode(SqlTypes.JSON)`       |
| **Database**      | PostgreSQL 16 + PostGIS extension                                                                        | Real geo queries, `ST_DWithin` + GiST index; future-proof             |
| **Geo strategy**  | PostGIS `geography(Point)` column with GiST index                                                        | Sub-millisecond `nearby` queries even at 100k+ rows                   |
| **Auth backend**  | Spring Security + custom filter that verifies Google/Apple ID tokens, issues our own session JWT (HS256) | No Firebase dependency; ~150 LOC; standard pattern                    |
| **Auth scope**    | Read-public, auth optional for v1                                                                        | No login friction for first-time users; auth gates v2 features only   |
| **Photo storage** | Local filesystem (`/data/photos/{restaurantId}/{n}.jpg`), served via `GET /restaurants/{id}/photos/{n}`  | Simplest; fits VPS deploy; migrate to S3 in v2 if needed              |
| **Google cache**  | Postgres column `places_fetched_at TIMESTAMPTZ` per restaurant; refresh on read if older than 1h         | Single source of truth, survives restart, no extra infra (no Redis)   |
| **Daily menu**    | Same menu every weekday in v1 — no `menus` table                                                         | Store dishes + price + price-includes JSONB on the `restaurants` row  |
| **Translations**  | Offline script (`scripts/translate-seed.main.kts`) runs once, writes `seed.json`, committed to repo      | Predictable, version-controlled, no API key in seeder runtime         |
| **Hosting**       | Hetzner Cloud / DigitalOcean VPS, Docker Compose (Postgres + Spring Boot + Caddy reverse proxy)          | ~€5–15/mo; persistent volumes for photo cache; full control           |
| **Module layout** | New `/backend/` Gradle module; old `/server/` deleted                                                    | Clean slate; no migration churn                                       |
| **Security MVP**  | TLS + HSTS, Bucket4j rate limiting, Bean Validation on DTOs, secrets in env vars                         | Standard OWASP-aligned baseline                                       |

Open data question (non-blocking, decide during phase 1): the xlsx `Menu Details` column ("ראשונה +
עיקרית + שתייה") describes **what's included** in the menu del día (first course + main + drink),
not the actual dishes. We'll model this as `priceIncludes: List<String>` (translated to ES/EN) per
restaurant, and leave actual dish lists empty for v1.

---

## 4. Build Phases

The plan is sequenced so each phase produces a usable state. Each phase ends with a manual smoke
test.

### Phase 1 — Foundation (skeleton + DB + seed)

Goal: Spring Boot app boots, Postgres/PostGIS is provisioned, seed data is loaded from xlsx (with
translated columns), `/health` and `/api/v1/restaurants` return real data.

**Deliverables:**

- `/backend/` module with Spring Boot 3.x + Kotlin
- Docker Compose for local Postgres+PostGIS
- Flyway migrations for schema
- JPA entities + repositories
- One-time `translate-seed.main.kts` script that reads xlsx, calls Anthropic API for ES/EN
  translations, writes `src/main/resources/seed.json`
- `SeederService` that loads `seed.json` on first boot
- `RestaurantController` with `GET /api/v1/restaurants` and `GET /api/v1/restaurants/{id}`
- `GlobalExceptionHandler`

**Done when:** `curl /api/v1/restaurants?lat=41.3851&lng=2.1734&radius=2000` returns the seeded list
with cuisine/price/hours in ES + EN.

### Phase 2 — Google Places enrichment + photo proxy

Goal: For each restaurant with a Place ID, fetch hours, photos, description from Google Places once
per hour. Photos are downloaded to disk and served from our domain.

**Deliverables:**

- `GooglePlacesClient` (Spring `RestClient`) wrapping Place Details + Place Photos APIs
- `PlacesEnrichmentService` — checks `places_fetched_at`, refreshes if older than 1h, persists to DB
- `PhotoStorageService` — downloads photos to `/data/photos/{restaurantId}/{n}.jpg`, returns local
  URL
- `PhotoController` with `GET /api/v1/restaurants/{id}/photos/{n}` (streams file)
- Background-trigger logic: enrichment runs lazily on read (not as a cron) — first request after 1h
  triggers refresh
- API quota safety: respect daily cap; circuit-breaker on Google errors

**Done when:** A fresh DB seed + first API call enriches all restaurants; photos are stored locally
and served via our URL; second call within 1h hits cache (no Google calls).

### Phase 3 — Authentication

Goal: Sign in with Google / Apple from the client; backend verifies the ID token and issues a
session JWT.

**Deliverables:**

- `POST /api/v1/auth/google` — accepts Google ID token, validates against Google's JWKS, upserts
  `users` row, returns `{accessToken, user}`
- `POST /api/v1/auth/apple` — accepts Apple identity token, validates against Apple's JWKS, upserts
  `users` row, returns `{accessToken, user}`
- `JwtAuthFilter` extending `OncePerRequestFilter` — validates our session JWT on `/api/v1/me/**`
  routes (public reads stay open)
- `users` table + `User` entity
- `GET /api/v1/me` — returns current user (auth required)
- `POST /api/v1/auth/logout` — stateless (client deletes token); endpoint for symmetry

**Done when:** Client signs in via Google → receives our JWT → `GET /api/v1/me` returns user with
that token; missing/invalid token returns 401.

### Phase 4 — Hardening + deploy

Goal: Production-ready VPS deploy with HTTPS, rate limiting, monitoring, and CI build.

**Deliverables:**

- Bucket4j rate limit filter (60 req/min per IP on read endpoints)
- Spring Security headers (HSTS, X-Frame-Options, CSP for images)
- Bean Validation (`@Valid`) on all DTOs
- Dockerfile (multi-stage Gradle build)
- `docker-compose.prod.yml` with Caddy (TLS auto-cert) → Spring Boot → Postgres
- `.env.example` documenting all required secrets
- GitHub Actions: build + test on PR
- Structured logging (logback JSON encoder)
- Spring Boot Actuator with `/actuator/health` (only) exposed publicly; rest behind admin token

**Done when:** App runs on a real Hetzner VPS at `https://api.menu-del-dia.example` with valid cert;
mobile client connects and works end-to-end.

---

## 5. Success Criteria (MVP backend)

- **Functional:** All endpoints in section 6 of `/overview.md` return correct data for 112 seeded
  restaurants.
- **Performance:** `/restaurants?lat&lng&radius` p95 < 200ms with photos served from local disk.
- **Cost:** Google Places API spend stays within free tier (caching + lazy enrichment + place IDs
  already known).
- **Security:** HTTPS-only, no secrets in repo, rate limit caps abuse, JWT signed and validated
  correctly.
- **Operational:** Single `docker-compose up -d` brings the whole stack up locally; production
  deploy is one `git push` away (or one manual SSH `docker compose pull && up -d`).

---

## 6. Risks & Mitigations

| Risk                                                        | Impact | Mitigation                                                                                                        |
|-------------------------------------------------------------|--------|-------------------------------------------------------------------------------------------------------------------|
| Hebrew → ES/EN translations are wrong or unidiomatic        | Medium | Translation script outputs JSON that gets manually reviewed before commit; cuisine type maps through a fixed enum |
| Google Places photos URL/policy gotchas                     | Medium | Use the new Places API (not legacy); proxy bytes through our domain so we never expose Google URLs to client      |
| PostGIS extension setup on Hetzner managed Postgres         | Low    | Use `postgis/postgis:16-3.4` Docker image (self-managed); no managed Postgres dependency                          |
| Photo disk fills VPS volume                                 | Low    | Cap to 5 photos × 200KB × 500 restaurants = ~500MB; budget 5GB; alert at 80%                                      |
| Google Sign-In ID token verification (rotating JWKS)        | Medium | Use `nimbus-jose-jwt` with cached JWKS source (Spring's built-in `NimbusJwtDecoder` handles rotation)             |
| First-launch enrichment storm (one user → 112 Google calls) | Medium | Phase 2 implements lazy enrichment with stagger (refresh max 5 stale rows per request) + global concurrency limit |

---

## 7. What's Next

Read `ARCHITECTURE.md` for the technical layout, schema, and module structure.
Read `TASKS.md` for the phased work breakdown with checkable items.

Once phase 1 is shipped, this PLAN.md gets a "Decisions Revisited" section if anything changes.
