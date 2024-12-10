package com.thebrownfoxx.neon.common.data.exposed

import com.thebrownfoxx.neon.common.data.AddError
import com.thebrownfoxx.neon.common.data.DataOperationError
import com.thebrownfoxx.neon.common.data.GetError
import com.thebrownfoxx.neon.common.data.UpdateError
import com.thebrownfoxx.outcome.Outcome
import com.thebrownfoxx.outcome.StackTrace
import com.thebrownfoxx.outcome.map.flatMap
import com.thebrownfoxx.outcome.map.flatMapError
import com.thebrownfoxx.outcome.map.map
import com.thebrownfoxx.outcome.map.mapError
import com.thebrownfoxx.outcome.runFailing
import kotlinx.coroutines.Dispatchers
import org.jetbrains.exposed.exceptions.ExposedSQLException
import org.jetbrains.exposed.sql.transactions.experimental.newSuspendedTransaction

suspend fun <T> dataTransaction(
    stackTrace: StackTrace = StackTrace(),
    block: () -> T,
): Outcome<T, DataTransactionError> =
    runFailing(stackTrace) { newSuspendedTransaction(Dispatchers.IO) { block() } }
        .mapError(stackTrace) { error ->
            when (error) {
                is ExposedSQLException -> DataTransactionError.SqlError
                else -> DataTransactionError.ConnectionError
            }
        }

fun <T> Outcome<T, DataTransactionError>.mapOperationTransaction(
    stackTrace: StackTrace = StackTrace(),
) = mapError(stackTrace, DataTransactionError::toDataOperationError)

fun Outcome<*, DataTransactionError>.mapUnitOperationTransaction(
    stackTrace: StackTrace = StackTrace(),
) = map(
    stackTrace = stackTrace,
    onSuccess = {},
    onFailure = DataTransactionError::toDataOperationError,
)

fun Outcome<Outcome<*, AddError>, DataTransactionError>.mapAddTransaction(
    stackTrace: StackTrace = StackTrace(),
) = flatMap(
    stackTrace = stackTrace,
    onSuccess = {},
    onInnerFailure = { it },
    onOuterFailure = DataTransactionError::toAddError,
)

fun Outcome<Outcome<*, UpdateError>, DataTransactionError>.mapUpdateTransaction(
    stackTrace: StackTrace = StackTrace(),
) = flatMap(
    stackTrace = stackTrace,
    onSuccess = {},
    onInnerFailure = { it },
    onOuterFailure = DataTransactionError::toUpdateError,
)

fun <T> Outcome<Outcome<T, GetError>, DataTransactionError>.mapGetTransaction(
    stackTrace: StackTrace = StackTrace(),
) =
    flatMapError(
        stackTrace = stackTrace,
        onInnerFailure = { it },
        onOuterFailure = DataTransactionError::toGetError,
    )

sealed interface DataTransactionError {
    data object SqlError : DataTransactionError
    data object ConnectionError : DataTransactionError

    fun toDataOperationError() = when (this) {
        ConnectionError -> DataOperationError.ConnectionError
        SqlError -> DataOperationError.UnexpectedError
    }

    fun toAddError() = when (this) {
        ConnectionError -> AddError.ConnectionError
        SqlError -> AddError.UnexpectedError
    }

    fun toUpdateError() = when (this) {
        ConnectionError -> UpdateError.ConnectionError
        SqlError -> UpdateError.UnexpectedError
    }

    fun toGetError() = when (this) {
        ConnectionError -> GetError.ConnectionError
        SqlError -> GetError.UnexpectedError
    }
}