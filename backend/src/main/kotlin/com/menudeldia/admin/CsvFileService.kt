package com.menudeldia.admin

import org.apache.commons.csv.CSVFormat
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import java.io.Reader
import java.math.BigDecimal

data class CsvRow(
    val name: String,
    val cuisineType: String?,
    val menuPrice: BigDecimal?,
    val menuDetailsRaw: String?,
    val vegetarianOptions: Boolean,
    val glutenFreeOptions: Boolean,
    val daysFrom: String?,
    val daysTo: String?,
    val excludedDay: String?,
    val openTime: String?,
    val closeTime: String?,
    val phone: String?,
    val website: String?,
    val googleMapsUrl: String?,
    val googlePlaceId: String?,
)

/** Parses uploaded CSV files for bulk restaurant import via the admin portal. */
@Service
class CsvFileService {
    private val log = LoggerFactory.getLogger(javaClass)

    fun parseRows(reader: Reader): List<CsvRow> =
        CSVFormat.DEFAULT.builder()
            .setHeader()
            .setSkipHeaderRecord(true)
            .build()
            .parse(reader)
            .records
            .mapNotNull { r ->
                val name = r.get("name")?.trim()?.ifEmpty { null } ?: return@mapNotNull null
                CsvRow(
                    name = name,
                    cuisineType = r.get("cuisine_type")?.blankToNull(),
                    menuPrice = r.get("price_normal")?.blankToNull()
                        ?.let { runCatching { BigDecimal(it) }.getOrNull() },
                    menuDetailsRaw = r.get("menu_details")?.blankToNull(),
                    vegetarianOptions = r.get("Vegeterian options")?.trim()
                        ?.equals("Yes", ignoreCase = true) ?: false,
                    glutenFreeOptions = r.get("Gluten free options")?.trim()
                        ?.equals("Yes", ignoreCase = true) ?: false,
                    daysFrom = r.get("days_from")?.blankToNull(),
                    daysTo = r.get("days_to")?.blankToNull(),
                    excludedDay = r.get("excluded_day")?.blankToNull(),
                    openTime = r.get("open_time")?.blankToNull(),
                    closeTime = r.get("close_time")?.blankToNull(),
                    phone = r.get("phone")?.blankToNull(),
                    website = r.get("website")?.blankToNull(),
                    googleMapsUrl = r.get("google_maps_url")?.blankToNull(),
                    googlePlaceId = r.get("google_place_id")?.blankToNull(),
                )
            }

    private fun String.blankToNull(): String? = trim().ifEmpty { null }
}
