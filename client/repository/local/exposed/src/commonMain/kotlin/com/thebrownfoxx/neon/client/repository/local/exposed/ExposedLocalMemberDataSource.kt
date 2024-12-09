package com.thebrownfoxx.neon.client.repository.local.exposed

import com.thebrownfoxx.neon.client.model.LocalMember
import com.thebrownfoxx.neon.client.repository.local.LocalMemberDataSource
import com.thebrownfoxx.neon.common.data.DataOperationError
import com.thebrownfoxx.neon.common.data.GetError
import com.thebrownfoxx.neon.common.data.exposed.ExposedDataSource
import com.thebrownfoxx.neon.common.data.exposed.dataTransaction
import com.thebrownfoxx.neon.common.data.exposed.firstOrNotFound
import com.thebrownfoxx.neon.common.data.exposed.mapGetTransaction
import com.thebrownfoxx.neon.common.data.exposed.mapUnitOperationTransaction
import com.thebrownfoxx.neon.common.data.exposed.toCommonUuid
import com.thebrownfoxx.neon.common.data.exposed.toJavaUuid
import com.thebrownfoxx.neon.common.type.Url
import com.thebrownfoxx.neon.common.type.id.MemberId
import com.thebrownfoxx.outcome.Outcome
import com.thebrownfoxx.outcome.UnitOutcome
import com.thebrownfoxx.outcome.map
import com.thebrownfoxx.outcome.memberBlockContext
import com.thebrownfoxx.outcome.onSuccess
import org.jetbrains.exposed.sql.Database
import org.jetbrains.exposed.sql.ResultRow
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import org.jetbrains.exposed.sql.Table
import org.jetbrains.exposed.sql.selectAll
import org.jetbrains.exposed.sql.upsert

class ExposedLocalMemberDataSource(
    database: Database,
) : LocalMemberDataSource, ExposedDataSource(database, LocalMemberTable) {
    private val reactiveCache = ReactiveCache(::get)

    override fun getAsFlow(id: MemberId) = reactiveCache.getAsFlow(id)

    override suspend fun upsert(member: LocalMember): UnitOutcome<DataOperationError> {
        memberBlockContext("upsert") {
            return dataTransaction {
                LocalMemberTable.upsert {
                    it[id] = member.id.toJavaUuid()
                    it[username] = member.username
                    it[avatarUrl] = member.avatarUrl?.value
                }
            }
                .mapUnitOperationTransaction(context)
                .onSuccess { reactiveCache.update(member.id) }
        }
    }

    private suspend fun get(id: MemberId): Outcome<LocalMember, GetError> {
        memberBlockContext("get") {
            return dataTransaction {
                LocalMemberTable
                    .selectAll()
                    .where(LocalMemberTable.id eq id.toJavaUuid())
                    .firstOrNotFound(context)
                    .map { it.toLocalMember() }
            }.mapGetTransaction(context)
        }
    }

    private fun ResultRow.toLocalMember() = LocalMember(
        id = MemberId(this[LocalMemberTable.id].toCommonUuid()),
        username = this[LocalMemberTable.username],
        avatarUrl = this[LocalMemberTable.avatarUrl]?.let { Url(it) },
    )
}

private object LocalMemberTable : Table("local_member") {
    val id = uuid("id")
    val username = varchar("username", 16).uniqueIndex()
    val avatarUrl = varchar("avatar_url", 2048).nullable()

    override val primaryKey = PrimaryKey(id)
}