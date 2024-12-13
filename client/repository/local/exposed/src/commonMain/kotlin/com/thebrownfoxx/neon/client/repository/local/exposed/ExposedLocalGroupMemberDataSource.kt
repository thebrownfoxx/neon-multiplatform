package com.thebrownfoxx.neon.client.repository.local.exposed

import com.thebrownfoxx.neon.client.repository.local.LocalGroupMemberDataSource
import com.thebrownfoxx.neon.client.repository.local.LocalGroupMemberDataSource.GroupMember
import com.thebrownfoxx.neon.common.data.DataOperationError
import com.thebrownfoxx.neon.common.data.exposed.ExposedDataSource
import com.thebrownfoxx.neon.common.data.exposed.dataTransaction
import com.thebrownfoxx.neon.common.data.exposed.mapOperationTransaction
import com.thebrownfoxx.neon.common.data.exposed.mapUnitOperationTransaction
import com.thebrownfoxx.neon.common.data.exposed.toCommonUuid
import com.thebrownfoxx.neon.common.data.exposed.toJavaUuid
import com.thebrownfoxx.neon.common.type.id.GroupId
import com.thebrownfoxx.neon.common.type.id.MemberId
import com.thebrownfoxx.outcome.Outcome
import com.thebrownfoxx.outcome.UnitOutcome
import org.jetbrains.exposed.sql.Database
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import org.jetbrains.exposed.sql.Table
import org.jetbrains.exposed.sql.batchUpsert
import org.jetbrains.exposed.sql.selectAll

class ExposedLocalGroupMemberDataSource(
    database: Database,
) : LocalGroupMemberDataSource, ExposedDataSource(database, LocalGroupMemberTable) {
    private val reactiveCache = ReactiveCache(::getMembers)

    override fun getMembersAsFlow(groupId: GroupId) = reactiveCache.getAsFlow(groupId)

    override suspend fun batchUpsert(
        groupMembers: List<GroupMember>,
    ): UnitOutcome<DataOperationError> {
        return dataTransaction {
            LocalGroupMemberTable.batchUpsert(groupMembers) { groupMember ->
                this[LocalGroupMemberTable.groupId] = groupMember.groupId.toJavaUuid()
                this[LocalGroupMemberTable.memberId] = groupMember.memberId.toJavaUuid()
                this[LocalGroupMemberTable.isAdmin] = groupMember.isAdmin
            }
        }.mapUnitOperationTransaction()
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