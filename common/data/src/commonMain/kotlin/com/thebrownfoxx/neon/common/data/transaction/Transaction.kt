package com.thebrownfoxx.neon.common.data.transaction

import com.thebrownfoxx.outcome.Failure
import com.thebrownfoxx.outcome.Outcome

suspend fun <T, E> transaction(
    block: suspend TransactionContext.() -> Outcome<T, E>,
): Outcome<T, E> {
    val context = TransactionContext()
    return context.block().also { outcome ->
        if (outcome is Failure) context.reverseAll()
        else context.finalizeAll()
    }
}