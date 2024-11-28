package com.thebrownfoxx.neon.common.data.transaction

import com.thebrownfoxx.neon.common.type.Failure
import com.thebrownfoxx.neon.common.type.Outcome
import com.thebrownfoxx.neon.common.type.Success
import com.thebrownfoxx.neon.common.type.UnitOutcome

typealias ReversibleOutcome<T, E> = Reversible<Outcome<T, E>>

typealias ReversibleUnitOutcome<E> = Reversible<UnitOutcome<E>>

fun <T, E> Outcome<T, E>.asReversible(reverse: suspend () -> Unit) =
    when (this) {
        is Success -> Reversible(this, reverse)
        is Failure -> asReversible()
    }

fun <E> Failure<E>.asReversible() = Reversible(this) {}