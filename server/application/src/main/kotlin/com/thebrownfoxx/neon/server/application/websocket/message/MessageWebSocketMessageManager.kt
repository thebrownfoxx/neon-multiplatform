package com.thebrownfoxx.neon.server.application.websocket.message

import com.thebrownfoxx.neon.common.data.JobManager
import com.thebrownfoxx.neon.common.data.SingleJobManager
import com.thebrownfoxx.neon.common.data.websocket.listen
import com.thebrownfoxx.neon.common.data.websocket.send
import com.thebrownfoxx.neon.common.type.id.GroupId
import com.thebrownfoxx.neon.common.type.id.MemberId
import com.thebrownfoxx.neon.common.type.id.MessageId
import com.thebrownfoxx.neon.server.application.websocket.KtorServerWebSocketSession
import com.thebrownfoxx.neon.server.route.websocket.message.GetChatPreviewsMemberNotFound
import com.thebrownfoxx.neon.server.route.websocket.message.GetChatPreviewsRequest
import com.thebrownfoxx.neon.server.route.websocket.message.GetChatPreviewsSuccessful
import com.thebrownfoxx.neon.server.route.websocket.message.GetDeliveryMessageNotFound
import com.thebrownfoxx.neon.server.route.websocket.message.GetDeliveryRequest
import com.thebrownfoxx.neon.server.route.websocket.message.GetDeliverySuccessful
import com.thebrownfoxx.neon.server.route.websocket.message.GetDeliveryUnauthorized
import com.thebrownfoxx.neon.server.route.websocket.message.GetDeliveryUnexpectedError
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
import com.thebrownfoxx.neon.server.route.websocket.message.MarkAsReadAlreadyRead
import com.thebrownfoxx.neon.server.route.websocket.message.MarkAsReadGroupNotFound
import com.thebrownfoxx.neon.server.route.websocket.message.MarkAsReadRequest
import com.thebrownfoxx.neon.server.route.websocket.message.MarkAsReadSuccessful
import com.thebrownfoxx.neon.server.route.websocket.message.MarkAsReadUnauthorized
import com.thebrownfoxx.neon.server.route.websocket.message.MarkAsReadUnexpectedError
import com.thebrownfoxx.neon.server.route.websocket.message.SendMessageDuplicateId
import com.thebrownfoxx.neon.server.route.websocket.message.SendMessageGroupNotFound
import com.thebrownfoxx.neon.server.route.websocket.message.SendMessageRequest
import com.thebrownfoxx.neon.server.route.websocket.message.SendMessageSuccessful
import com.thebrownfoxx.neon.server.route.websocket.message.SendMessageUnauthorized
import com.thebrownfoxx.neon.server.route.websocket.message.SendMessageUnexpectedError
import com.thebrownfoxx.neon.server.service.Messenger
import com.thebrownfoxx.neon.server.service.Messenger.GetChatPreviewsError
import com.thebrownfoxx.neon.server.service.Messenger.GetMessageError
import com.thebrownfoxx.outcome.map.onFailure
import com.thebrownfoxx.outcome.map.onSuccess
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch

class MessageWebSocketMessageManager(
    private val session: KtorServerWebSocketSession,
    private val messenger: Messenger,
    externalScope: CoroutineScope,
) {
    private val getChatPreviewsJobManager = SingleJobManager(externalScope)
    private val getMessagesJobManager = JobManager<GroupId>(externalScope)
    private val getMessageJobManager = JobManager<MessageId>(externalScope)
    private val getDeliveryJobManager = JobManager<GetDeliveryKey>(externalScope)
    private val sendMessageJobManager = JobManager<MessageId>(externalScope)
    private val markAsReadJobManager = JobManager<MarkAsReadKey>(externalScope)

    init {
        externalScope.launch {
            session.listen<GetChatPreviewsRequest>(externalScope) { it.fulfill() }
            session.listen<GetMessagesRequest>(externalScope) { it.fulfill() }
            session.listen<GetMessageRequest>(externalScope) { it.fulfill() }
            session.listen<GetDeliveryRequest>(externalScope) { it.fulfill() }
            session.listen<SendMessageRequest>(externalScope) { it.fulfill() }
            session.listen<MarkAsReadRequest>(externalScope) { it.fulfill() }
        }
    }

    private fun GetChatPreviewsRequest.fulfill() {
        getChatPreviewsJobManager.set {
            messenger.getChatPreviews(session.memberId).collect { conversationsOutcome ->
                conversationsOutcome.onSuccess { conversations ->
                    session.send(GetChatPreviewsSuccessful(requestId, conversations))
                }.onFailure { error ->
                    when (error) {
                        GetChatPreviewsError.MemberNotFound -> session.send(
                            GetChatPreviewsMemberNotFound(requestId, session.memberId)
                        )

                        GetChatPreviewsError.UnexpectedError -> session.send(
                            GetChatPreviewsMemberNotFound(requestId, session.memberId)
                        )
                    }
                }
            }
        }
    }

    private fun GetMessagesRequest.fulfill() {
        getMessagesJobManager[groupId] = {
            messenger.getMessages(session.memberId, groupId).collect { messagesOutcome ->
                messagesOutcome.onSuccess { messages ->
                    session.send(GetMessagesSuccessful(requestId, groupId, messages))
                }.onFailure { error ->
                    when (error) {
                        Messenger.GetMessagesError.Unauthorized -> session.send(
                            GetMessagesUnauthorized(requestId, groupId, session.memberId)
                        )

                        Messenger.GetMessagesError.GroupNotFound -> session.send(
                            GetMessagesGroupNotFound(requestId, groupId)
                        )

                        Messenger.GetMessagesError.UnexpectedError ->
                            session.send(GetMessagesUnexpectedError(requestId, groupId))
                    }
                }
            }
        }
    }

    private fun GetMessageRequest.fulfill() {
        getMessageJobManager[id] = {
            messenger.getMessage(session.memberId, id).collect { messageOutcome ->
                messageOutcome.onSuccess { message ->
                    session.send(GetMessageSuccessful(requestId, message))
                }.onFailure { error ->
                    when (error) {
                        GetMessageError.Unauthorized ->
                            session.send(GetMessageUnauthorized(requestId, id, session.memberId))

                        GetMessageError.NotFound -> session.send(GetMessageNotFound(requestId, id))

                        GetMessageError.UnexpectedError ->
                            session.send(GetMessageUnexpectedError(requestId, id))
                    }
                }
            }
        }
    }

    private fun GetDeliveryRequest.fulfill() {
        val memberId = session.memberId
        getDeliveryJobManager[GetDeliveryKey(messageId, memberId)] = {
            messenger.getDelivery(session.memberId, messageId).collect { deliveryOutcome ->
                deliveryOutcome.onSuccess { delivery ->
                    session.send(GetDeliverySuccessful(requestId, delivery))
                }.onFailure { error ->
                    when (error) {
                        Messenger.GetDeliveryError.Unauthorized ->
                            session.send(GetDeliveryUnauthorized(requestId, messageId, memberId))

                        Messenger.GetDeliveryError.MessageNotFound ->
                            session.send(GetDeliveryMessageNotFound(requestId, messageId))

                        Messenger.GetDeliveryError.UnexpectedError ->
                            session.send(GetDeliveryUnexpectedError(requestId, messageId))
                    }
                }
            }
        }
    }

    private fun SendMessageRequest.fulfill() {
        sendMessageJobManager[id] = {
            messenger.sendMessage(id, session.memberId, groupId, content).onSuccess {
                session.send(SendMessageSuccessful(requestId, id))
            }.onFailure { error ->
                when (error) {
                    Messenger.SendMessageError.Unauthorized ->
                        session.send(SendMessageUnauthorized(requestId, id, session.memberId))

                    Messenger.SendMessageError.GroupNotFound ->
                        session.send(SendMessageGroupNotFound(requestId, id, groupId))

                    Messenger.SendMessageError.DuplicateId ->
                        session.send(SendMessageDuplicateId(requestId, id))

                    Messenger.SendMessageError.UnexpectedError ->
                        session.send(SendMessageUnexpectedError(requestId, id))
                }
            }
        }
    }

    private fun MarkAsReadRequest.fulfill() {
        val memberId = session.memberId
        markAsReadJobManager[MarkAsReadKey(groupId, memberId)] = {
            messenger.markAsRead(memberId, groupId).onSuccess {
                session.send(MarkAsReadSuccessful(requestId, groupId, memberId))
            }.onFailure { error ->
                when (error) {
                    Messenger.MarkAsReadError.Unauthorized -> session.send(
                        MarkAsReadUnauthorized(requestId, groupId, memberId),
                    )

                    Messenger.MarkAsReadError.AlreadyRead -> session.send(
                        MarkAsReadAlreadyRead(requestId, groupId, memberId)
                    )

                    Messenger.MarkAsReadError.GroupNotFound -> session.send(
                        MarkAsReadGroupNotFound(requestId, groupId),
                    )

                    Messenger.MarkAsReadError.UnexpectedError -> session.send(
                        MarkAsReadUnexpectedError(requestId, groupId, memberId),
                    )
                }
            }
        }
    }

    private data class GetDeliveryKey(
        val messageId: MessageId,
        val memberId: MemberId,
    )

    private data class MarkAsReadKey(
        val groupId: GroupId,
        val memberId: MemberId,
    )
}