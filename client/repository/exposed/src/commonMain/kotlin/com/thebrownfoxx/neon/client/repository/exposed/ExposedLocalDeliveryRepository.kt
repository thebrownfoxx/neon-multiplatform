package com.thebrownfoxx.neon.client.repository.exposed

import com.thebrownfoxx.neon.client.model.LocalDelivery
import com.thebrownfoxx.neon.client.repository.LocalDeliveryRepository
import com.thebrownfoxx.neon.common.data.DataOperationError
import com.thebrownfoxx.neon.common.data.ReactiveCache
import com.thebrownfoxx.neon.common.data.exposed.dataTransaction
import com.thebrownfoxx.neon.common.data.exposed.initializeExposeDatabase
import com.thebrownfoxx.neon.common.data.exposed.mapOperationTransaction
import com.thebrownfoxx.neon.common.data.exposed.mapUnitOperationTransaction
import com.thebrownfoxx.neon.common.data.exposed.toJavaUuid
import com.thebrownfoxx.neon.common.type.id.MemberId
import com.thebrownfoxx.neon.common.type.id.MessageId
import com.thebrownfoxx.outcome.Outcome
import com.thebrownfoxx.outcome.UnitOutcome
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.Flow
import org.jetbrains.exposed.sql.Database
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import org.jetbrains.exposed.sql.Table
import org.jetbrains.exposed.sql.and
import org.jetbrains.exposed.sql.selectAll
import org.jetbrains.exposed.sql.upsert

class ExposedLocalDeliveryRepository(
    database: Database,
    externalScope: CoroutineScope,
) : LocalDeliveryRepository {
    init {
        initializeExposeDatabase(database, LocalDeliveryTable)
    }

    private val deliveryCache = ReactiveCache(externalScope, ::get)

    override fun getAsFlow(
        messageId: MessageId,
        memberId: MemberId,
    ): Flow<Outcome<LocalDelivery, DataOperationError>> {
        return deliveryCache.getAsFlow(LocalDeliveryKey(messageId, memberId))
    }

    override suspend fun get(
        messageId: MessageId,
        memberId: MemberId,
    ): Outcome<LocalDelivery, DataOperationError> {
        return get(LocalDeliveryKey(messageId, memberId))
    }

    override suspend fun set(
        messageId: MessageId,
        memberId: MemberId,
        delivery: LocalDelivery,
    ): UnitOutcome<DataOperationError> {
        return dataTransaction {
            LocalDeliveryTable.upsert {
                it[this.messageId] = messageId.toJavaUuid()
                it[this.memberId] = memberId.toJavaUuid()
                it[this.delivery] = delivery.name
            }
        }.mapUnitOperationTransaction()
    }

    private suspend fun get(key: LocalDeliveryKey): Outcome<LocalDelivery, DataOperationError> {
        val (messageId, memberId) = key
        return dataTransaction {
            val messageIdMatches = LocalDeliveryTable.messageId eq messageId.toJavaUuid()
            val memberIdMatches = LocalDeliveryTable.memberId eq memberId.toJavaUuid()
            val deliveryName = LocalDeliveryTable
                .selectAll()
                .where(messageIdMatches and memberIdMatches)
                .firstOrNull()
                ?.get(LocalDeliveryTable.delivery)
                ?: LocalDelivery.Sending.name
            LocalDelivery.valueOf(deliveryName)
        }.mapOperationTransaction()
    }

    private data class LocalDeliveryKey(
        val messageId: MessageId,
        val memberId: MemberId,
    )
}

internal object LocalDeliveryTable : Table("delivery") {
    val messageId = uuid("message_id")
    val memberId = uuid("member_id")
    val delivery = varchar("delivery", 32)

    override val primaryKey = PrimaryKey(messageId, memberId)
}