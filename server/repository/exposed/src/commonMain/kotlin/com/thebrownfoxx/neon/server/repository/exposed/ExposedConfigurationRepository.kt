package com.thebrownfoxx.neon.server.repository.exposed

import com.thebrownfoxx.neon.common.data.ConnectionError
import com.thebrownfoxx.neon.common.data.exposed.ExposedDataSource
import com.thebrownfoxx.neon.common.data.exposed.dbQuery
import com.thebrownfoxx.neon.common.data.exposed.firstOrNotFound
import com.thebrownfoxx.neon.common.data.transaction.ReversibleUnitOutcome
import com.thebrownfoxx.neon.common.data.transaction.asReversible
import com.thebrownfoxx.neon.common.type.Failure
import com.thebrownfoxx.neon.common.type.Outcome
import com.thebrownfoxx.neon.common.type.asSuccess
import com.thebrownfoxx.neon.common.type.fold
import com.thebrownfoxx.neon.common.type.getOrElse
import com.thebrownfoxx.neon.common.type.unitSuccess
import com.thebrownfoxx.neon.server.repository.ConfigurationRepository
import org.jetbrains.exposed.sql.Database
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import org.jetbrains.exposed.sql.Table
import org.jetbrains.exposed.sql.selectAll
import org.jetbrains.exposed.sql.upsert

class ExposedConfigurationRepository(
    database: Database,
) : ConfigurationRepository, ExposedDataSource(database, ConfigurationTable) {
    override suspend fun getInitialized(): Outcome<Boolean, ConnectionError> {
        return dbQuery {
            ConfigurationTable
                .selectAll()
                .where(ConfigurationTable.key eq ConfigurationTable.INITIALIZED_KEY)
                .firstOrNotFound()
                .fold(
                    onSuccess = { it[ConfigurationTable.value].toBoolean() },
                    onFailure = { false },
                )
                .asSuccess()
        }
    }

    override suspend fun setInitialized(
        initialized: Boolean,
    ): ReversibleUnitOutcome<ConnectionError> {
        val oldInitialized = getInitialized()
            .getOrElse { return Failure(ConnectionError).asReversible() }

        dbQuery {
            ConfigurationTable.upsert {
                it[key] = INITIALIZED_KEY
                it[value] = initialized.toString()
            }
        }
        return unitSuccess().asReversible {
            ConfigurationTable.upsert {
                it[key] = INITIALIZED_KEY
                it[value] = oldInitialized.toString()
            }
        }
    }
}

private object ConfigurationTable : Table("configuration") {
    val key = varchar("key", length = 64)
    val value = varchar("value", length = 64)

    override val primaryKey: PrimaryKey = PrimaryKey(key)

    const val INITIALIZED_KEY = "INITIALIZED"
}