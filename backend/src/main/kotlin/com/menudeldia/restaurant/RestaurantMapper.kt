package com.menudeldia.restaurant

import com.menudeldia.common.ApiPaths
import com.menudeldia.geo.GeoUtils
import com.menudeldia.restaurant.dto.OpeningHoursDto
import com.menudeldia.restaurant.dto.RestaurantDto
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
        val photos = (0 until entity.photoCount).map { n ->
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
            todayHasMenu = todayHasMenu(entity.weekdayHours, now),
            cuisineEmoji = entity.cuisineEmoji,
            cuisineType = entity.cuisineType,
            openingHours = buildOpeningHours(entity.weekdayHours),
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
            servesVegetarianFood = entity.servesVegetarian,
            outdoorSeating = entity.outdoorSeating,
            reservable = entity.reservable,
            takeout = entity.takeout,
            distanceMeters = if (originLat != null && originLng != null) {
                GeoUtils.haversineMeters(originLat, originLng, entity.lat, entity.lng)
            } else null,
            isOpenNow = isOpenNow(entity.weekdayHours, now),
            priceIncludesEs = entity.priceIncludesEs,
            priceIncludesEn = entity.priceIncludesEn,
        )
    }

    private fun todayHasMenu(weekdayHours: Map<String, String>, now: ZonedDateTime): Boolean {
        val key = now.dayOfWeek.toKey() ?: return false
        return weekdayHours[key] != null
    }

    private fun isOpenNow(weekdayHours: Map<String, String>, now: ZonedDateTime): Boolean {
        val key = now.dayOfWeek.toKey() ?: return false
        val range = weekdayHours[key] ?: return false
        return range.toTimeRange()?.contains(now.toLocalTime()) ?: false
    }

    private fun buildOpeningHours(weekdayHours: Map<String, String>): List<OpeningHoursDto> {
        val days = listOf("mon" to 1, "tue" to 2, "wed" to 3, "thu" to 4, "fri" to 5)
        return days.map { (key, dayNum) ->
            val range = weekdayHours[key]
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
