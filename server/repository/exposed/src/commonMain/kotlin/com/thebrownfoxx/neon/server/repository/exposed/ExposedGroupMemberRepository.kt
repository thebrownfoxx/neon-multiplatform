package com.thebrownfoxx.neon.server.repository.exposed

import com.thebrownfoxx.neon.common.data.AddError
import com.thebrownfoxx.neon.common.data.DataOperationError
import com.thebrownfoxx.neon.common.data.GetError
import com.thebrownfoxx.neon.common.data.ReactiveCache
import com.thebrownfoxx.neon.common.data.exposed.dataTransaction
import com.thebrownfoxx.neon.common.data.exposed.firstOrNotFound
import com.thebrownfoxx.neon.common.data.exposed.initializeExposeDatabase
import com.thebrownfoxx.neon.common.data.exposed.mapAddTransaction
import com.thebrownfoxx.neon.common.data.exposed.mapOperationTransaction
import com.thebrownfoxx.neon.common.data.exposed.toCommonUuid
import com.thebrownfoxx.neon.common.data.exposed.toJavaUuid
import com.thebrownfoxx.neon.common.data.exposed.tryAdd
import com.thebrownfoxx.neon.common.data.transaction.ReversibleUnitOutcome
import com.thebrownfoxx.neon.common.data.transaction.asReversible
import com.thebrownfoxx.neon.common.data.transaction.onFinalize
import com.thebrownfoxx.neon.common.type.id.GroupId
import com.thebrownfoxx.neon.common.type.id.MemberId
import com.thebrownfoxx.neon.server.repository.GroupMemberRepository
import com.thebrownfoxx.outcome.Failure
import com.thebrownfoxx.outcome.Outcome
import com.thebrownfoxx.outcome.map.onInnerSuccess
import com.thebrownfoxx.outcome.map.onOuterFailure
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.Flow
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
    externalScope: CoroutineScope,
) : GroupMemberRepository {
    init {
        initializeExposeDatabase(database, GroupMemberTable)
    }

    private val membersCache = ReactiveCache(externalScope, ::getMembers)
    private val groupsCache = ReactiveCache(externalScope, ::getGroups)
    private val adminsCache = ReactiveCache(externalScope, ::getAdmins)

    override fun getMembersAsFlow(
        groupId: GroupId,
    ): Flow<Outcome<Set<MemberId>, DataOperationError>> {
        return membersCache.getAsFlow(groupId)
    }

    override fun getGroupsAsFlow(
        memberId: MemberId,
    ): Flow<Outcome<Set<GroupId>, DataOperationError>> {
        return groupsCache.getAsFlow(memberId)
    }

    override fun getAdminsAsFlow(
        groupId: GroupId,
    ): Flow<Outcome<Set<MemberId>, DataOperationError>> {
        return adminsCache.getAsFlow(groupId)
    }

    override suspend fun getMembers(groupId: GroupId): Outcome<Set<MemberId>, DataOperationError> {
        return dataTransaction {
            GroupMemberTable
                .selectAll()
                .where(GroupMemberTable.groupId eq groupId.toJavaUuid())
                .map { MemberId(it[GroupMemberTable.memberId].toCommonUuid()) }
                .toSet()
        }.mapOperationTransaction()
    }

    override suspend fun getGroups(memberId: MemberId): Outcome<Set<GroupId>, DataOperationError> {
        return dataTransaction {
            GroupMemberTable
                .selectAll()
                .where(GroupMemberTable.memberId eq memberId.toJavaUuid())
                .map { GroupId(it[GroupMemberTable.groupId].toCommonUuid()) }
                .toSet()
        }.mapOperationTransaction()
    }

    override suspend fun getAdmins(groupId: GroupId): Outcome<Set<MemberId>, DataOperationError> {
        return dataTransaction {
            val groupIdMatches = GroupMemberTable.groupId eq groupId.toJavaUuid()
            val isAdmin = GroupMemberTable.isAdmin eq true
            GroupMemberTable
                .selectAll()
                .where(groupIdMatches and isAdmin)
                .map { MemberId(it[GroupMemberTable.memberId].toCommonUuid()) }
                .toSet()
        }.mapOperationTransaction()
    }

    override suspend fun addMember(
        groupId: GroupId,
        memberId: MemberId,
        isAdmin: Boolean,
    ): ReversibleUnitOutcome<AddError> {
        dataTransaction { getMembership(groupId, memberId) }
            .onInnerSuccess { return Failure(AddError.Duplicate).asReversible() }
            .onOuterFailure { return Failure(it.toAddError()).asReversible() }

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
            .asReversible {
                dataTransaction { GroupMemberTable.deleteWhere { GroupMemberTable.id eq id } }
            }.onFinalize {
                membersCache.update(groupId)
                groupsCache.update(memberId)
                adminsCache.update(groupId)
            }
    }

    private fun getMembership(
        groupId: GroupId,
        memberId: MemberId,
    ): Outcome<ResultRow, GetError> {
        val groupIdMatches = GroupMemberTable.groupId eq groupId.toJavaUuid()
        val memberIdMatches = GroupMemberTable.memberId eq memberId.toJavaUuid()
        return GroupMemberTable
            .selectAll()
            .where(groupIdMatches and memberIdMatches)
            .firstOrNotFound()
    }
}

internal object GroupMemberTable : Table("group_member") {
    val id = uuid("id")
    val groupId = uuid("group_id")
    val memberId = uuid("member_id")
    val isAdmin = bool("is_admin")

    override val primaryKey = PrimaryKey(id)
}