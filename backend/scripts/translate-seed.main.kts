#!/usr/bin/env kotlin
// One-time script: read resturant-list.xlsx, translate Hebrew columns to ES + EN via Anthropic API,
// write src/main/resources/seed.json. Run manually; commit the resulting JSON.
//
// Usage:
//   kotlin scripts/translate-seed.main.kts \
//     --xlsx ../resturant-list.xlsx \
//     --out src/main/resources/seed.json \
//     --anthropic-key $ANTHROPIC_API_KEY
//
// TODO B1.5.1: declare @file:DependsOn for Apache POI and an HTTP client (kotlinx-serialization
//              + java.net.http.HttpClient is enough — no extra deps needed for HTTP).
// TODO B1.5.1: parse argv (xlsx path, output path, API key); fail loudly if missing.
// TODO B1.5.1: open xlsx via Apache POI WorkbookFactory; iterate rows of Sheet1.
// TODO B1.5.2: maintain a Hebrew->Cuisine enum mapping table; warn on unknown values.
// TODO B1.5.3: regex-parse hours strings ("שני עד שישי 12:30-16:00") into weekday_hours JSON.
// TODO B1.5.1: per-row, call Anthropic /v1/messages with claude-haiku-4-5-20251001 to translate
//              cuisine_type + menu_details + free-text hours notes -> {es, en}.
//              Cache results by SHA-256 of source string to avoid re-spending on reruns.
// TODO B1.5.4: emit JSON Array<SeedRecord> matching com.menudeldia.seed.SeedRecord shape.
//              Format with two-space indent so diffs are reviewable.

fun main() {
    error("translate-seed.main.kts is a stub — implement during task B1.5.1")
}

main()
