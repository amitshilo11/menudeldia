package com.menudeldia.admin

import com.menudeldia.config.AppProperties
import com.menudeldia.restaurant.Restaurant
import org.apache.commons.csv.CSVFormat
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.io.TempDir
import java.math.BigDecimal
import java.nio.file.Files
import java.nio.file.Path
import java.time.Duration
import java.time.Instant

class CsvFileServiceTest {

    private fun service(csvPath: Path) = CsvFileService(
        AppProperties(
            google = AppProperties.GoogleProps("", Duration.ofHours(1), 5),
            auth = AppProperties.AuthProps("", "", "a".repeat(32), Duration.ofDays(30)),
            cors = AppProperties.CorsProps(emptyList()),
            rateLimit = AppProperties.RateLimitProps(60, 10),
            csv = AppProperties.CsvProps(csvPath.toString()),
        )
    )

    private fun restaurant(
        name: String,
        cuisine: String? = "Asian",
        price: BigDecimal? = BigDecimal("12.50"),
        menuDetails: String? = "Starter + Main + Drink",
        includesDessert: Boolean = false,
        includesDrink: Boolean = true,
        daysFrom: String? = "Mon",
        daysTo: String? = "Fri",
        excludedDay: String? = null,
        openTime: String? = "12:30",
        closeTime: String? = "16:00",
        phone: String? = "+34936227430",
        website: String? = "https://example.com",
        mapsUrl: String? = "https://maps.app.goo.gl/abc",
        placeId: String? = "ChIJabc123",
        createdAt: Instant = Instant.parse("2026-01-01T00:00:00Z"),
    ) = Restaurant(
        name = name,
        lat = 41.3851, lng = 2.1734,
        cuisineType = cuisine,
        menuPrice = price,
        menuDetailsRaw = menuDetails,
        includesDessert = includesDessert,
        includesDrink = includesDrink,
        daysFrom = daysFrom, daysTo = daysTo, excludedDay = excludedDay,
        openTime = openTime, closeTime = closeTime,
        phone = phone, website = website,
        googleMapsUrl = mapsUrl,
        googlePlaceId = placeId,
        createdAt = createdAt,
    )

    @Test
    fun `writeAll produces header and rows in createdAt order with regenerated ids`(
        @TempDir dir: Path,
    ) {
        val csvPath = dir.resolve("out.csv")
        val svc = service(csvPath)

        // Second restaurant has earlier createdAt, should appear first.
        val a = restaurant("Kemo", createdAt = Instant.parse("2026-02-01T00:00:00Z"))
        val b = restaurant(
            "Lady babka", cuisine = "Mediterranean", price = BigDecimal("18.90"),
            includesDessert = true, createdAt = Instant.parse("2026-01-15T00:00:00Z")
        )

        svc.writeAll(listOf(a, b))

        assertTrue(Files.exists(csvPath), "CSV file should exist")
        val parsed = CSVFormat.DEFAULT.builder().setHeader().setSkipHeaderRecord(true).build()
            .parse(Files.newBufferedReader(csvPath)).records

        assertEquals(2, parsed.size)
        // b (earlier createdAt) is row 1, a is row 2
        assertEquals("1", parsed[0].get("id"))
        assertEquals("Lady babka", parsed[0].get("name"))
        assertEquals("Mediterranean", parsed[0].get("cuisine_type"))
        assertEquals("18.90", parsed[0].get("price_normal"))
        assertEquals("True", parsed[0].get("includes_dessert"))
        assertEquals("True", parsed[0].get("includes_drink"))

        assertEquals("2", parsed[1].get("id"))
        assertEquals("Kemo", parsed[1].get("name"))
        assertEquals("False", parsed[1].get("includes_dessert"))
    }

    @Test
    fun `writeAll reconstructs schedule text with and without excluded day`(@TempDir dir: Path) {
        val csvPath = dir.resolve("out.csv")
        val svc = service(csvPath)

        val plain = restaurant("Plain")
        val excluded = restaurant(
            "Excluded", excludedDay = "Wed",
            createdAt = Instant.parse("2026-02-01T00:00:00Z")
        )

        svc.writeAll(listOf(plain, excluded))

        val parsed = CSVFormat.DEFAULT.builder().setHeader().setSkipHeaderRecord(true).build()
            .parse(Files.newBufferedReader(csvPath)).records

        assertEquals("Mon–Fri 12:30–16:00", parsed[0].get("schedule"))
        assertEquals("Mon–Fri (excl. Wed) 12:30–16:00", parsed[1].get("schedule"))
    }

    @Test
    fun `writeAll round-trips every CSV-mapped field`(@TempDir dir: Path) {
        val csvPath = dir.resolve("out.csv")
        val svc = service(csvPath)

        val r = restaurant(
            "MOSCADA", cuisine = "Mediterranean", price = BigDecimal("18.80"),
            menuDetails = "Starter + Main + Dessert + Drink",
            includesDessert = true, includesDrink = true,
            phone = "+34931018764", website = "https://www.moscadabcn.com/",
            mapsUrl = "https://maps.app.goo.gl/iC6w7Ho8Z64egXH57",
            placeId = "ChIJbzY6rRWjpBIRNVxSPbboqZA",
        )
        svc.writeAll(listOf(r))

        val record = CSVFormat.DEFAULT.builder().setHeader().setSkipHeaderRecord(true).build()
            .parse(Files.newBufferedReader(csvPath)).records.single()

        assertEquals("MOSCADA", record.get("name"))
        assertEquals("Mediterranean", record.get("cuisine_type"))
        assertEquals("18.80", record.get("price_normal"))
        assertEquals("Starter + Main + Dessert + Drink", record.get("menu_details"))
        assertEquals("True", record.get("includes_dessert"))
        assertEquals("True", record.get("includes_drink"))
        assertEquals("Mon", record.get("days_from"))
        assertEquals("Fri", record.get("days_to"))
        assertEquals("", record.get("excluded_day"))
        assertEquals("12:30", record.get("open_time"))
        assertEquals("16:00", record.get("close_time"))
        assertEquals("+34931018764", record.get("phone"))
        assertEquals("https://www.moscadabcn.com/", record.get("website"))
        assertEquals("https://maps.app.goo.gl/iC6w7Ho8Z64egXH57", record.get("google_maps_url"))
        assertEquals("ChIJbzY6rRWjpBIRNVxSPbboqZA", record.get("google_place_id"))
    }

    @Test
    fun `writeAll atomically replaces existing file`(@TempDir dir: Path) {
        val csvPath = dir.resolve("out.csv")
        Files.writeString(csvPath, "stale content")
        val svc = service(csvPath)

        svc.writeAll(listOf(restaurant("Replaced")))

        val text = Files.readString(csvPath)
        assertTrue(text.startsWith("id,name,"), "expected header line, got: ${text.take(40)}")
        assertTrue(text.contains("Replaced"))
        assertTrue(!text.contains("stale content"))
    }
}
