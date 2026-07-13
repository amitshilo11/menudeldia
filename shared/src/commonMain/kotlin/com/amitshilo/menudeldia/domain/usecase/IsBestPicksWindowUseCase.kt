package com.amitshilo.menudeldia.domain.usecase

import kotlinx.datetime.DayOfWeek
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime
import kotlin.time.Clock
import kotlin.time.ExperimentalTime

/** Returns true only on Mon–Fri between 10:00 and 16:59 (before 17:00). */
class IsBestPicksWindowUseCase {

    @OptIn(ExperimentalTime::class)
    operator fun invoke(): Boolean {
        val now = Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault())
        if (now.dayOfWeek == DayOfWeek.SATURDAY ||
            now.dayOfWeek == DayOfWeek.SUNDAY
        )
            return false
        return now.hour in 10..16
    }
}
