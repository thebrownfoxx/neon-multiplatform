package com.thebrownfoxx.neon.common.data.exposed

import com.thebrownfoxx.neon.common.data.GetError
import com.thebrownfoxx.outcome.BlockContext
import com.thebrownfoxx.outcome.Failure
import com.thebrownfoxx.outcome.Success
import org.jetbrains.exposed.sql.Query

fun Query.firstOrNotFound(context: BlockContext) = when (val row = firstOrNull()) {
    null -> Failure(GetError.NotFound, context)
    else -> Success(row)
}