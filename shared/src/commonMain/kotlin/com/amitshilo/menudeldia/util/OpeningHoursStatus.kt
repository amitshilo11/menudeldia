package com.amitshilo.menudeldia.util

import com.amitshilo.menudeldia.domain.model.OpeningHours
import com.amitshilo.menudeldia.domain.model.Restaurant
import kotlinx.datetime.LocalDateTime
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime
import kotlin.time.Clock
import kotlin.time.ExperimentalTime

@OptIn(ExperimentalTime::class)
fun currentLocalDateTime(timeZone: TimeZone = TimeZone.currentSystemDefault()): LocalDateTime =
    Clock.System.now().toLocalDateTime(timeZone)

/**
 * True if `now` falls inside any non-closed opening-hours window for that day-of-week.
 * Closing time is exclusive (e.g., a 13:00–16:00 window is "open" at 15:59 but "closed" at 16:00).
 */
fun isOpenAt(hours: List<OpeningHours>, now: LocalDateTime): Boolean {
    val today = hours.firstOrNull { it.dayOfWeek == now.dayOfWeek && !it.isClosed } ?: return false
    val nowTime = now.time
    return nowTime >= today.openTime && nowTime < today.closeTime
}

fun isOpenNow(hours: List<OpeningHours>): Boolean = isOpenAt(hours, currentLocalDateTime())

fun Restaurant.isCurrentlyOpen(now: LocalDateTime = currentLocalDateTime()): Boolean =
    isOpenAt(openingHours, now)

/**
 * Today's opening window if any (first non-closed entry for today's day-of-week).
 */
fun todayHours(
    hours: List<OpeningHours>,
    now: LocalDateTime = currentLocalDateTime()
): OpeningHours? =
    hours.firstOrNull { it.dayOfWeek == now.dayOfWeek && !it.isClosed }
