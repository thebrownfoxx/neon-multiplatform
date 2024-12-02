package com.thebrownfoxx.neon.server.repository.exposed

import com.thebrownfoxx.neon.common.data.AddError
import com.thebrownfoxx.neon.common.data.GetError
import com.thebrownfoxx.neon.common.data.exposed.ExposedDataSource
import com.thebrownfoxx.neon.common.data.exposed.dataTransaction
import com.thebrownfoxx.neon.common.data.exposed.firstOrNotFound
import com.thebrownfoxx.neon.common.data.exposed.mapAddTransaction
import com.thebrownfoxx.neon.common.data.exposed.mapGetTransaction
import com.thebrownfoxx.neon.common.data.exposed.toCommonUuid
import com.thebrownfoxx.neon.common.data.exposed.toJavaUuid
import com.thebrownfoxx.neon.common.data.exposed.tryAdd
import com.thebrownfoxx.neon.common.data.transaction.ReversibleUnitOutcome
import com.thebrownfoxx.neon.common.data.transaction.asReversible
import com.thebrownfoxx.neon.common.outcome.Outcome
import com.thebrownfoxx.neon.common.outcome.map
import com.thebrownfoxx.neon.common.type.Url
import com.thebrownfoxx.neon.common.type.id.GroupId
import com.thebrownfoxx.neon.server.model.ChatGroup
import com.thebrownfoxx.neon.server.model.Community
import com.thebrownfoxx.neon.server.model.Group
import com.thebrownfoxx.neon.server.repository.GroupRepository
import org.jetbrains.exposed.sql.Database
import org.jetbrains.exposed.sql.ResultRow
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import org.jetbrains.exposed.sql.Table
import org.jetbrains.exposed.sql.deleteWhere
import org.jetbrains.exposed.sql.selectAll

class ExposedGroupRepository(
    database: Database,
) : GroupRepository, ExposedDataSource(database, GroupTable) {
    private val reactiveCache = ReactiveCache(::get)

    override fun getAsFlow(id: GroupId) = reactiveCache.getAsFlow(id)

    override suspend fun get(id: GroupId): Outcome<Group, GetError> {
        return dataTransaction {
            GroupTable
                .selectAll()
                .where(GroupTable.id eq id.toJavaUuid())
                .firstOrNotFound()
                .map { it.toGroup() }
        }.mapGetTransaction()
    }

    override suspend fun add(group: Group): ReversibleUnitOutcome<AddError> {
        return dataTransaction {
            GroupTable.tryAdd {
                it[id] = group.id.toJavaUuid()
                if (group is Community) {
                    it[name] = group.name
                    it[avatarUrl] = group.avatarUrl?.value
                    it[isGod] = group.isGod
                }
            }
        }
            .mapAddTransaction()
            .asReversible {
                dataTransaction { GroupTable.deleteWhere { id eq group.id.toJavaUuid() } }
            }
    }

    private fun ResultRow.toGroup(): Group {
        val id = GroupId(this[GroupTable.id].toCommonUuid())
        val name = this[GroupTable.name]
        val avatarUrl = this[GroupTable.avatarUrl]?.let { Url(it) }
        val god = this[GroupTable.isGod]

        if (name == null || god == null) return ChatGroup(id = id)

        return Community(
            id = id,
            name = name,
            avatarUrl = avatarUrl,
            isGod = god,
        )
    }
}

private object GroupTable : Table("group") {
    val id = uuid("id")
    val name = varchar("name", length = 64).nullable()
    val avatarUrl = varchar("avatar_url", length = 2048).nullable()
    val isGod = bool("is_god").nullable()

    override val primaryKey: PrimaryKey = PrimaryKey(id)
}