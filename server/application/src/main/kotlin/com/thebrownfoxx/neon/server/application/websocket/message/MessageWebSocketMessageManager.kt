package com.thebrownfoxx.neon.server.application.websocket.message

import com.thebrownfoxx.neon.common.data.JobManager
import com.thebrownfoxx.neon.common.data.SingleJobManager
import com.thebrownfoxx.neon.common.data.websocket.listen
import com.thebrownfoxx.neon.common.data.websocket.model.RequestId
import com.thebrownfoxx.neon.common.data.websocket.send
import com.thebrownfoxx.neon.common.type.id.GroupId
import com.thebrownfoxx.neon.common.type.id.MessageId
import com.thebrownfoxx.neon.server.application.websocket.KtorServerWebSocketSession
import com.thebrownfoxx.neon.server.route.websocket.message.GetConversationPreviewsMemberNotFound
import com.thebrownfoxx.neon.server.route.websocket.message.GetConversationPreviewsRequest
import com.thebrownfoxx.neon.server.route.websocket.message.GetConversationPreviewsSuccessful
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
import com.thebrownfoxx.neon.server.route.websocket.message.SendMessageDuplicateId
import com.thebrownfoxx.neon.server.route.websocket.message.SendMessageGroupNotFound
import com.thebrownfoxx.neon.server.route.websocket.message.SendMessageRequest
import com.thebrownfoxx.neon.server.route.websocket.message.SendMessageSuccessful
import com.thebrownfoxx.neon.server.route.websocket.message.SendMessageUnauthorized
import com.thebrownfoxx.neon.server.route.websocket.message.SendMessageUnexpectedError
import com.thebrownfoxx.neon.server.service.Messenger
import com.thebrownfoxx.neon.server.service.Messenger.GetConversationPreviewsError
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
    private val getConversationPreviewsJobManager = SingleJobManager(externalScope)
    private val getMessagesJobManager = JobManager<GroupId>(externalScope)
    private val getMessageJobManager = JobManager<MessageId>(externalScope)
    private val sendMessageJobManager = JobManager<RequestId>(externalScope)

    init {
        externalScope.launch {
            session.listen<GetConversationPreviewsRequest>(externalScope) { it.fulfill() }
            session.listen<GetMessagesRequest>(externalScope) { it.fulfill() }
            session.listen<GetMessageRequest>(externalScope) { it.fulfill() }
            session.listen<SendMessageRequest>(externalScope) { it.fulfill() }
        }
    }

    private fun GetConversationPreviewsRequest.fulfill() {
        getConversationPreviewsJobManager.set {
            messenger.getConversationPreviews(session.memberId).collect { conversationsOutcome ->
                conversationsOutcome.onSuccess { conversations ->
                    session.send(GetConversationPreviewsSuccessful(requestId, conversations))
                }.onFailure { error ->
                    when (error) {
                        GetConversationPreviewsError.MemberNotFound -> session.send(
                            GetConversationPreviewsMemberNotFound(requestId, session.memberId)
                        )

                        GetConversationPreviewsError.UnexpectedError -> session.send(
                            GetConversationPreviewsMemberNotFound(requestId, session.memberId)
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

    private fun SendMessageRequest.fulfill() {
        sendMessageJobManager[requestId] = {
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
}