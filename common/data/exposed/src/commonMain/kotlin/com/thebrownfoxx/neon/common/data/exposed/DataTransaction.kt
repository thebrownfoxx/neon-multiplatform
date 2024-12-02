package com.thebrownfoxx.neon.common.data.exposed

import com.thebrownfoxx.neon.common.data.AddError
import com.thebrownfoxx.neon.common.data.GetError
import com.thebrownfoxx.neon.common.data.UpdateError
import com.thebrownfoxx.neon.common.outcome.Outcome
import com.thebrownfoxx.neon.common.outcome.flatMap
import com.thebrownfoxx.neon.common.outcome.flatMapError
import com.thebrownfoxx.neon.common.outcome.mapError
import com.thebrownfoxx.neon.common.outcome.runFailing
import kotlinx.coroutines.Dispatchers
import org.jetbrains.exposed.exceptions.ExposedSQLException
import org.jetbrains.exposed.sql.transactions.experimental.newSuspendedTransaction

suspend fun <T> dataTransaction(block: () -> T): Outcome<T, DataTransactionError> =
    runFailing { newSuspendedTransaction(Dispatchers.IO) { block() } }
        .mapError { error ->
            when (error) {
                is ExposedSQLException -> DataTransactionError.SqlException
                else -> DataTransactionError.ConnectionError
            }
        }

fun <T> Outcome<Outcome<T, AddError>, DataTransactionError>.mapAddTransaction() =
    flatMap(
        onSuccess = {},
        onInnerFailure = { it },
        onOuterFailure = { AddError.ConnectionError },
    )


fun <T> Outcome<Outcome<T, UpdateError>, DataTransactionError>.mapUpdateTransaction() =
    flatMap(
        onSuccess = {},
        onInnerFailure = { it },
        onOuterFailure = { UpdateError.ConnectionError },
    )


fun <T> Outcome<Outcome<T, GetError>, DataTransactionError>.mapGetTransaction() =
    flatMapError(
        onInnerFailure = { it },
        onOuterFailure = { GetError.ConnectionError },
    )

enum class DataTransactionError {
    SqlException,
    ConnectionError,
}