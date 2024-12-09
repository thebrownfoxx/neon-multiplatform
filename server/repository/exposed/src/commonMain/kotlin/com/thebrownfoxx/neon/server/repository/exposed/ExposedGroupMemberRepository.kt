package com.thebrownfoxx.neon.server.repository.exposed

import com.thebrownfoxx.neon.common.data.AddError
import com.thebrownfoxx.neon.common.data.DataOperationError
import com.thebrownfoxx.neon.common.data.GetError
import com.thebrownfoxx.neon.common.data.exposed.ExposedDataSource
import com.thebrownfoxx.neon.common.data.exposed.dataTransaction
import com.thebrownfoxx.neon.common.data.exposed.firstOrNotFound
import com.thebrownfoxx.neon.common.data.exposed.mapAddTransaction
import com.thebrownfoxx.neon.common.data.exposed.mapOperationTransaction
import com.thebrownfoxx.neon.common.data.exposed.toCommonUuid
import com.thebrownfoxx.neon.common.data.exposed.toJavaUuid
import com.thebrownfoxx.neon.common.data.exposed.tryAdd
import com.thebrownfoxx.neon.common.data.transaction.ReversibleUnitOutcome
import com.thebrownfoxx.neon.common.data.transaction.asReversible
import com.thebrownfoxx.neon.common.type.id.GroupId
import com.thebrownfoxx.neon.common.type.id.MemberId
import com.thebrownfoxx.neon.server.repository.GroupMemberRepository
import com.thebrownfoxx.outcome.BlockContextScope
import com.thebrownfoxx.outcome.Outcome
import com.thebrownfoxx.outcome.memberBlockContext
import com.thebrownfoxx.outcome.onInnerSuccess
import com.thebrownfoxx.outcome.onOuterFailure
import org.jetbrains.exposed.sql.Database
import org.jetbrains.exposed.sql.ResultRow
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

    override suspend fun getMembers(groupId: GroupId): Outcome<List<MemberId>, DataOperationError> {
        memberBlockContext("getMembers") {
            return dataTransaction {
                GroupMemberTable
                    .selectAll()
                    .where(GroupMemberTable.groupId eq groupId.toJavaUuid())
                    .map { MemberId(it[GroupMemberTable.memberId].toCommonUuid()) }
            }.mapOperationTransaction(context)
        }
    }

    override suspend fun getGroups(memberId: MemberId): Outcome<List<GroupId>, DataOperationError> {
        memberBlockContext("getGroups") {
            return dataTransaction {
                GroupMemberTable
                    .selectAll()
                    .where(GroupMemberTable.memberId eq memberId.toJavaUuid())
                    .map { GroupId(it[GroupMemberTable.groupId].toCommonUuid()) }
            }.mapOperationTransaction(context)
        }
    }

    override suspend fun getAdmins(groupId: GroupId): Outcome<List<MemberId>, DataOperationError> {
        memberBlockContext("getAdmins") {
            return dataTransaction {
                GroupMemberTable
                    .selectAll()
                    .where(
                        (GroupMemberTable.groupId eq groupId.toJavaUuid()) and
                                (GroupMemberTable.isAdmin eq true)
                    )
                    .map { MemberId(it[GroupMemberTable.memberId].toCommonUuid()) }
            }.mapOperationTransaction(context)
        }
    }

    override suspend fun addMember(
        groupId: GroupId,
        memberId: MemberId,
        isAdmin: Boolean,
    ): ReversibleUnitOutcome<AddError> {
        memberBlockContext("addMember") {
            dataTransaction { getMembership(groupId, memberId) }
                .onInnerSuccess { return Failure(AddError.Duplicate).asReversible() }
                .onOuterFailure {
                    return mapError(error.toAddError()).asReversible()
                }

            val id = UUID.randomUUID()
            return dataTransaction {
                GroupMemberTable.tryAdd(context) {
                    it[this.id] = id
                    it[this.groupId] = groupId.toJavaUuid()
                    it[this.memberId] = memberId.toJavaUuid()
                    it[this.isAdmin] = isAdmin
                }
            }
                .mapAddTransaction(context)
                .asReversible(
                    finalize = {
                        reactiveMembersCache.update(groupId)
                        reactiveGroupsCache.update(memberId)
                        reactiveAdminsCache.update(groupId)
                    }
                ) {
                    dataTransaction { GroupMemberTable.deleteWhere { GroupMemberTable.id eq id } }
                }
        }
    }

    private fun BlockContextScope.getMembership(
        groupId: GroupId,
        memberId: MemberId,
    ): Outcome<ResultRow, GetError> {
        return GroupMemberTable
            .selectAll()
            .where(
                (GroupMemberTable.groupId eq groupId.toJavaUuid()) and
                        (GroupMemberTable.memberId eq memberId.toJavaUuid())
            )
            .firstOrNotFound(context)
    }
}

internal object GroupMemberTable : Table("group_member") {
    val id = uuid("id")
    val groupId = uuid("group_id")
    val memberId = uuid("member_id")
    val isAdmin = bool("is_admin")

    override val primaryKey = PrimaryKey(id)
}