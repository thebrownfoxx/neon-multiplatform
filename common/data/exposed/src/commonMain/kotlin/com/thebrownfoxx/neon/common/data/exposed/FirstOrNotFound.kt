package com.thebrownfoxx.neon.common.data.exposed

import com.thebrownfoxx.neon.common.data.GetError
import com.thebrownfoxx.neon.common.type.Failure
import com.thebrownfoxx.neon.common.type.Success
import org.jetbrains.exposed.sql.Query

fun Query.firstOrNotFound() = when (val row = firstOrNull()) {
    null -> Failure(GetError.NotFound)
    else -> Success(row)
}