package com.amitshilo.menudeldia.util

import com.amitshilo.menudeldia.domain.model.OpeningHours
import kotlinx.datetime.DayOfWeek
import kotlinx.datetime.LocalDateTime
import kotlinx.datetime.LocalTime
import kotlinx.datetime.Month
import kotlin.test.Test
import kotlin.test.assertFalse
import kotlin.test.assertTrue

private val WEEKDAY_LUNCH = listOf(
    OpeningHours(DayOfWeek.MONDAY, LocalTime(13, 0), LocalTime(16, 0)),
    OpeningHours(DayOfWeek.TUESDAY, LocalTime(13, 0), LocalTime(16, 0)),
    OpeningHours(DayOfWeek.WEDNESDAY, LocalTime(13, 0), LocalTime(16, 0)),
    OpeningHours(DayOfWeek.THURSDAY, LocalTime(13, 0), LocalTime(16, 0)),
    OpeningHours(DayOfWeek.FRIDAY, LocalTime(13, 0), LocalTime(16, 0)),
    OpeningHours(DayOfWeek.SATURDAY, LocalTime(0, 0), LocalTime(0, 0), isClosed = true),
    OpeningHours(DayOfWeek.SUNDAY, LocalTime(0, 0), LocalTime(0, 0), isClosed = true),
)

private fun ldt(year: Int, month: Month, day: Int, hour: Int, minute: Int = 0): LocalDateTime =
    LocalDateTime(year, month, day, hour, minute)

class OpeningHoursStatusTest {

    @Test
    fun `mid-window weekday is open`() {
        // 2026-05-13 is a Wednesday
        assertTrue(isOpenAt(WEEKDAY_LUNCH, ldt(2026, Month.MAY, 13, 14, 30)))
    }

    @Test
    fun `before open is closed`() {
        assertFalse(isOpenAt(WEEKDAY_LUNCH, ldt(2026, Month.MAY, 13, 12, 59)))
    }

    @Test
    fun `exactly at open is open`() {
        assertTrue(isOpenAt(WEEKDAY_LUNCH, ldt(2026, Month.MAY, 13, 13, 0)))
    }

    @Test
    fun `exactly at close is closed (close is exclusive)`() {
        assertFalse(isOpenAt(WEEKDAY_LUNCH, ldt(2026, Month.MAY, 13, 16, 0)))
    }

    @Test
    fun `one minute before close is open`() {
        assertTrue(isOpenAt(WEEKDAY_LUNCH, ldt(2026, Month.MAY, 13, 15, 59)))
    }

    @Test
    fun `weekend explicit closed`() {
        // 2026-05-16 is Saturday
        assertFalse(isOpenAt(WEEKDAY_LUNCH, ldt(2026, Month.MAY, 16, 14, 0)))
        // 2026-05-17 is Sunday
        assertFalse(isOpenAt(WEEKDAY_LUNCH, ldt(2026, Month.MAY, 17, 14, 0)))
    }

    @Test
    fun `empty hours list is always closed`() {
        assertFalse(isOpenAt(emptyList(), ldt(2026, Month.MAY, 13, 14, 0)))
    }
}
