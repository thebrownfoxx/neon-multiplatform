package com.thebrownfoxx.neon.server.repository

import com.thebrownfoxx.neon.common.data.DataOperationError
import com.thebrownfoxx.neon.common.data.transaction.ReversibleUnitOutcome
import com.thebrownfoxx.neon.common.type.id.MemberId
import com.thebrownfoxx.neon.common.type.id.MessageId
import com.thebrownfoxx.neon.server.model.Delivery
import com.thebrownfoxx.outcome.Outcome
import kotlinx.coroutines.flow.Flow

interface DeliveryRepository {
    fun getAsFlow(
        messageId: MessageId,
        memberId: MemberId,
    ): Flow<Outcome<Delivery, DataOperationError>>

    suspend fun get(
        messageId: MessageId,
        memberId: MemberId,
    ): Outcome<Delivery, DataOperationError>

    suspend fun set(
        messageId: MessageId,
        memberId: MemberId,
        delivery: Delivery,
    ): ReversibleUnitOutcome<DataOperationError>
}