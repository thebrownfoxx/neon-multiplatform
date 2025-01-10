package com.thebrownfoxx.neon.common.data.exposed

import com.thebrownfoxx.neon.common.data.AddError
import com.thebrownfoxx.neon.common.data.UpdateError
import com.thebrownfoxx.outcome.Failure
import com.thebrownfoxx.outcome.Outcome
import com.thebrownfoxx.outcome.StackTrace
import com.thebrownfoxx.outcome.Success
import com.thebrownfoxx.outcome.UnitOutcome
import com.thebrownfoxx.outcome.UnitSuccess
import com.thebrownfoxx.outcome.map.mapError
import com.thebrownfoxx.outcome.runFailing
import org.jetbrains.exposed.exceptions.ExposedSQLException
import org.jetbrains.exposed.sql.Table
import org.jetbrains.exposed.sql.statements.InsertStatement
import org.jetbrains.exposed.sql.statements.UpdateStatement
import org.jetbrains.exposed.sql.transactions.TransactionManager

inline fun <T : Table> T.tryAdd(
    stackTrace: StackTrace = StackTrace(),
    crossinline body: T.(InsertStatement<Number>) -> Unit,
): Outcome<InsertStatement<Number>, AddError> {
    return runFailing(stackTrace) {
        InsertStatement<Number>(this@tryAdd).apply {
            body(this)
            execute(TransactionManager.current())
        }
    }.mapError(stackTrace) { error ->
        when (error) {
            is ExposedSQLException -> AddError.Duplicate
            else -> AddError.ConnectionError
        }
    }
}

inline fun <T : Table> T.tryUpdate(
    stackTrace: StackTrace = StackTrace(),
    limit: Int? = null,
    crossinline body: T.(UpdateStatement) -> Unit,
): UnitOutcome<UpdateError> {
    val changedRows = runFailing(stackTrace) {
        val query = UpdateStatement(this, limit, null)
        body(query)
        query.execute(TransactionManager.current()) ?: 0
    }.mapError(stackTrace) { error ->
        when (error) {
            is ExposedSQLException -> UpdateError.NotFound
            else -> UpdateError.ConnectionError
        }
    }

    return when (changedRows) {
        is Success -> when {
            changedRows.value >= 0 -> UnitSuccess
            else -> Failure(UpdateError.NotFound, stackTrace)
        }
        is Failure -> Failure(changedRows.error, stackTrace)
    }
}