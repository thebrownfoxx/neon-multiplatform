package com.thebrownfoxx.neon.client.repository.exposed

import com.thebrownfoxx.neon.client.model.LocalChatGroup
import com.thebrownfoxx.neon.client.model.LocalCommunity
import com.thebrownfoxx.neon.client.model.LocalGroup
import com.thebrownfoxx.neon.client.repository.GroupRepository
import com.thebrownfoxx.neon.common.data.DataOperationError
import com.thebrownfoxx.neon.common.data.GetError
import com.thebrownfoxx.neon.common.data.ReactiveCache
import com.thebrownfoxx.neon.common.data.exposed.dataTransaction
import com.thebrownfoxx.neon.common.data.exposed.firstOrNotFound
import com.thebrownfoxx.neon.common.data.exposed.initializeExposeDatabase
import com.thebrownfoxx.neon.common.data.exposed.mapGetTransaction
import com.thebrownfoxx.neon.common.data.exposed.mapUnitOperationTransaction
import com.thebrownfoxx.neon.common.data.exposed.toCommonUuid
import com.thebrownfoxx.neon.common.data.exposed.toJavaUuid
import com.thebrownfoxx.neon.common.type.Url
import com.thebrownfoxx.neon.common.type.id.GroupId
import com.thebrownfoxx.outcome.Outcome
import com.thebrownfoxx.outcome.UnitOutcome
import com.thebrownfoxx.outcome.map.map
import com.thebrownfoxx.outcome.map.onSuccess
import kotlinx.coroutines.CoroutineScope
import org.jetbrains.exposed.sql.Database
import org.jetbrains.exposed.sql.ResultRow
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import org.jetbrains.exposed.sql.Table
import org.jetbrains.exposed.sql.selectAll
import org.jetbrains.exposed.sql.upsert

class ExposedGroupRepository(
    database: Database,
    externalScope: CoroutineScope,
) : GroupRepository {
    init {
        initializeExposeDatabase(database, LocalGroupTable)
    }

    private val cache = ReactiveCache(externalScope, ::get)

    override fun getAsFlow(id: GroupId) = cache.getAsFlow(id)

    override suspend fun upsert(group: LocalGroup): UnitOutcome<DataOperationError> {
        return dataTransaction {
            LocalGroupTable.upsert {
                it[id] = group.id.toJavaUuid()
                if (group is LocalCommunity) {
                    it[name] = group.name
                    it[avatarUrl] = group.avatarUrl?.value
                    it[isGod] = group.isGod
                }
            }
        }
            .mapUnitOperationTransaction()
            .onSuccess { cache.update(group.id) }
    }

    private suspend fun get(id: GroupId): Outcome<LocalGroup, GetError> {
        return dataTransaction {
            LocalGroupTable
                .selectAll()
                .where(LocalGroupTable.id eq id.toJavaUuid())
                .firstOrNotFound()
                .map { it.toLocalGroup() }
        }.mapGetTransaction()
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

private object LocalGroupTable : Table("group") {
    val id = uuid("id")
    val name = varchar("name", length = 64).nullable()
    val avatarUrl = varchar("avatar_url", length = 2048).nullable()
    val isGod = bool("is_god").nullable()

    override val primaryKey: PrimaryKey = PrimaryKey(id)
}