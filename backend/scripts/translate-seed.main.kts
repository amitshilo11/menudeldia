#!/usr/bin/env kotlin
// One-time script: read resturant-list.xlsx, translate Hebrew columns to ES + EN via Anthropic API,
// write src/main/resources/seed.json. Run manually; commit the resulting JSON.
//
// Usage:
//   kotlin scripts/translate-seed.main.kts \
//     --xlsx ../resturant-list.xlsx \
//     --out src/main/resources/seed.json \
//     --anthropic-key $ANTHROPIC_API_KEY

@file:DependsOn("org.apache.poi:poi-ooxml:5.3.0")
@file:DependsOn("org.slf4j:slf4j-simple:2.0.13")

import org.apache.poi.ss.usermodel.CellType
import org.apache.poi.ss.usermodel.Row
import org.apache.poi.xssf.usermodel.XSSFWorkbook
import java.io.File
import java.io.FileInputStream
import java.math.BigDecimal
import java.net.URI
import java.net.http.HttpClient
import java.net.http.HttpRequest
import java.net.http.HttpResponse
import java.security.MessageDigest

// ---------------------------------------------------------------------------
// Argument parsing
// ---------------------------------------------------------------------------

val args = args.toList()
fun arg(name: String): String? {
    val idx = args.indexOf("--$name")
    return if (idx >= 0 && idx + 1 < args.size) args[idx + 1] else null
}

val xlsxPath = arg("xlsx") ?: error("--xlsx <path> is required")
val outPath = arg("out") ?: error("--out <path> is required")
val anthropicKey = arg("anthropic-key") ?: error("--anthropic-key <key> is required")

// ---------------------------------------------------------------------------
// Hebrew → Cuisine enum mapping (manual, curated against actual xlsx data)
// ---------------------------------------------------------------------------

val hebrewToCuisine: Map<String, String> = mapOf(
    "ספרדי" to "SPANISH",
    "ספרדית" to "SPANISH",
    "מסעדה ספרדית" to "SPANISH",
    "ים תיכוני" to "MEDITERRANEAN",
    "ים-תיכוני" to "MEDITERRANEAN",
    "מדיטרני" to "MEDITERRANEAN",
    "אסיאתי" to "ASIAN",
    "אסיאתית" to "ASIAN",
    "יפני" to "JAPANESE",
    "יפנית" to "JAPANESE",
    "סושי" to "JAPANESE",
    "איטלקי" to "ITALIAN",
    "איטלקית" to "ITALIAN",
    "פיצה" to "ITALIAN",
    "מקסיקני" to "MEXICAN",
    "מקסיקנית" to "MEXICAN",
    "מזרח תיכוני" to "MIDDLE_EASTERN",
    "ערבי" to "MIDDLE_EASTERN",
    "הודי" to "INDIAN",
    "הודית" to "INDIAN",
    "בורגר" to "OTHER",
    "אמריקאי" to "OTHER",
    "בשרי" to "SPANISH",
    "דגים" to "MEDITERRANEAN",
    "פירות ים" to "MEDITERRANEAN",
    "קטלאני" to "SPANISH",
    "קטלאנית" to "SPANISH",
    "אירופאי" to "MEDITERRANEAN",
    "עולמי" to "OTHER",
    "מטבח עולמי" to "OTHER",
    "פיוז'ן" to "OTHER",
    "צמחוני" to "MEDITERRANEAN",
    "טפאס" to "SPANISH",
)

val cuisineEmojis: Map<String, String> = mapOf(
    "SPANISH" to "🥘",
    "MEDITERRANEAN" to "🫒",
    "ASIAN" to "🥢",
    "JAPANESE" to "🍣",
    "ITALIAN" to "🍝",
    "MEXICAN" to "🌮",
    "MIDDLE_EASTERN" to "🥙",
    "INDIAN" to "🍛",
    "OTHER" to "🍽️",
)

// ---------------------------------------------------------------------------
// Hebrew hours → weekday_hours JSON
// ---------------------------------------------------------------------------

// Hebrew weekday name → key
val hebrewDays: Map<String, String> = mapOf(
    "ראשון" to "sun",
    "שני" to "mon",
    "שלישי" to "tue",
    "רביעי" to "wed",
    "חמישי" to "thu",
    "שישי" to "fri",
    "שבת" to "sat",
)

val allWeekdays = listOf("mon", "tue", "wed", "thu", "fri")

fun parseHours(hebrewHours: String): Map<String, String> {
    if (hebrewHours.isBlank()) return defaultWeekdayHours()

    // Extract time range: "HH:MM-HH:MM" or "HH:MM–HH:MM" or "HH:MM - HH:MM"
    val timeRangeRegex = Regex("""(\d{1,2}:\d{2})\s*[-–]\s*(\d{1,2}:\d{2})""")
    val timeMatch = timeRangeRegex.find(hebrewHours) ?: return defaultWeekdayHours()
    val range = "${timeMatch.groupValues[1]}-${timeMatch.groupValues[2]}"

    // "שני עד שישי" = Mon-Fri
    if (hebrewHours.contains("שני") && hebrewHours.contains("שישי")) {
        return allWeekdays.associateWith { range }
    }

    // Single day names
    val days = hebrewDays.entries
        .filter { (he, _) -> hebrewHours.contains(he) }
        .map { it.value }
        .filter { it in allWeekdays }

    return if (days.isNotEmpty()) days.associateWith { range } else defaultWeekdayHours()
}

fun defaultWeekdayHours(): Map<String, String> =
    allWeekdays.associateWith { "13:00-16:00" }

// ---------------------------------------------------------------------------
// Anthropic translation (Hebrew text → ES + EN)
// ---------------------------------------------------------------------------

val http = HttpClient.newHttpClient()

// SHA-256 cache to avoid re-calling on reruns with the same source text
val translationCache = mutableMapOf<String, Pair<String, String>>()

fun sha256(s: String): String =
    MessageDigest.getInstance("SHA-256").digest(s.toByteArray())
        .joinToString("") { "%02x".format(it) }

fun translate(hebrewText: String, context: String): Pair<String, String> {
    val key = sha256("$context:$hebrewText")
    translationCache[key]?.let { return it }

    val prompt = """
        Translate the following Hebrew text into Spanish and English.
        Context: $context (for a Barcelona restaurant menu).
        Return ONLY valid JSON in this exact format: {"es":"<Spanish>","en":"<English>"}
        Hebrew text: $hebrewText
    """.trimIndent()

    val body = """
        {
          "model": "claude-haiku-4-5-20251001",
          "max_tokens": 200,
          "messages": [{"role": "user", "content": ${jsonString(prompt)}}]
        }
    """.trimIndent()

    val request = HttpRequest.newBuilder()
        .uri(URI.create("https://api.anthropic.com/v1/messages"))
        .header("x-api-key", anthropicKey)
        .header("anthropic-version", "2023-06-01")
        .header("content-type", "application/json")
        .POST(HttpRequest.BodyPublishers.ofString(body))
        .build()

    val response = http.send(request, HttpResponse.BodyHandlers.ofString())
    if (response.statusCode() != 200) {
        System.err.println("Anthropic API error ${response.statusCode()}: ${response.body()}")
        return hebrewText to hebrewText
    }

    // Extract JSON from {"es":"...","en":"..."} in the response content
    val jsonRegex = Regex(""""es"\s*:\s*"([^"]+)"\s*,\s*"en"\s*:\s*"([^"]+)"""")
    val match = jsonRegex.find(response.body()) ?: run {
        System.err.println("Could not parse translation response: ${response.body()}")
        return hebrewText to hebrewText
    }

    val result = match.groupValues[1] to match.groupValues[2]
    translationCache[key] = result
    return result
}

// Translate comma-separated items (e.g. "ראשונה + עיקרית + שתייה")
fun translateIncludes(hebrewText: String): Pair<List<String>, List<String>> {
    if (hebrewText.isBlank()) return emptyList<String>() to emptyList()

    val items = hebrewText.split(Regex("[+,]")).map { it.trim() }.filter { it.isNotBlank() }
    val esItems = mutableListOf<String>()
    val enItems = mutableListOf<String>()

    for (item in items) {
        val (es, en) = translate(item, "menu course or item included in the fixed lunch menu")
        esItems += es
        enItems += en
    }
    return esItems to enItems
}

fun jsonString(s: String): String {
    val sb = StringBuilder("\"")
    for (c in s) when (c) {
        '"' -> sb.append("\\\"")
        '\\' -> sb.append("\\\\")
        '\n' -> sb.append("\\n")
        '\r' -> sb.append("\\r")
        '\t' -> sb.append("\\t")
        else -> sb.append(c)
    }
    sb.append("\"")
    return sb.toString()
}

// ---------------------------------------------------------------------------
// Excel parsing
// ---------------------------------------------------------------------------

fun Row.cell(idx: Int): String {
    val cell = getCell(idx) ?: return ""
    return when (cell.cellType) {
        CellType.STRING -> cell.stringCellValue.trim()
        CellType.NUMERIC -> {
            val d = cell.numericCellValue
            if (d == kotlin.math.floor(d)) d.toLong().toString() else d.toString()
        }

        CellType.BOOLEAN -> cell.booleanCellValue.toString()
        CellType.FORMULA -> cell.cachedFormulaResultType.let { _ ->
            runCatching { cell.stringCellValue.trim() }.getOrElse { cell.numericCellValue.toString() }
        }

        else -> ""
    }
}

// Extract Place ID from Google Maps URL if not in the dedicated column
fun extractPlaceId(mapsUrl: String, directPlaceId: String): String? {
    if (directPlaceId.isNotBlank()) return directPlaceId
    val match = Regex("""place_id=([^&]+)""").find(mapsUrl)
    return match?.groupValues?.get(1)
}

// ---------------------------------------------------------------------------
// Main
// ---------------------------------------------------------------------------

println("Reading $xlsxPath ...")
val workbook = XSSFWorkbook(FileInputStream(File(xlsxPath)))
val sheet = workbook.getSheetAt(0)

// Column indices (0-based). Based on xlsx layout described in PLAN.md:
// 0=Record#, 1=Name, 2=CuisineType(HE), 3=MenuDetails(HE),
// 4=alt price (skip), 5=normal price, 6=Hours(HE), 7=GoogleMapsLink, 8=PlaceID, 9=web, 10=Phone
val COL_NAME = 1
val COL_CUISINE = 2
val COL_MENU_DETAILS = 3
val COL_PRICE = 5
val COL_HOURS = 6
val COL_MAPS_LINK = 7
val COL_PLACE_ID = 8
val COL_WEB = 9
val COL_PHONE = 10

data class SeedRecord(
    val name: String,
    val googlePlaceId: String?,
    val phone: String?,
    val website: String?,
    val menuPrice: BigDecimal?,
    val cuisineType: String,
    val cuisineEmoji: String,
    val priceIncludesEs: List<String>,
    val priceIncludesEn: List<String>,
    val weekdayHours: Map<String, String>,
    val sourceNotesHe: String? = null,
)

val records = mutableListOf<SeedRecord>()

for (rowNum in 1..sheet.lastRowNum) {
    val row = sheet.getRow(rowNum) ?: continue
    val name = row.cell(COL_NAME)
    if (name.isBlank()) continue

    print("Processing [$rowNum] $name ... ")

    val cuisineHe = row.cell(COL_CUISINE)
    val menuDetailsHe = row.cell(COL_MENU_DETAILS)
    val priceStr = row.cell(COL_PRICE)
    val hoursHe = row.cell(COL_HOURS)
    val mapsLink = row.cell(COL_MAPS_LINK)
    val placeId = row.cell(COL_PLACE_ID)
    val web = row.cell(COL_WEB)
    val phone = row.cell(COL_PHONE)

    val cuisineKey = hebrewToCuisine.entries
        .firstOrNull { (he, _) -> cuisineHe.contains(he, ignoreCase = true) }
        ?.value ?: "OTHER"

    val (priceIncludesEs, priceIncludesEn) = if (menuDetailsHe.isNotBlank())
        translateIncludes(menuDetailsHe)
    else
        emptyList<String>() to emptyList()

    val weekdayHours = parseHours(hoursHe)

    val menuPrice = priceStr.replace(",", ".").toBigDecimalOrNull()

    records += SeedRecord(
        name = name,
        googlePlaceId = extractPlaceId(mapsLink, placeId),
        phone = phone.takeIf { it.isNotBlank() },
        website = web.takeIf { it.isNotBlank() },
        menuPrice = menuPrice,
        cuisineType = cuisineKey,
        cuisineEmoji = cuisineEmojis[cuisineKey] ?: "🍽️",
        priceIncludesEs = priceIncludesEs,
        priceIncludesEn = priceIncludesEn,
        weekdayHours = weekdayHours,
        sourceNotesHe = listOfNotNull(
            cuisineHe.takeIf { it.isNotBlank() },
            menuDetailsHe.takeIf { it.isNotBlank() },
            hoursHe.takeIf { it.isNotBlank() },
        ).joinToString(" | ").takeIf { it.isNotBlank() },
    )
    println("done (cuisine=$cuisineKey, price=${menuPrice}€)")
}

workbook.close()

// ---------------------------------------------------------------------------
// Write JSON output
// ---------------------------------------------------------------------------

fun Any?.toJson(indent: String = ""): String = when (this) {
    null -> "null"
    is String -> jsonString(this)
    is Number -> toString()
    is Boolean -> toString()
    is List<*> -> {
        if (isEmpty()) "[]"
        else "[\n$indent  ${joinToString(",\n$indent  ") { it.toJson("$indent  ") }}\n$indent]"
    }

    is Map<*, *> -> {
        if (isEmpty()) "{}"
        else "{\n$indent  ${
            entries.joinToString(",\n$indent  ") {
                "${jsonString(it.key.toString())}: ${it.value.toJson("$indent  ")}"
            }
        }\n$indent}"
    }

    else -> jsonString(toString())
}

fun SeedRecord.toJsonObject(): Map<String, Any?> = mapOf(
    "name" to name,
    "googlePlaceId" to googlePlaceId,
    "phone" to phone,
    "website" to website,
    "menuPrice" to menuPrice,
    "cuisineType" to cuisineType,
    "cuisineEmoji" to cuisineEmoji,
    "priceIncludesEs" to priceIncludesEs,
    "priceIncludesEn" to priceIncludesEn,
    "weekdayHours" to weekdayHours,
    "sourceNotesHe" to sourceNotesHe,
)

val json = records.map { it.toJsonObject() }.toJson()
File(outPath).writeText(json)
println("\nWrote ${records.size} records to $outPath")
