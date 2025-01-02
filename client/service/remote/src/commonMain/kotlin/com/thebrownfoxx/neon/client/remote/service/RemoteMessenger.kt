package com.thebrownfoxx.neon.client.remote.service

import com.thebrownfoxx.neon.client.converter.toLocalMessage
import com.thebrownfoxx.neon.client.model.LocalConversationPreviews
import com.thebrownfoxx.neon.client.model.LocalDelivery
import com.thebrownfoxx.neon.client.model.LocalMessage
import com.thebrownfoxx.neon.client.service.Authenticator
import com.thebrownfoxx.neon.client.service.Messenger
import com.thebrownfoxx.neon.client.service.Messenger.GetConversationPreviewsError
import com.thebrownfoxx.neon.client.service.Messenger.GetMessageError
import com.thebrownfoxx.neon.client.service.Messenger.GetMessagesError
import com.thebrownfoxx.neon.client.service.Messenger.SendMessageError
import com.thebrownfoxx.neon.client.websocket.WebSocketRequester
import com.thebrownfoxx.neon.client.websocket.WebSocketSubscriber
import com.thebrownfoxx.neon.client.websocket.request
import com.thebrownfoxx.neon.client.websocket.subscribeAsFlow
import com.thebrownfoxx.neon.common.Logger
import com.thebrownfoxx.neon.common.data.Cache
import com.thebrownfoxx.neon.common.data.SingleCache
import com.thebrownfoxx.neon.common.extension.mirrorTo
import com.thebrownfoxx.neon.common.type.id.GroupId
import com.thebrownfoxx.neon.common.type.id.MemberId
import com.thebrownfoxx.neon.common.type.id.MessageId
import com.thebrownfoxx.neon.server.route.websocket.message.GetConversationPreviewsMemberNotFound
import com.thebrownfoxx.neon.server.route.websocket.message.GetConversationPreviewsRequest
import com.thebrownfoxx.neon.server.route.websocket.message.GetConversationPreviewsSuccessful
import com.thebrownfoxx.neon.server.route.websocket.message.GetConversationPreviewsUnexpectedError
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
import com.thebrownfoxx.neon.server.route.websocket.message.SendMessageRequest
import com.thebrownfoxx.neon.server.route.websocket.message.SendMessageSuccessful
import com.thebrownfoxx.neon.server.route.websocket.message.SendMessageUnauthorized
import com.thebrownfoxx.outcome.Failure
import com.thebrownfoxx.outcome.Outcome
import com.thebrownfoxx.outcome.Success
import com.thebrownfoxx.outcome.UnitOutcome
import com.thebrownfoxx.outcome.UnitSuccess
import com.thebrownfoxx.outcome.map.flatMapError
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.Flow

class RemoteMessenger(
    private val authenticator: Authenticator,
    private val subscriber: WebSocketSubscriber,
    private val requester: WebSocketRequester,
    externalScope: CoroutineScope,
    private val logger: Logger,
) : Messenger {
    private val conversationPreviewCache =
        SingleCache<Outcome<LocalConversationPreviews, GetConversationPreviewsError>>(externalScope)
    private val messagesCache =
        Cache<GroupId, Outcome<Set<MessageId>, GetMessagesError>>(externalScope)
    private val messageCache =
        Cache<MessageId, Outcome<LocalMessage, GetMessageError>>(externalScope)

    override val conversationPreviews:
            Flow<Outcome<LocalConversationPreviews, GetConversationPreviewsError>>
        get() =
            conversationPreviewCache.getAsFlow {
                subscriber.subscribeAsFlow(GetConversationPreviewsRequest()) {
                    map<GetConversationPreviewsMemberNotFound> {
                        Failure(GetConversationPreviewsError.MemberNotFound)
                    }
                    map<GetConversationPreviewsUnexpectedError> {
                        Failure(GetConversationPreviewsError.UnexpectedError)
                    }
                    map<GetConversationPreviewsSuccessful> { response ->
                        val loggedInMemberId = authenticator.loggedInMemberId.value
                            ?: return@map Failure(GetConversationPreviewsError.MemberNotFound)
                        Success(response.toLocalConversationPreviews(loggedInMemberId))
                    }
                }.mirrorTo(this)
            }

    override fun getMessages(groupId: GroupId): Flow<Outcome<Set<MessageId>, GetMessagesError>> {
        return messagesCache.getAsFlow(groupId) {
            subscriber.subscribeAsFlow(GetMessagesRequest(groupId = groupId)) {
                map<GetMessagesUnauthorized> { Failure(GetMessagesError.Unauthorized) }
                map<GetMessagesGroupNotFound> { Failure(GetMessagesError.GroupNotFound) }
                map<GetMessagesUnexpectedError> { Failure(GetMessagesError.UnexpectedError) }
                map<GetMessagesSuccessful> { response ->
                    Success(response.messages.map { it.id }.toSet())
                }
            }.mirrorTo(this)
        }
    }

    override fun getMessage(id: MessageId): Flow<Outcome<LocalMessage, GetMessageError>> {
        return messageCache.getAsFlow(id) {
            subscriber.subscribeAsFlow(GetMessageRequest(id = id)) {
                map<GetMessageUnauthorized> { Failure(GetMessageError.Unauthorized) }
                map<GetMessageNotFound> { Failure(GetMessageError.NotFound) }
                map<GetMessageUnexpectedError> { Failure(GetMessageError.UnexpectedError) }
                map<GetMessageSuccessful> { Success(it.message.toLocalMessage()) }
            }.mirrorTo(this)
        }
    }

    override suspend fun sendMessage(
        groupId: GroupId,
        content: String,
    ): UnitOutcome<SendMessageError> {
        val request = SendMessageRequest(id = MessageId(), groupId = groupId, content = content)
        return requester.request(request) {
            map<SendMessageUnauthorized> { Failure(SendMessageError.Unauthorized) }
            map<GetMessagesGroupNotFound> { Failure(SendMessageError.GroupNotFound) }
            map<GetMessagesUnexpectedError> { Failure(SendMessageError.UnexpectedError) }
            map<SendMessageSuccessful> { UnitSuccess }
        }.flatMapError(
            onInnerFailure = { it },
            onOuterFailure = { SendMessageError.RequestTimeout },
        )
    }

    private fun GetConversationPreviewsSuccessful.toLocalConversationPreviews(
        loggedInMemberId: MemberId,
    ): LocalConversationPreviews {
        val localPreviews = conversationPreviews.map { it.toLocalMessage() }

        val (allUnreadPreviews, readPreviews) = localPreviews.partition { message ->
            message.senderId != loggedInMemberId || message.delivery != LocalDelivery.Delivered
        }
        val unreadLarge = allUnreadPreviews.size > 10
        val nudgedPreviews = when {
            unreadLarge -> allUnreadPreviews.takeLast(2)
            else -> emptyList()
        }
        val unreadPreviews = when {
            unreadLarge -> allUnreadPreviews.dropLast(2)
            else -> allUnreadPreviews
        }

        return LocalConversationPreviews(
            nudgedPreviews = nudgedPreviews,
            unreadPreviews = unreadPreviews,
            readPreviews = readPreviews,
        )
    }
}