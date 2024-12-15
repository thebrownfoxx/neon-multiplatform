package com.thebrownfoxx.neon.common.data.exposed

import com.thebrownfoxx.neon.common.data.ReactiveCache
import com.thebrownfoxx.neon.common.data.SingleReactiveCache
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
    vararg tables: Table,
) {
    protected val dataSourceScope = CoroutineScope(Dispatchers.IO) + SupervisorJob()

    init {
        transaction(database) {
            for (table in tables) {
                SchemaUtils.create(table)
            }
        }
    }

    protected fun <K, V> ReactiveCache(get: suspend (K) -> V) =
        ReactiveCache(dataSourceScope, get)

    protected fun <V> SingleReactiveCache(get: suspend () -> V) =
        SingleReactiveCache(dataSourceScope, get)
}