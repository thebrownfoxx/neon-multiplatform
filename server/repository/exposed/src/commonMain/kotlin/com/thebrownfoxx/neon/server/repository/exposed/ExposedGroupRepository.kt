//package com.thebrownfoxx.neon.server.repository.exposed
//
//import com.thebrownfoxx.neon.common.data.AddError
//import com.thebrownfoxx.neon.common.data.GetError
//import com.thebrownfoxx.neon.common.data.exposed.ExposedDataSource
//import com.thebrownfoxx.neon.common.data.exposed.dbQuery
//import com.thebrownfoxx.neon.common.data.exposed.toJavaUuid
//import com.thebrownfoxx.neon.common.data.exposed.toCommonUuid
//import com.thebrownfoxx.neon.common.type.Failure
//import com.thebrownfoxx.neon.common.type.Outcome
//import com.thebrownfoxx.neon.common.type.Success
//import com.thebrownfoxx.neon.common.type.UnitOutcome
//import com.thebrownfoxx.neon.common.type.Url
//import com.thebrownfoxx.neon.common.type.id.GroupId
//import com.thebrownfoxx.neon.common.type.onFailure
//import com.thebrownfoxx.neon.common.type.runFailing
//import com.thebrownfoxx.neon.common.type.unitSuccess
//import com.thebrownfoxx.neon.server.model.ChatGroup
//import com.thebrownfoxx.neon.server.model.Community
//import com.thebrownfoxx.neon.server.model.Group
//import com.thebrownfoxx.neon.server.repository.GroupRepository
//import com.thebrownfoxx.neon.server.repository.group.model.RepositoryAddGroupError
//import com.thebrownfoxx.neon.server.repository.group.model.RepositoryGetGroupError
//import kotlinx.coroutines.CoroutineScope
//import kotlinx.coroutines.Dispatchers
//import kotlinx.coroutines.SupervisorJob
//import kotlinx.coroutines.flow.Flow
//import kotlinx.coroutines.flow.MutableSharedFlow
//import kotlinx.coroutines.launch
//import kotlinx.coroutines.plus
//import org.jetbrains.exposed.sql.Database
//import org.jetbrains.exposed.sql.ResultRow
//import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
//import org.jetbrains.exposed.sql.Table
//import org.jetbrains.exposed.sql.insert
//import org.jetbrains.exposed.sql.selectAll
//
//class ExposedGroupRepository(database: Database) : GroupRepository, ExposedDataSource(database) {
//    private object GroupTable : Table() {
//        val id = uuid(name = "id")
//        val name = varchar(name = "name", length = 64).nullable()
//        val avatarUrl = varchar(name = "avatar_url", length = 2048).nullable()
//        val isGod = bool(name = "is_god").nullable()
//
//        override val primaryKey: PrimaryKey = PrimaryKey(id)
//    }
//
//    override val table: Table = GroupTable
//
//    private val flows:
//            MutableMap<GroupId, MutableSharedFlow<Outcome<Group, RepositoryGetGroupError>>> =
//        HashMap()
//
//    private val coroutineScope = CoroutineScope(Dispatchers.IO) + SupervisorJob()
//
//    override fun get(id: GroupId): Flow<Outcome<Group, GetError>> {
//        val savedFlow = flows[id]
//        if (savedFlow != null) return savedFlow
//
//        val sharedFlow = MutableSharedFlow<Outcome<Group, RepositoryGetGroupError>>(replay = 1)
//        flows[id] = sharedFlow
//        updateFromDatabase(id)
//        return sharedFlow
//    }
//
//    override suspend fun add(group: Group): UnitOutcome<AddError> {
//        runFailing {
//            dbQuery {
//                GroupTable.insert {
//                    it[id] = group.id.toJavaUuid()
//                    if (group is Community) {
//                        it[name] = group.name
//                        it[avatarUrl] = group.avatarUrl?.value
//                        it[isGod] = group.isGod
//                    }
//                }
//            }
//        }.onFailure { return Failure(RepositoryAddGroupError.DuplicateId) }
//        return unitSuccess()
//    }
//
//    private fun updateFromDatabase(id: GroupId) {
//        flows[id]?.let { flow ->
//            coroutineScope.launch {
//                val group = dbQuery {
//                    GroupTable
//                        .selectAll()
//                        .where(GroupTable.id eq id.toJavaUuid())
//                        .firstOrNull()
//                        ?.toGroup()
//                }
//                val groupOutcome = when (group) {
//                    null -> Failure(RepositoryGetGroupError.NotFound)
//                    else -> Success(group)
//                }
//                flow.emit(groupOutcome)
//            }
//        }
//    }
//
//    private fun ResultRow.toGroup(): Group {
//        val id = GroupId(this[GroupTable.id].toCommonUuid())
//        val name = this[GroupTable.name]
//        val avatarUrl = this[GroupTable.avatarUrl]?.let { Url(it) }
//        val god = this[GroupTable.isGod]
//
//        if (name == null || god == null) return ChatGroup(id = id)
//
//        return Community(
//            id = id,
//            name = name,
//            avatarUrl = avatarUrl,
//            isGod = god,
//        )
//    }
//}