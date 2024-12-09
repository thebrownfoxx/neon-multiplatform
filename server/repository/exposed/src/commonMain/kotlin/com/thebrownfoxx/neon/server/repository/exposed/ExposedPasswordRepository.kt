package com.thebrownfoxx.neon.server.repository.exposed

import com.thebrownfoxx.neon.common.data.DataOperationError
import com.thebrownfoxx.neon.common.data.GetError
import com.thebrownfoxx.neon.common.data.exposed.ExposedDataSource
import com.thebrownfoxx.neon.common.data.exposed.dataTransaction
import com.thebrownfoxx.neon.common.data.exposed.firstOrNotFound
import com.thebrownfoxx.neon.common.data.exposed.mapGetTransaction
import com.thebrownfoxx.neon.common.data.exposed.mapOperationTransaction
import com.thebrownfoxx.neon.common.data.exposed.toJavaUuid
import com.thebrownfoxx.neon.common.data.transaction.ReversibleUnitOutcome
import com.thebrownfoxx.neon.common.data.transaction.asReversible
import com.thebrownfoxx.neon.common.hash.Hash
import com.thebrownfoxx.neon.common.type.id.MemberId
import com.thebrownfoxx.neon.server.repository.PasswordRepository
import com.thebrownfoxx.outcome.Outcome
import com.thebrownfoxx.outcome.UnitSuccess
import com.thebrownfoxx.outcome.getOrElse
import com.thebrownfoxx.outcome.map
import com.thebrownfoxx.outcome.memberBlockContext
import org.jetbrains.exposed.sql.Database
import org.jetbrains.exposed.sql.ResultRow
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import org.jetbrains.exposed.sql.Table
import org.jetbrains.exposed.sql.deleteWhere
import org.jetbrains.exposed.sql.selectAll
import org.jetbrains.exposed.sql.upsert
import java.util.UUID

@OptIn(ExperimentalStdlibApi::class)
class ExposedPasswordRepository(
    database: Database,
) : PasswordRepository, ExposedDataSource(database, PasswordTable) {
    override suspend fun getHash(memberId: MemberId): Outcome<Hash, GetError> {
        memberBlockContext("getHash") {
            return dataTransaction {
                PasswordTable
                    .selectAll()
                    .where(PasswordTable.memberId eq memberId.toJavaUuid())
                    .firstOrNotFound(context)
                    .map { it.toHash() }
            }.mapGetTransaction(context)
        }
    }

    override suspend fun setHash(
        memberId: MemberId,
        hash: Hash,
    ): ReversibleUnitOutcome<DataOperationError> {
        memberBlockContext("setHash") {
            val oldHash = getHash(memberId).getOrElse {
                when (error) {
                    GetError.NotFound -> null

                    GetError.ConnectionError ->
                        return mapError(DataOperationError.ConnectionError).asReversible()

                    GetError.UnexpectedError ->
                        return mapError(DataOperationError.UnexpectedError).asReversible()
                }
            }

            val id = UUID.randomUUID()
            dataTransaction {
                updateHash(memberId, hash)
            }.mapOperationTransaction(context)

            return UnitSuccess.asReversible {
                dataTransaction {
                    if (oldHash == null) PasswordTable.deleteWhere { PasswordTable.id eq id }
                    else updateHash(memberId, oldHash)
                }
            }
        }
    }

    private fun updateHash(
        memberId: MemberId,
        hash: Hash,
    ) = PasswordTable.upsert(PasswordTable.memberId) {
        it[this.memberId] = memberId.toJavaUuid()
        it[this.passwordHash] = hash.value.toHexString()
        it[this.passwordSalt] = hash.salt.toHexString()
    }

    private fun ResultRow.toHash() = Hash(
        value = this[PasswordTable.passwordHash].hexToByteArray(),
        salt = this[PasswordTable.passwordSalt].hexToByteArray(),
    )
}

private object PasswordTable : Table("password") {
    val id = uuid("id")
    val memberId = uuid("member_id").uniqueIndex()
    val passwordHash = varchar("password_hash", 128)
    val passwordSalt = varchar("password_salt", 32)

    override val primaryKey = PrimaryKey(id)
}