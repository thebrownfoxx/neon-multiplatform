package com.thebrownfoxx.neon.client.repository.exposed

import com.thebrownfoxx.neon.client.repository.TokenRepository
import com.thebrownfoxx.neon.client.repository.TokenRepository.Token
import com.thebrownfoxx.neon.common.data.DataOperationError
import com.thebrownfoxx.neon.common.data.GetError
import com.thebrownfoxx.neon.common.data.SingleReactiveCache
import com.thebrownfoxx.neon.common.data.exposed.dataTransaction
import com.thebrownfoxx.neon.common.data.exposed.firstOrNotFound
import com.thebrownfoxx.neon.common.data.exposed.initializeExposeDatabase
import com.thebrownfoxx.neon.common.data.exposed.mapGetTransaction
import com.thebrownfoxx.neon.common.data.exposed.mapUnitOperationTransaction
import com.thebrownfoxx.neon.common.data.exposed.toCommonUuid
import com.thebrownfoxx.neon.common.data.exposed.toJavaUuid
import com.thebrownfoxx.neon.common.type.Jwt
import com.thebrownfoxx.neon.common.type.id.MemberId
import com.thebrownfoxx.outcome.Outcome
import com.thebrownfoxx.outcome.UnitOutcome
import com.thebrownfoxx.outcome.map.map
import com.thebrownfoxx.outcome.map.onSuccess
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.Flow
import org.jetbrains.exposed.sql.Database
import org.jetbrains.exposed.sql.ResultRow
import org.jetbrains.exposed.sql.Table
import org.jetbrains.exposed.sql.deleteAll
import org.jetbrains.exposed.sql.selectAll
import org.jetbrains.exposed.sql.upsert
import java.util.UUID

class ExposedTokenRepository(
    database: Database,
    externalScope: CoroutineScope,
) : TokenRepository {
    init {
        initializeExposeDatabase(database, TokenTable)
    }

    private val cache = SingleReactiveCache(externalScope, ::get)

    override fun getAsFlow(): Flow<Outcome<Token, GetError>> {
        return cache.getAsFlow()
    }

    override suspend fun get(): Outcome<Token, GetError> {
        return dataTransaction {
            TokenTable
                .selectAll()
                .firstOrNotFound()
                .map { it.toToken() }
        }.mapGetTransaction()
    }

    override suspend fun set(token: Token): UnitOutcome<DataOperationError> {
        return dataTransaction {
            val id = TokenTable.selectAll().firstOrNull()?.let { it[TokenTable.id] }
                ?: UUID.randomUUID()

            TokenTable.upsert {
                it[TokenTable.id] = id
                it[TokenTable.token] = token.jwt.value
                it[memberId] = token.memberId.toJavaUuid()
            }
        }
            .mapUnitOperationTransaction()
            .onSuccess { cache.update() }
    }

    override suspend fun clear(): UnitOutcome<DataOperationError> {
        return dataTransaction {
            TokenTable.deleteAll()
        }
            .mapUnitOperationTransaction()
            .onSuccess { cache.update() }
    }

    private fun ResultRow.toToken() = Token(
        jwt = Jwt(this[TokenTable.token]),
        memberId = MemberId(this[TokenTable.memberId].toCommonUuid()),
    )
}

private object TokenTable : Table("token") {
    val id = uuid("id")
    val token = varchar("token", 2048)
    val memberId = uuid("member_id")

    override val primaryKey = PrimaryKey(id)
}