package com.thebrownfoxx.neon.server.repository.exposed

import com.thebrownfoxx.neon.common.data.DataOperationError
import com.thebrownfoxx.neon.common.data.ReactiveCache
import com.thebrownfoxx.neon.common.data.exposed.dataTransaction
import com.thebrownfoxx.neon.common.data.exposed.initializeExposeDatabase
import com.thebrownfoxx.neon.common.data.exposed.mapOperationTransaction
import com.thebrownfoxx.neon.common.data.exposed.mapUnitOperationTransaction
import com.thebrownfoxx.neon.common.data.exposed.toJavaUuid
import com.thebrownfoxx.neon.common.data.transaction.ReversibleUnitOutcome
import com.thebrownfoxx.neon.common.data.transaction.asReversible
import com.thebrownfoxx.neon.common.data.transaction.onFinalize
import com.thebrownfoxx.neon.common.type.id.GroupId
import com.thebrownfoxx.neon.common.type.id.MemberId
import com.thebrownfoxx.neon.common.type.id.MessageId
import com.thebrownfoxx.neon.server.model.Delivery
import com.thebrownfoxx.neon.server.repository.DeliveryRepository
import com.thebrownfoxx.outcome.Outcome
import com.thebrownfoxx.outcome.UnitOutcome
import com.thebrownfoxx.outcome.map.getOrElse
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.Flow
import org.jetbrains.exposed.sql.Database
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import org.jetbrains.exposed.sql.Table
import org.jetbrains.exposed.sql.and
import org.jetbrains.exposed.sql.not
import org.jetbrains.exposed.sql.or
import org.jetbrains.exposed.sql.selectAll
import org.jetbrains.exposed.sql.upsert

class ExposedDeliveryRepository(
    database: Database,
    externalScope: CoroutineScope,
) : DeliveryRepository {
    init {
        initializeExposeDatabase(database, DeliveryTable)
    }

    private val deliveryCache = ReactiveCache(externalScope, ::get)

    override fun getAsFlow(
        messageId: MessageId,
        memberId: MemberId,
    ): Flow<Outcome<Delivery, DataOperationError>> {
        return deliveryCache.getAsFlow(DeliveryKey(messageId, memberId))
    }

    override suspend fun get(
        messageId: MessageId,
        memberId: MemberId,
    ): Outcome<Delivery, DataOperationError> {
        return get(DeliveryKey(messageId, memberId))
    }

    override suspend fun set(
        messageId: MessageId,
        memberId: MemberId,
        delivery: Delivery,
    ): ReversibleUnitOutcome<DataOperationError> {
        val key = DeliveryKey(messageId, memberId)
        val previousDelivery = get(key).getOrElse { return Failure(it).asReversible() }
        return upsertDelivery(messageId, memberId, delivery)
            .asReversible { upsertDelivery(messageId, memberId, previousDelivery) }
            .onFinalize { deliveryCache.update(key) }
    }

    override suspend fun getUnreadMessages(
        memberId: MemberId,
        groupId: GroupId,
    ): Outcome<Set<MessageId>, DataOperationError> {
        val x = dataTransaction {
            val sent = MessageTable.senderId eq memberId.toJavaUuid()
            val read = DeliveryTable.delivery eq Delivery.Read.name or sent
            MessageTable
                .selectAll()
                .where(not(read))
        }
        TODO()
    }

    private suspend fun get(key: DeliveryKey): Outcome<Delivery, DataOperationError> {
        val (messageId, memberId) = key
        return dataTransaction {
            val messageIdMatches = DeliveryTable.messageId eq messageId.toJavaUuid()
            val memberIdMatches = DeliveryTable.memberId eq memberId.toJavaUuid()
            val deliveryName = DeliveryTable
                .selectAll()
                .where(messageIdMatches and memberIdMatches)
                .firstOrNull()
                ?.get(DeliveryTable.delivery)
                ?: Delivery.Sent.name
            Delivery.valueOf(deliveryName)
        }.mapOperationTransaction()
    }

    private suspend fun upsertDelivery(
        messageId: MessageId,
        memberId: MemberId,
        delivery: Delivery,
    ): UnitOutcome<DataOperationError> {
        return dataTransaction {
            DeliveryTable.upsert {
                it[this.messageId] = messageId.toJavaUuid()
                it[this.memberId] = memberId.toJavaUuid()
                it[this.delivery] = delivery.name
            }
        }.mapUnitOperationTransaction()
    }

    private data class DeliveryKey(
        val messageId: MessageId,
        val memberId: MemberId,
    )
}

private object DeliveryTable : Table("delivery") {
    val messageId = uuid("message_id")
    val memberId = uuid("member_id")
    val delivery = varchar("delivery", 32)

    override val primaryKey = PrimaryKey(messageId, memberId)
}