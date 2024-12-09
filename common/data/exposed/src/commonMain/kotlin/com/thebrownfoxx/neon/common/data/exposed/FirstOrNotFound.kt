package com.thebrownfoxx.neon.common.data.exposed

import com.thebrownfoxx.neon.common.data.GetError
import com.thebrownfoxx.outcome.Failure
import com.thebrownfoxx.outcome.StackTrace
import com.thebrownfoxx.outcome.Success
import org.jetbrains.exposed.sql.Query

fun Query.firstOrNotFound(stackTrace: StackTrace = StackTrace()) = when (val row = firstOrNull()) {
    null -> Failure(GetError.NotFound, stackTrace)
    else -> Success(row)
}