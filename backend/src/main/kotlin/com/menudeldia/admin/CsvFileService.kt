package com.menudeldia.admin

import com.menudeldia.config.AppProperties
import com.menudeldia.restaurant.Restaurant
import org.apache.commons.csv.CSVFormat
import org.apache.commons.csv.CSVPrinter
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import java.io.IOException
import java.io.Reader
import java.math.BigDecimal
import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.Paths
import java.nio.file.StandardCopyOption

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

/**
 * Round-trip writer for `restaurants_db_ready.csv`.
 *
 * The DB is the source-of-truth; this service rewrites the CSV from the DB
 * whenever an admin edits a field that maps to a CSV column. We rewrite the
 * whole file (rather than patch one row) so layout, escaping and column order
 * stay consistent. Writes go to a temp file and `ATOMIC_MOVE` into place so a
 * crash mid-write never leaves a half-written CSV.
 */
@Service
class CsvFileService(
    private val props: AppProperties,
) {
    private val log = LoggerFactory.getLogger(javaClass)

    fun targetPath(): Path = Paths.get(props.csv.path).toAbsolutePath().normalize()

    fun writeAll(restaurants: List<Restaurant>) {
        val target = targetPath()
        val parent = target.parent
            ?: throw IllegalStateException("CSV path has no parent directory: $target")
        Files.createDirectories(parent)

        val tmp = Files.createTempFile(parent, ".restaurants-", ".csv.tmp")
        try {
            Files.newBufferedWriter(tmp).use { writer ->
                CSVPrinter(writer, CSV_FORMAT).use { printer ->
                    val ordered = restaurants.sortedBy { it.createdAt }
                    ordered.forEachIndexed { idx, r -> printer.printRecord(toRow(idx + 1, r)) }
                }
            }
            Files.move(
                tmp,
                target,
                StandardCopyOption.REPLACE_EXISTING,
                StandardCopyOption.ATOMIC_MOVE,
            )
            log.info("Wrote {} rows to CSV at {}", restaurants.size, target)
        } catch (ex: IOException) {
            log.error("Failed to write CSV at {}: {}", target, ex.message)
            runCatching { Files.deleteIfExists(tmp) }
            throw ex
        }
    }

    private fun toRow(sequentialId: Int, r: Restaurant): List<String?> = listOf(
        sequentialId.toString(),
        r.name,
        r.cuisineType.orEmpty(),
        r.menuPrice?.toPlainString().orEmpty(),
        r.menuDetailsRaw.orEmpty(),
        r.vegetarianOptions.toYesNo(),
        r.glutenFreeOptions.toYesNo(),
        r.daysFrom.orEmpty(),
        r.daysTo.orEmpty(),
        r.excludedDay.orEmpty(),
        r.openTime.orEmpty(),
        r.closeTime.orEmpty(),
        r.phone.orEmpty(),
        r.website.orEmpty(),
        r.googleMapsUrl.orEmpty(),
        r.googlePlaceId.orEmpty(),
    )

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

    private fun Boolean.toYesNo(): String = if (this) "Yes" else "No"

    private fun String.blankToNull(): String? = trim().ifEmpty { null }

    companion object {
        /** CSV column headers. Order is the wire contract with `restaurants_db_ready.csv`. */
        val HEADERS = arrayOf(
            "id",
            "name",
            "cuisine_type",
            "price_normal",
            "menu_details",
            "Vegeterian options",
            "Gluten free options",
            "days_from",
            "days_to",
            "excluded_day",
            "open_time",
            "close_time",
            "phone",
            "website",
            "google_maps_url",
            "google_place_id",
        )

        private val CSV_FORMAT: CSVFormat = CSVFormat.DEFAULT.builder()
            .setHeader(*HEADERS)
            .setRecordSeparator("\n")
            .build()
    }
}
