package com.thebrownfoxx.neon.server.repository.exposed

import com.thebrownfoxx.neon.common.data.AddError
import com.thebrownfoxx.neon.common.data.GetError
import com.thebrownfoxx.neon.common.data.exposed.ExposedDataSource
import com.thebrownfoxx.neon.common.data.exposed.dataTransaction
import com.thebrownfoxx.neon.common.data.exposed.firstOrNotFound
import com.thebrownfoxx.neon.common.data.exposed.mapAddTransaction
import com.thebrownfoxx.neon.common.data.exposed.mapGetTransaction
import com.thebrownfoxx.neon.common.data.exposed.toCommonUuid
import com.thebrownfoxx.neon.common.data.exposed.toJavaUuid
import com.thebrownfoxx.neon.common.data.exposed.tryAdd
import com.thebrownfoxx.neon.common.data.transaction.ReversibleUnitOutcome
import com.thebrownfoxx.neon.common.data.transaction.asReversible
import com.thebrownfoxx.neon.common.outcome.Failure
import com.thebrownfoxx.neon.common.outcome.Outcome
import com.thebrownfoxx.neon.common.outcome.fold
import com.thebrownfoxx.neon.common.outcome.map
import com.thebrownfoxx.neon.common.outcome.mapError
import com.thebrownfoxx.neon.common.type.Url
import com.thebrownfoxx.neon.common.type.id.MemberId
import com.thebrownfoxx.neon.server.model.Member
import com.thebrownfoxx.neon.server.repository.MemberRepository
import com.thebrownfoxx.neon.server.repository.RepositoryAddMemberError
import org.jetbrains.exposed.sql.Database
import org.jetbrains.exposed.sql.ResultRow
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import org.jetbrains.exposed.sql.Table
import org.jetbrains.exposed.sql.deleteWhere
import org.jetbrains.exposed.sql.selectAll

class ExposedMemberRepository(
    database: Database,
) : MemberRepository, ExposedDataSource(database, MemberTable) {
    private val reactiveCache = ReactiveCache(::get)

    override fun getAsFlow(id: MemberId) = reactiveCache.getAsFlow(id)

    override suspend fun get(id: MemberId): Outcome<Member, GetError> {
        return dataTransaction {
            MemberTable
                .selectAll()
                .where(MemberTable.id eq id.toJavaUuid())
                .firstOrNotFound()
                .map { it.toMember() }
        }.mapGetTransaction()
    }

    override suspend fun getId(username: String): Outcome<MemberId, GetError> {
        return dataTransaction {
            MemberTable
                .selectAll()
                .where(MemberTable.username eq username)
                .firstOrNotFound()
                .map { MemberId(it[MemberTable.id].toCommonUuid()) }
        }.mapGetTransaction()
    }

    override suspend fun add(member: Member): ReversibleUnitOutcome<RepositoryAddMemberError> {
        val usernameExists = getId(member.username).fold(
            onSuccess = { true },
            onFailure = { error ->
                when (error) {
                    GetError.NotFound -> false
                    GetError.ConnectionError ->
                        return Failure(RepositoryAddMemberError.ConnectionError).asReversible()
                }
            }
        )

        if (usernameExists)
            return Failure(RepositoryAddMemberError.DuplicateUsername).asReversible()

        return dataTransaction {
            MemberTable.tryAdd {
                it[id] = member.id.toJavaUuid()
                it[username] = member.username
                it[avatarUrl] = member.avatarUrl?.value
            }
        }
            .mapAddTransaction()
            .mapError { error ->
                when (error) {
                    AddError.Duplicate -> RepositoryAddMemberError.DuplicateId
                    AddError.ConnectionError -> RepositoryAddMemberError.ConnectionError
                }
            }.asReversible {
                dataTransaction { MemberTable.deleteWhere { id eq member.id.toJavaUuid() } }
            }
    }

    private fun ResultRow.toMember() = Member(
        id = MemberId(this[MemberTable.id].toCommonUuid()),
        username = this[MemberTable.username],
        avatarUrl = this[MemberTable.avatarUrl]?.let { Url(it) },
    )
}

private object MemberTable : Table("member") {
    val id = uuid("id")
    val username = varchar("username", 16).uniqueIndex()
    val avatarUrl = varchar("avatar_url", 2048).nullable()

    override val primaryKey = PrimaryKey(id)
}