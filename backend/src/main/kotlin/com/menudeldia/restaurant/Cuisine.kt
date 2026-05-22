package com.menudeldia.restaurant

/**
 * Canonical cuisine taxonomy. Hebrew xlsx values are mapped into this enum at seed time.
 * TODO B1.4.2: tune list against actual xlsx data; review mapping table in translate-seed.main.kts.
 */
enum class Cuisine(val emoji: String) {
    SPANISH("🥘"),
    MEDITERRANEAN("🫒"),
    ASIAN("🥢"),
    JAPANESE("🍣"),
    ITALIAN("🍝"),
    MEXICAN("🌮"),
    MIDDLE_EASTERN("🥙"),
    INDIAN("🍛"),
    OTHER("🍽️"),
}
