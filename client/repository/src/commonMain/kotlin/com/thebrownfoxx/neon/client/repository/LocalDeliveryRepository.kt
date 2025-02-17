package com.thebrownfoxx.neon.client.repository

import com.thebrownfoxx.neon.client.model.LocalDelivery
import com.thebrownfoxx.neon.common.data.DataOperationError
import com.thebrownfoxx.neon.common.type.id.MemberId
import com.thebrownfoxx.neon.common.type.id.MessageId
import com.thebrownfoxx.outcome.Outcome
import com.thebrownfoxx.outcome.UnitOutcome
import kotlinx.coroutines.flow.Flow

interface LocalDeliveryRepository {
    fun getAsFlow(
        messageId: MessageId,
        memberId: MemberId,
    ): Flow<Outcome<LocalDelivery, DataOperationError>>

    suspend fun get(
        messageId: MessageId,
        memberId: MemberId,
    ): Outcome<LocalDelivery, DataOperationError>

    suspend fun set(
        messageId: MessageId,
        memberId: MemberId,
        delivery: LocalDelivery,
    ): UnitOutcome<DataOperationError>
}