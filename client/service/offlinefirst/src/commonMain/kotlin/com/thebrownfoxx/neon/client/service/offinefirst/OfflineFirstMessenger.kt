package com.thebrownfoxx.neon.client.service.offinefirst

import com.thebrownfoxx.neon.client.model.LocalConversationPreviews
import com.thebrownfoxx.neon.client.model.LocalMessage
import com.thebrownfoxx.neon.client.model.LocalTimestampedMessageId
import com.thebrownfoxx.neon.client.repository.MessageRepository
import com.thebrownfoxx.neon.client.service.Messenger
import com.thebrownfoxx.neon.client.service.Messenger.GetConversationPreviewsError
import com.thebrownfoxx.neon.client.service.Messenger.GetMessageError
import com.thebrownfoxx.neon.client.service.Messenger.GetMessagesError
import com.thebrownfoxx.neon.client.service.Messenger.SendMessageError
import com.thebrownfoxx.neon.common.data.Cache
import com.thebrownfoxx.neon.common.extension.mirrorTo
import com.thebrownfoxx.neon.common.type.id.GroupId
import com.thebrownfoxx.neon.common.type.id.MessageId
import com.thebrownfoxx.outcome.Outcome
import com.thebrownfoxx.outcome.UnitOutcome
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.Flow

class OfflineFirstMessenger(
    private val remoteMessenger: Messenger,
    private val localMessageRepository: MessageRepository,
    externalScope: CoroutineScope,
) : Messenger {
    private val messagesCache =
        Cache<GroupId, Outcome<List<LocalTimestampedMessageId>, GetMessagesError>>(externalScope)
    private val messageCache =
        Cache<MessageId, Outcome<LocalMessage, GetMessageError>>(externalScope)

    override val conversationPreviews:
            Flow<Outcome<LocalConversationPreviews, GetConversationPreviewsError>> =
        offlineFirst(
            localFlow = localMessageRepository.conversationPreviews,
            remoteFlow = remoteMessenger.conversationPreviews,
        ) { conversationPreviews ->
            listOf(
                conversationPreviews.nudgedPreviews,
                conversationPreviews.unreadPreviews,
                conversationPreviews.readPreviews,
            ).forEach { localMessageRepository.batchUpsert(it) }
        }

    override fun getMessages(groupId: GroupId): Flow<Outcome<List<LocalTimestampedMessageId>, GetMessagesError>> {
        return messagesCache.getAsFlow(groupId) {
            offlineFirst(
                localFlow = localMessageRepository.getMessagesAsFlow(groupId),
                remoteFlow = remoteMessenger.getMessages(groupId),
            ) { localMessageRepository.batchUpsertTimestampedIds(it) }.mirrorTo(this)
        }
    }

    override fun getMessage(id: MessageId): Flow<Outcome<LocalMessage, GetMessageError>> {
        return messageCache.getAsFlow(id) {
            offlineFirst(
                localFlow = localMessageRepository.getMessageAsFlow(id),
                remoteFlow = remoteMessenger.getMessage(id),
            ) { localMessageRepository.upsert(it) }.mirrorTo(this)
        }
    }

    override suspend fun sendMessage(
        groupId: GroupId,
        content: String,
    ): UnitOutcome<SendMessageError> {
        return remoteMessenger.sendMessage(groupId, content)
    }
}