package com.thebrownfoxx.neon.common.data.exposed

import com.thebrownfoxx.neon.common.data.AddError
import com.thebrownfoxx.neon.common.data.UpdateError
import com.thebrownfoxx.outcome.BlockContext
import com.thebrownfoxx.outcome.Failure
import com.thebrownfoxx.outcome.Outcome
import com.thebrownfoxx.outcome.Success
import com.thebrownfoxx.outcome.UnitOutcome
import com.thebrownfoxx.outcome.UnitSuccess
import com.thebrownfoxx.outcome.mapError
import com.thebrownfoxx.outcome.runFailing
import org.jetbrains.exposed.exceptions.ExposedSQLException
import org.jetbrains.exposed.sql.Table
import org.jetbrains.exposed.sql.statements.InsertStatement
import org.jetbrains.exposed.sql.statements.UpdateStatement
import org.jetbrains.exposed.sql.transactions.TransactionManager

inline fun <T : Table> T.tryAdd(
    context: BlockContext,
    crossinline body: T.(InsertStatement<Number>) -> Unit,
): Outcome<InsertStatement<Number>, AddError> {
    return runFailing(context) {
        InsertStatement<Number>(this@tryAdd).apply {
            body(this)
            execute(TransactionManager.current())
        }
    }.mapError(context) { error ->
        when (error) {
            is ExposedSQLException -> AddError.Duplicate
            else -> AddError.ConnectionError
        }
    }
}

inline fun <T : Table> T.tryUpdate(
    context: BlockContext,
    limit: Int? = null,
    crossinline body: T.(UpdateStatement) -> Unit,
): UnitOutcome<UpdateError> {
    val changedRows = runFailing(context) {
        val query = UpdateStatement(this, limit, null)
        body(query)
        query.execute(TransactionManager.current()) ?: 0
    }.mapError(context) { error ->
        when (error) {
            is ExposedSQLException -> UpdateError.NotFound
            else -> UpdateError.ConnectionError
        }
    }

    return when (changedRows) {
        is Success -> when {
            changedRows.value >= 0 -> UnitSuccess
            else -> Failure(UpdateError.NotFound, context)
        }
        is Failure -> Failure(changedRows.error, context)
    }
}