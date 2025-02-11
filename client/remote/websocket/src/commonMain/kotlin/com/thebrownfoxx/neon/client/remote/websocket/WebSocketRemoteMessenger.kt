package com.thebrownfoxx.neon.client.remote.websocket

import com.thebrownfoxx.neon.client.remote.RemoteMessenger
import com.thebrownfoxx.neon.client.remote.RemoteMessenger.GetChatPreviewsError
import com.thebrownfoxx.neon.client.remote.RemoteMessenger.GetMessageError
import com.thebrownfoxx.neon.client.remote.RemoteMessenger.GetMessagesError
import com.thebrownfoxx.neon.client.remote.RemoteMessenger.GetUnreadMessagesError
import com.thebrownfoxx.neon.client.remote.RemoteMessenger.SendMessageError
import com.thebrownfoxx.neon.client.websocket.WebSocketRequester
import com.thebrownfoxx.neon.client.websocket.WebSocketSubscriber
import com.thebrownfoxx.neon.client.websocket.request
import com.thebrownfoxx.neon.client.websocket.subscribeAsFlow
import com.thebrownfoxx.neon.common.data.cacheIn
import com.thebrownfoxx.neon.common.data.flowCacheMap
import com.thebrownfoxx.neon.common.type.id.GroupId
import com.thebrownfoxx.neon.common.type.id.MessageId
import com.thebrownfoxx.neon.server.model.Delivery
import com.thebrownfoxx.neon.server.model.Message
import com.thebrownfoxx.neon.server.model.TimestampedMessageId
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
import com.thebrownfoxx.neon.server.route.websocket.message.GetUnreadMessagesGroupNotFound
import com.thebrownfoxx.neon.server.route.websocket.message.GetUnreadMessagesRequest
import com.thebrownfoxx.neon.server.route.websocket.message.GetUnreadMessagesUnauthorized
import com.thebrownfoxx.neon.server.route.websocket.message.GetUnreadMessagesUnexpectedError
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
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.asSharedFlow

class WebSocketRemoteMessenger(
    private val subscriber: WebSocketSubscriber,
    private val requester: WebSocketRequester,
    private val externalScope: CoroutineScope,
) : RemoteMessenger {
    private val messagesCache =
        flowCacheMap<GroupId, Outcome<List<TimestampedMessageId>, GetMessagesError>>(externalScope)
    private val messageCache =
        flowCacheMap<MessageId, Outcome<Message, GetMessageError>>(externalScope)

    override val chatPreviews: Flow<Outcome<List<Message>, GetChatPreviewsError>> =
        subscriber.subscribeAsFlow(GetChatPreviewsRequest()) {
            map<GetChatPreviewsMemberNotFound> { Failure(GetChatPreviewsError.MemberNotFound) }
            map<GetChatPreviewsUnexpectedError> {
                Failure(GetChatPreviewsError.UnexpectedError)
            }
            map<GetChatPreviewsSuccessful> { Success(it.chatPreviews) }
        }.cacheIn(externalScope).asSharedFlow()

    override fun getMessages(
        groupId: GroupId,
    ): Flow<Outcome<List<TimestampedMessageId>, GetMessagesError>> {
        return messagesCache.getOrPut(groupId) {
            subscriber.subscribeAsFlow(GetMessagesRequest(groupId = groupId)) {
                map<GetMessagesUnauthorized> { Failure(GetMessagesError.Unauthorized) }
                map<GetMessagesGroupNotFound> { Failure(GetMessagesError.GroupNotFound) }
                map<GetMessagesUnexpectedError> { Failure(GetMessagesError.UnexpectedError) }
                map<GetMessagesSuccessful> { response ->
                    Success(response.messages.map { it })
                }
            }.cacheIn(externalScope)
        }.asSharedFlow()
    }

    override fun getMessage(id: MessageId): Flow<Outcome<Message, GetMessageError>> {
        return messageCache.getOrPut(id) {
            subscriber.subscribeAsFlow(GetMessageRequest(id = id)) {
                map<GetMessageUnauthorized> { Failure(GetMessageError.Unauthorized) }
                map<GetMessageNotFound> { Failure(GetMessageError.NotFound) }
                map<GetMessageUnexpectedError> { Failure(GetMessageError.UnexpectedError) }
                map<GetMessageSuccessful> { Success(it.message) }
            }.cacheIn(externalScope)
        }.asSharedFlow()
    }

    override fun getDelivery(messageId: MessageId): Flow<Outcome<Delivery, RemoteMessenger.GetDeliveryError>> {
        TODO("Not yet implemented")
    }

    override suspend fun getUnreadMessages(
        groupId: GroupId,
    ): Outcome<Set<MessageId>, GetUnreadMessagesError> {
        val request = GetUnreadMessagesRequest(groupId = groupId)
        return requester.request(request) {
            map<GetUnreadMessagesUnauthorized> { Failure(GetUnreadMessagesError.Unauthorized) }
            map<GetUnreadMessagesGroupNotFound> { Failure(GetUnreadMessagesError.GroupNotFound) }
            map<GetUnreadMessagesUnexpectedError> {
                Failure(GetUnreadMessagesError.UnexpectedError)
            }
        }.flatMapError(
            onInnerFailure = { it },
            onOuterFailure = { GetUnreadMessagesError.RequestTimeout },
        )
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

    override suspend fun updateDelivery(
        messageId: MessageId,
        delivery: Delivery,
    ): UnitOutcome<RemoteMessenger.UpdateDeliveryError> {
        TODO("Not yet implemented")
    }
}