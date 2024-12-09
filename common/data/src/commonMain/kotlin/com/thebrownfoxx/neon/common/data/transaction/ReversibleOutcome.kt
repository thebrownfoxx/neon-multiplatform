package com.thebrownfoxx.neon.common.data.transaction

import com.thebrownfoxx.outcome.Failure
import com.thebrownfoxx.outcome.Outcome
import com.thebrownfoxx.outcome.Success
import com.thebrownfoxx.outcome.UnitOutcome

typealias ReversibleOutcome<T, E> = Reversible<Outcome<T, E>>

typealias ReversibleUnitOutcome<E> = Reversible<UnitOutcome<E>>

fun <T, E> Outcome<T, E>.asReversible(
    finalize: suspend () -> Unit = {},
    reverse: suspend () -> Unit,
) =
    when (this) {
        is Success -> Reversible(this, finalize, reverse)
        is Failure -> asReversible()
    }

fun <E> Failure<E>.asReversible() = Reversible(this) {}