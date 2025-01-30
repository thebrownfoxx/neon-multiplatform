package com.thebrownfoxx.neon.client.service.offinefirst.mesage

import com.thebrownfoxx.neon.client.model.LocalChatPreviews
import com.thebrownfoxx.neon.client.model.LocalMessage
import com.thebrownfoxx.neon.client.model.LocalTimestampedMessageId
import com.thebrownfoxx.neon.client.repository.LocalMessageRepository
import com.thebrownfoxx.neon.client.service.Messenger
import com.thebrownfoxx.neon.client.service.Messenger.GetChatPreviewsError
import com.thebrownfoxx.neon.client.service.Messenger.GetMessageError
import com.thebrownfoxx.neon.client.service.Messenger.GetMessagesError
import com.thebrownfoxx.neon.client.service.Messenger.GetUnreadMessagesError
import com.thebrownfoxx.neon.client.service.Messenger.MarkAsReadError
import com.thebrownfoxx.neon.client.service.Messenger.SendMessageError
import com.thebrownfoxx.neon.client.service.offinefirst.offlineFirstFlow
import com.thebrownfoxx.neon.common.data.Cache
import com.thebrownfoxx.neon.common.data.DataOperationError
import com.thebrownfoxx.neon.common.data.GetError
import com.thebrownfoxx.neon.common.data.SingleCache
import com.thebrownfoxx.neon.common.extension.flow.mirrorTo
import com.thebrownfoxx.neon.common.type.id.GroupId
import com.thebrownfoxx.neon.common.type.id.MessageId
import com.thebrownfoxx.outcome.Outcome
import com.thebrownfoxx.outcome.UnitOutcome
import com.thebrownfoxx.outcome.map.mapError
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.Flow

class OfflineFirstMessenger(
    private val remoteMessenger: Messenger,
    private val localMessageRepository: LocalMessageRepository,
    externalScope: CoroutineScope,
) : Messenger {
    // TODO: Maybe type-alias the Outcome<x, y>s? Something like GetChatPreviewsOutcome
    private val chatPreviewsCache =
        SingleCache<Outcome<LocalChatPreviews, GetChatPreviewsError>>(externalScope)
    private val messagesCache =
        Cache<GroupId, Outcome<List<LocalTimestampedMessageId>, GetMessagesError>>(externalScope)
    private val messageCache =
        Cache<MessageId, Outcome<LocalMessage, GetMessageError>>(externalScope)

    override val chatPreviews: Flow<Outcome<LocalChatPreviews, GetChatPreviewsError>> =
        chatPreviewsCache.getOrInitialize {
            offlineFirstFlow(
                localFlow = localMessageRepository.chatPreviews,
                remoteFlow = remoteMessenger.chatPreviews,
                handler = ChatPreviewsOfflineFirstHandler(localMessageRepository),
            ).mirrorTo(this) { chatPreviewsOutcome ->
                chatPreviewsOutcome.mapError { it.toGetChatPreviewsError() }
            }
        }

    override fun getMessages(
        groupId: GroupId,
    ): Flow<Outcome<List<LocalTimestampedMessageId>, GetMessagesError>> {
        return messagesCache.getOrInitialize(groupId) {
            offlineFirstFlow(
                localFlow = localMessageRepository.getMessagesAsFlow(groupId),
                remoteFlow = remoteMessenger.getMessages(groupId),
                handler = MessagesOfflineFirstHandler(localMessageRepository),
            ).mirrorTo(this) { messagesOutcome ->
                messagesOutcome.mapError { it.toGetMessagesError() }
            }
        }
    }

    override fun getMessage(id: MessageId): Flow<Outcome<LocalMessage, GetMessageError>> {
        return messageCache.getOrInitialize(id) {
            offlineFirstFlow(
                localFlow = localMessageRepository.getMessageAsFlow(id),
                remoteFlow = remoteMessenger.getMessage(id),
                handler = MessageOfflineFirstHandler(localMessageRepository),
            ).mirrorTo(this) { messageOutcome ->
                messageOutcome.mapError { it.toGetMessageError() }
            }
        }
    }

    // TODO: How would OfflineFirstMessenger handle getUnreadMessages? Maybe I should separate
    //  XService from RemoteService? Make a separate remote fetcher? I feel like remote fetcher
    //  should not be coupled with the service. If I want to make a separate remote server,
    //  it should rely on remote fetcher. The problem with my current approach is getUnreadMessages
    //  here will only be used by markAsRead. OfflineFirstMessenger is forced to implement this,
    //  even tho it shouldn't use it. It should use the getUnreadMessages from local repository,
    //  then from remote. Remote fetchers should just be a one-to-one mapping of the remote services
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

    private fun DataOperationError.toGetChatPreviewsError() = when (this) {
        DataOperationError.ConnectionError, DataOperationError.UnexpectedError ->
            GetChatPreviewsError.UnexpectedError
    }

    private fun DataOperationError.toGetMessagesError() = when (this) {
        DataOperationError.ConnectionError, DataOperationError.UnexpectedError ->
            GetMessagesError.UnexpectedError
    }

    private fun GetError.toGetMessageError() = when (this) {
        GetError.NotFound -> GetMessageError.NotFound
        GetError.ConnectionError, GetError.UnexpectedError -> GetMessageError.UnexpectedError
    }
}