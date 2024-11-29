package com.thebrownfoxx.neon.server.repository.exposed

import com.thebrownfoxx.neon.common.data.AddError
import com.thebrownfoxx.neon.common.data.ConnectionError
import com.thebrownfoxx.neon.common.data.exposed.ExposedDataSource
import com.thebrownfoxx.neon.common.data.exposed.dbQuery
import com.thebrownfoxx.neon.common.data.exposed.firstOrNotFound
import com.thebrownfoxx.neon.common.data.exposed.toCommonUuid
import com.thebrownfoxx.neon.common.data.exposed.toJavaUuid
import com.thebrownfoxx.neon.common.data.exposed.tryAdd
import com.thebrownfoxx.neon.common.data.transaction.ReversibleUnitOutcome
import com.thebrownfoxx.neon.common.data.transaction.asReversible
import com.thebrownfoxx.neon.common.type.Failure
import com.thebrownfoxx.neon.common.type.Outcome
import com.thebrownfoxx.neon.common.type.asSuccess
import com.thebrownfoxx.neon.common.type.id.GroupId
import com.thebrownfoxx.neon.common.type.id.MemberId
import com.thebrownfoxx.neon.common.type.onSuccess
import com.thebrownfoxx.neon.server.repository.GroupMemberRepository
import org.jetbrains.exposed.sql.Database
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import org.jetbrains.exposed.sql.Table
import org.jetbrains.exposed.sql.and
import org.jetbrains.exposed.sql.deleteWhere
import org.jetbrains.exposed.sql.insert
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
        return dbQuery {
            GroupMemberTable
                .selectAll()
                .where(GroupMemberTable.groupId eq groupId.toJavaUuid())
                .map { MemberId(it[GroupMemberTable.memberId].toCommonUuid()) }
                .asSuccess()
        }
    }

    override suspend fun addMember(
        groupId: GroupId,
        memberId: MemberId,
        isAdmin: Boolean,
    ): ReversibleUnitOutcome<AddError> {
        dbQuery {
            GroupMemberTable
                .selectAll()
                .where(
                    (GroupMemberTable.groupId eq groupId.toJavaUuid()) and
                            (GroupMemberTable.memberId eq memberId.toJavaUuid())
                )
                .firstOrNotFound()
        }.onSuccess { return Failure(AddError.Duplicate).asReversible() }

        val id = UUID.randomUUID()
        return tryAdd {
            dbQuery {
                GroupMemberTable.insert {
                    it[this.id] = id
                    it[this.groupId] = groupId.toJavaUuid()
                    it[this.memberId] = memberId.toJavaUuid()
                    it[this.isAdmin] = isAdmin
                }
            }
        }.onSuccess {
            reactiveMembersCache.updateCache(groupId)
            reactiveGroupsCache.updateCache(memberId)
            reactiveAdminsCache.updateCache(groupId)
        }.asReversible { dbQuery { GroupMemberTable.deleteWhere { GroupMemberTable.id eq id } } }
    }

    override suspend fun getGroups(memberId: MemberId): Outcome<List<GroupId>, ConnectionError> {
        return dbQuery {
            GroupMemberTable
                .selectAll()
                .where(GroupMemberTable.memberId eq memberId.toJavaUuid())
                .map { GroupId(it[GroupMemberTable.groupId].toCommonUuid()) }
                .asSuccess()
        }
    }

    override suspend fun getAdmins(groupId: GroupId): Outcome<List<MemberId>, ConnectionError> {
        return dbQuery {
            GroupMemberTable
                .selectAll()
                .where(
                    (GroupMemberTable.groupId eq groupId.toJavaUuid()) and
                            (GroupMemberTable.isAdmin eq true)
                )
                .map { MemberId(it[GroupMemberTable.memberId].toCommonUuid()) }
                .asSuccess()
        }
    }
}

internal object GroupMemberTable : Table("group_member") {
    val id = uuid("id")
    val groupId = uuid("group_id")
    val memberId = uuid("member_id")
    val isAdmin = bool("is_admin")

    override val primaryKey = PrimaryKey(id)
}