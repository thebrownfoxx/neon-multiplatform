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
import com.thebrownfoxx.neon.common.type.Url
import com.thebrownfoxx.neon.common.type.id.MemberId
import com.thebrownfoxx.neon.server.model.Member
import com.thebrownfoxx.neon.server.repository.MemberRepository
import com.thebrownfoxx.neon.server.repository.MemberRepository.AddMemberError
import com.thebrownfoxx.outcome.BlockContextScope
import com.thebrownfoxx.outcome.Outcome
import com.thebrownfoxx.outcome.Success
import com.thebrownfoxx.outcome.getOrElse
import com.thebrownfoxx.outcome.map
import com.thebrownfoxx.outcome.memberBlockContext
import com.thebrownfoxx.outcome.transform
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
        memberBlockContext("get") {
            return dataTransaction {
                MemberTable
                    .selectAll()
                    .where(MemberTable.id eq id.toJavaUuid())
                    .firstOrNotFound(context)
                    .map { it.toMember() }
            }.mapGetTransaction(context)
        }
    }

    override suspend fun getId(username: String): Outcome<MemberId, GetError> {
        memberBlockContext("getId") {
            return dataTransaction {
                MemberTable
                    .selectAll()
                    .where(MemberTable.username eq username)
                    .firstOrNotFound(context)
                    .map { MemberId(it[MemberTable.id].toCommonUuid()) }
            }.mapGetTransaction(context)
        }
    }

    override suspend fun add(member: Member): ReversibleUnitOutcome<AddMemberError> {
        memberBlockContext("add") {
            val usernameExists = usernameExists(member.username)
                .getOrElse { return this.asReversible() }
            if (usernameExists) return Failure(AddMemberError.DuplicateUsername).asReversible()

            return dataTransaction {
                MemberTable.tryAdd(context) {
                    it[id] = member.id.toJavaUuid()
                    it[username] = member.username
                    it[avatarUrl] = member.avatarUrl?.value
                }
            }
                .mapAddTransaction(context)
                .mapError { it.toAddMemberError() }
                .asReversible {
                    dataTransaction { MemberTable.deleteWhere { id eq member.id.toJavaUuid() } }
                }
        }
    }

    private fun ResultRow.toMember() = Member(
        id = MemberId(this[MemberTable.id].toCommonUuid()),
        username = this[MemberTable.username],
        avatarUrl = this[MemberTable.avatarUrl]?.let { Url(it) },
    )

    private suspend fun BlockContextScope.usernameExists(
        username: String,
    ): Outcome<Boolean, AddMemberError> {
        return getId(username).transform(
            onSuccess = { Success(true) },
            onFailure = {
                when (error) {
                    GetError.NotFound -> Success(false)
                    GetError.ConnectionError -> mapError(AddMemberError.ConnectionError)
                    GetError.UnexpectedError -> mapError(AddMemberError.UnexpectedError)
                }
            }
        )
    }

    private fun AddError.toAddMemberError() = when (this) {
        AddError.Duplicate -> AddMemberError.DuplicateId
        AddError.ConnectionError -> AddMemberError.ConnectionError
        AddError.UnexpectedError -> AddMemberError.UnexpectedError
    }
}

private object MemberTable : Table("member") {
    val id = uuid("id")
    val username = varchar("username", 16).uniqueIndex()
    val avatarUrl = varchar("avatar_url", 2048).nullable()

    override val primaryKey = PrimaryKey(id)
}