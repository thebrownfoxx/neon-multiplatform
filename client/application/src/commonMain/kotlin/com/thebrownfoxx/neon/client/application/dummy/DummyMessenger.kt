package com.thebrownfoxx.neon.client.application.dummy

import com.thebrownfoxx.neon.client.model.LocalConversationPreviews
import com.thebrownfoxx.neon.client.model.LocalDelivery
import com.thebrownfoxx.neon.client.model.LocalMessage
import com.thebrownfoxx.neon.client.model.LocalTimestampedMessageId
import com.thebrownfoxx.neon.client.service.Messenger
import com.thebrownfoxx.neon.client.service.Messenger.GetMessageError
import com.thebrownfoxx.neon.client.service.Messenger.GetMessagesError
import com.thebrownfoxx.neon.client.service.Messenger.SendMessageError
import com.thebrownfoxx.neon.common.extension.firstOrNotFound
import com.thebrownfoxx.neon.common.type.id.GroupId
import com.thebrownfoxx.neon.common.type.id.MemberId
import com.thebrownfoxx.neon.common.type.id.MessageId
import com.thebrownfoxx.outcome.Outcome
import com.thebrownfoxx.outcome.Success
import com.thebrownfoxx.outcome.UnitOutcome
import com.thebrownfoxx.outcome.map.mapError
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.datetime.Clock
import kotlin.random.Random
import kotlin.time.Duration
import kotlin.time.Duration.Companion.seconds

class DummyMessenger(
    private val conversationPreviewsDelay: Duration = 0.seconds,
    private val getMessagesDelay: Duration = 0.seconds,
    private val getMessageDelay: Duration = 0.seconds,
) : Messenger {
    private val generatedMessages = mutableListOf<LocalMessage>()

    private val generatedConversationPreviews = run {
        val nudged = generateConversationPreviews(2)
        val unread = generateConversationPreviews(10)
        val read = generateConversationPreviews(500)
        LocalConversationPreviews(nudged, unread, read)
    }

    override val conversationPreviews: Flow<Success<LocalConversationPreviews>> = flow {
        delay(conversationPreviewsDelay)
        emit(Success(generatedConversationPreviews))
    }

    override fun getMessages(
        groupId: GroupId,
    ): Flow<Outcome<List<LocalTimestampedMessageId>, GetMessagesError>> {
        return flow {
            delay(getMessagesDelay)
            if (generatedMessages.count { it.groupId == groupId } < 100) generateMessages(groupId)
            val messages = generatedMessages
                .filter { it.groupId == groupId }
                .map { LocalTimestampedMessageId(it.id, it.groupId, it.timestamp) }
            emit(Success(messages))
        }
    }

    override fun getMessage(id: MessageId): Flow<Outcome<LocalMessage, GetMessageError>> {
        return flow {
            delay(getMessageDelay)
            val message = generatedMessages
                .firstOrNotFound { it.id == id }
                .mapError { GetMessageError.NotFound }
            emit(message)
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
            val message = generateMessage(MessageId(), GroupId())
            generatedMessages.add(message)
            previews.add(message)
        }
        return previews
    }

    private fun generateMessages(groupId: GroupId) {
        repeat(200) {
            val message = generateMessage(MessageId(), groupId)
            generatedMessages.add(message)
        }
    }

    private fun generateMessage(
        id: MessageId,
        groupId: GroupId,
    ) = LocalMessage(
        id = id,
        groupId = groupId,
        senderId = MemberId(),
        content = "Message ${Random.nextInt()}",
        timestamp = Clock.System.now(),
        delivery = LocalDelivery.Sent,
    )
}