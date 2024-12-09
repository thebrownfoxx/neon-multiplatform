package com.thebrownfoxx.neon.common.data.exposed

import com.thebrownfoxx.neon.common.data.AddError
import com.thebrownfoxx.neon.common.data.DataOperationError
import com.thebrownfoxx.neon.common.data.GetError
import com.thebrownfoxx.neon.common.data.UpdateError
import com.thebrownfoxx.outcome.BlockContext
import com.thebrownfoxx.outcome.BlockContextScope
import com.thebrownfoxx.outcome.Outcome
import com.thebrownfoxx.outcome.flatMap
import com.thebrownfoxx.outcome.flatMapError
import com.thebrownfoxx.outcome.map
import com.thebrownfoxx.outcome.mapError
import kotlinx.coroutines.Dispatchers
import org.jetbrains.exposed.exceptions.ExposedSQLException
import org.jetbrains.exposed.sql.transactions.experimental.newSuspendedTransaction

suspend fun <T> BlockContextScope.dataTransaction(
    block: () -> T,
): Outcome<T, DataTransactionError> =
    runFailing { newSuspendedTransaction(Dispatchers.IO) { block() } }
        .mapError { error ->
            when (error) {
                is ExposedSQLException -> DataTransactionError.SqlError
                else -> DataTransactionError.ConnectionError
            }
        }

fun <T> Outcome<T, DataTransactionError>.mapOperationTransaction(
    context: BlockContext,
) = mapError(context, DataTransactionError::toDataOperationError)

fun Outcome<*, DataTransactionError>.mapUnitOperationTransaction(
    context: BlockContext,
) = map(
    context,
    onSuccess = {},
    onFailure = DataTransactionError::toDataOperationError,
)

fun Outcome<Outcome<*, AddError>, DataTransactionError>.mapAddTransaction(
    context: BlockContext,
) = flatMap(
    context = context,
    onSuccess = {},
    onInnerFailure = { it },
    onOuterFailure = DataTransactionError::toAddError,
)

fun Outcome<Outcome<*, UpdateError>, DataTransactionError>.mapUpdateTransaction(
    context: BlockContext,
) = flatMap(
    context = context,
    onSuccess = {},
    onInnerFailure = { it },
    onOuterFailure = DataTransactionError::toUpdateError,
)

fun <T> Outcome<Outcome<T, GetError>, DataTransactionError>.mapGetTransaction(
    context: BlockContext,
) =
    flatMapError(
        context = context,
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