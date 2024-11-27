//package com.thebrownfoxx.neon.server.repository.exposed
//
//import com.thebrownfoxx.neon.common.data.ConnectionError
//import com.thebrownfoxx.neon.common.data.exposed.ExposedDataSource
//import com.thebrownfoxx.neon.common.data.exposed.dbQuery
//import com.thebrownfoxx.neon.common.data.exposed.toCommonUuid
//import com.thebrownfoxx.neon.common.data.exposed.toJavaUuid
//import com.thebrownfoxx.neon.common.type.Failure
//import com.thebrownfoxx.neon.common.type.Outcome
//import com.thebrownfoxx.neon.common.type.Success
//import com.thebrownfoxx.neon.common.type.UnitOutcome
//import com.thebrownfoxx.neon.common.type.id.GroupId
//import com.thebrownfoxx.neon.common.type.id.MemberId
//import com.thebrownfoxx.neon.common.type.onFailure
//import com.thebrownfoxx.neon.common.type.runFailing
//import com.thebrownfoxx.neon.common.type.unitSuccess
//import com.thebrownfoxx.neon.server.repository.GroupMemberRepository
//import com.thebrownfoxx.neon.server.repository.groupmember.model.RepositoryAddGroupMemberError
//import com.thebrownfoxx.neon.server.repository.groupmember.model.RepositoryGetAdminsError
//import com.thebrownfoxx.neon.server.repository.groupmember.model.RepositoryGetGroupMembersError
//import com.thebrownfoxx.neon.server.repository.groupmember.model.RepositoryGetMemberGroupsError
//import kotlinx.coroutines.flow.Flow
//import kotlinx.coroutines.flow.MutableStateFlow
//import kotlinx.coroutines.flow.map
//import kotlinx.coroutines.flow.update
//import kotlinx.coroutines.launch
//import org.jetbrains.exposed.sql.Database
//import org.jetbrains.exposed.sql.ResultRow
//import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
//import org.jetbrains.exposed.sql.Table
//import org.jetbrains.exposed.sql.and
//import org.jetbrains.exposed.sql.insert
//import org.jetbrains.exposed.sql.selectAll
//import java.util.UUID
//
//class ExposedGroupMemberRepository(
//    database: Database,
//) : GroupMemberRepository, ExposedDataSource(database, GroupMemberTable) {
//    private val flow = MutableStateFlow(emptySet<GroupMember>())
//
//    override fun getMembers(
//        groupId: GroupId,
//    ): Flow<Outcome<List<MemberId>, ConnectionError>> {
//        updateMembersFromDatabase(groupId)
//        return flow.map { groupMembers ->
//            Success(groupMembers.filter { it.groupId == groupId }.map { it.memberId })
//        }
//    }
//
//    override fun getGroups(
//        memberId: MemberId,
//    ): Flow<Outcome<List<GroupId>, RepositoryGetMemberGroupsError>> {
//        updateGroupsFromDatabase(memberId)
//        return flow.map { groupMembers ->
//            Success(groupMembers.filter { it.memberId == memberId }.map { it.groupId })
//        }
//    }
//
//    override fun getAdmins(
//        groupId: GroupId,
//    ): Flow<Outcome<List<MemberId>, RepositoryGetAdminsError>> {
//        updateMembersFromDatabase(groupId)
//        return flow.map { groupMembers ->
//            Success(groupMembers.filter { it.groupId == groupId && it.isAdmin }.map { it.memberId })
//        }
//    }
//
//    override suspend fun addMember(
//        groupId: GroupId,
//        memberId: MemberId,
//        isAdmin: Boolean,
//    ): UnitOutcome<RepositoryAddGroupMemberError> {
//        runFailing {
//            dbQuery {
//                GroupMemberTable.insert {
//                    it[this.id] = UUID.randomUUID()
//                    it[this.groupId] = groupId.toJavaUuid()
//                    it[this.memberId] = memberId.toJavaUuid()
//                    it[this.isAdmin] = isAdmin
//                }
//            }
//        }.onFailure { return Failure(RepositoryAddGroupMemberError.DuplicateMembership) }
//        if (flow.value.any { it.groupId == groupId || it.memberId == memberId }) {
//            // TODO: Investigate if this might lead to race conditions
//            updateMembershipFromDatabase(groupId, memberId)
//        }
//        return unitSuccess()
//    }
//
//    private fun updateGroupsFromDatabase(memberId: MemberId) {
//        dataSourceScope.launch {
//            val groupMembers = dbQuery {
//                GroupMemberTable
//                    .selectAll()
//                    .where(GroupMemberTable.memberId eq memberId.toJavaUuid())
//            }.map { it.toGroupMember() }
//            upsertGroupMembers(groupMembers)
//        }
//    }
//
//    private fun updateMembersFromDatabase(groupId: GroupId) {
//        dataSourceScope.launch {
//            val groupMembers = dbQuery {
//                GroupMemberTable
//                    .selectAll()
//                    .where(GroupMemberTable.groupId eq groupId.toJavaUuid())
//            }.map { it.toGroupMember() }
//            upsertGroupMembers(groupMembers)
//        }
//    }
//
//    private fun updateMembershipFromDatabase(groupId: GroupId, memberId: MemberId) {
//        dataSourceScope.launch {
//            dbQuery {
//                GroupMemberTable
//                    .selectAll()
//                    .where(
//                        (GroupMemberTable.groupId eq groupId.toJavaUuid()) and
//                                (GroupMemberTable.memberId eq memberId.toJavaUuid())
//                    ).firstOrNull()
//            }?.let { upsertGroupMembers(listOf(it.toGroupMember())) }
//        }
//    }
//
//    private fun ResultRow.toGroupMember(): GroupMember {
//        return GroupMember(
//            groupId = GroupId(this[GroupMemberTable.groupId].toCommonUuid()),
//            memberId = MemberId(this[GroupMemberTable.memberId].toCommonUuid()),
//            isAdmin = this[GroupMemberTable.isAdmin],
//        )
//    }
//
//    private fun upsertGroupMembers(groupMembers: List<GroupMember>) {
//        flow.update { oldGroupMembers ->
//            (groupMembers + oldGroupMembers).distinctBy { it.groupId to it.memberId }.toSet()
//        }
//    }
//}
//
//private object GroupMemberTable : Table() {
//    val id = uuid("id")
//    val groupId = uuid("group_id")
//    val memberId = uuid("member_id")
//    val isAdmin = bool("is_admin")
//
//    override val primaryKey = PrimaryKey(id)
//}
//
//private class GroupMember(
//    val groupId: GroupId,
//    val memberId: MemberId,
//    val isAdmin: Boolean,
//)
