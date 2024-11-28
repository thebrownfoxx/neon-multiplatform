package com.thebrownfoxx.neon.common.data.exposed

import com.thebrownfoxx.neon.common.data.AddError
import com.thebrownfoxx.neon.common.data.UpdateError
import com.thebrownfoxx.neon.common.type.UnitOutcome
import com.thebrownfoxx.neon.common.type.mapError
import com.thebrownfoxx.neon.common.type.runFailing

suspend fun tryAdd(add: suspend () -> Unit): UnitOutcome<AddError> {
    return runFailing { add() }.mapError { AddError.Duplicate }
}

suspend fun tryUpdate(update: suspend () -> Unit): UnitOutcome<UpdateError> {
    return runFailing { update() }.mapError { UpdateError.NotFound }
}