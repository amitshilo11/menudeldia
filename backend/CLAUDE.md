# CLAUDE.md — backend

Architectural context for Claude Code working in `backend/`. Root-level rules and the multi-module
overview live in `/CLAUDE.md`.

## Stack

Spring Boot 3.x + Kotlin (Java 21) · Spring Data JPA + Hibernate · PostgreSQL 16 + PostGIS 3.4 ·
Flyway migrations · Spring Security + custom JWT filter · Bucket4j rate limiting · Resilience4j
circuit breaker · Caffeine cache · Bean Validation.

## Commands

```bash
# Boot (dev profile reads backend/.env)
./gradlew :backend:bootRun -Pprofiles=dev

# Tests
./gradlew :backend:test
./gradlew :backend:test --tests "com.menudeldia.auth.JwtServiceTest"

# Local Postgres+PostGIS only
docker compose -f backend/docker-compose.yml up -d db

# Smoke
curl 'http://localhost:8080/api/v1/restaurants?lat=41.3851&lng=2.1734&radius=2000' | jq
```

## Package Layout (package-by-feature)

```
com.menudeldia/
├── MenuDelDiaApplication.kt
├── config/      # Beans, security, CORS, AppProperties, AdminTokenAuthorizationManager
├── auth/        # JwtService + JwtAuthFilter, Google/Apple verifiers, AuthController, MeController
├── restaurant/  # Entity, repository (native PostGIS), service, controller, DTO, mapper
├── places/      # GooglePlacesClient (RestClient), PlacesEnrichmentService
├── photo/       # PhotoController — streams cached photo bytes
├── admin/       # AdminController (admin-token: enrich, CSV upload/sync)
├── geo/         # GeoUtils (distance math beyond PostGIS)
└── common/      # Errors, GlobalExceptionHandler, rate limit, web config
```

A feature package owns its entity, repository, service, controller, DTO, and mapper. Don't split
by layer across packages.

## Layered Rules within a Feature

- **Controllers** validate input (`@Valid` on DTOs), map DTO ↔ domain, return `ResponseEntity`.
  No business logic.
- **Services** own business logic. They never see DTOs and never touch HTTP.
- **Repositories** are JPA-only — `JpaRepository` + native queries for PostGIS. No conditional
  filtering in code that belongs in the query.
- **Entities** model the DB only. Never leak through controllers — always map to a DTO.
- **DTOs** are versioned per API version (`v1`). Renaming an entity field never breaks the wire.

## Database — PostgreSQL + PostGIS

Flyway owns the schema (`src/main/resources/db/migration/V<n>__*.sql`); Hibernate runs with
`ddl-auto=validate`. **Never rename or edit a committed migration** — always add a new `V<n>__`
file.

### `restaurants` (key columns)

| Column                  | Type                        | Notes                                               |
|-------------------------|-----------------------------|-----------------------------------------------------|
| `id`                    | `UUID PRIMARY KEY`          |                                                     |
| `lat`, `lng`            | `DOUBLE PRECISION NOT NULL` |                                                     |
| `geom`                  | `GEOGRAPHY(Point, 4326)`    | Generated from lat/lng; GiST `idx_restaurants_geom` |
| `google_place_id`       | `TEXT UNIQUE`               |                                                     |
| `cuisine_type`          | `TEXT`                      | Mapped to `Cuisine` enum (see below)                |
| `description_es/_en`    | `TEXT`                      | Translated at seed time (Anthropic) or from Places  |
| `menu_price`            | `NUMERIC(6,2)`              | EUR                                                 |
| `price_includes_es/_en` | `JSONB`                     | `["Primero","Segundo","Bebida"]`                    |
| `weekday_hours`         | `JSONB`                     | Mon–Fri: `{"mon":"12:30-16:00",...}`                |
| `opening_hours`         | `JSONB`                     | Full week from Places (detail screen)               |
| `places_fetched_at`     | `TIMESTAMPTZ`               | Last enrichment; NULL = never. BTREE-indexed        |

JSONB columns are bound on the entity via `@JdbcTypeCode(SqlTypes.JSON)`.

`Cuisine` enum: `SPANISH, MEDITERRANEAN, ASIAN, JAPANESE, ITALIAN, MEXICAN, MIDDLE_EASTERN, OTHER`
(plus per-cuisine emoji map used on map markers).

### `users`

`provider` ∈ {`google`, `apple`} · `external_id` = ID token `sub` claim · UNIQUE
`(provider, external_id)`. Email may be NULL (Apple private relay).

**Why no `menus` table:** the menú del día is the same Mon–Fri per restaurant in v1.

## API Surface (v1)

All routes under `/api/v1/`. JWT-protected unless marked otherwise.

| Method | Path                            | Auth        |
|--------|---------------------------------|-------------|
| `GET`  | `/actuator/health`              | public      |
| `POST` | `/auth/google` · `/auth/apple`  | public      |
| `GET`  | `/me`                           | JWT         |
| `GET`  | `/restaurants`                  | JWT         |
| `GET`  | `/restaurants/{id}`             | JWT         |
| `GET`  | `/restaurants/{id}/photos/{n}`  | JWT         |
| `POST` | `/admin/enrich`                 | admin token |
| `POST` | `/admin/restaurants/sync-csv`   | admin token |

**`/restaurants` query params:** `lat` (req), `lng` (req), `radius` (m, default 2000, max 10000),
`q` (ILIKE on name/cuisine), `openNow`, `cuisine[]`, `minPrice`, `maxPrice`.

## Request Flow — `GET /restaurants`

```
RateLimitFilter (Bucket4j 60/min/IP) → SecurityFilterChain → RestaurantController
  → RestaurantService.findNearby(...)
       ├── PlacesEnrichmentService.refreshIfStale(rows, max=5)   # lazy, in-line
       └── RestaurantRepository.findNearby(...)                   # ST_DWithin(geom, point, radius)
  → RestaurantMapper.toDto()  # adds distanceMeters + isOpenNow (Europe/Madrid clock)
  → 200 { restaurants: [...] }
```

Invariants:

- Geo filtering happens in the DB query — never in memory.
- `isOpenNow` uses the server's `Europe/Madrid` clock, not the client's.
- Photo URLs in responses point at our `/photos/{n}` endpoint — never at Google.

## Google Places Enrichment

Triggered lazily on `GET /restaurants` when result-set rows have
`places_fetched_at < now() - INTERVAL '1 hour'` or NULL.

- Uses the new Places API v1 (`places.googleapis.com/v1/places/{id}`) via Spring `RestClient`,
  with `X-Goog-FieldMask` =
  `location,photos,regularOpeningHours,formattedAddress,internationalPhoneNumber,websiteUri`.
- Per request: refresh **at most 5 stalest rows** in parallel under a semaphore.
- Caffeine 60s dedupe map prevents two concurrent requests refreshing the same row.
- Resilience4j circuit-breaker on Google failures — serve stale rows rather than 5xx.
- Photos downloaded to `${PHOTOS_DIR}/{restaurantId}/{n}.jpg`. `PhotoController` serves with
  `Cache-Control: public, max-age=86400, immutable` and ETag = `places_fetched_at + n`.

## Authentication

`POST /auth/{google|apple}` accepts an ID token, verifies via Google JWKS (`google-api-client`) or
Apple JWKS (`nimbus-jose-jwt`), upserts the `users` row, returns
`{ accessToken: <our HS256 JWT>, user }`. JWT lifetime: 30 days.

`JwtAuthFilter extends OncePerRequestFilter` validates `Authorization: Bearer …` and populates
`SecurityContext`. Public routes: `/api/v1/auth/**`, `/actuator/health`. Admin routes additionally
require `X-Admin-Token` (validated by `AdminTokenAuthorizationManager`).

**Why our own JWT instead of forwarding Google/Apple tokens:** Apple ID tokens are ~10 min lived;
making the client refresh on every request is brittle. Our JWT is the only thing that grants API
access.

## Security Baseline

- TLS terminated at Caddy reverse proxy (auto Let's Encrypt). HSTS, X-Content-Type-Options,
  X-Frame-Options, CSP via Spring config.
- CORS allows the configured client origin from env; never `*` in prod.
- Bean Validation (`@Valid`, `@NotNull`, `@Size`, `@DecimalMin`, `@Max`) on all DTOs.
- Bucket4j: 60 req/min/IP on reads, 10 req/min/IP on `/auth/*`.
- Never log tokens, secrets, or full request bodies.
- Actuator: only `/actuator/health` is public; the rest is admin-token gated.

## Configuration

Env vars (documented in `.env.example`):

| Var                                  | Purpose                                                |
|--------------------------------------|--------------------------------------------------------|
| `DB_URL` / `DB_USER` / `DB_PASSWORD` | Postgres connection                                    |
| `GOOGLE_PLACES_API_KEY`              | Places API requests                                    |
| `GOOGLE_OAUTH_CLIENT_ID`             | Audience for Google ID token verification              |
| `APPLE_BUNDLE_ID`                    | Audience for Apple ID token verification               |
| `JWT_SIGNING_KEY`                    | Hex-encoded HS256 key (`openssl rand -hex 32`)         |
| `PHOTOS_DIR`                         | Local photo storage root (default `./var/photos`)      |
| `ADMIN_TOKEN`                        | Required on `X-Admin-Token` header for admin endpoints |

`application.yml` exposes these under `menudeldia.*` (e.g.
`menudeldia.google.places-cache-ttl=PT1H`,
`menudeldia.google.places-refresh-batch-size=5`, `menudeldia.auth.jwt-ttl=P30D`).

Profiles: `dev` (local Docker Compose), `test` (Testcontainers), `prod` (no defaults for secrets).

## Testing

Tests use **Testcontainers** to stand up Postgres+PostGIS per test class — no shared mutable state.
Mock the Google Places HTTP layer at the `RestClient` level.

## Bootstrap

The DB starts empty. Populate by uploading `restaurants_db_ready.csv` via the admin portal
(Settings → Import CSV), then run **Enrich** and **Find Place IDs** to fill coordinates and photos.

## Non-goals (v1)

Ratings · reviews · favorites · restaurant self-publishing · push notifications · multi-day menu
modeling · S3 photo storage.
