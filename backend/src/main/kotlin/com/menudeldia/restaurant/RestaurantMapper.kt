package com.menudeldia.restaurant

import com.menudeldia.common.ApiPaths
import com.menudeldia.geo.GeoUtils
import com.menudeldia.restaurant.dto.OpeningHoursDto
import com.menudeldia.restaurant.dto.RestaurantDto
import com.menudeldia.restaurant.dto.RestaurantSummaryDto
import com.menudeldia.restaurant.dto.ReviewDto
import org.springframework.stereotype.Component
import java.time.DayOfWeek
import java.time.LocalTime
import java.time.ZoneId
import java.time.ZonedDateTime

@Component
class RestaurantMapper {

    private val madridZone = ZoneId.of("Europe/Madrid")

    fun toDto(
        entity: Restaurant,
        originLat: Double? = null,
        originLng: Double? = null,
    ): RestaurantDto {
        val now = ZonedDateTime.now(madridZone)
        val photos = entity.photoNames.indices.map { n ->
            "${ApiPaths.V1}/restaurants/${entity.id}/photos/$n"
        }
        return RestaurantDto(
            id = entity.id.toString(),
            name = entity.name,
            lat = entity.lat,
            lng = entity.lng,
            address = entity.address ?: "",
            phone = entity.phone,
            thumbnailUrl = photos.firstOrNull(),
            photos = photos,
            descriptionEs = entity.descriptionEs,
            descriptionEn = entity.descriptionEn,
            menuPrice = entity.menuPrice?.toDouble(),
            currency = entity.currency,
            todayHasMenu = todayHasMenu(entity.weekdayHours, now, entity.openTime, entity.closeTime),
            cuisineEmoji = entity.cuisineEmoji,
            cuisineType = entity.cuisineType,
            openingHours = buildOpeningHours(entity.weekdayHours, entity.openTime, entity.closeTime),
            rating = entity.rating,
            userRatingCount = entity.userRatingCount,
            editorialSummary = entity.editorialSummary,
            aiSummary = entity.aiSummary,
            reviews = entity.reviews.map { r ->
                ReviewDto(
                    authorName = r.authorName,
                    authorPhotoUri = r.authorPhotoUri,
                    rating = r.rating,
                    text = r.text,
                    originalText = r.originalText,
                    relativeTime = r.relativeTime,
                )
            },
            servesLunch = entity.servesLunch,
            servesVegetarianFood = entity.vegetarianOptions || entity.servesVegetarian,
            servesGlutenFree = entity.glutenFreeOptions,
            outdoorSeating = entity.outdoorSeating,
            reservable = entity.reservable,
            takeout = entity.takeout,
            distanceMeters = if (originLat != null && originLng != null) {
                GeoUtils.haversineMeters(originLat, originLng, entity.lat, entity.lng)
            } else null,
            isOpenNow = isOpenNow(entity.weekdayHours, now, entity.openTime, entity.closeTime),
            priceIncludesEs = entity.priceIncludesEs,
            priceIncludesEn = entity.priceIncludesEn,
            includesDessert = entity.priceIncludesEn.any {
                it.contains(
                    "dessert",
                    ignoreCase = true
                )
            },
            includesDrink = entity.priceIncludesEn.any { it.contains("drink", ignoreCase = true) },
        )
    }

    fun toSummaryDto(
        entity: Restaurant,
        originLat: Double? = null,
        originLng: Double? = null,
    ): RestaurantSummaryDto {
        val now = ZonedDateTime.now(madridZone)
        val thumbnailUrl = if (entity.photoNames.isNotEmpty())
            "${ApiPaths.V1}/restaurants/${entity.id}/photos/0" else null
        return RestaurantSummaryDto(
            id = entity.id.toString(),
            name = entity.name,
            lat = entity.lat,
            lng = entity.lng,
            address = entity.address ?: "",
            phone = entity.phone,
            thumbnailUrl = thumbnailUrl,
            menuPrice = entity.menuPrice?.toDouble(),
            currency = entity.currency,
            todayHasMenu = todayHasMenu(entity.weekdayHours, now, entity.openTime, entity.closeTime),
            cuisineEmoji = entity.cuisineEmoji,
            cuisineType = entity.cuisineType,
            openingHours = buildOpeningHours(entity.weekdayHours, entity.openTime, entity.closeTime),
            rating = entity.rating,
            userRatingCount = entity.userRatingCount,
            servesVegetarianFood = entity.vegetarianOptions || entity.servesVegetarian,
            servesGlutenFree = entity.glutenFreeOptions,
            distanceMeters = if (originLat != null && originLng != null)
                GeoUtils.haversineMeters(originLat, originLng, entity.lat, entity.lng) else null,
            isOpenNow = isOpenNow(entity.weekdayHours, now, entity.openTime, entity.closeTime),
            priceIncludesEn = entity.priceIncludesEn,
            includesDessert = entity.priceIncludesEn.any {
                it.contains(
                    "dessert",
                    ignoreCase = true
                )
            },
            includesDrink = entity.priceIncludesEn.any { it.contains("drink", ignoreCase = true) },
        )
    }

    private fun todayHasMenu(
        weekdayHours: Map<String, String>,
        now: ZonedDateTime,
        openTime: String?,
        closeTime: String?,
    ): Boolean {
        val key = now.dayOfWeek.toKey() ?: return false
        return weekdayHours[key] != null || (openTime != null && closeTime != null)
    }

    private fun isOpenNow(
        weekdayHours: Map<String, String>,
        now: ZonedDateTime,
        openTime: String?,
        closeTime: String?,
    ): Boolean {
        val key = now.dayOfWeek.toKey() ?: return false
        val range = weekdayHours[key]
            ?: if (openTime != null && closeTime != null) "$openTime-$closeTime" else return false
        return range.toTimeRange()?.contains(now.toLocalTime()) ?: false
    }

    private fun buildOpeningHours(
        weekdayHours: Map<String, String>,
        openTime: String?,
        closeTime: String?,
    ): List<OpeningHoursDto> {
        val days = listOf("mon" to 1, "tue" to 2, "wed" to 3, "thu" to 4, "fri" to 5)
        val fallbackRange = if (openTime != null && closeTime != null) "$openTime-$closeTime" else null
        return days.map { (key, dayNum) ->
            val range = weekdayHours[key] ?: fallbackRange
            val parsed = range?.toTimeRange()
            if (parsed != null) {
                OpeningHoursDto(
                    dayNum,
                    parsed.first.toString(),
                    parsed.second.toString(),
                    isClosed = false
                )
            } else {
                OpeningHoursDto(dayNum, "", "", isClosed = true)
            }
        }
    }

    private fun DayOfWeek.toKey(): String? = when (this) {
        DayOfWeek.MONDAY -> "mon"
        DayOfWeek.TUESDAY -> "tue"
        DayOfWeek.WEDNESDAY -> "wed"
        DayOfWeek.THURSDAY -> "thu"
        DayOfWeek.FRIDAY -> "fri"
        else -> null
    }

    /** Parses "HH:MM-HH:MM" or "HH:MM – HH:MM" into an open/close pair. */
    private fun String.toTimeRange(): Pair<LocalTime, LocalTime>? {
        val parts = split(Regex("\\s*[-–]\\s*"))
        if (parts.size != 2) return null
        return runCatching {
            LocalTime.parse(parts[0].trim()) to LocalTime.parse(parts[1].trim())
        }.getOrNull()
    }

    private fun ClosedRange<LocalTime>.contains(time: LocalTime) =
        time >= start && time <= endInclusive

    private fun Pair<LocalTime, LocalTime>.contains(time: LocalTime) =
        time >= first && time <= second
}

private val menuIncludesDelimiter = Regex("""\s*\+\s*""")

/** Splits CSV `menu_details` text (e.g. "Starter + Main + Dessert + Drink") into course labels. */
fun parseMenuIncludes(raw: String?): List<String> =
    raw?.split(menuIncludesDelimiter)?.map { it.trim() }?.filter { it.isNotEmpty() } ?: emptyList()
