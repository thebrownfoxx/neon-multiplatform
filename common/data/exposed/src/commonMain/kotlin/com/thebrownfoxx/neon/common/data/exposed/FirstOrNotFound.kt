package com.thebrownfoxx.neon.common.data.exposed

import com.thebrownfoxx.neon.common.data.GetError
import com.thebrownfoxx.neon.common.outcome.Failure
import com.thebrownfoxx.neon.common.outcome.Success
import org.jetbrains.exposed.sql.Query

fun Query.firstOrNotFound() = when (val row = firstOrNull()) {
    null -> Failure(GetError.NotFound)
    else -> Success(row)
}