package com.thebrownfoxx.neon.common.data.exposed

import org.jetbrains.exposed.sql.Database
import org.jetbrains.exposed.sql.SchemaUtils
import org.jetbrains.exposed.sql.Table
import org.jetbrains.exposed.sql.transactions.transaction

fun initializeExposeDatabase(
    database: Database,
    vararg tables: Table,
) {
    transaction(database) {
        tables.forEach { SchemaUtils.create(it) }
    }
}