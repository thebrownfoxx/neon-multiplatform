package com.thebrownfoxx.neon.client.remote.service

import com.thebrownfoxx.neon.client.converter.toLocalMessage
import com.thebrownfoxx.neon.client.converter.toLocalTimestampedMessageId
import com.thebrownfoxx.neon.client.model.LocalChatPreviews
import com.thebrownfoxx.neon.client.model.LocalMessage
import com.thebrownfoxx.neon.client.model.LocalTimestampedMessageId
import com.thebrownfoxx.neon.client.service.Authenticator
import com.thebrownfoxx.neon.client.service.Messenger
import com.thebrownfoxx.neon.client.service.Messenger.GetChatPreviewsError
import com.thebrownfoxx.neon.client.service.Messenger.GetMessageError
import com.thebrownfoxx.neon.client.service.Messenger.GetMessagesError
import com.thebrownfoxx.neon.client.service.Messenger.MarkConversationAsReadError
import com.thebrownfoxx.neon.client.service.Messenger.SendMessageError
import com.thebrownfoxx.neon.client.service.toChatPreviews
import com.thebrownfoxx.neon.client.websocket.WebSocketRequester
import com.thebrownfoxx.neon.client.websocket.WebSocketSubscriber
import com.thebrownfoxx.neon.client.websocket.request
import com.thebrownfoxx.neon.client.websocket.subscribeAsFlow
import com.thebrownfoxx.neon.common.data.Cache
import com.thebrownfoxx.neon.common.extension.flow.mirrorTo
import com.thebrownfoxx.neon.common.type.id.GroupId
import com.thebrownfoxx.neon.common.type.id.MemberId
import com.thebrownfoxx.neon.common.type.id.MessageId
import com.thebrownfoxx.neon.server.route.websocket.message.GetChatPreviewsMemberNotFound
import com.thebrownfoxx.neon.server.route.websocket.message.GetChatPreviewsRequest
import com.thebrownfoxx.neon.server.route.websocket.message.GetChatPreviewsSuccessful
import com.thebrownfoxx.neon.server.route.websocket.message.GetChatPreviewsUnexpectedError
import com.thebrownfoxx.neon.server.route.websocket.message.GetMessageNotFound
import com.thebrownfoxx.neon.server.route.websocket.message.GetMessageRequest
import com.thebrownfoxx.neon.server.route.websocket.message.GetMessageSuccessful
import com.thebrownfoxx.neon.server.route.websocket.message.GetMessageUnauthorized
import com.thebrownfoxx.neon.server.route.websocket.message.GetMessageUnexpectedError
import com.thebrownfoxx.neon.server.route.websocket.message.GetMessagesGroupNotFound
import com.thebrownfoxx.neon.server.route.websocket.message.GetMessagesRequest
import com.thebrownfoxx.neon.server.route.websocket.message.GetMessagesSuccessful
import com.thebrownfoxx.neon.server.route.websocket.message.GetMessagesUnauthorized
import com.thebrownfoxx.neon.server.route.websocket.message.GetMessagesUnexpectedError
import com.thebrownfoxx.neon.server.route.websocket.message.MarkConversationAsReadAlreadyRead
import com.thebrownfoxx.neon.server.route.websocket.message.MarkConversationAsReadGroupNotFound
import com.thebrownfoxx.neon.server.route.websocket.message.MarkConversationAsReadRequest
import com.thebrownfoxx.neon.server.route.websocket.message.MarkConversationAsReadUnauthorized
import com.thebrownfoxx.neon.server.route.websocket.message.MarkConversationAsReadUnexpectedError
import com.thebrownfoxx.neon.server.route.websocket.message.SendMessageDuplicateId
import com.thebrownfoxx.neon.server.route.websocket.message.SendMessageGroupNotFound
import com.thebrownfoxx.neon.server.route.websocket.message.SendMessageRequest
import com.thebrownfoxx.neon.server.route.websocket.message.SendMessageSuccessful
import com.thebrownfoxx.neon.server.route.websocket.message.SendMessageUnauthorized
import com.thebrownfoxx.neon.server.route.websocket.message.SendMessageUnexpectedError
import com.thebrownfoxx.outcome.Failure
import com.thebrownfoxx.outcome.Outcome
import com.thebrownfoxx.outcome.Success
import com.thebrownfoxx.outcome.UnitOutcome
import com.thebrownfoxx.outcome.UnitSuccess
import com.thebrownfoxx.outcome.map.flatMapError
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf

@OptIn(ExperimentalCoroutinesApi::class)
class RemoteMessenger(
    authenticator: Authenticator,
    private val subscriber: WebSocketSubscriber,
    private val requester: WebSocketRequester,
    externalScope: CoroutineScope,
) : Messenger {
    private val messagesCache =
        Cache<GroupId, Outcome<List<LocalTimestampedMessageId>, GetMessagesError>>(externalScope)
    private val messageCache =
        Cache<MessageId, Outcome<LocalMessage, GetMessageError>>(externalScope)

    override val chatPreviews:
            Flow<Outcome<LocalChatPreviews, GetChatPreviewsError>> =
        authenticator.loggedInMemberId.flatMapLatest { loggedInMemberId ->
            if (loggedInMemberId == null)
                return@flatMapLatest flowOf(Failure(GetChatPreviewsError.MemberNotFound))
            getChatPreviews(loggedInMemberId)
        }

    override fun getMessages(groupId: GroupId): Flow<Outcome<List<LocalTimestampedMessageId>, GetMessagesError>> {
        return messagesCache.getOrInitialize(groupId) {
            subscriber.subscribeAsFlow(GetMessagesRequest(groupId = groupId)) {
                map<GetMessagesUnauthorized> { Failure(GetMessagesError.Unauthorized) }
                map<GetMessagesGroupNotFound> { Failure(GetMessagesError.GroupNotFound) }
                map<GetMessagesUnexpectedError> { Failure(GetMessagesError.UnexpectedError) }
                map<GetMessagesSuccessful> { response ->
                    Success(response.messages.map { it.toLocalTimestampedMessageId(groupId) })
                }
            }.mirrorTo(this)
        }
    }

    override fun getMessage(id: MessageId): Flow<Outcome<LocalMessage, GetMessageError>> {
        return messageCache.getOrInitialize(id) {
            subscriber.subscribeAsFlow(GetMessageRequest(id = id)) {
                map<GetMessageUnauthorized> { Failure(GetMessageError.Unauthorized) }
                map<GetMessageNotFound> { Failure(GetMessageError.NotFound) }
                map<GetMessageUnexpectedError> { Failure(GetMessageError.UnexpectedError) }
                map<GetMessageSuccessful> { Success(it.message.toLocalMessage()) }
            }.mirrorTo(this)
        }
    }

    override suspend fun sendMessage(
        id: MessageId,
        groupId: GroupId,
        content: String,
    ): UnitOutcome<SendMessageError> {
        val request = SendMessageRequest(id = id, groupId = groupId, content = content)
        return requester.request(request) {
            map<SendMessageUnauthorized> { Failure(SendMessageError.Unauthorized) }
            map<SendMessageGroupNotFound> { Failure(SendMessageError.GroupNotFound) }
            map<SendMessageDuplicateId> { Failure(SendMessageError.DuplicateId) }
            map<SendMessageUnexpectedError> { Failure(SendMessageError.UnexpectedError) }
            map<SendMessageSuccessful> { UnitSuccess }
        }.flatMapError(
            onInnerFailure = { it },
            onOuterFailure = { SendMessageError.RequestTimeout },
        )
    }

    override suspend fun markConversationAsRead(
        groupId: GroupId,
    ): UnitOutcome<MarkConversationAsReadError> {
        return requester.request(MarkConversationAsReadRequest(groupId = groupId)) {
            map<MarkConversationAsReadUnauthorized> {
                Failure(MarkConversationAsReadError.Unauthorized)
            }
            map<MarkConversationAsReadAlreadyRead> {
                Failure(MarkConversationAsReadError.AlreadyRead)
            }
            map<MarkConversationAsReadGroupNotFound> {
                Failure(MarkConversationAsReadError.GroupNotFound)
            }
            map<MarkConversationAsReadUnexpectedError> {
                Failure(MarkConversationAsReadError.UnexpectedError)
            }
        }.flatMapError(
            onInnerFailure = { it },
            onOuterFailure = { MarkConversationAsReadError.RequestTimeout },
        )
    }

    private fun getChatPreviews(
        loggedInMemberId: MemberId,
    ): Flow<Outcome<LocalChatPreviews, GetChatPreviewsError>> {
        return subscriber.subscribeAsFlow(GetChatPreviewsRequest()) {
            map<GetChatPreviewsMemberNotFound> {
                Failure(GetChatPreviewsError.MemberNotFound)
            }
            map<GetChatPreviewsUnexpectedError> {
                Failure(GetChatPreviewsError.UnexpectedError)
            }
            map<GetChatPreviewsSuccessful> { response ->
                Success(response.toLocalChatPreviews(loggedInMemberId))
            }
        }
    }

    private fun GetChatPreviewsSuccessful.toLocalChatPreviews(
        loggedInMemberId: MemberId,
    ): LocalChatPreviews {
        return chatPreviews.map { it.toLocalMessage() }
            .toChatPreviews(loggedInMemberId)
    }
}