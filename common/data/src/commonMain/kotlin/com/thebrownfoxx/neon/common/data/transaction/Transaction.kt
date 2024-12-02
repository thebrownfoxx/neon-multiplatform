package com.thebrownfoxx.neon.common.data.transaction

import com.thebrownfoxx.neon.common.outcome.Failure
import com.thebrownfoxx.neon.common.outcome.Outcome

suspend fun <T, E> transaction(
    block: suspend TransactionContext.() -> Outcome<T, E>,
): Outcome<T, E> {
    val context = TransactionContext()
    return context.block().also { outcome -> if (outcome is Failure) context.reverseAll() }
}