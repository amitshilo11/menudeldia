package com.menudeldia.admin

import com.menudeldia.config.AppProperties
import com.menudeldia.restaurant.Restaurant
import org.apache.commons.csv.CSVFormat
import org.apache.commons.csv.CSVPrinter
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import java.io.IOException
import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.Paths
import java.nio.file.StandardCopyOption

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
        r.priceAlt.orEmpty(),
        r.menuDetailsRaw.orEmpty(),
        r.includesDessert.toCsvBool(),
        r.includesDrink.toCsvBool(),
        buildScheduleText(r),
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

    /**
     * Reconstructs the human-readable schedule text from the structured columns —
     * matches the format used in the original CSV (e.g. "Mon–Fri 13:00–16:00",
     * "Mon–Fri (excl. Wed) 13:00–16:00").
     */
    private fun buildScheduleText(r: Restaurant): String {
        val from = r.daysFrom?.takeIf { it.isNotBlank() } ?: return ""
        val to = r.daysTo?.takeIf { it.isNotBlank() } ?: return ""
        val open = r.openTime?.takeIf { it.isNotBlank() } ?: return ""
        val close = r.closeTime?.takeIf { it.isNotBlank() } ?: return ""
        val excl = r.excludedDay?.takeIf { it.isNotBlank() }
        val rangeText = "$from–$to" + if (excl != null) " (excl. $excl)" else ""
        return "$rangeText $open–$close"
    }

    private fun Boolean.toCsvBool(): String = if (this) "True" else "False"

    companion object {
        /** CSV column headers. Order is the wire contract with `restaurants_db_ready.csv`. */
        val HEADERS = arrayOf(
            "id",
            "name",
            "cuisine_type",
            "price_normal",
            "price_alt",
            "menu_details",
            "includes_dessert",
            "includes_drink",
            "schedule",
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
