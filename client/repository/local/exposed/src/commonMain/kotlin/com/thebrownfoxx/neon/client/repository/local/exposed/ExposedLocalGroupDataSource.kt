package com.thebrownfoxx.neon.client.repository.local.exposed

import com.thebrownfoxx.neon.client.model.LocalChatGroup
import com.thebrownfoxx.neon.client.model.LocalCommunity
import com.thebrownfoxx.neon.client.model.LocalGroup
import com.thebrownfoxx.neon.client.repository.local.group.LocalGroupDataSource
import com.thebrownfoxx.neon.client.repository.local.group.model.LocalGetGroupError
import com.thebrownfoxx.neon.client.repository.local.group.model.LocalUpsertGroupError
import com.thebrownfoxx.neon.common.type.Failure
import com.thebrownfoxx.neon.common.type.Outcome
import com.thebrownfoxx.neon.common.type.Success
import com.thebrownfoxx.neon.common.type.UnitOutcome
import com.thebrownfoxx.neon.common.type.Url
import com.thebrownfoxx.neon.common.type.id.GroupId
import com.thebrownfoxx.neon.common.type.id.Uuid
import com.thebrownfoxx.neon.common.type.unitSuccess
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.plus
import org.jetbrains.exposed.sql.Database
import org.jetbrains.exposed.sql.ResultRow
import org.jetbrains.exposed.sql.SchemaUtils
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import org.jetbrains.exposed.sql.Table
import org.jetbrains.exposed.sql.selectAll
import org.jetbrains.exposed.sql.transactions.transaction
import org.jetbrains.exposed.sql.upsert

class ExposedLocalGroupDataSource(database: Database) : LocalGroupDataSource {
    private object GroupTable : Table() {
        val id = varchar(name = "id", length = 36)
        val name = varchar(name = "name", length = 64).nullable()
        val avatarUrl = varchar(name = "avatar_url", length = 1024).nullable()
        val god = bool(name = "god").nullable()

        override val primaryKey: PrimaryKey = PrimaryKey(id)
    }

    init {
        transaction(database) {
            SchemaUtils.create(GroupTable)
        }
    }

    private val flows:
            MutableMap<GroupId, MutableSharedFlow<Outcome<LocalGroup, LocalGetGroupError>>> =
        HashMap()

    private val coroutineScope = CoroutineScope(Dispatchers.IO) + SupervisorJob()

    override fun get(id: GroupId): Flow<Outcome<LocalGroup, LocalGetGroupError>> {
        val savedFlow = flows[id]
        if (savedFlow != null) return savedFlow

        val sharedFlow = MutableSharedFlow<Outcome<LocalGroup, LocalGetGroupError>>(replay = 1)
        flows[id] = sharedFlow
        sharedFlow.updateFromDatabase(id)
        return sharedFlow
    }

    override suspend fun upsert(group: LocalGroup): UnitOutcome<LocalUpsertGroupError> {
        dbQuery {
            GroupTable.upsert {
                it[id] = group.id.value
                if (group is LocalCommunity) {
                    it[name] = group.name
                    it[avatarUrl] = group.avatarUrl?.value
                    it[god] = group.god
                }
            }
        }
        flows[group.id]?.updateFromDatabase(group.id)
        return unitSuccess()
    }

    private suspend fun getFromDatabase(id: GroupId): LocalGroup? {
        return dbQuery {
            GroupTable
                .selectAll()
                .where(GroupTable.id eq id.value)
                .firstOrNull()
                ?.toLocalGroup()
        }
    }

    private fun MutableSharedFlow<Outcome<LocalGroup, LocalGetGroupError>>.updateFromDatabase(
        id: GroupId,
    ) {
        coroutineScope.launch {
            val group = getFromDatabase(id)
            val groupOutcome = (group?.let { Success(it) } ?: Failure(LocalGetGroupError.NotFound))
            emit(groupOutcome)
        }
    }

    private fun ResultRow.toLocalGroup(): LocalGroup {
        val id = GroupId(Uuid(this[GroupTable.id]))
        val name = this[GroupTable.name]
        val avatarUrl = this[GroupTable.avatarUrl]?.let { Url(it) }
        val god = this[GroupTable.god]

        if (name == null || god == null) return LocalChatGroup(id = id)

        return LocalCommunity(
            id = id,
            name = name,
            avatarUrl = avatarUrl,
            god = god,
        )
    }
}