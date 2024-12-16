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
import com.thebrownfoxx.outcome.map.getOrElse
import com.thebrownfoxx.outcome.map.map
import org.jetbrains.exposed.sql.Database
import org.jetbrains.exposed.sql.ResultRow
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import org.jetbrains.exposed.sql.Table
import org.jetbrains.exposed.sql.deleteWhere
import org.jetbrains.exposed.sql.selectAll
import org.jetbrains.exposed.sql.upsert

@OptIn(ExperimentalStdlibApi::class)
class ExposedPasswordRepository(
    database: Database,
) : PasswordRepository, ExposedDataSource(database, PasswordTable) {
    override suspend fun getHash(memberId: MemberId): Outcome<Hash, GetError> {
        return dataTransaction {
            PasswordTable
                .selectAll()
                .where(PasswordTable.memberId eq memberId.toJavaUuid())
                .firstOrNotFound()
                .map { it.toHash() }
        }.mapGetTransaction()
    }

    override suspend fun setHash(
        memberId: MemberId,
        hash: Hash,
    ): ReversibleUnitOutcome<DataOperationError> {
        val oldHash = getHash(memberId).getOrElse { error ->
            when (error) {
                GetError.NotFound -> null

                GetError.ConnectionError ->
                    return Failure(DataOperationError.ConnectionError).asReversible()

                GetError.UnexpectedError ->
                    return Failure(DataOperationError.UnexpectedError).asReversible()
            }
        }

        dataTransaction {
            updateHash(memberId, hash)
        }.mapOperationTransaction()

        return UnitSuccess.asReversible {
            dataTransaction {
                if (oldHash == null)
                    PasswordTable.deleteWhere { PasswordTable.memberId eq memberId.toJavaUuid() }
                else updateHash(memberId, oldHash)
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
    val memberId = uuid("member_id")
    val passwordHash = varchar("password_hash", 128)
    val passwordSalt = varchar("password_salt", 32)

    override val primaryKey = PrimaryKey(memberId)
}