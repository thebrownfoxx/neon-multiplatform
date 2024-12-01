package com.thebrownfoxx.neon.client.repository.local.exposed

import com.thebrownfoxx.neon.client.model.LocalChatGroup
import com.thebrownfoxx.neon.client.model.LocalCommunity
import com.thebrownfoxx.neon.client.model.LocalGroup
import com.thebrownfoxx.neon.client.repository.local.LocalGroupDataSource
import com.thebrownfoxx.neon.common.data.ConnectionError
import com.thebrownfoxx.neon.common.data.GetError
import com.thebrownfoxx.neon.common.data.exposed.ExposedDataSource
import com.thebrownfoxx.neon.common.data.exposed.dbQuery
import com.thebrownfoxx.neon.common.data.exposed.firstOrNotFound
import com.thebrownfoxx.neon.common.data.exposed.toCommonUuid
import com.thebrownfoxx.neon.common.data.exposed.toJavaUuid
import com.thebrownfoxx.neon.common.type.Outcome
import com.thebrownfoxx.neon.common.type.UnitOutcome
import com.thebrownfoxx.neon.common.type.Url
import com.thebrownfoxx.neon.common.type.id.GroupId
import com.thebrownfoxx.neon.common.type.map
import com.thebrownfoxx.neon.common.type.unitSuccess
import org.jetbrains.exposed.sql.Database
import org.jetbrains.exposed.sql.ResultRow
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import org.jetbrains.exposed.sql.Table
import org.jetbrains.exposed.sql.selectAll
import org.jetbrains.exposed.sql.upsert

class ExposedLocalGroupDataSource(
    database: Database,
) : LocalGroupDataSource, ExposedDataSource(database, LocalGroupTable) {
    private val reactiveCache = ReactiveCache(::get)

    override fun getAsFlow(id: GroupId) = reactiveCache.getAsFlow(id)

    override suspend fun upsert(group: LocalGroup): UnitOutcome<ConnectionError> {
        dbQuery {
            LocalGroupTable.upsert {
                it[id] = group.id.toJavaUuid()
                if (group is LocalCommunity) {
                    it[name] = group.name
                    it[avatarUrl] = group.avatarUrl?.value
                    it[isGod] = group.isGod
                }
            }
        }
        reactiveCache.updateCache(group.id)
        return unitSuccess()
    }

    private suspend fun get(id: GroupId): Outcome<LocalGroup, GetError> {
        return dbQuery {
            LocalGroupTable
                .selectAll()
                .where(LocalGroupTable.id eq id.toJavaUuid())
                .firstOrNotFound()
                .map { it.toLocalGroup() }
        }
    }

    private fun ResultRow.toLocalGroup(): LocalGroup {
        val id = GroupId(this[LocalGroupTable.id].toCommonUuid())
        val name = this[LocalGroupTable.name]
        val avatarUrl = this[LocalGroupTable.avatarUrl]?.let { Url(it) }
        val god = this[LocalGroupTable.isGod]

        if (name == null || god == null) return LocalChatGroup(id = id)

        return LocalCommunity(
            id = id,
            name = name,
            avatarUrl = avatarUrl,
            isGod = god,
        )
    }
}

private object LocalGroupTable : Table() {
    val id = uuid("id")
    val name = varchar(name = "name", length = 64).nullable()
    val avatarUrl = varchar(name = "avatar_url", length = 2048).nullable()
    val isGod = bool(name = "is_god").nullable()

    override val primaryKey: PrimaryKey = PrimaryKey(id)
}