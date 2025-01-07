package com.thebrownfoxx.neon.client.repository.exposed

import com.thebrownfoxx.neon.client.repository.GroupMemberRepository
import com.thebrownfoxx.neon.common.data.DataOperationError
import com.thebrownfoxx.neon.common.data.ReactiveCache
import com.thebrownfoxx.neon.common.data.exposed.dataTransaction
import com.thebrownfoxx.neon.common.data.exposed.initializeExposeDatabase
import com.thebrownfoxx.neon.common.data.exposed.mapOperationTransaction
import com.thebrownfoxx.neon.common.data.exposed.mapUnitOperationTransaction
import com.thebrownfoxx.neon.common.data.exposed.toCommonUuid
import com.thebrownfoxx.neon.common.data.exposed.toJavaUuid
import com.thebrownfoxx.neon.common.type.id.GroupId
import com.thebrownfoxx.neon.common.type.id.MemberId
import com.thebrownfoxx.outcome.Outcome
import com.thebrownfoxx.outcome.UnitOutcome
import com.thebrownfoxx.outcome.map.onSuccess
import kotlinx.coroutines.CoroutineScope
import org.jetbrains.exposed.sql.Database
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import org.jetbrains.exposed.sql.Table
import org.jetbrains.exposed.sql.batchUpsert
import org.jetbrains.exposed.sql.selectAll

class ExposedGroupMemberRepository(
    database: Database,
    externalScope: CoroutineScope,
) : GroupMemberRepository {
    init {
        initializeExposeDatabase(database, LocalGroupMemberTable)
    }

    private val cache = ReactiveCache(externalScope, ::getMembers)

    override fun getMembersAsFlow(groupId: GroupId) = cache.getAsFlow(groupId)

    override suspend fun batchUpsert(
        groupId: GroupId,
        memberIds: Set<MemberId>,
    ): UnitOutcome<DataOperationError> {
        return dataTransaction {
            LocalGroupMemberTable.batchUpsert(memberIds) { memberId ->
                this[LocalGroupMemberTable.groupId] = groupId.toJavaUuid()
                this[LocalGroupMemberTable.memberId] = memberId.toJavaUuid()
                this[LocalGroupMemberTable.isAdmin] = false
            }
        }
            .mapUnitOperationTransaction()
            .onSuccess { cache.update(groupId) }
    }

    private suspend fun getMembers(groupId: GroupId): Outcome<Set<MemberId>, DataOperationError> {
        return dataTransaction {
            LocalGroupMemberTable
                .selectAll()
                .where(LocalGroupMemberTable.groupId eq groupId.toJavaUuid())
                .map { MemberId(it[LocalGroupMemberTable.memberId].toCommonUuid()) }
                .toSet()
        }.mapOperationTransaction()
    }
}

private object LocalGroupMemberTable : Table("group_member") {
    val groupId = uuid("group_id")
    val memberId = uuid("member_id")
    val isAdmin = bool("is_admin")

    override val primaryKey = PrimaryKey(groupId, memberId)
}