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
//import com.thebrownfoxx.neon.common.type.asUrl
//import com.thebrownfoxx.neon.common.type.id.MemberId
//import com.thebrownfoxx.neon.common.type.unitSuccess
//import com.thebrownfoxx.neon.server.model.Member
//import com.thebrownfoxx.neon.server.repository.MemberRepository
//import com.thebrownfoxx.neon.server.repository.member.model.RepositoryAddMemberError
//import com.thebrownfoxx.neon.server.repository.member.model.RepositoryGetMemberError
//import com.thebrownfoxx.neon.server.repository.member.model.RepositoryGetMemberIdError
//import kotlinx.coroutines.flow.Flow
//import kotlinx.coroutines.flow.MutableSharedFlow
//import kotlinx.coroutines.launch
//import org.jetbrains.exposed.sql.Database
//import org.jetbrains.exposed.sql.ResultRow
//import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
//import org.jetbrains.exposed.sql.Table
//import org.jetbrains.exposed.sql.insert
//import org.jetbrains.exposed.sql.selectAll
//
//class ExposedMemberRepository(database: Database) : MemberRepository, ExposedDataSource(database) {
//    private object MemberTable : Table() {
//        val id = uuid("id")
//        val username = varchar("username", 32)
//        val avatarUrl = varchar("avatar_url", 2048).nullable()
//
//        override val primaryKey = PrimaryKey(id)
//    }
//
//    override val table: Table = MemberTable
//
//    private val memberFlows =
//        HashMap<MemberId, MutableSharedFlow<Outcome<Member, RepositoryGetMemberError>>>()
//
//    override fun get(id: MemberId): Flow<Outcome<Member, RepositoryGetMemberError>> {
//        val savedFlow = memberFlows[id]
//        if (savedFlow != null) return savedFlow
//
//        val sharedFlow = MutableSharedFlow<Outcome<Member, RepositoryGetMemberError>>(replay = 1)
//        memberFlows[id] = sharedFlow
//        updateFromDatabase(id)
//        return sharedFlow
//    }
//
//    override suspend fun getId(username: String): Outcome<MemberId, RepositoryGetMemberIdError> {
//        val uuid = dbQuery {
//            MemberTable
//                .selectAll()
//                .where(MemberTable.username eq username)
//                .firstOrNull()
//                ?.get(MemberTable.id)
//                ?.toCommonUuid()
//        }
//        return when (uuid) {
//            null -> Failure(RepositoryGetMemberIdError.NotFound)
//            else -> Success(MemberId(uuid))
//        }
//    }
//
//    override suspend fun add(member: Member): UnitOutcome<RepositoryAddMemberError> {
//        dbQuery {
//            MemberTable.insert {
//                it[id] = member.id.toJavaUuid()
//                it[username] = member.username
//                it[avatarUrl] = member.avatarUrl?.value
//            }
//        }
//        return unitSuccess()
//    }
//
//    private fun updateFromDatabase(id: MemberId) {
//        memberFlows[id]?.let { flow ->
//            dataSourceScope.launch {
//                val member = dbQuery {
//                    MemberTable
//                        .selectAll()
//                        .where(MemberTable.id eq id.toJavaUuid())
//                        .firstOrNull()
//                        ?.toMember()
//                }
//                val memberOutcome = when (member) {
//                    null -> Failure(RepositoryGetMemberError.NotFound)
//                    else -> Success(member)
//                }
//                flow.emit(memberOutcome)
//            }
//        }
//    }
//
//    private fun ResultRow.toMember() = Member(
//        id = MemberId(this[MemberTable.id].toCommonUuid()),
//        username = this[MemberTable.username],
//        avatarUrl = this[MemberTable.avatarUrl]?.asUrl(),
//    )
//}
//
//private typealias Username = String