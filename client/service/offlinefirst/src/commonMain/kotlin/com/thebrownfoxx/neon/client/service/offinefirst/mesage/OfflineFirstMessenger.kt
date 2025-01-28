package com.thebrownfoxx.neon.client.service.offinefirst.mesage

import com.thebrownfoxx.neon.client.model.LocalChatPreviews
import com.thebrownfoxx.neon.client.model.LocalMessage
import com.thebrownfoxx.neon.client.model.LocalTimestampedMessageId
import com.thebrownfoxx.neon.client.repository.MessageRepository
import com.thebrownfoxx.neon.client.service.Messenger
import com.thebrownfoxx.neon.client.service.Messenger.GetChatPreviewsError
import com.thebrownfoxx.neon.client.service.Messenger.GetMessageError
import com.thebrownfoxx.neon.client.service.Messenger.GetMessagesError
import com.thebrownfoxx.neon.client.service.Messenger.GetUnreadMessagesError
import com.thebrownfoxx.neon.client.service.Messenger.MarkAsReadError
import com.thebrownfoxx.neon.client.service.Messenger.SendMessageError
import com.thebrownfoxx.neon.client.service.offinefirst.offlineFirstFlow
import com.thebrownfoxx.neon.common.data.Cache
import com.thebrownfoxx.neon.common.data.SingleCache
import com.thebrownfoxx.neon.common.extension.flow.mirrorTo
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
    private val chatPreviewsCache =
        SingleCache<Outcome<LocalChatPreviews, GetChatPreviewsError>>(externalScope)
    private val messagesCache =
        Cache<GroupId, Outcome<List<LocalTimestampedMessageId>, GetMessagesError>>(externalScope)

    override val chatPreviews: Flow<Outcome<LocalChatPreviews, GetChatPreviewsError>> =
        chatPreviewsCache.getOrInitialize {
            offlineFirstFlow(
                localFlow = localMessageRepository.chatPreviews,
                remoteFlow = remoteMessenger.chatPreviews,
                handler = ChatPreviewsOfflineFirstHandler(localMessageRepository),
            ).mirrorTo(this)
        }

    override fun getMessages(
        groupId: GroupId,
    ): Flow<Outcome<List<LocalTimestampedMessageId>, GetMessagesError>> {
        return messagesCache.getOrInitialize(groupId) {
            offlineFirstFlow(
                localFlow = localMessageRepository.getMessagesAsFlow(groupId),
                remoteFlow = remoteMessenger.getMessages(groupId),
                handler = MessagesOfflineFirstHandler(localMessageRepository),
            ).mirrorTo(this)
        }
    }

    override fun getMessage(id: MessageId): Flow<Outcome<LocalMessage, GetMessageError>> {
        TODO("Not yet implemented")
    }

    override suspend fun getUnreadMessages(
        groupId: GroupId,
    ): Outcome<Set<MessageId>, GetUnreadMessagesError> {
        TODO("Not yet implemented")
    }

    override suspend fun sendMessage(
        id: MessageId,
        groupId: GroupId,
        content: String,
    ): UnitOutcome<SendMessageError> {
        TODO("Not yet implemented")
    }

    override suspend fun markAsRead(groupId: GroupId): UnitOutcome<MarkAsReadError> {
        TODO("Not yet implemented")
    }
}