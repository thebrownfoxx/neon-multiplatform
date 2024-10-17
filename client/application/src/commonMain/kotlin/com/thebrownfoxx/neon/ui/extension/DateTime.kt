package com.thebrownfoxx.neon.ui.extension

import com.thebrownfoxx.neon.common.extension.toInstant
import kotlinx.datetime.Clock
import kotlinx.datetime.LocalDate
import kotlinx.datetime.LocalDateTime
import kotlinx.datetime.LocalTime
import kotlinx.datetime.format
import kotlinx.datetime.format.DayOfWeekNames
import kotlinx.datetime.format.MonthNames
import kotlinx.datetime.format.Padding
import kotlinx.datetime.format.char
import kotlin.time.Duration.Companion.days

val TimeFormat = LocalTime.Format {
    amPmHour()
    char(':')
    minute()
    char(' ')
    amPmMarker("am", "pm")
}

val AbbreviatedDayFormat = LocalDate.Format {
    dayOfWeek(DayOfWeekNames.ENGLISH_ABBREVIATED)
}

val AbbreviatedDateFormat = LocalDate.Format {
    monthName(MonthNames.ENGLISH_ABBREVIATED)
    char(' ')
    dayOfMonth(Padding.NONE)
}

fun LocalDateTime.toReadableTime(): String {
    val durationElapsed = Clock.System.now() - toInstant()
    return when {
        durationElapsed < 1.days -> time.format(TimeFormat)
        durationElapsed < 7.days -> date.format(AbbreviatedDayFormat)
        else -> date.format(AbbreviatedDateFormat)
    }
}