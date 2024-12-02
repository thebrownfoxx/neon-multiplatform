package com.thebrownfoxx.neon.server.repository.exposed

import com.thebrownfoxx.neon.common.data.ConnectionError
import com.thebrownfoxx.neon.common.data.exposed.ExposedDataSource
import com.thebrownfoxx.neon.common.data.exposed.dataTransaction
import com.thebrownfoxx.neon.common.data.exposed.firstOrNotFound
import com.thebrownfoxx.neon.common.data.transaction.ReversibleUnitOutcome
import com.thebrownfoxx.neon.common.data.transaction.asReversible
import com.thebrownfoxx.neon.common.outcome.Failure
import com.thebrownfoxx.neon.common.outcome.Outcome
import com.thebrownfoxx.neon.common.outcome.fold
import com.thebrownfoxx.neon.common.outcome.getOrElse
import com.thebrownfoxx.neon.common.outcome.mapError
import com.thebrownfoxx.neon.common.outcome.onFailure
import com.thebrownfoxx.neon.common.outcome.unitSuccess
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
        return dataTransaction {
            ConfigurationTable
                .selectAll()
                .where(ConfigurationTable.key eq ConfigurationTable.INITIALIZED_KEY)
                .firstOrNotFound()
                .fold(
                    onSuccess = { it[ConfigurationTable.value].toBoolean() },
                    onFailure = { false },
                )
        }.mapError { ConnectionError }
    }

    override suspend fun setInitialized(
        initialized: Boolean,
    ): ReversibleUnitOutcome<ConnectionError> {
        val oldInitialized = getInitialized()
            .getOrElse { return Failure(ConnectionError).asReversible() }

        dataTransaction {
            ConfigurationTable.upsert {
                it[key] = INITIALIZED_KEY
                it[value] = initialized.toString()
            }
        }.onFailure { return Failure(ConnectionError).asReversible() }

        return unitSuccess().asReversible {
            dataTransaction {
                ConfigurationTable.upsert {
                    it[key] = INITIALIZED_KEY
                    it[value] = oldInitialized.toString()
                }
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