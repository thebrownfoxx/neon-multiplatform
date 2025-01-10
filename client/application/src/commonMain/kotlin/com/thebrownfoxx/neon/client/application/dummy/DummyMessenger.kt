package com.thebrownfoxx.neon.client.application.dummy

import com.thebrownfoxx.neon.client.model.LocalConversationPreviews
import com.thebrownfoxx.neon.client.model.LocalDelivery
import com.thebrownfoxx.neon.client.model.LocalMessage
import com.thebrownfoxx.neon.client.model.LocalTimestampedMessageId
import com.thebrownfoxx.neon.client.service.Messenger
import com.thebrownfoxx.neon.client.service.Messenger.GetMessageError
import com.thebrownfoxx.neon.client.service.Messenger.GetMessagesError
import com.thebrownfoxx.neon.client.service.Messenger.SendMessageError
import com.thebrownfoxx.neon.common.type.id.GroupId
import com.thebrownfoxx.neon.common.type.id.MemberId
import com.thebrownfoxx.neon.common.type.id.MessageId
import com.thebrownfoxx.outcome.Outcome
import com.thebrownfoxx.outcome.Success
import com.thebrownfoxx.outcome.UnitOutcome
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.datetime.Clock
import kotlin.random.Random
import kotlin.time.Duration
import kotlin.time.Duration.Companion.seconds

class DummyMessenger(
    private val conversationPreviewsDelay: Duration = 0.seconds,
    private val getMessageDelay: Duration = 0.seconds,
) : Messenger {
    private val generatedMessages = mutableMapOf<MessageId, LocalMessage>()

    override val conversationPreviews: Flow<Success<LocalConversationPreviews>> =
        flow {
            delay(conversationPreviewsDelay)
            val nudged = generateConversationPreviews(2)
            val unread = generateConversationPreviews(10)
            val read = generateConversationPreviews(500)
            emit(Success(LocalConversationPreviews(nudged, unread, read)))
        }

    override fun getMessages(
        groupId: GroupId,
    ): Flow<Outcome<List<LocalTimestampedMessageId>, GetMessagesError>> {
        TODO("Not yet implemented")
    }

    override fun getMessage(id: MessageId): Flow<Outcome<LocalMessage, GetMessageError>> {
        return flow {
            delay(getMessageDelay)
            emit(Success(generatedMessages.getOrPut(id) { generateMessage(id) }))
        }
    }

    override suspend fun sendMessage(
        id: MessageId,
        groupId: GroupId,
        content: String,
    ): UnitOutcome<SendMessageError> {
        TODO("Not yet implemented")
    }

    private fun generateConversationPreviews(count: Int): List<LocalMessage> {
        val previews = mutableListOf<LocalMessage>()
        for (i in 1..count) {
            val message = generateMessage(id = MessageId())
            generatedMessages[message.id] = message
            previews.add(message)
        }
        return previews
    }

    private fun generateMessage(id: MessageId) = LocalMessage(
        id = id,
        groupId = GroupId(),
        senderId = MemberId(),
        content = "Message ${Random.nextInt()}",
        timestamp = Clock.System.now(),
        delivery = LocalDelivery.Sent,
    )
}