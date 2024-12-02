package com.thebrownfoxx.neon.server.repository.exposed

import com.thebrownfoxx.neon.common.data.AddError
import com.thebrownfoxx.neon.common.data.ConnectionError
import com.thebrownfoxx.neon.common.data.exposed.ExposedDataSource
import com.thebrownfoxx.neon.common.data.exposed.dataTransaction
import com.thebrownfoxx.neon.common.data.exposed.firstOrNotFound
import com.thebrownfoxx.neon.common.data.exposed.mapAddTransaction
import com.thebrownfoxx.neon.common.data.exposed.toCommonUuid
import com.thebrownfoxx.neon.common.data.exposed.toJavaUuid
import com.thebrownfoxx.neon.common.data.exposed.tryAdd
import com.thebrownfoxx.neon.common.data.transaction.ReversibleUnitOutcome
import com.thebrownfoxx.neon.common.data.transaction.asReversible
import com.thebrownfoxx.neon.common.outcome.Failure
import com.thebrownfoxx.neon.common.outcome.Outcome
import com.thebrownfoxx.neon.common.outcome.mapError
import com.thebrownfoxx.neon.common.outcome.onInnerSuccess
import com.thebrownfoxx.neon.common.outcome.onOuterFailure
import com.thebrownfoxx.neon.common.outcome.onSuccess
import com.thebrownfoxx.neon.common.type.id.GroupId
import com.thebrownfoxx.neon.common.type.id.MemberId
import com.thebrownfoxx.neon.server.repository.GroupMemberRepository
import org.jetbrains.exposed.sql.Database
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import org.jetbrains.exposed.sql.Table
import org.jetbrains.exposed.sql.and
import org.jetbrains.exposed.sql.deleteWhere
import org.jetbrains.exposed.sql.selectAll
import java.util.UUID

class ExposedGroupMemberRepository(
    database: Database,
) : GroupMemberRepository, ExposedDataSource(database, GroupMemberTable) {
    private val reactiveMembersCache = ReactiveCache(::getMembers)
    private val reactiveGroupsCache = ReactiveCache(::getGroups)
    private val reactiveAdminsCache = ReactiveCache(::getAdmins)

    override fun getMembersAsFlow(groupId: GroupId) = reactiveMembersCache.getAsFlow(groupId)

    override fun getGroupsAsFlow(memberId: MemberId) = reactiveGroupsCache.getAsFlow(memberId)

    override fun getAdminsAsFlow(groupId: GroupId) = reactiveAdminsCache.getAsFlow(groupId)

    override suspend fun getMembers(groupId: GroupId): Outcome<List<MemberId>, ConnectionError> {
        return dataTransaction {
            GroupMemberTable
                .selectAll()
                .where(GroupMemberTable.groupId eq groupId.toJavaUuid())
                .map { MemberId(it[GroupMemberTable.memberId].toCommonUuid()) }
        }.mapError { ConnectionError }
    }

    override suspend fun addMember(
        groupId: GroupId,
        memberId: MemberId,
        isAdmin: Boolean,
    ): ReversibleUnitOutcome<AddError> {
        dataTransaction {
            GroupMemberTable
                .selectAll()
                .where(
                    (GroupMemberTable.groupId eq groupId.toJavaUuid()) and
                            (GroupMemberTable.memberId eq memberId.toJavaUuid())
                )
                .firstOrNotFound()
        }
            .onInnerSuccess { return Failure(AddError.Duplicate).asReversible() }
            .onOuterFailure { return Failure(AddError.ConnectionError).asReversible() }

        val id = UUID.randomUUID()
        return dataTransaction {
            GroupMemberTable.tryAdd {
                it[this.id] = id
                it[this.groupId] = groupId.toJavaUuid()
                it[this.memberId] = memberId.toJavaUuid()
                it[this.isAdmin] = isAdmin
            }
        }
            .mapAddTransaction()
            .onSuccess {
                reactiveMembersCache.updateCache(groupId)
                reactiveGroupsCache.updateCache(memberId)
                reactiveAdminsCache.updateCache(groupId)
            }
            .asReversible {
                dataTransaction { GroupMemberTable.deleteWhere { GroupMemberTable.id eq id } }
            }
    }

    override suspend fun getGroups(memberId: MemberId): Outcome<List<GroupId>, ConnectionError> {
        return dataTransaction {
            GroupMemberTable
                .selectAll()
                .where(GroupMemberTable.memberId eq memberId.toJavaUuid())
                .map { GroupId(it[GroupMemberTable.groupId].toCommonUuid()) }
        }.mapError { ConnectionError }
    }

    override suspend fun getAdmins(groupId: GroupId): Outcome<List<MemberId>, ConnectionError> {
        return dataTransaction {
            GroupMemberTable
                .selectAll()
                .where(
                    (GroupMemberTable.groupId eq groupId.toJavaUuid()) and
                            (GroupMemberTable.isAdmin eq true)
                )
                .map { MemberId(it[GroupMemberTable.memberId].toCommonUuid()) }
        }.mapError { ConnectionError }
    }
}

internal object GroupMemberTable : Table("group_member") {
    val id = uuid("id")
    val groupId = uuid("group_id")
    val memberId = uuid("member_id")
    val isAdmin = bool("is_admin")

    override val primaryKey = PrimaryKey(id)
}