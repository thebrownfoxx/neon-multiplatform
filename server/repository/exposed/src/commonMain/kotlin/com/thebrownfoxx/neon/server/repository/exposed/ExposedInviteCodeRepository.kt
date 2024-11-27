//package com.thebrownfoxx.neon.server.repository.exposed
//
//import com.thebrownfoxx.neon.common.data.exposed.ExposedDataSource
//import com.thebrownfoxx.neon.common.data.exposed.dbQuery
//import com.thebrownfoxx.neon.common.data.exposed.toCommonUuid
//import com.thebrownfoxx.neon.common.data.exposed.toJavaUuid
//import com.thebrownfoxx.neon.common.type.Failure
//import com.thebrownfoxx.neon.common.type.Outcome
//import com.thebrownfoxx.neon.common.type.Success
//import com.thebrownfoxx.neon.common.type.UnitOutcome
//import com.thebrownfoxx.neon.common.type.id.GroupId
//import com.thebrownfoxx.neon.common.type.unitSuccess
//import com.thebrownfoxx.neon.server.repository.InviteCodeRepository
//import com.thebrownfoxx.neon.server.repository.invite.model.RepositoryGetInviteCodeError
//import com.thebrownfoxx.neon.server.repository.invite.model.RepositoryGetInviteCodeGroupError
//import com.thebrownfoxx.neon.server.repository.invite.model.RepositorySetInviteCodeError
//import kotlinx.coroutines.flow.Flow
//import kotlinx.coroutines.flow.MutableSharedFlow
//import kotlinx.coroutines.launch
//import org.jetbrains.exposed.sql.Database
//import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
//import org.jetbrains.exposed.sql.Table
//import org.jetbrains.exposed.sql.selectAll
//import org.jetbrains.exposed.sql.upsert
//import java.util.UUID
//
//class ExposedInviteCodeRepository(
//    database: Database,
//) : InviteCodeRepository, ExposedDataSource(database) {
//    private object InviteCodeTable : Table() {
//        val id = uuid("id")
//        val groupId = uuid("group_id").uniqueIndex()
//        val inviteCode = varchar("invite_code", 16).uniqueIndex()
//
//        override val primaryKey = PrimaryKey(id)
//    }
//
//    override val table: Table = InviteCodeTable
//
//    private val flows =
//        HashMap<GroupId, MutableSharedFlow<Outcome<String, RepositoryGetInviteCodeError>>>()
//
//    override fun get(groupId: GroupId): Flow<Outcome<String, RepositoryGetInviteCodeError>> {
//        val savedFlow = flows[groupId]
//        if (savedFlow != null) return savedFlow
//
//        val sharedFlow =
//            MutableSharedFlow<Outcome<String, RepositoryGetInviteCodeError>>(replay = 1)
//        flows[groupId] = sharedFlow
//        updateFromDatabase(groupId)
//        return sharedFlow
//    }
//
//    override suspend fun getGroup(
//        inviteCode: String,
//    ): Outcome<GroupId, RepositoryGetInviteCodeGroupError> {
//        val groupUuid = dbQuery {
//            InviteCodeTable
//                .selectAll()
//                .where(InviteCodeTable.inviteCode eq inviteCode)
//                .firstOrNull()
//                ?.get(InviteCodeTable.groupId)
//                ?.toCommonUuid()
//        }
//        return when (groupUuid) {
//            null -> Failure(RepositoryGetInviteCodeGroupError.NotFound)
//            else -> Success(GroupId(groupUuid))
//        }
//    }
//
//    override suspend fun set(
//        groupId: GroupId,
//        inviteCode: String,
//    ): UnitOutcome<RepositorySetInviteCodeError> {
//        dbQuery {
//            InviteCodeTable.upsert {
//                it[this.id] = UUID.randomUUID()
//                it[this.groupId] = groupId.toJavaUuid()
//                it[this.inviteCode] = inviteCode
//            }
//        }
//        updateFromDatabase(groupId)
//        return unitSuccess()
//    }
//
//    private fun updateFromDatabase(id: GroupId) {
//        flows[id]?.let { flow ->
//            dataSourceScope.launch {
//                val inviteCode = dbQuery {
//                    InviteCodeTable
//                        .selectAll()
//                        .where(InviteCodeTable.groupId eq id.toJavaUuid())
//                        .firstOrNull()
//                        ?.get(InviteCodeTable.inviteCode)
//                }
//                val inviteCodeOutcome = when (inviteCode) {
//                    null -> Failure(RepositoryGetInviteCodeError.NotFound)
//                    else -> Success(inviteCode)
//                }
//                flow.emit(inviteCodeOutcome)
//            }
//        }
//    }
//
//}