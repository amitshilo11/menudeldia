# Design Brief — Menú del Día

Kotlin Multiplatform app (Android + iOS + Web). All in-app assets go into a shared
`composeResources/drawable/` folder that is compiled once for all platforms.

## Format rules

| Asset type                       | Format                        | Why                                                                                                                                                       |
|----------------------------------|-------------------------------|-----------------------------------------------------------------------------------------------------------------------------------------------------------|
| All in-app illustrations & icons | **SVG**                       | Compose Resources compiles SVG natively on Android, iOS, and Web from a single file. Do NOT use PDF or XML vector drawable — those are platform-specific. |
| App launcher icon (Android)      | **PNG** at each density below | Android launcher icon spec                                                                                                                                |
| App launcher icon (iOS)          | **PNG 1024 × 1024 px**        | Xcode generates all required sizes from this single file (no alpha channel)                                                                               |
| Splash / launch screen           | See per-platform notes below  | Different mechanism on each platform                                                                                                                      |

---

## 1. App Icon (launcher)

Used on the home screen and app switcher.

**Provide:**

- Master artwork as SVG (for reference / resizing)
- Android PNG exports:

| Density      | Size         |
|--------------|--------------|
| mdpi (1×)    | 48 × 48 px   |
| hdpi (1.5×)  | 72 × 72 px   |
| xhdpi (2×)   | 96 × 96 px   |
| xxhdpi (3×)  | 144 × 144 px |
| xxxhdpi (4×) | 192 × 192 px |
| Play Store   | 512 × 512 px |

- Android adaptive icon: provide foreground layer only (108 × 108 dp canvas, safe zone 72 × 72 dp in
  the center) as SVG. Background is a solid color — confirm brand color.
- iOS PNG: **1024 × 1024 px**, no rounded corners (iOS clips automatically), **no alpha channel**,
  no transparency.

---

## 2. Splash Screen

**Android (API 31+):**

- Icon shown centred on a white/brand-color background.
- Size: 288 × 288 dp total area; icon artwork should fit within the inner **192 × 192 dp** circle.
- Format: SVG (will be placed in `res/drawable/ic_launcher_foreground.xml` via Android Studio's SVG
  import).
- Background color: confirm hex — currently `#FFE65100` (orange).

**iOS:**

- iOS uses a LaunchScreen storyboard — no separate image file needed if we use a solid color +
  centered logo.
- Provide the logo as SVG (same file as the in-app logo below).

---

## 3. In-App Logo (Login Screen)

Displayed centred on the login/onboarding screen above the "Sign in with Google" button.
Currently a 🍽️ emoji placeholder.

- Format: **SVG**
- Recommended canvas: **200 × 200 dp** (the composable fills available width up to ~280 dp on most
  phones)
- Should work on both light and dark backgrounds (use brand colors, not system colors)

---

## 4. Empty State Illustrations

Shown inside the bottom sheet when the map has no results. Container is full-width, centred content.
The icon circle is **72 × 72 dp** (rounded, `primaryContainer` background color).

Provide **2 distinct illustrations**:

### 4a. No restaurants in this area

- Trigger: user pans the map to a zone with no menús del día
- Suggested concept: empty street / fork & knife with a location pin
- Format: **SVG**, artwork sized to ~**120 × 120 dp** viewBox (will be displayed at 72 dp inside the
  circle, SVG scales)

### 4b. No results match filters

- Trigger: user applies filters (vegan, gluten-free, price, etc.) and nothing matches
- Suggested concept: funnel/filter with an X, or empty plate
- Format: **SVG**, same size as above

---

## 5. Error State Illustration

Shown full-screen when the API call fails (no internet, server error, etc.).
Icon circle is **72 × 72 dp** (`errorContainer` background color).

- One illustration covers both the map screen and detail screen error cases
- Suggested concept: broken wifi / sad fork
- Format: **SVG**, ~**120 × 120 dp** viewBox

---

## 6. Photo Placeholders

Shown while a restaurant photo is loading or when no photo exists.

| Placeholder      | Where used                    | Display size                                          | Format |
|------------------|-------------------------------|-------------------------------------------------------|--------|
| **Thumbnail**    | Restaurant list cards         | **64 × 64 dp**, corner radius 8 dp                    | SVG    |
| **Card photo**   | Map bottom-sheet detail card  | **full width × 220 dp**, with a 100 dp fallback strip | SVG    |
| **Detail photo** | Full restaurant detail screen | **full width × 220 dp**                               | SVG    |

All three can reuse the same SVG (a simple plate/restaurant icon centred on a `primaryContainer`
fill). The SVG just needs to be horizontally scalable (use `preserveAspectRatio="xMidYMid meet"` or
a flexible layout).

---

## 7. Icons (functional UI icons)

All delivered as **SVG**, **24 × 24 dp viewBox** (standard Material size). The app already has
working XML drawables for these — confirm the style matches the brand or replace:

| Icon                   | File name         | Current status                  |
|------------------------|-------------------|---------------------------------|
| Back arrow             | `arrow_back.svg`  | Has placeholder — confirm style |
| Close / dismiss        | `close.svg`       | Has placeholder — confirm style |
| Search                 | `search.svg`      | Has placeholder — confirm style |
| Filter                 | `filter_list.svg` | Has placeholder — confirm style |
| My location / recenter | `my_location.svg` | Has placeholder — confirm style |
| Phone / call           | `phone.svg`       | Has placeholder — confirm style |
| Info                   | `info.svg`        | Has placeholder — confirm style |

---

## 8. Dietary & Status Icons

Currently emoji placeholders in restaurant cards, detail screen, and filter chips.
Displayed at **14–18 dp** inside chips.

| Icon                | Emoji replaced | File name            | Used in                     |
|---------------------|----------------|----------------------|-----------------------------|
| Vegan / plant-based | 🌱             | `ic_vegan.svg`       | Cards, detail, filter chips |
| Gluten-free         | 🌾             | `ic_gluten_free.svg` | Cards, detail, filter chips |
| Open now / clock    | 🕒             | `ic_open_now.svg`    | Filter chips, search bar    |
| Rating star         | ★              | `ic_star.svg`        | Restaurant detail card      |

Format: **SVG**, **24 × 24 dp viewBox** (will be rendered at 14–18 dp, so keep shapes simple).

---

## Summary checklist

| #  | Asset                               | Files                                   | Format    |
|----|-------------------------------------|-----------------------------------------|-----------|
| 1  | App launcher icon                   | 6 PNG sizes + iOS 1024px + adaptive SVG | PNG + SVG |
| 2  | Splash screen icon                  | 1 SVG                                   | SVG       |
| 3  | In-app logo (login)                 | 1 SVG                                   | SVG       |
| 4  | Empty state — no restaurants        | 1 SVG                                   | SVG       |
| 5  | Empty state — no filter results     | 1 SVG                                   | SVG       |
| 6  | Error state illustration            | 1 SVG                                   | SVG       |
| 7  | Photo placeholder (thumbnail 64 dp) | 1 SVG                                   | SVG       |
| 8  | Photo placeholder (wide 220 dp)     | 1 SVG (same file scales)                | SVG       |
| 9  | Functional icons (7 icons)          | 7 SVG                                   | SVG       |
| 10 | Dietary & status icons (4 icons)    | 4 SVG                                   | SVG       |

**Total: ~24 files** (most SVG, launcher icons as PNG)