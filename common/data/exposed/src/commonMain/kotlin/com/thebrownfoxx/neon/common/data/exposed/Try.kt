package com.thebrownfoxx.neon.common.data.exposed

import com.thebrownfoxx.neon.common.data.AddError
import com.thebrownfoxx.neon.common.data.UpdateError
import com.thebrownfoxx.neon.common.outcome.Failure
import com.thebrownfoxx.neon.common.outcome.Outcome
import com.thebrownfoxx.neon.common.outcome.Success
import com.thebrownfoxx.neon.common.outcome.UnitOutcome
import com.thebrownfoxx.neon.common.outcome.mapError
import com.thebrownfoxx.neon.common.outcome.runFailing
import com.thebrownfoxx.neon.common.outcome.unitSuccess
import org.jetbrains.exposed.exceptions.ExposedSQLException
import org.jetbrains.exposed.sql.Table
import org.jetbrains.exposed.sql.statements.InsertStatement
import org.jetbrains.exposed.sql.statements.UpdateStatement
import org.jetbrains.exposed.sql.transactions.TransactionManager

inline fun <T : Table> T.tryAdd(
    crossinline body: T.(InsertStatement<Number>) -> Unit,
): Outcome<InsertStatement<Number>, AddError> {
    return runFailing {
        InsertStatement<Number>(this@tryAdd).apply {
            body(this)
            execute(TransactionManager.current())
        }
    }.mapError { error ->
        when (error) {
            is ExposedSQLException -> AddError.Duplicate
            else -> AddError.ConnectionError
        }
    }
}

inline fun <T : Table> T.tryUpdate(
    limit: Int? = null,
    crossinline body: T.(UpdateStatement) -> Unit,
): UnitOutcome<UpdateError> {
    val changedRows = runFailing {
        val query = UpdateStatement(this, limit, null)
        body(query)
        query.execute(TransactionManager.current()) ?: 0
    }.mapError { error ->
        when (error) {
            is ExposedSQLException -> UpdateError.NotFound
            else -> UpdateError.ConnectionError
        }
    }

    return when (changedRows) {
        is Success -> if (changedRows.value >= 0) unitSuccess() else Failure(UpdateError.NotFound)
        is Failure -> Failure(changedRows.error)
    }
}