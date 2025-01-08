package com.thebrownfoxx.neon.server.repository.exposed

import com.thebrownfoxx.neon.common.data.DataOperationError
import com.thebrownfoxx.neon.common.data.exposed.dataTransaction
import com.thebrownfoxx.neon.common.data.exposed.firstOrNotFound
import com.thebrownfoxx.neon.common.data.exposed.initializeExposeDatabase
import com.thebrownfoxx.neon.common.data.exposed.mapOperationTransaction
import com.thebrownfoxx.neon.common.data.transaction.ReversibleUnitOutcome
import com.thebrownfoxx.neon.common.data.transaction.asReversible
import com.thebrownfoxx.neon.server.repository.ConfigurationRepository
import com.thebrownfoxx.outcome.Outcome
import com.thebrownfoxx.outcome.UnitSuccess
import com.thebrownfoxx.outcome.map.fold
import com.thebrownfoxx.outcome.map.getOrElse
import com.thebrownfoxx.outcome.map.onFailure
import org.jetbrains.exposed.sql.Database
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import org.jetbrains.exposed.sql.Table
import org.jetbrains.exposed.sql.selectAll
import org.jetbrains.exposed.sql.upsert

class ExposedConfigurationRepository(
    database: Database,
) : ConfigurationRepository {
    init {
        initializeExposeDatabase(database, ConfigurationTable)
    }

    override suspend fun getInitialized(): Outcome<Boolean, DataOperationError> {
        return dataTransaction {
            ConfigurationTable
                .selectAll()
                .where(ConfigurationTable.key eq ConfigurationTable.INITIALIZED_KEY)
                .firstOrNotFound()
                .fold(
                    onSuccess = { it[ConfigurationTable.value].toBoolean() },
                    onFailure = { false },
                )
        }.mapOperationTransaction()
    }

    override suspend fun setInitialized(
        initialized: Boolean,
    ): ReversibleUnitOutcome<DataOperationError> {
        val oldInitialized = getInitialized()
            .getOrElse { return Failure(it).asReversible() }

        dataTransaction {
            ConfigurationTable.upsert {
                it[key] = INITIALIZED_KEY
                it[value] = initialized.toString()
            }
        }.onFailure { return Failure(it.toDataOperationError()).asReversible() }

        return UnitSuccess.asReversible {
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