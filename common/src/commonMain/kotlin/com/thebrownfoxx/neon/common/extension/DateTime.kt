package com.thebrownfoxx.neon.common.extension

import kotlinx.datetime.Clock
import kotlinx.datetime.Instant
import kotlinx.datetime.LocalDateTime
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toInstant
import kotlinx.datetime.toLocalDateTime
import kotlin.time.Duration

fun Instant.toLocalDateTime(timeZone: TimeZone = TimeZone.currentSystemDefault()) =
    toLocalDateTime(timeZone)

fun LocalDateTime.toInstant(timeZone: TimeZone = TimeZone.currentSystemDefault()) =
    toInstant(timeZone)

val Duration.ago get() = Clock.System.now() - this