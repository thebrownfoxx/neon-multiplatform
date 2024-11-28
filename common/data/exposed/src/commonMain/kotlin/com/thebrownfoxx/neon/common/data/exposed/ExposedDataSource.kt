package com.thebrownfoxx.neon.common.data.exposed

import com.thebrownfoxx.neon.common.data.Gettable
import com.thebrownfoxx.neon.common.data.ReactiveCache
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.plus
import org.jetbrains.exposed.sql.Database
import org.jetbrains.exposed.sql.SchemaUtils
import org.jetbrains.exposed.sql.Table
import org.jetbrains.exposed.sql.transactions.transaction

abstract class ExposedDataSource(
    database: Database,
    table: Table,
) {
    protected val dataSourceScope = CoroutineScope(Dispatchers.IO) + SupervisorJob()

    init {
        transaction(database) {
            SchemaUtils.create(table)
        }
    }

    protected fun <K, V> ReactiveCache(gettable: Gettable<K, V>) =
        ReactiveCache(dataSourceScope, gettable)
}